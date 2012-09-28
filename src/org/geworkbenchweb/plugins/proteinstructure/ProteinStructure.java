package org.geworkbenchweb.plugins.proteinstructure;

import org.geworkbenchweb.layout.VisualPlugin;

public class ProteinStructure extends VisualPlugin {

	private Long dataSetId;
	
	public ProteinStructure(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Protein Strucure Data";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "PDB File";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

}
