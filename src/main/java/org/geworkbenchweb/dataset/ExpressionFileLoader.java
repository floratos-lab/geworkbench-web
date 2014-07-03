package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class ExpressionFileLoader extends LoaderUsingAnnotation {
	private static Log log = LogFactory.getLog(ExpressionFileLoader.class);
	
	transient private Long datasetId;	

	// meant to be used by the factory, not publicly
	ExpressionFileLoader() {
	};

	@Override
	public void load(File file, DataSet dataset) throws GeWorkbenchLoaderException {
		// this should have been checked earlier one
		if (!file.getName().toLowerCase().endsWith(".exp")) {
			throw new GeWorkbenchLoaderException(
					"File name "+file.getName()+" does not end with .exp. Please choose file with .exp extension");
		}

		MicroarraySet cleanMicroaraySet;
		
		GeWorkbenchExpFileParser parser = new GeWorkbenchExpFileParser(file);
		try {
			cleanMicroaraySet = parser.parse();
			MicroarrayDataset jpaDataset = new MicroarrayDataset(cleanMicroaraySet);
			FacadeFactory.getFacade().store(jpaDataset);
			Long id = jpaDataset.getId();
			
			dataset.setDataId(id);
			dataset.setName(file.getName());
			dataset.setType("org.geworkbenchweb.pojos.MicroarrayDataset");
			dataset.setDescription("Microarray experiment"+". # of microarrays: " + cleanMicroaraySet.arrayNumber + ",   "
					+ "# of markers: " + cleanMicroaraySet.markerNumber);
			dataset.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
			FacadeFactory.getFacade().store(dataset);

			datasetId = dataset.getId();
			Map<String, String[]> setInformation = parser.parseSetInformation(cleanMicroaraySet.arrayNumber);
			storeContext(setInformation, cleanMicroaraySet.arrayLabels);
		} catch (InputFileFormatException e1) {
			e1.printStackTrace();
			throw new GeWorkbenchLoaderException("input file format "+e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new GeWorkbenchLoaderException("io exception "+e1);
		}

		DataHistory dataHistory = new DataHistory();
		dataHistory.setParent(datasetId);
		dataHistory.setData("Data File Name : " + file.getName() + "\n");
		FacadeFactory.getFacade().store(dataHistory);

		ExperimentInfo experimentInfo = new ExperimentInfo();
		experimentInfo.setParent(datasetId);
		StringBuilder info = new StringBuilder();
		info.append("Number of phenotypes in the data set - "
				+ cleanMicroaraySet.arrayNumber + "\n");
		info.append("Number of markers in the data set - "
				+ cleanMicroaraySet.markerNumber + "\n");
		experimentInfo.setInfo(info.toString());
		FacadeFactory.getFacade().store(experimentInfo);
		log.debug("finish loading");
	}

	/**
	 * store Contexts, CurrentContext, arrays SubSets and SubSetContexts for microarraySet
	 */
	private void storeContext(Map<String, String[]> setInformation, String[] arrayLabels){
		Context defaultContext = new Context("Default Context", SubSet.SET_TYPE_MICROARRAY, datasetId);
		FacadeFactory.getFacade().store(defaultContext);
		CurrentContext current = new CurrentContext(SubSet.SET_TYPE_MICROARRAY, datasetId, defaultContext.getId());
		FacadeFactory.getFacade().store(current);
		
		for (String contextName : setInformation.keySet()){
			
			Context context = new Context(contextName, SubSet.SET_TYPE_MICROARRAY, datasetId);
			FacadeFactory.getFacade().store(context);

			Map<String, ArrayList<String>> arraySets = new LinkedHashMap<String, ArrayList<String>>();
			
			String[] labels = setInformation.get(contextName);
			for (int arrayIndex = 0; arrayIndex < labels.length; arrayIndex++) {
				if (labels[arrayIndex] == null
						|| labels[arrayIndex].length() == 0)
					continue;

				if (labels[arrayIndex].indexOf("|") > -1) {
					for (String tok : labels[arrayIndex].split("\\|")) {
						if(arraySets.get(tok)==null) {
							arraySets.put(tok, new ArrayList<String>());
						}
						arraySets.get(tok).add(arrayLabels[arrayIndex]);
					}
				} else {
					String tok = labels[arrayIndex]; 
					if(arraySets.get(tok)==null) {
						arraySets.put(tok, new ArrayList<String>());
					}
					arraySets.get(tok).add(arrayLabels[arrayIndex]);
				}
			}
			for (String setName : arraySets.keySet()){
				/* Removing default Selection set from geWorkbench Swing version */
				if(!setName.equalsIgnoreCase("Selection")) { 
					ArrayList<String> arrays = arraySets.get(setName);
					SubSetOperations.storeArraySetInContext(arrays, setName, datasetId, context);
				}
			}
		}

		/* add a default context for markers */
		Context defaultMarkerContext = new Context("Default Context", SubSet.SET_TYPE_MARKER, datasetId);
		FacadeFactory.getFacade().store(defaultMarkerContext);
		CurrentContext currentMarkerContext = new CurrentContext(SubSet.SET_TYPE_MARKER, datasetId, defaultMarkerContext.getId());
		FacadeFactory.getFacade().store(currentMarkerContext);
	}

	@Override
	public String toString() {
		return "Expression File (.exp)";
	}

}
