package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class TabDelimitedFormatLoader implements LoaderUsingAnnotation {
	private static Log log = LogFactory.getLog(TabDelimitedFormatLoader.class);

	public static final String MAIN_DELIMITER = "\\s*///\\s*";
	
	transient private Long datasetId;	

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
			dataset.setType("org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet");
			dataset.setDescription("Microarray experiment"+". # of microarrays: " + cleanMicroaraySet.arrayNumber + ",   "
					+ "# of markers: " + cleanMicroaraySet.markerNumber);
			FacadeFactory.getFacade().store(dataset);
			
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
				
				int[] biologicalProcess = getGoTerms(fields.getBiologicalProcess());
			    int[] cellularComponent = getGoTerms(fields.getCellularComponent());
			    int[] molecularFunction = getGoTerms(fields.getMolecularFunction());
				AnnotationEntry entry = new AnnotationEntry(probeSetId, geneSymbol, geneDescription, entrezId, biologicalProcess, cellularComponent, molecularFunction);
				list.add(entry);
			}
			return list;
		} catch (InputFileFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int[] getGoTerms(String goTermInfo)
	{
        if (goTermInfo ==  null || goTermInfo.trim().equals("---"))
        	return null;
		String[] goTerms =  goTermInfo.split(MAIN_DELIMITER);
        int[] goTermIds = new int[goTerms.length];
        for (int i=0; i<goTerms.length; i++) {
        	goTermIds[i] = new Integer(goTerms[i].split("/")[0].trim()).intValue();
			
        }
        return goTermIds;
        
	}

	@Override
	public String toString() {
		return "Tab-delimited format File";
	}
}
