package org.geworkbenchweb.events;

import java.io.Serializable;
import java.util.HashMap;

import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class AnalysisSubmissionEvent implements Event {

	private ResultSet resultSet;
	
	private HashMap<Serializable, Serializable> params;
	
	final private AnalysisUI analysisUi;

	public AnalysisSubmissionEvent(ResultSet resultSet,
			HashMap<Serializable, Serializable> params, AnalysisUI analysisUi) {
		this.resultSet = resultSet;
		this.params = params;
		this.analysisUi = analysisUi;
	}
	
	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public HashMap<Serializable, Serializable> getParameters() {
		return params;
	}
	
	public interface AnalysisSubmissionEventListener extends Listener {
		public void SubmitAnalysis(final AnalysisSubmissionEvent event);
	}
	
	public AnalysisUI getAnalaysisUI() {
		return analysisUi;
	}
}