package org.geworkbenchweb.plugins.ttest.results;

import org.geworkbenchweb.layout.VisualPlugin;

public class TTestResults extends VisualPlugin {

	private Long dataSetId;
	
	public TTestResults(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "TTest Results";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "TTest Results";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return true;
	}

}
