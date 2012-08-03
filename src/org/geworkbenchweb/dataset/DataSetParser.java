package org.geworkbenchweb.dataset;

import java.io.File;
import java.io.InterruptedIOException;

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
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class DataSetParser {

	private String fileName;
	private String fileType;  
	private String dataDescription;
	
	public DataSetParser(File dataFile, File annotFile, String fileType, String dataDescription) {
		
		
		this.fileName 			= 	dataFile.getName();
		this.fileType 			= 	fileType;
		this.dataDescription 	= 	dataDescription;
		
		
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
			NodeAddEvent resultEvent = new NodeAddEvent(dataSet.getDataSetName(), "Data Node");
			GeworkbenchRoot.getBlackboard().fire(resultEvent);
			
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
				NodeAddEvent resultEvent = new NodeAddEvent(dataSet.getDataSetName(), "Data Node");
				GeworkbenchRoot.getBlackboard().fire(resultEvent);
				
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
			NodeAddEvent resultEvent = new NodeAddEvent(dataSet.getDataSetName(), "Data Node");
			GeworkbenchRoot.getBlackboard().fire(resultEvent);

		}
	}

	private void storeData(Object dataSet) {
		
		User user 		= 	SessionHandler.get();
		DataSet dataset = 	new DataSet();
		
		dataset.setName(fileName);
		dataset.setType(fileType);
		dataset.setDescription(dataDescription);
	    dataset.setOwner(user.getId());	
	    dataset.setData(ObjectConversion.convertToByte(dataSet));
	    FacadeFactory.getFacade().store(dataset);
	    
	}
}



