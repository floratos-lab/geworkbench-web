package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.parsers.PDBFileFormat;

public class PdbFileLoader extends Loader {

	// meant to be used by the factory, not publicly
	PdbFileLoader() {
	};

	@Override
	public void load(File file) throws GeWorkbenchLoaderException {
		// this should have been checked earlier one
		if (!file.getName().toLowerCase().endsWith(".pdb")) {
			throw new GeWorkbenchLoaderException(
					"File name "+file.getName()+" does not end with .pdb. Please choose a file with .pdb extension.");
		}

		DSDataSet<? extends DSBioObject> dataSet = new PDBFileFormat()
				.getDataFile(file);

		/*
		 * return value is ignored; it is useful only for expression file to
		 * associate with annotation
		 */
		// FIXME hard-code type name breaks many things. kept only temporarily 
		storeData(dataSet, file.getName()); //this.getClass().getName());
	}

	@Override
	public String toString() {
		return "PDB File";
	}
}
