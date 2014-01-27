package org.geworkbenchweb.layout;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.UI;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;
import de.steinwedel.messagebox.MessageBoxListener;

/**
 * Used to submit the analysis in geWorkbench and updates the data tree with result nodes once the 
 * analysis is complete in the background.
 * @author Nikhil
 */
public class AnalysisListener implements AnalysisSubmissionEventListener {

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

		Thread analysisThread = new Thread() {
			@Override
			public void run() {
				UI.getCurrent().access(new Runnable(){
					@Override
					public void run(){
						runInAccess();
					}
				});
			}
			public void runInAccess(){
				Long resultId = event.getResultSet().getId();
				HashMap<Serializable, Serializable> params = event.getParameters();

				AnalysisUI analysisUI = event.getAnalaysisUI();
				String resultName = null;
				try {
					resultName = analysisUI.execute(resultId, event.getDatasetId(), params, userId);
				} catch (RemoteException e) { // this may happen for marina analysis
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox.showPlain(Icon.ERROR, "Analysis Problem", msg, ButtonId.OK);
					ResultSet resultSet = event.getResultSet();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;	
				} catch (IOException e) {
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox.showPlain(Icon.ERROR, "Analysis Problem", msg, ButtonId.OK);
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
				
				MessageBox.showPlain(Icon.INFO, "Analysis Completed",
						"Analysis you submitted is now completed. "
								+ "Click on the node to see the results",
						new MessageBoxListener() {
							@Override
							public void buttonClicked(ButtonId buttonId) {
								if (buttonId == ButtonId.OK) {
									NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
									GeworkbenchRoot.getBlackboard().fire(resultEvent);
								}
							}
						}, ButtonId.OK);
			}
		};
		analysisThread.start();
	}
}