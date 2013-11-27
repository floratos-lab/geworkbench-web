package org.geworkbenchweb.plugins.proteinstructure;

import org.geworkbenchweb.plugins.DataTypeMenuPage;

public class ProteinStructureUI extends DataTypeMenuPage {

	private static final long serialVersionUID = 1L;
	
	public ProteinStructureUI(Long dataSetId) {
		super("PDB File", "Protein Structure Data", org.geworkbenchweb.pojos.PdbFileInfo.class, dataSetId);
	}
}
