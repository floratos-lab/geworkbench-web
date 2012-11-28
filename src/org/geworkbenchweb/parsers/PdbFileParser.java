package org.geworkbenchweb.parsers;

import java.io.File;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.parsers.PDBFileFormat;

public class PdbFileParser extends Parser {

	// meant to be used by the factory, not publicly
	PdbFileParser() {
	};

	@Override
	public void parse(File file) throws GeWorkbenchParserException {
		// this should have been checked earlier one
		if (!file.getName().toLowerCase().endsWith(".pdb")) {
			throw new GeWorkbenchParserException(
					"file name "+file.getName()+" does not end with .pdb");
		}

		DSDataSet<? extends DSBioObject> dataSet = new PDBFileFormat()
				.getDataFile(file);

		/*
		 * return value is ignored; it is useful only for expression file to
		 * associate with annotation
		 */
		// FIXME hard-code type name breaks many things. kept only temporarily 
		storeData(dataSet, file.getName(), "PDB File"); //this.getClass().getName());
	}

	@Override
	public String toString() {
		return "PDB File";
	}
}
