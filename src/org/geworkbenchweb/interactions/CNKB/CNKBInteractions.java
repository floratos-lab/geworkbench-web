package org.geworkbenchweb.interactions.CNKB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class CNKBInteractions {
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	
	private int timeout = 3000;
	
	private int interaction_flag = 1;
	
	User user 	= 	SessionHandler.get();
	
	public CNKBInteractions(DSMicroarraySet dataSet, String[] params) {
		
		loadApplicationProperty();

		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

		String context = "BCi (66193 interactions)".split(" \\(")[0].trim();

		String version = "1.0";
			
			
		hits = new Vector<CellularNetWorkElementInformation>();
		
		for(int i=0; i<dataSet.getMarkers().size(); i++) {
				
			hits.addElement(new CellularNetWorkElementInformation(dataSet.getMarkers().get(i)));
			
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
		java.util.Date date= new java.util.Date();
		resultSet.setName("CNKB - " + date);
		resultSet.setType("CNKB");
		resultSet.setParent(dataSet.getDataSetName());
		resultSet.setOwner(user.getId());	
		resultSet.setData(convertToByte(hits));
		FacadeFactory.getFacade().store(resultSet);	
		
	}

	
	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		
		String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
		ResultSetlUtil.setUrl(interactionsServletUrl);
		ResultSetlUtil.setTimeout(timeout);
		
	}
	
	private byte[] convertToByte(Object object) {

		byte[] byteData = null;
		ByteArrayOutputStream bos 	= 	new ByteArrayOutputStream();

		try {

			ObjectOutputStream oos 	= 	new ObjectOutputStream(bos); 

			oos.writeObject(object);
			oos.flush(); 
			oos.close(); 
			bos.close();
			byteData 				= 	bos.toByteArray();

		} catch (IOException ex) {

			System.out.println("Exception with in convertToByte");

		}

		return byteData;

	}
}
