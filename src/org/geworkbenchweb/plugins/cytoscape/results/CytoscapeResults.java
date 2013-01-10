package org.geworkbenchweb.plugins.cytoscape.results;

import org.geworkbenchweb.layout.VisualPlugin;

public class CytoscapeResults extends VisualPlugin {

	private Long dataSetId;
	
	public CytoscapeResults(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Cytoscape Results";
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
