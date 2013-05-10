package org.geworkbenchweb.layout;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Used to submit the analysis in geWorkbench and updates the data tree with result nodes once the 
 * analysis is complete in the background.
 * @author Nikhil
 * @version $Id$
 */
public class AnalysisListener implements AnalysisSubmissionEventListener {

	private final UMainLayout uMainLayout;
	private final ICEPush pusher;

	/**
	 * @param uMainLayout
	 */
	AnalysisListener(UMainLayout uMainLayout, ICEPush pusher) {
		this.uMainLayout = uMainLayout;
		this.pusher = pusher;
	}

	@Override
	public void SubmitAnalysis(final AnalysisSubmissionEvent event) {

		Thread analysisThread = new Thread() {
			@Override
			public void run() {
				final ResultSet resultSet = event.getResultSet();
				HashMap<Serializable, Serializable> params = event.getParameters();

				DSMicroarraySet dataSet = null;
				try {
					dataSet = (DSMicroarraySet) event.getDataSet();
				} catch (Exception e) {
					// FIXME catching all clause is evil; catching all and doing nothing is the evil of evils
					e.printStackTrace();
				}
				AnalysisUI analysisUI = event.getAnalaysisUI();
				String resultName = null;
				try {
					resultName = analysisUI.execute(resultSet.getId(), dataSet, params);
				} catch (RemoteException e) { // this may happen for marina analysis
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox mb = new MessageBox(uMainLayout.getWindow(), 
							"Analysis Problem", MessageBox.Icon.ERROR, msg,  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();	
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;	
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				if (resultName.equalsIgnoreCase("UnAuthenticatedException"))
				{
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;	
				}
				
				resultSet.setName(resultName);

				FacadeFactory.getFacade().store(resultSet);	
				synchronized(uMainLayout.getApplication()) {
					MessageBox mb = new MessageBox(uMainLayout.getWindow(), 
							"Analysis Completed", 
							MessageBox.Icon.INFO, 
							"Analysis you submitted is now completed. " +
									"Click on the node to see the results",  
									new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show(new MessageBox.EventListener() {
						private static final long serialVersionUID = 1L;
						@Override
						public void buttonClicked(ButtonType buttonType) {    	
							if(buttonType == ButtonType.OK) {
								NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
								GeworkbenchRoot.getBlackboard().fire(resultEvent);
							}
						}
					});	
				}
				pusher.push();
			}
		};
		analysisThread.start();
	}
}