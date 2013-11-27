package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class PdbFileLoader extends Loader {

	// meant to be used by the factory, not publicly
	PdbFileLoader() {
	};

	@Override
	public void load(File file, DataSet dataset) throws GeWorkbenchLoaderException {
		// this should have been checked earlier one
		if (!file.getName().toLowerCase().endsWith(".pdb")) {
			throw new GeWorkbenchLoaderException(
					"File name "+file.getName()+" does not end with .pdb. Please choose a file with .pdb extension.");
		}

		/* note: only the file name is retained */
		String fileName = file.getName();
		dataset.setName(fileName);
		dataset.setType("org.geworkbenchweb.pojos.PdbFileInfo"); // this used to be DSProteinStructure/CSProteinStructure
		dataset.setDescription(""); // this used to be number of chains, parsed from the PDF file TODO
		FacadeFactory.getFacade().store(dataset);

		DataHistory dataHistory = new DataHistory();
		dataHistory.setParent(dataset.getId());
		dataHistory.setData("Data File Name : " + fileName + "\n");
		FacadeFactory.getFacade().store(dataHistory);
	}

	@Override
	public String toString() {
		return "PDB File";
	}
}
