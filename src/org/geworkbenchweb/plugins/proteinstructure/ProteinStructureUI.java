package org.geworkbenchweb.plugins.proteinstructure;

import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.plugins.DataTypeUI;

public class ProteinStructureUI extends DataTypeMenuPage implements DataTypeUI {

	private static final long serialVersionUID = 1L;
	
	public ProteinStructureUI(Long dataSetId) {
		super("PDB File", "Protein Structure Data", DSProteinStructure.class, dataSetId);
	}
}
