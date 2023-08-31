package org.geworkbenchweb.authentication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/* A connector from the existing pending nodes to the 'main layout' when the user logs in again,
 * so when it finishes during this session, the UI will update. */
public class PendingNodeProcessor {

	private static final long CHECKING_INTEVAL_IN_SECOND = 5;

	private static Log log = LogFactory.getLog(PendingNodeProcessor.class);

	private final UMainLayout mainLayout;

	public PendingNodeProcessor(UMainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}

	public void start() {
		if (mainLayout == null) {
			log.error("main winwdow content is not UMainLayout");
			return;
		}

		Thread analysisThread = new Thread() {

			@Override
			public void run() {
				List<Long> pendingResultIds = new ArrayList<Long>();
				List<ResultSet> results = FacadeFactory.getFacade().list(
						ResultSet.class);
				for (ResultSet result : results) {
					if (isPending(result)) {
						pendingResultIds.add(result.getId());
					}
				}

				while (pendingResultIds != null && pendingResultIds.size() > 0) {

					try {
						TimeUnit.SECONDS.sleep(CHECKING_INTEVAL_IN_SECOND);
					} catch (InterruptedException e) {
						// no-op
						e.printStackTrace();
					}

					Iterator<Long> pending = pendingResultIds.iterator();
					while (pending.hasNext()) {
						Long id = pending.next();
						ResultSet result = FacadeFactory.getFacade().find(
								ResultSet.class, id);

						log.debug("checking pending node ...");
						if (!isPending(result)) {
							addResultNode(result);
							pending.remove();
							log.debug("one pending node removed");
						}
					}
					log.debug("pending node count = " + pendingResultIds.size());
				}
			}
		};
		analysisThread.start();
	}

	// TODO using name to identify pending node is obviously not a good idea
	private static boolean isPending(ResultSet result) {
		if (result.getName().contains("Pending")) {
			return true;
		} else {
			return false;
		}
	}

	/* this is based on the code copied from AnalysisListener */
	/*
	 * this is mainly to force 'adding result node' invoked from GUI thread. That is
	 * really the limitation of current design
	 */
	private void addResultNode(final ResultSet resultSet) {
		MessageBox mb = new MessageBox(mainLayout.getWindow(),
				"Analysis Completed",
				MessageBox.Icon.INFO,
				"Analysis you submitted is now completed. " +
						"Click on the node to see the results",
				new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
		mb.show(new MessageBox.EventListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClicked(ButtonType buttonType) {
				/*
				 * TODO uMainLayout.addNode(resultSet) is implemented in a way that works
				 * properly only in GUI thread,
				 * so we need to call this method here. Theoretically, this is not necessary. If
				 * the design is improved,
				 * this action could possibly be done in the background thread, and the
				 * confirmation dialog will no be necessary.
				 */
				/*
				 * In short, the AnalysisUI should be implemented separating the constructor and
				 * method attach() because only the
				 * latter logically requires the GUI thread. In a background thread,
				 * getApplication() would return null,
				 * and SessionHandler.get() throws null pointer exception by appfoundation.
				 */
				mainLayout.addNode(resultSet);
			}
		});
		mainLayout.push();
	}
}
