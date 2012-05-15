package org.geworkbenchweb.dataset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.parsers.GeoSeriesMatrixParser;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.parsers.MicroarraySetParser;
import org.geworkbenchweb.layout.UAccordionPanel;
import org.geworkbenchweb.pojos.DataSet;
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
			
		}
	}
	
	private void ExpressionDataSet(File dataFile, File annotFile) {
		
		MicroarraySetParser parser 	= 	new MicroarraySetParser();
		DSMicroarraySet dataSet 	= 	parser.parseCSMicroarraySet(dataFile);
		
		/* Here we are parsing Annotation file */
		/*if(annotFile != null) {
			
			AnnotationParser.loadAnnotationFile(dataSet, annotFile);
		
		} */
		
		if(dataSet.isEmpty()) {
			
			System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
		
		}else {
			
			storeData(dataSet);
			UAccordionPanel.resetDataContainer();
			
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
				UAccordionPanel.resetDataContainer();
				
			}
		} catch (InputFileFormatException e) {
			
			System.out.println("Check file format");
		
		} catch (InterruptedIOException e) {
			
			System.out.println("Interrupted by parser");
		
		}
	}

	private void storeData(Object dataSet) {
		
		User user 		= 	SessionHandler.get();
		DataSet dataset = 	new DataSet();
		
		dataset.setName(fileName);
		dataset.setType(fileType);
		dataset.setDescription(dataDescription);
	    dataset.setOwner(user.getId());	
	    dataset.setData(convertToByte(dataSet));
	    FacadeFactory.getFacade().store(dataset);
	    
	    UAccordionPanel.resetDataContainer();
	    
	}
	
	private byte[] convertToByte(Object object) {
		
		byte[] byteData = null;
		ByteArrayOutputStream bos 	= 	new ByteArrayOutputStream();
		  
		try {
			
			ObjectOutputStream oos 	= 	new ObjectOutputStream(bos); 
		    
			oos.writeObject(object);
		    oos.flush(); 
		    oos.close(); 
		    bos.close();
		    byteData 				= 	bos.toByteArray();
		  
		} catch (IOException ex) {
			  
			  System.out.println("Exception with in convertToByte");
		  
		}
		  
		return byteData;
	
	}
}



