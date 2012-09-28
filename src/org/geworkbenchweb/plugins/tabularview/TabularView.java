package org.geworkbenchweb.plugins.tabularview;

import org.geworkbenchweb.layout.VisualPlugin;

public class TabularView extends VisualPlugin {

	private Long dataSetId;
	
	public TabularView(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Tabular View";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Tabular view of Microarray Data";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

}
