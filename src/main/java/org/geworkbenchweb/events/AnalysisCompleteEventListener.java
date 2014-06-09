package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Listener;

public interface AnalysisCompleteEventListener extends Listener {
	void receive(final AnalysisCompleteEvent event);
}
