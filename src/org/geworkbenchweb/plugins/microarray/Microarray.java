package org.geworkbenchweb.plugins.microarray;

import org.geworkbenchweb.layout.VisualPlugin;

public class Microarray extends VisualPlugin {

	private Long dataSetId;
	
	@Override
	public String getName() {
		return "Microarray Data";
	}

	@Override
	public String getDescription() {
		String desc = "Microarray Description";
		return desc;
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Microarray(Long dataSetId) {
		this.dataSetId = dataSetId;
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}
}
