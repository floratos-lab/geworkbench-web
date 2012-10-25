package org.geworkbenchweb.plugins.aracne.results;

import org.geworkbenchweb.layout.VisualPlugin;

public class AracneResults extends VisualPlugin {

	private Long dataSetId;
	
	public AracneResults(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AracneResults";
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
