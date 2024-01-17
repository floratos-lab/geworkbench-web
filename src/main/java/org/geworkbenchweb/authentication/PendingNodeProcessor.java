package org.geworkbenchweb.authentication;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.UI;

/* A connector from the existing pending nodes to the 'main layout' when the user logs in again,
 * so when it finishes during this session, the UI will update. */
public class PendingNodeProcessor {

	private static final long CHECKING_INTERVAL_IN_SECOND = 5;

	private static Log log = LogFactory.getLog(PendingNodeProcessor.class);

	private final UMainLayout mainLayout;

	public PendingNodeProcessor(UMainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}

	public void start() {
		if (mainLayout == null) {
			log.error("main window content is not UMainLayout");
			return;
		}

		User user = SessionHandler.get();

		Thread analysisThread = new Thread() {

			@Override
			public void run() {
				while (true) {
					List<ResultSet> results = FacadeFactory.getFacade().list(ResultSet.class);
					int count = 0;
					int updated = 0;
					int pending = 0;
					for (ResultSet result : results) {
						if (!result.getOwner().equals(user.getId())) {
							continue;
						}
						if (result != null && result.getDataId() == null) {
							log.debug("NULL RESULT DATASET " + result.getId());
						} else if (mainLayout.updateNode(result)) {
							updated++;
						}
						count++;
						if (result.getName().contains("Pending")) {
							pending++;
						}
					}
					GeworkbenchRoot ui = (GeworkbenchRoot)UI.getCurrent();
					ui.push();
					log.debug("total result sets of this user: " + count);
					log.debug("updated nodes: " + updated);
					log.debug("pending nodes: " + pending);

					if (pending == 0) {
						return;
					}

					try {
						TimeUnit.SECONDS.sleep(CHECKING_INTERVAL_IN_SECOND);
					} catch (InterruptedException e) {
						// no-op
						e.printStackTrace();
					}
				}
			}
		};
		analysisThread.start();
	}
}
