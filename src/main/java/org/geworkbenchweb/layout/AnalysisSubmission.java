package org.geworkbenchweb.layout;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.UserActivityLog;
import org.geworkbenchweb.utils.OverLimitException;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.UI;

import de.steinwedel.messagebox.MessageBox;

/**
 * Used to submit the analysis in geWorkbench and updates the data tree with
 * result nodes once the
 * analysis is complete in the background.
 * Mostly based on the original class AnalysisListener.
 */
public class AnalysisSubmission {

	private static Log log = LogFactory.getLog(AnalysisSubmission.class);
	private final UMainLayout uMainLayout;

	/**
	 * @param uMainLayout
	 */
	public AnalysisSubmission(UMainLayout uMainLayout) {
		this.uMainLayout = uMainLayout;
	}

	public void submit(final HashMap<Serializable, Serializable> params, final AnalysisUI analysisUI, final ResultSet resultSet) {

		final Long userId = SessionHandler.get().getId();
		final String username = SessionHandler.get().getUsername();

		Thread analysisThread = new Thread() {
			@Override
			public void run() {
				final Long resultId = resultSet.getId();
				String resultName = null;
				try {
					resultName = analysisUI.execute(resultId, params, userId);
					if (resultName == null)
						return;
				} catch (OverLimitException e) {
					MessageBox.createInfo().withCaption("Size Limit").withMessage(e.getMessage()).withOkButton(() -> {
						uMainLayout.noSelection();
					}).open();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				} catch (RemoteException e) { // this may happen for marina analysis
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox.createInfo().withCaption("Analysis Problem").withMessage(msg).withOkButton().open();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				} catch (IOException e) {
					e.printStackTrace();
					String msg = e.getMessage().replaceAll("\n", "<br>");
					MessageBox.createInfo().withCaption("Analysis Problem").withMessage(msg).withOkButton().open();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				} catch (Exception e) {
					// TODO this catch-all exception clause should not be used.
					// when we still have it, it definitely should not continue from here
					e.printStackTrace();
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				}

				if (resultName.equalsIgnoreCase("UnAuthenticatedException")) {
					FacadeFactory.getFacade().delete(resultSet);
					uMainLayout.removeItem(resultSet.getId());
					return;
				}

				final ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
				resultSet.setName(resultName);
				// REVIEW this should have been done in AnalysisUI.execute so it is not
				// necessary here.
				// If it is indeed taken care of in every implementation, it should be removed.
				FacadeFactory.getFacade().store(resultSet);

				UserActivityLog ual = new UserActivityLog(username,
						UserActivityLog.ACTIVITY_TYPE.RESULT.toString(), resultName);
				FacadeFactory.getFacade().store(ual);

				uMainLayout.addNode(resultSet);
				GeworkbenchRoot ui = (GeworkbenchRoot)UI.getCurrent();
				ui.push();
			}
		};
		analysisThread.start();
		UserActivityLog ual = new UserActivityLog(username,
				UserActivityLog.ACTIVITY_TYPE.ANALYSIS.toString(), analysisUI.getClass().getName());
		FacadeFactory.getFacade().store(ual);
	}
}