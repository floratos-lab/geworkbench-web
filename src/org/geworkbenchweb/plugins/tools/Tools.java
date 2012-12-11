package org.geworkbenchweb.plugins.tools;

import org.geworkbenchweb.layout.VisualPlugin;

public class Tools extends VisualPlugin {

	private Long dataSetId;
	
	public Tools(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		return "Tools";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		return "The list of all the available tools.";
	}

	@Override
	// TODO bad name. it should be isVisualizer()
	public boolean checkForVisualizer() {
		return false;
	}

}
