package org.geworkbenchweb.plugins.cnkb;

import org.geworkbenchweb.layout.VisualPlugin;

public class CNKB extends VisualPlugin {

	private Long dataSetId;
	
	public CNKB(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Cellular Network Knowledge Base";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "CNKB";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

}
