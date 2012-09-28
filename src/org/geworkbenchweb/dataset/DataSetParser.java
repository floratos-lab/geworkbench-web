package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.parsers.GeoSeriesMatrixParser;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.parsers.MicroarraySetParser;
import org.geworkbench.parsers.PDBFileFormat;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class DataSetParser {

	private String fileName;
	private String fileType;  
	private String dataDescription;
	private String projectName;
	
	public DataSetParser(File dataFile, File annotFile, String fileType, String projectName, String dataDescription) {
		
		
		this.fileName 			= 	dataFile.getName();
		this.fileType 			= 	fileType;
		this.dataDescription 	= 	dataDescription;
		this.projectName		= 	projectName;
		
		
		if(fileType == "GEO SOFT File") {
			
			GeoSeriesDataSet(dataFile, annotFile);
		
		} else if(fileType == "Expression File") {
			
			ExpressionDataSet(dataFile, annotFile);
			
		} else if (fileType == "PDB File"){
			
			PDBDataSet(dataFile);
		}
	}
	
	private void ExpressionDataSet(File dataFile, File annotFile) {
		
		MicroarraySetParser parser 	= 	new MicroarraySetParser();
		DSMicroarraySet dataSet 	= 	parser.parseCSMicroarraySet(dataFile);
		
		if(dataSet.isEmpty()) {
			
			System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
		
		}else {
			storeData(dataSet);
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

	private void PDBDataSet(File dataFile){
		DSDataSet<? extends DSBioObject> dataSet = new PDBFileFormat().getDataFile(dataFile);

		if(dataSet.getFile() == null) {
			
			System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
		
		}else {
			
			storeData(dataSet);

		}
	}

	private void storeData(DSDataSet<? extends DSBioObject> dataSet) {
		
		User user 		= 	SessionHandler.get();
		DataSet dataset = 	new DataSet();
		
		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("name", projectName);
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());
		
		List<?> projects =  FacadeFactory.getFacade().list("Select p from Project as p where p.name=:name and p.workspace =:workspace", param);	
		
		dataset.setName(fileName);
		dataset.setType(fileType);
		dataset.setDescription(dataDescription);
	    dataset.setOwner(user.getId());	
	    dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
	    dataset.setProject(((Project) projects.get(0)).getId());
	    dataset.setData(ObjectConversion.convertToByte(dataSet));
	    FacadeFactory.getFacade().store(dataset);
	    
	    NodeAddEvent resultEvent = new NodeAddEvent(dataset);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);
	    
	}
}



