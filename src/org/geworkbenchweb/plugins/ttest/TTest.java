package org.geworkbenchweb.plugins.ttest;

import org.geworkbenchweb.layout.VisualPlugin;

public class TTest extends VisualPlugin {

	private Long dataSetId;
	
	public TTest(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Differential Expression (T-Test)";
	}

	@Override
	public Long getDataSetId() {
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "A t-Test analysis can be used to identify markers with statistically " +
				"significant differential expression between two sets of microarrays.";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}
}
