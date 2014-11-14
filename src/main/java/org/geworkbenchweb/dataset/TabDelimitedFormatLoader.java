package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;

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
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class TabDelimitedFormatLoader extends LoaderUsingAnnotation {
	private static Log log = LogFactory.getLog(TabDelimitedFormatLoader.class);

	/* meant to be used by the factory, not publicly */
	TabDelimitedFormatLoader() {
	};

	@Override
	public void load(File file, DataSet dataset) throws GeWorkbenchLoaderException {
		// this should have been checked earlier one
		String filename = file.getName();
		if (!filename.toLowerCase().endsWith(".tsv")
				&& !filename.toLowerCase().endsWith(".txt")) {
			throw new GeWorkbenchLoaderException(
					"File name "
							+ file.getName()
							+ " does not end with .tsv or .txt. Please choose a file with those extensions");
		}

		MicroarraySet cleanMicroaraySet;

		TabDelimitedFormatParser parser = new TabDelimitedFormatParser(file);
		try {
			cleanMicroaraySet = parser.parse();
			MicroarrayDataset jpaDataset = new MicroarrayDataset(cleanMicroaraySet);
			FacadeFactory.getFacade().store(jpaDataset);
			Long id = jpaDataset.getId();
			
			dataset.setDataId(id);
			dataset.setName(file.getName());
			dataset.setType(jpaDataset.getClass().getName());
			dataset.setDescription("Microarray experiment"+". # of microarrays: " + cleanMicroaraySet.arrayNumber + ",   "
					+ "# of markers: " + cleanMicroaraySet.markerNumber);
			FacadeFactory.getFacade().store(dataset);
			createDefaultContexts(dataset.getId());
		} catch (InputFileFormatException e1) {
			e1.printStackTrace();
			throw new GeWorkbenchLoaderException("input file format "+e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new GeWorkbenchLoaderException("io exception "+e1);
		}

		Long datasetId = dataset.getId();
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

	@Override
	public String toString() {
		return "Tab-delimited format File";
	}

	private static void createDefaultContexts(Long datasetId){
		/* for microarrays */
		Context defaultContext = new Context("Default Context", SubSet.SET_TYPE_MICROARRAY, datasetId);
		FacadeFactory.getFacade().store(defaultContext);
		CurrentContext current = new CurrentContext(SubSet.SET_TYPE_MICROARRAY, datasetId, defaultContext.getId());
		FacadeFactory.getFacade().store(current);

		/* for markers */
		Context defaultMarkerContext = new Context("Default Context", SubSet.SET_TYPE_MARKER, datasetId);
		FacadeFactory.getFacade().store(defaultMarkerContext);
		CurrentContext currentMarkerContext = new CurrentContext(SubSet.SET_TYPE_MARKER, datasetId, defaultMarkerContext.getId());
		FacadeFactory.getFacade().store(currentMarkerContext);
	}
}
