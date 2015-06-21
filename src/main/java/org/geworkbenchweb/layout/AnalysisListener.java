package org.geworkbenchweb.layout;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisCompleteEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.UserActivityLog;
import org.geworkbenchweb.utils.OverLimitException;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Used to submit the analysis in geWorkbench and updates the data tree with result nodes once the 
 * analysis is complete in the background.
 * @author Nikhil
 */
public class AnalysisListener implements AnalysisSubmissionEventListener {

	private static Log log = LogFactory.getLog(AnalysisListener.class);
	private final UMainLayout uMainLayout;

	/**
	 * @param uMainLayout
	 */
	AnalysisListener(UMainLayout uMainLayout) {
		this.uMainLayout = uMainLayout;
	}

	@Override
	public void SubmitAnalysis(final AnalysisSubmissionEvent event) {

		final Long userId = SessionHandler.get().getId();
		final String username = SessionHandler.get().getUsername();

		Thread analysisThread = new Thread() {
			@Override
			public void run() {
				final Long resultId = event.getResultSet().getId();
				HashMap<Serializable, Serializable> params = event.getParameters();

				final AnalysisUI analysisUI = event.getAnalaysisUI();
				String resultName = null;
				try {
					resultName = analysisUI.execute(resultId, params, userId);
				} catch (OverLimitException e) {
					MessageBox mb = new MessageBox(uMainLayout.getWindow(),
							"Size Limit", MessageBox.Icon.INFO, e.getMessage(),
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show(new MessageBox.EventListener(){

						private static final long serialVersionUID = 737428008969387125L;

						@Override
						public void buttonClicked(ButtonType buttonType) {
							uMainLayout.noSelection();
						}
						
					});
					ResultSet resultSet = event.getResultSet();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				} catch (RemoteException e) { // this may happen for marina analysis
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox mb = new MessageBox(uMainLayout.getWindow(), 
							"Analysis Problem", MessageBox.Icon.ERROR, msg,  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					ResultSet resultSet = event.getResultSet();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;	
				} catch (IOException e) {
					e.printStackTrace();
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox mb = new MessageBox(uMainLayout.getWindow(), 
							"Analysis Problem", MessageBox.Icon.ERROR, msg,  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
					ResultSet resultSet = event.getResultSet();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;	
				} catch (Exception e) {
					// TODO this catch-all exception clause should not be used.
					// when we still have it, it definitely should not continue from here
					e.printStackTrace();
					ResultSet resultSet = event.getResultSet();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				}

				if (resultName.equalsIgnoreCase("UnAuthenticatedException"))
				{
					ResultSet resultSet = event.getResultSet();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;	
				}
				
				final ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
				resultSet.setName(resultName);
				FacadeFactory.getFacade().store(resultSet);
				
				UserActivityLog ual = new UserActivityLog(username,
						UserActivityLog.ACTIVITY_TYPE.RESULT.toString(), resultName);
				FacadeFactory.getFacade().store(ual);
				
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
							/* Note that everything else in the run() method is in a thread different from the GUI thread (vaadin main thread),
							 * so the blackboard is not available, until you get into this method,
							 * where we are back in the main thread so we have access to the blackboard. */
							AnalysisCompleteEvent analysisCompleleteEvent = new AnalysisCompleteEvent(
									analysisUI.getClass().getName(), resultId);
							log.debug("complelte event being fired: "+analysisCompleleteEvent.analysisClassName+" "+analysisCompleleteEvent.resultId);
							GeworkbenchRoot.getBlackboard().fire(analysisCompleleteEvent);

							/* TODO uMainLayout.addNode(resultSet) is implemented in a way that works properly only in GUI thread,
							 * so we need to call this method here. Theoretically, this is not necessary. If the design is improved, 
							 * this action could possibly be done in the background thread, and the confirmation dialog will no be necessary. */
							/* In short, the AnalysisUI should be implemented separating the constructor and method attach() because only the
							 * latter logically requires the GUI thread. In a background thread, getApplication() would return null, 
							 * and SessionHandler.get() throws null pointer exception by appfoundation. */
							uMainLayout.addNode(resultSet);
						}
					});	
				}
				uMainLayout.push();
			}
		};
		analysisThread.start();
		UserActivityLog ual = new UserActivityLog(username,
				UserActivityLog.ACTIVITY_TYPE.ANALYSIS.toString(), event
						.getAnalaysisUI().getClass().getName());
		FacadeFactory.getFacade().store(ual);
	}
}