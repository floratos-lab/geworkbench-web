package org.geworkbenchweb.interactions.CNKB;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;

public class CNKBInteractions {
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	
	private int timeout = 0;
	
	private int interaction_flag = 1;
	
	public Vector<CellularNetWorkElementInformation> getHits() {
		return hits;
	}
	
	public CNKBInteractions() {
		
		//These are loaded from geWorkbench Swing version
		
		loadApplicationProperty();
		
		Runnable r = new Runnable() {
			public void run() {

				InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

				try {

					String context = null;

					String version = null;
		
					int retrievedQueryNumber = 0;
					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
						retrievedQueryNumber++;

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
								
							} catch (ConnectException ce) {
								

							} catch (SocketTimeoutException se) {
								

							} catch (IOException ie) {
								

							}
							cellularNetWorkElementInformation.setDirty(false);
							cellularNetWorkElementInformation
									.setInteractionDetails(interactionDetails);

						}

					}

				} catch (java.util.ConcurrentModificationException ce) {
					
				} catch (Exception e) {
								 

				} finally {
					
					interactionsConnection.closeDbConnection();
				
				}
			}
		};

		// SwingUtilities.invokeLater(r);
		Thread thread = new Thread(r);
		thread.start();
		
	}

	
	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		Properties iteractionsProp = new Properties();
		try {
			iteractionsProp
					.load(new FileInputStream(Constants.PROPERTIES_FILE));

			timeout = new Integer(
					iteractionsProp
							.getProperty(Constants.INTERACTIONS_SERVLET_CONNECTION_TIMEOUT));
		 
			interaction_flag = new Integer(iteractionsProp
					.getProperty(Constants.INTERACTIONS_FLAG));

			String interactionsServletUrl = PropertiesManager.getInstance().getProperty(this.getClass(),
					"url", "");
			if (interactionsServletUrl == null
					|| interactionsServletUrl.trim().equals("")) {

				interactionsServletUrl = iteractionsProp
						.getProperty(Constants.INTERACTIONS_SERVLET_URL);
			}
			ResultSetlUtil.setUrl(interactionsServletUrl);
			ResultSetlUtil.setTimeout(timeout);
		} catch (java.io.IOException ie) {
			
		} catch (Exception e) {
			
		}

	}

	
}
