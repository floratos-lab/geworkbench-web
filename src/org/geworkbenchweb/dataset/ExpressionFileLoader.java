package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.annotation.Affy3ExpressionAnnotationParser;
import org.geworkbenchweb.annotation.AffyAnnotationParser;
import org.geworkbenchweb.annotation.AffyGeneExonStAnnotationParser;
import org.geworkbenchweb.annotation.AnnotationFields;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.MicroarrayRow;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.data.User;
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
			MicroarrayDataset jpaDataset = convert(cleanMicroaraySet);
			FacadeFactory.getFacade().store(jpaDataset);
			Long id = jpaDataset.getId();
			
			dataset.setDataId(id);
			dataset.setName(file.getName());
			dataset.setType("org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet");
			dataset.setDescription("Microarray experiment"+". # of microarrays: " + cleanMicroaraySet.arrayNumber + ",   "
					+ "# of markers: " + cleanMicroaraySet.markerNumber);
			FacadeFactory.getFacade().store(dataset);
			
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
	}

	private static MicroarrayDataset convert(MicroarraySet cleanMicroaraySet) {
		List<String> markerLabels = Arrays.asList(cleanMicroaraySet.markerLabels);
		List<String> arrayLabels = Arrays.asList(cleanMicroaraySet.arrayLabels);
		List<MicroarrayRow> rows = new ArrayList<MicroarrayRow>();
		for(int i=0; i<cleanMicroaraySet.markerNumber; i++) {
			float[] rowValues = cleanMicroaraySet.values[i];
			MicroarrayRow row = new MicroarrayRow(rowValues);
			rows.add(row );
		}
		MicroarrayDataset jpaDataset = new MicroarrayDataset(markerLabels, arrayLabels, rows);
		return jpaDataset;
	}

	/** 
	 * Find the annotation. 
	 * If it is a new annotation file, parse it and serialize with all the entries with JPA. 
	 * 
	 * @param annotOwner: either the actual user, or null - meaning a public annotation is chosen.
	 * */
	@Override
	public void parseAnnotation(File annotFile, AnnotationType annotType,
			User annotOwner, Long dsId) throws GeWorkbenchLoaderException {

		datasetId = dsId;

		if (annotFile == null) {
			return;
		}
		
		Long annotationId = null;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("name", annotFile.getName());
		// if a public annotation is chosen,
		if (annotOwner == null) {
			List<Annotation> annots = FacadeFactory
					.getFacade()
					.list("Select a from Annotation as a where a.name=:name and a.owner is NULL",
							parameters);
			if (!annots.isEmpty()){
				annotationId = annots.get(0).getId();
			}
		}
		// if the user's private annotation is chosen,
		else {
			parameters.put("owner", annotOwner.getId());
			List<Annotation> annots = FacadeFactory
					.getFacade()
					.list("Select a from Annotation as a where a.name=:name and a.owner=:owner",
							parameters);
			if (!annots.isEmpty()){
				annotationId = annots.get(0).getId();
			}
			if (annotType == null){
				log.warn("Private annotation "+annotFile.getName()+" not found in database.");
				return;
			}
		}

		// otherwise, this is a new annotation file.
		if (!annotFile.exists()){
			log.warn("New annotation "+annotFile.getPath()+" not found on server.");
			return;
		}
		List<AnnotationEntry> newAnnotation = parse(annotFile, annotType.toString());

		Annotation annotation = new Annotation(annotFile.getName(),
				annotType.toString(), newAnnotation);
		annotation.setOwner(annotOwner == null ? null : annotOwner.getId());
		FacadeFactory.getFacade().store(annotation);
		annotationId = annotation.getId();

		if (annotationId != null){
			DataSetAnnotation da = new DataSetAnnotation();
			da.setDatasetId(datasetId);
			da.setAnnotationId(annotationId);
			FacadeFactory.getFacade().store(da);
		}
	}
	
	private static List<AnnotationEntry> parse(File annotFile,
			String type) {
		AffyAnnotationParser annotParser = null;
		if (type.equals(AnnotationType.AFFYMETRIX_3_EXPRESSION.toString())) {
			annotParser = new Affy3ExpressionAnnotationParser();
		} else if (type.equals(AnnotationType.AFFY_GENE_EXON_ST.toString())) {
			annotParser = new AffyGeneExonStAnnotationParser();
		}

		try {
			Map<String, AnnotationFields> annotation = annotParser.parse(
					annotFile, false);
			List<AnnotationEntry> list = new ArrayList<AnnotationEntry>();
			for(String probeSetId : annotation.keySet()) {
				AnnotationFields fields = annotation.get(probeSetId);
				String geneSymbol = fields.getGeneSymbol();
				if(geneSymbol.contains("///")) {
					// TODO simple solution for now: if there is multiple values, keep only the first one.
					String[] s = geneSymbol.split("///");
					geneSymbol = s[0];
				}
				String geneDescription = fields.getDescription();
				if(geneDescription.contains("///")) {
					// TODO simple solution for now: if there is multiple values, keep only the first one.
					String[] s = geneDescription.split("///");
					geneDescription = s[0];
				}
				if(geneDescription.length()>200) {
					log.warn("gene description length="+geneDescription.length()+"\n"+geneDescription);
					geneDescription = "too long";
				}
				String entrezId = fields.getLocusLink();
				if(entrezId.contains("///")) {
					// TODO simple solution for now: if there is multiple values, keep only the first one.
					String[] s = entrezId.split("///");
					entrezId = s[0];
				}
				AnnotationEntry entry = new AnnotationEntry(probeSetId, geneSymbol, geneDescription, entrezId);
				list.add(entry);
			}
			return list;
		} catch (InputFileFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * store Contexts, CurrentContext, arrays SubSets and SubSetContexts for microarraySet
	 */
	private void storeContext(Map<String, String[]> setInformation, String[] arrayLabels){
		Context defaultContext = new Context("Default Context", "microarray", datasetId);
		FacadeFactory.getFacade().store(defaultContext);
		CurrentContext current = new CurrentContext("microarray", datasetId, defaultContext.getId());
		FacadeFactory.getFacade().store(current);
		
		for (String contextName : setInformation.keySet()){
			
			Context context = new Context(contextName, "microarray", datasetId);
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
		Context defaultMarkerContext = new Context("Default Context", "marker", datasetId);
		FacadeFactory.getFacade().store(defaultMarkerContext);
		CurrentContext currentMarkerContext = new CurrentContext("marker", datasetId, defaultMarkerContext.getId());
		FacadeFactory.getFacade().store(currentMarkerContext);
	}

	@Override
	public String toString() {
		return "Expression File";
	}

}
