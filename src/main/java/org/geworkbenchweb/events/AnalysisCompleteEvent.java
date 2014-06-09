package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;

/**
 * @author zji
 * 
 */
public class AnalysisCompleteEvent implements Event {

	public final String analysisClassName;
	public final Long resultId;

	public AnalysisCompleteEvent(String analysisClassName, Long resultId) {
		this.analysisClassName = analysisClassName;
		this.resultId = resultId;
	}

}
