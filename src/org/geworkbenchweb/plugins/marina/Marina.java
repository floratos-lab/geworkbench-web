package org.geworkbenchweb.plugins.marina;

import org.geworkbenchweb.layout.VisualPlugin;

public class Marina extends VisualPlugin {

	private Long dataSetId;
	
	public Marina(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MARINa";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "MARINa Analysis";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

}
