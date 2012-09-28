package org.geworkbenchweb.plugins.anova;

import org.geworkbenchweb.layout.VisualPlugin;

public class Anova extends VisualPlugin {

	private Long dataSetId;
	
	public Anova(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Anova";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Anova Analysis";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
