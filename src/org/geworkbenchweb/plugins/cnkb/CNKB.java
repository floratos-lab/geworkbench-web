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
		return "The Cellular Network Knowledge Base (CNKB) is a repository of molecular interactions, " +
				"including ones both computationally and experimentally derived. Sources for interactions " +
				"include both publicly available databases such as BioGRID and HPRD, as well as reverse-engineered " +
				"cellular regulatory interactomes developed in the lab of Dr. Andrea Califano at Columbia University.";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

}
