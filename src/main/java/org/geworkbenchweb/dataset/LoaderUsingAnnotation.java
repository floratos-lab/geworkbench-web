package org.geworkbenchweb.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.annotation.Affy3ExpressionAnnotationParser;
import org.geworkbenchweb.annotation.AffyAnnotationParser;
import org.geworkbenchweb.annotation.AffyGeneExonStAnnotationParser;
import org.geworkbenchweb.annotation.AnnotationFields; 
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public abstract class LoaderUsingAnnotation implements Loader {

	private static Log log = LogFactory.getLog(LoaderUsingAnnotation.class);
	
	private static final String MAIN_DELIMITER = "\\s*///\\s*";

	/** 
	 * Find the annotation. 
	 * If it is a new annotation file, parse it and serialize with all the entries with JPA. 
	 * 
	 * @param annotOwner: either the actual user, or null - meaning a public annotation is chosen.
	 * */
	public void parseAnnotation(File annotFile, AnnotationType annotType,
			User annotOwner, Long dsId, Long annoId) throws GeWorkbenchLoaderException {
 
		Long annotationId = null;		
		Map<String, Object> parameters = new HashMap<String, Object>();	
		// if a public or private  annotation is chosen,
		if (annoId != null) {
			parameters.put("id", annoId);
			List<Annotation> annots = FacadeFactory
					.getFacade()
					.list("Select a from Annotation as a where a.id=:id",
							parameters);
			if (annots.isEmpty()){
				log.warn("The selected annotation file which id is "+ annoId +" not found in database.");
				return;
			}
			annotationId = annoId;
		}
		// if a new annotation is chosen,
		else {
			
			if (annotFile == null || !annotFile.exists()){
				if (annotFile != null)
				    log.warn("New annotation "+annotFile.getPath()+" not found on server.");
				return;
			}
			List<AnnotationEntry> newAnnotation = parse(annotFile, annotType.toString());

			Annotation annotation = new Annotation(annotFile.getName(),
					annotType.toString(), newAnnotation);
			annotation.setOwner(annotOwner == null ? null : annotOwner.getId());
			log.debug("started store annotation to database.");
			FacadeFactory.getFacade().store(annotation);
			log.debug("finished store annotation to database.");			
			annotationId = annotation.getId();
		}
	 
		if (annotationId != null){
			DataSetAnnotation da = new DataSetAnnotation();
			da.setDatasetId(dsId);
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
			log.debug("started  parse annotation file");
			Map<String, AnnotationFields> annotation = annotParser.parse(
					annotFile, false);
			log.debug("finished parse annotation file");
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
}
