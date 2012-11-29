package org.geworkbenchweb.dataset;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.Affy3ExpressionAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyGeneExonStAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.parsers.MicroarraySetParser;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class ExpressionFileLoader extends LoaderUsingAnnotation {

	// FIXME the reason we need to retain this is that the annotation mechanism
	// is not really fixed
	transient private DSMicroarraySet microarraySet;
	transient private Long datasetId;

	// meant to be used by the factory, not publicly
	ExpressionFileLoader() {
	};

	@Override
	public void load(File file) throws GeWorkbenchLoaderException {
		// this should have been checked earlier one
		if (!file.getName().toLowerCase().endsWith(".exp")) {
			throw new GeWorkbenchLoaderException(
					"file name "+file.getName()+" does not end with .exp");
		}

		MicroarraySetParser parser = new MicroarraySetParser();
		microarraySet = parser.parseCSMicroarraySet(file);

		// FIXME hard-code type name has to be fixed
		datasetId = storeData(microarraySet, file.getName(), "microarray");
		//this.getClass().getName());
	}

	// this has to be called right after parse to have access to microarraySet
	// and datasetId
	@Override
	public void parseAnnotation(File annotFile, AnnotationType annotType,
			User annotOwner) throws GeWorkbenchLoaderException {
		Long annotationId = storeAnnotation(microarraySet, annotFile,
				annotType, annotOwner);

		if (annotationId != null){
			DataSetAnnotation da = new DataSetAnnotation();
			da.setDatasetId(datasetId);
			da.setAnnotationId(annotationId);
			FacadeFactory.getFacade().store(da);
		}

		microarraySet = null;
	}

	private static Long storeAnnotation(DSMicroarraySet dataSet, File annotFile,
			AnnotationType annotType, User annotOwner) {
		if (annotFile == null) {
			AnnotationParser.setCurrentDataSet(dataSet);
			return null;
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("name", annotFile.getName());
		// if shared default annotation exists, return it
		if (annotOwner == null) {
			List<Annotation> annots = FacadeFactory
					.getFacade()
					.list("Select a from Annotation as a where a.name=:name and a.owner is NULL",
							parameters);
			if (!annots.isEmpty())
				return annots.get(0).getId();
		}
		// if user's annotation exists, return it
		else {
			parameters.put("owner", annotOwner.getId());
			List<Annotation> annots = FacadeFactory
					.getFacade()
					.list("Select a from Annotation as a where a.name=:name and a.owner=:owner",
							parameters);
			if (!annots.isEmpty())
				return annots.get(0).getId();
		}

		// otherwise create it
		if (!annotFile.exists())
			return null;
		AffyAnnotationParser annotParser = null;
		if (annotType.equals(AnnotationType.AFFYMETRIX_3_EXPRESSION))
			annotParser = new Affy3ExpressionAnnotationParser();
		else if (annotType.equals(AnnotationType.AFFY_GENE_EXON_ST))
			annotParser = new AffyGeneExonStAnnotationParser();
		try {
			AnnotationParser
					.loadAnnotationFile(dataSet, annotFile, annotParser);
		} catch (InputFileFormatException e) {
			e.printStackTrace();
		}
		Annotation annotation = new Annotation(annotFile.getName(),
				annotType.toString());
		annotation.setOwner(annotOwner == null ? null : annotOwner.getId());
		// FIXME storing the complete map is not the right way to store one
		// annotation file;
		// FIXME storing the weak reference map makes this even more
		// problematic.
		annotation.setAnnotation(ObjectConversion
				.convertToByte(AnnotationParser.getSerializable()));
		FacadeFactory.getFacade().store(annotation);

		return annotation.getId();
	}

	@Override
	public String toString() {
		return "Expression File";
	}

}
