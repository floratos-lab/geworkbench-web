package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/** data set parser */
// class name DataSetParser has been used (not very properly)
public abstract class Loader {
		
	/**
	 * parse the file and store it (make it persistent)
	 * 
	 * @throws GeWorkbenchLoaderException
	 */
	// TODO return or indicate the type of data set thus created
	public abstract void load(File file, DataSet dataset) throws GeWorkbenchLoaderException;

	public DataSet storePendingData(String fileName, Long userId){

		DataSet dataset = new DataSet();
		dataset.setName(fileName + " - Pending");
		dataset.setDescription("pending");
		dataset.setType(DSDataSet.class.getName());
		dataset.setOwner(userId);
		dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
		FacadeFactory.getFacade().store(dataset);
		
		return dataset;
	}
}
