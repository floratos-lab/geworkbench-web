package org.geworkbenchweb.utils;

import java.util.ArrayList;
import java.util.Date;
 
import java.util.List;
import java.util.Map;
import java.io.File;

 
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
 
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.geworkbenchweb.annotation.Affy3ExpressionAnnotationParser;
import org.geworkbenchweb.annotation.AffyAnnotationParser;
import org.geworkbenchweb.annotation.AffyGeneExonStAnnotationParser;
import org.geworkbenchweb.annotation.AnnotationFields; 
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
 

public class ImportAnnotation {
	private static final String MAIN_DELIMITER = "\\s*///\\s*";

	 
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		if (args.length < 2)
		{
			System.out.println("please specify an annotation file name with full path\n and an annotation type (0 or 1) on the command line, \n where 0 stands for AFFYMETRIX_3_EXPRESSION, 1 stands for AFFY_GENE_EXON_ST. ");
		    System.exit(0);
		}
		
		 
					 
		String annoFile = args[0];
		int  annoType = new Integer(args[1]);
		importAnno(annoFile, annoType);

	}

	public static void importAnno(String annoFile, int annoType) {
		System.out.println("Import annotation process started at: "
				+ (new Date()).toString());
		try {
		File inFile = new File(annoFile);
		if (!inFile.exists()) {
			throw new Exception("Directory: " + annoFile + " does not exist.");
		}
		
		FacadeFactory.registerFacade("default", true);
 
		parseAnnotation(inFile, annoType);
		System.out.println("Import annotation process finished at: "
				+ (new Date()).toString());
		
		} catch (InstantiationException e) {
			e.printStackTrace();
			 
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			
		} catch(InputFileFormatException ife)
		{
			ife.printStackTrace();
		}		
		catch (Exception e) {
			 
			e.printStackTrace();
		}
		
	}
	
	public static void parseAnnotation(File annotFile, int annotType){
 
		AnnotationType annotationType = AnnotationType.AFFYMETRIX_3_EXPRESSION;
		AnnotationType[] annotationTypes = AnnotationType.values();
		for (AnnotationType at : annotationTypes)
		{
			if (at.ordinal() == annotType)
			{
				annotationType = at;
				break;
			}
		}
		
		 
		List<AnnotationEntry> newAnnotation = parse(annotFile, annotationType.toString());

			Annotation annotation = new Annotation(annotFile.getName(),
					annotationType.toString(), newAnnotation);
			annotation.setOwner(null);
			System.out.println("started store annotation to database.");
			FacadeFactory.getFacade().store(annotation);
			System.out.println("finished store annotation to database.");			
			 
	 
	 
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
			System.out.println("started  parse annotation file");
			Map<String, AnnotationFields> annotation = annotParser.parse(
					annotFile, false);
			System.out.println("finished parse annotation file");
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
					System.out.println("gene description length="+geneDescription.length()+"\n"+geneDescription);
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
