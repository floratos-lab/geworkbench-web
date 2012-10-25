package org.geworkbenchweb.plugins.anova.results;

import org.geworkbenchweb.layout.VisualPlugin;

public class AnovaResults extends VisualPlugin {

	private Long dataSetId;
	
	public AnovaResults(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AnovaResults";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return true;
	}

}
