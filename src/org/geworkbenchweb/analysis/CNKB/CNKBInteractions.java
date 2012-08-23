package org.geworkbenchweb.analysis.CNKB;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class CNKBInteractions {
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	
	private int interaction_flag = 1;
	
	User user 	= 	SessionHandler.get();
	
	public CNKBInteractions(DSMicroarraySet dataSet, String[] params) {
		
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

		String context 		= 	params[0];

		String version 		=	params[1];
			
		String positions 	= 	params[2];
		
		String[] temp 		=   (positions.substring(1, positions.length()-1)).split(",");
			
		hits = new Vector<CellularNetWorkElementInformation>();
		for(int i=0; i<temp.length; i++) {
				
			hits.addElement(new CellularNetWorkElementInformation(dataSet.getMarkers().get(Integer.parseInt(temp[i].trim()))));
			
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
		
		
		ResultSet resultSet = 	new ResultSet();
		java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName	=	"CNKB - " + new java.util.Date(); 
		resultSet.setName(dataSetName);
		resultSet.setType("CNKB");
		resultSet.setParent(DataSetOperations.getDataSetID(dataSet.getDataSetName()));
		resultSet.setOwner(user.getId());	
		resultSet.setData(ObjectConversion.convertToByte(hits));
		FacadeFactory.getFacade().store(resultSet);	
		
		NodeAddEvent resultEvent = new NodeAddEvent(resultSet.getId(), dataSetName, "Result Node");
		GeworkbenchRoot.getBlackboard().fire(resultEvent);
		
	}
}
