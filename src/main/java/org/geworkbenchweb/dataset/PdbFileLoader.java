package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.PdbFileInfo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class PdbFileLoader implements Loader {

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

		PdbFileInfo pdbFileInfo = new PdbFileInfo(file);
		FacadeFactory.getFacade().store(pdbFileInfo);
		Long id = pdbFileInfo.getId();
		
		String fileName = file.getName();
		dataset.setName(fileName);
		dataset.setType("org.geworkbenchweb.pojos.PdbFileInfo");
		dataset.setDescription("# of chains: " + pdbFileInfo.getChains().size());
		dataset.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
		dataset.setDataId(id);
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
