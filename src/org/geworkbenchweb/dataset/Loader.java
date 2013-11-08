package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/** data set parser */
// class name DataSetParser has been used (not very properly)
public abstract class Loader {
	private static Log log = LogFactory.getLog(Loader.class);
			
	/**
	 * parse the file and store it (make it persistent)
	 * 
	 * @throws GeWorkbenchLoaderException
	 */
	// TODO return or indicate the type of data set thus created
	public abstract void load(File file, DataSet dataset) throws GeWorkbenchLoaderException;

	/**
	 * Store the data set.
	 * 
	 * This is called by the load method and is allowed to be modified by derived classes.
	 * @param dataSet
	 * @param fileName
	 * @param dataset
	 * @return dataset ID
	 * @throws GeWorkbenchLoaderException 
	 */
	protected Long storeData(DSDataSet<? extends DSBioObject> bisonDataSet,
			String fileName, DataSet dataset) throws GeWorkbenchLoaderException {
		
		dataset.setName(fileName);
		dataset.setType(bisonDataSet.getClass().getName());
		dataset.setDescription(bisonDataSet.getDescription());
		FacadeFactory.getFacade().store(dataset);
		
		try {
			UserDirUtils.serializeDataSet(dataset.getId(), bisonDataSet, dataset.getOwner());
		} catch (IOException e) {
			log.error("serialization of dataset failed due to IOException");
			e.printStackTrace();
			throw new GeWorkbenchLoaderException("serialization of dataset failed due to IOException");
		}
		
		DataHistory dataHistory = new DataHistory();
		dataHistory.setParent(dataset.getId());
		dataHistory.setData("Data File Name : " + bisonDataSet.getLabel() + "\n");
		//data.append("Annotation File - " + annotationFileName + "\n");
		//data.append("Gene Ontology File - \n");
		FacadeFactory.getFacade().store(dataHistory);

		return dataset.getId();
	}
	
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
