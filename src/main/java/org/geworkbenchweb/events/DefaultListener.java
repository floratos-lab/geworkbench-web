package org.geworkbenchweb.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultListener implements AnalysisCompleteEventListener {
	
	private static Log log = LogFactory.getLog(DefaultListener.class);

	@Override
	public void receive(AnalysisCompleteEvent event) {
		log.debug("complelte event received: "+event.analysisClassName+" "+event.resultId);
	}

}
