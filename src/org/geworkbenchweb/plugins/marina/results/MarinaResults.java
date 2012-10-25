package org.geworkbenchweb.plugins.marina.results;

import org.geworkbenchweb.layout.VisualPlugin;

public class MarinaResults extends VisualPlugin {

	private Long dataSetId;
	
	public MarinaResults(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MarinaResults";
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
