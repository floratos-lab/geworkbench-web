package org.geworkbenchweb.plugins.markus;

import org.geworkbenchweb.layout.VisualPlugin;

public class MarkUs extends VisualPlugin {

	private Long dataSetId;
	
	public MarkUs(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MarkUs";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "MarkUs is a web server to assist the assessment of the biochemical function " +
				"for a given protein structure. MarkUs identifies related protein structures " +
				"and sequences, detects protein cavities, and calculates the surface electrostatic " +
				"potentials and amino acid conservation profile. ";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

}
