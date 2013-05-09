package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.utils.UserDirUtils;
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

	// this method was written like being shared mechanism (for now) to store
	// data
	// in fact, the behavior is overridden for expression file to implement
	// annotation.
	// TODO the return value is used only by expression file's annotation
	// implementation. this is an inconsistency in the design
	static Long storeData(DSDataSet<? extends DSBioObject> dataSet,
			String fileName, DataSet dataset) {
		// FIXME dependency on annotation should be re-designed.
		String annotationFileName = null;
		
		dataset.setName(fileName);
		dataset.setType(dataSet.getClass().getName());
		FacadeFactory.getFacade().store(dataset);

		boolean success = true;
		try {
			UserDirUtils.serializeDataSet(dataset.getId(), dataSet, dataset.getOwner());
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		if(!success) {
			System.out.println("something went wrong");
		}
		
		DataHistory dataHistory = new DataHistory();
		dataHistory.setParent(dataset.getId());
		StringBuilder data = new StringBuilder();
		data.append("Data File Name : " + dataSet.getLabel() + "\n");
		// TODO special things for CSMicroarraySet should be part of the overall design
		// not an aftermath fix
		if (dataSet.getClass()==CSMicroarraySet.class) {
			// (1)
			data.append("Annotation File - " + annotationFileName + "\n");
			data.append("Gene Ontology File - \n");
			
			// (2)
			ExperimentInfo experimentInfo = new ExperimentInfo();
			experimentInfo.setParent(dataset.getId());
			StringBuilder info = new StringBuilder();
			info.append("Number of phenotypes in the data set - "
					+ ((DSMicroarraySet) dataSet).size() + "\n");
			info.append("Number of markers in the data set - "
					+ ((DSMicroarraySet) dataSet).getMarkers().size() + "\n");
			experimentInfo.setInfo(info.toString());
			FacadeFactory.getFacade().store(experimentInfo);
		}
		dataHistory.setData(data.toString());
		FacadeFactory.getFacade().store(dataHistory);

		//NodeAddEvent resultEvent = new NodeAddEvent(dataset);
		//GeworkbenchRoot.getBlackboard().fire(resultEvent);

		return dataset.getId();
	}
	
	public DataSet storePendingData(String fileName, Long userId){

		DataSet dataset = new DataSet();
		dataset.setName(fileName + " - Pending");
		dataset.setType(DSDataSet.class.getName());
		dataset.setOwner(userId);
		dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
		FacadeFactory.getFacade().store(dataset);
		
		return dataset;
	}
}
