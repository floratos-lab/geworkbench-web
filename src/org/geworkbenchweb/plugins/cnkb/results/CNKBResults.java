package org.geworkbenchweb.plugins.cnkb.results;

import org.geworkbenchweb.layout.VisualPlugin;

public class CNKBResults extends VisualPlugin {

	private Long dataSetId;
	
	public CNKBResults(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CNKB Results";
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
