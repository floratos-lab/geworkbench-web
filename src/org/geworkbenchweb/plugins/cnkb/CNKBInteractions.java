package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix; 
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
 
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations; 

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class CNKBInteractions {
	
    private Log log = LogFactory.getLog(CNKBInteractions.class);

	private Vector<CellularNetWorkElementInformation> hits = null;

	private int interaction_flag = 1;	 

	public Vector<CellularNetWorkElementInformation> CNKB(
			DSMicroarraySet dataSet, HashMap<Serializable, Serializable> params) {

		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		String context = (String) params.get(CNKBParameters.INTERACTOME);
		String version = (String) params.get(CNKBParameters.VERSION);
		Long subSetId = Long.parseLong(((String) params
				.get(CNKBParameters.MARKER_SET_ID)).trim());

		@SuppressWarnings("unchecked")
		List<SubSet> data = (List<SubSet>) SubSetOperations
				.getMarkerSet(subSetId);
		SubSet markerSet = data.get(0);

		ArrayList<String> markers = markerSet.getPositions();
		hits = new Vector<CellularNetWorkElementInformation>();

		for (int i = 0; i < dataSet.getMarkers().size(); i++) {
			if (markers.contains(dataSet.getMarkers().get(i).getLabel())) {
				hits.addElement(new CellularNetWorkElementInformation(dataSet
						.getMarkers().get(i)));
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

	 
	 @SuppressWarnings("unchecked")
	public AdjacencyMatrixDataSet CreateNetwork(HashMap<Serializable, Serializable> params)
	  { 
		 Vector<CellularNetWorkElementInformation> hits = null;
		
		 if (params.get(CNKBParameters.NETWORK_ELEMENT_INFO) != null)
				hits = (Vector<CellularNetWorkElementInformation>)params.get(CNKBParameters.NETWORK_ELEMENT_INFO);
		  
		
		  
		  AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		  AdjacencyMatrix matrix = new AdjacencyMatrix(null);
		  
		  int interactionNum = 0;
		 
			 
		 List<String> selectedTypes = (ArrayList<String>)params.get(CNKBParameters.SELECTED_INTERACTION_TYPES);;
			 
		  
		  for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
	  
			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions(selectedTypes);			 

			DSGeneMarker marker1 = cellularNetWorkElementInformation
					.getdSGeneMarker();
			AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(
					NodeType.GENE_SYMBOL, marker1.getGeneName());


			for (InteractionDetail interactionDetail : arrayList) {				 
				 
			 
				String mid2 = interactionDetail.getdSGeneId();
				String mName2  = interactionDetail.getdSGeneName();
				AdjacencyMatrix.Node node2 = null;
				
				if (mName2 !=  null && !mName2.trim().equals(""))
					node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
							mName2);
				else
				{
					node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
							mid2);
				}		 

				matrix.add(
						node1,
						node2,
						new Float(interactionDetail 
								.getConfidenceValue(interactionDetail.getConfidenceTypes().get(0))));

				interactionNum++;
			}
		} // end for loop

		 

		adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix,
					1, "Adjacency Matrix","CNKB Interactions", null);
			 
	 
		 
        return adjacencyMatrixdataSet;
		 

}
}
