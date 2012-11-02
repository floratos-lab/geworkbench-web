package org.geworkbenchweb.plugins.tools;

import org.geworkbenchweb.layout.VisualPlugin;

public class Tools extends VisualPlugin {

	private Long dataSetId;
	
	public Tools(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Available Analysis in geWorkbench-Web";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Tools Menu";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return true;
	}

}
