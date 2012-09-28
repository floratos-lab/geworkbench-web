package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;

public class CNKBInteractions {
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	
	private int interaction_flag = 1;
	
	public Vector<CellularNetWorkElementInformation> CNKB(DSMicroarraySet dataSet, String[] params, long dataSetId) {
		
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

		String context 		= 	params[0];

		String version 		=	params[1];
			
		Long subSetId 		= 	Long.parseLong(params[2].trim());
		
		ArrayList<String> markers = getMarkerData(subSetId);
			
		hits = new Vector<CellularNetWorkElementInformation>();
		
		for(int i=0; i<dataSet.getMarkers().size(); i++) {
			if(markers.contains(dataSet.getMarkers().get(i).getLabel())) {
				hits.addElement(new CellularNetWorkElementInformation(dataSet.getMarkers().get(i)));	
			}
		}
		try {
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {	

				DSGeneMarker marker = cellularNetWorkElementInformation
						.getdSGeneMarker();
				
				if (marker != null && marker.getGeneId() != 0
						&& cellularNetWorkElementInformation.isDirty()) {
					
					List<InteractionDetail> interactionDetails = null;
					try {
						if (interaction_flag == 0) {
							interactionDetails = interactionsConnection
									.getInteractionsByEntrezIdOrGeneSymbol_1(
											marker, context, version);
						} else {
							interactionDetails = interactionsConnection
									.getInteractionsByEntrezIdOrGeneSymbol_2(
											marker, context, version);
						}
					} catch (UnAuthenticatedException uae) {
						uae.printStackTrace();
					} catch (ConnectException ce) {
						ce.printStackTrace();
					} catch (SocketTimeoutException se) {
						se.printStackTrace();
					} catch (IOException ie) {
						ie.printStackTrace();
					} 
					cellularNetWorkElementInformation.setDirty(false);
					cellularNetWorkElementInformation
					.setInteractionDetails(interactionDetails);
				}	
			}
		} catch (java.util.ConcurrentModificationException ce) {
			System.out.println("ie - 1");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			interactionsConnection.closeDbConnection();
		}
		return hits;
	}
	
	/**
	 * Create Dataset for selected markerSet 
	 */
	public ArrayList<String> getMarkerData(Long subSetId) {

		@SuppressWarnings("rawtypes")
		List subSet 			= 	SubSetOperations.getMarkerSet(subSetId);
		ArrayList<String> positions 	= 	(((SubSet) subSet.get(0)).getPositions());
		
		return positions;
	}
	
}