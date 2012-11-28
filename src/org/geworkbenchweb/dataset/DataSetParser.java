package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.Affy3ExpressionAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyGeneExonStAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.parsers.GeoSeriesMatrixParser;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.parsers.MicroarraySetParser;
import org.geworkbench.parsers.PDBFileFormat;
import org.geworkbench.parsers.SOFTFileFormat;
import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

@Deprecated // the functionality implemented by this classes is replaced by org.geworkbenchweb.parsers
public class DataSetParser {

	private String fileName;
	private String dataType = null; 
	private String annotationFileName;
	
	public DataSetParser(File dataFile, File annotFile, String fileType,
			AnnotationType annotType, User annotOwner) {
		
		this.fileName 			= 	dataFile.getName();
		
		if (annotFile != null)
			this.annotationFileName		= 	annotFile.getName();

		if(fileType == "GEO SOFT File") {
			this.dataType = "microarray";
			GeoSeriesDataSet(dataFile, annotFile);
		} else if(fileType == "Expression File") {
			this.dataType = "microarray";
			ExpressionDataSet(dataFile, annotFile, annotType, annotOwner);
		} else if (fileType == "PDB File"){
			this.dataType = "PDB File";
			PDBDataSet(dataFile);
		} else if(fileType == "GDS") {
			this.dataType = "microarray";
			GDSDataSet(dataFile, annotFile);
		}
	}
	
	private void ExpressionDataSet(File dataFile, File annotFile, AnnotationType annotType, User annotOwner) {
		
		MicroarraySetParser parser 	= 	new MicroarraySetParser();
		DSMicroarraySet dataSet 	= 	parser.parseCSMicroarraySet(dataFile);
		
		if(dataSet.isEmpty()) {
			System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
		}else {
			DataSet dataset			= storeData(dataSet);
			Annotation annotation	= storeAnnotation(dataSet, annotFile, annotType, annotOwner);
			storeDatasetAnnotation(dataset, annotation);
		}
	}

	public void GeoSeriesDataSet(File dataFile, File annotFile) {
		GeoSeriesMatrixParser parser 	= 	new GeoSeriesMatrixParser();
		try {
			DSMicroarraySet dataSet 	= 	parser.getMArraySet(dataFile);
			if(annotFile.getName() != null) {
				dataSet.setAnnotationFileName(annotFile.getName());
			} 
			if(dataSet.isEmpty()) {	
				System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
			}else {
				storeData(dataSet);
			}
		} catch (InputFileFormatException e) {
			System.out.println("Check file format");
		} catch (InterruptedIOException e) {
			System.out.println("Interrupted by parser");
		}
	}
	
	public void GDSDataSet(File dataFile, File annotFile) {
		SOFTFileFormat parser 	= 	new SOFTFileFormat();
		try {
			DSMicroarraySet dataSet 	= 	parser.parseFile(dataFile);
			if(dataSet.isEmpty()) {
				System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
			}else {
				storeData(dataSet);
			}
		} catch (InputFileFormatException e) {
			System.out.println("Check file format");
		} catch (InterruptedIOException e) {
			System.out.println("Interrupted by parser");
		}
	}

	private void PDBDataSet(File dataFile){
		DSDataSet<? extends DSBioObject> dataSet = new PDBFileFormat().getDataFile(dataFile);
		if(dataSet.getFile() == null || !dataFile.getName().toLowerCase().endsWith(".pdb")) {
			System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
		}else {
			storeData(dataSet);
		}
	}

	private DataSet storeData(DSDataSet<? extends DSBioObject> dataSet) {
		
		User user 		= 	SessionHandler.get();
		DataSet dataset = 	new DataSet();
		
		dataset.setName(fileName);
		dataset.setType(dataType);
	    dataset.setOwner(user.getId());	
	    dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
	    dataset.setData(ObjectConversion.convertToByte(dataSet));
	    FacadeFactory.getFacade().store(dataset);
	    
	    DataHistory dataHistory = new DataHistory();
		dataHistory.setParent(dataset.getId());
		StringBuilder data =	new StringBuilder(); 
		data.append("Data File Name : " + dataSet.getLabel() + "\n");
		if(dataType.equalsIgnoreCase("microarray")) {
			data.append("Annotation File - " + annotationFileName +"\n");
			data.append("Gene Ontology File - \n");
		}
		dataHistory.setData(ObjectConversion.convertToByte(data.toString()));
		FacadeFactory.getFacade().store(dataHistory);
	
		if(dataType.equalsIgnoreCase("microarray")) {
			ExperimentInfo experimentInfo = new ExperimentInfo();
			experimentInfo.setParent(dataset.getId());
			StringBuilder info =	new StringBuilder(); 
			info.append("Number of phenotypes in the data set - " + ((DSMicroarraySet) dataSet).size() + "\n");
			info.append("Number of markers in the data set - " + ((DSMicroarraySet) dataSet).getMarkers().size() + "\n");
			experimentInfo.setInfo(ObjectConversion.convertToByte(info.toString()));
			FacadeFactory.getFacade().store(experimentInfo);
		}
		
	    NodeAddEvent resultEvent = new NodeAddEvent(dataset);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);
		return dataset;
	}

	private Annotation storeAnnotation(DSMicroarraySet dataSet, File annotFile, AnnotationType annotType, User annotOwner){
		if (annotFile == null) {
			AnnotationParser.setCurrentDataSet(dataSet);
			return null;
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("name", annotFile.getName());
		//if shared default annotation exists, return it
		if (annotOwner == null){
			List<Annotation> annots = FacadeFactory.getFacade().list(
					"Select a from Annotation as a where a.name=:name and a.owner is NULL", parameters);
			if (!annots.isEmpty()) return annots.get(0);
		}
		//if user's annotation exists, return it
		else{
			parameters.put("owner", annotOwner.getId());
			List<Annotation> annots = FacadeFactory.getFacade().list(
					"Select a from Annotation as a where a.name=:name and a.owner=:owner", parameters);
			if (!annots.isEmpty()) return annots.get(0);
		}
		
		//otherwise create it
		if (!annotFile.exists()) return null;
		AffyAnnotationParser annotParser = null;
		if (annotType.equals(AnnotationType.AFFYMETRIX_3_EXPRESSION))
			annotParser = new Affy3ExpressionAnnotationParser();
		else if (annotType.equals(AnnotationType.AFFY_GENE_EXON_ST))
			annotParser = new AffyGeneExonStAnnotationParser();
		try{
			AnnotationParser.loadAnnotationFile(dataSet, annotFile, annotParser);
		}catch(InputFileFormatException e){
			e.printStackTrace();
		}
		Annotation annotation = new Annotation(annotFile.getName(), annotType.toString());
		annotation.setOwner(annotOwner==null?null:annotOwner.getId());
		annotation.setAnnotation(ObjectConversion.convertToByte(AnnotationParser.getSerializable()));
		FacadeFactory.getFacade().store(annotation);
		return annotation;
	}

	private void storeDatasetAnnotation(DataSet dataset, Annotation annotation){	
		if (dataset == null || annotation == null) return;

		DataSetAnnotation da = new DataSetAnnotation();
		da.setDatasetId(dataset.getId());
		da.setAnnotationId(annotation.getId());
		FacadeFactory.getFacade().store(da);
	}
}



