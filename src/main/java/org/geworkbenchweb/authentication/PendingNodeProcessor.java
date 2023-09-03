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
						TimeUnit.SECONDS.sleep(CHECKING_INTERVAL_IN_SECOND);
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
							mainLayout.addNode(result);
							mainLayout.push();
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
}
