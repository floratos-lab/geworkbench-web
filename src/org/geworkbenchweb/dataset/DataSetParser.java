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
import org.geworkbench.parsers.SOFTFileFormat;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class DataSetParser {

	private String fileName;
	private String dataType = null;  
	public DataSetParser(File dataFile, File annotFile, String fileType) {
		
		this.fileName 			= 	dataFile.getName();
		if(fileType == "GEO SOFT File") {
			this.dataType = "microarray";
			GeoSeriesDataSet(dataFile, annotFile);
		} else if(fileType == "Expression File") {
			this.dataType = "microarray";
			ExpressionDataSet(dataFile, annotFile);
		} else if (fileType == "PDB File"){
			this.dataType = "PDB File";
			PDBDataSet(dataFile);
		} else if(fileType == "GDS") {
			this.dataType = "microarray";
			GDSDataSet(dataFile, annotFile);
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
		if(dataSet.getFile() == null) {
			System.out.println("Dataset loading failed due to some unknown error. Go debug !!");
		}else {
			storeData(dataSet);
		}
	}

	private void storeData(DSDataSet<? extends DSBioObject> dataSet) {
		User user 		= 	SessionHandler.get();
		DataSet dataset = 	new DataSet();
		
		dataset.setName(fileName);
		dataset.setType(dataType);
	    dataset.setOwner(user.getId());	
	    dataset.setWorkspace(WorkspaceUtils.getActiveWorkSpace());
	    dataset.setData(ObjectConversion.convertToByte(dataSet));
	    FacadeFactory.getFacade().store(dataset);
	    
	    NodeAddEvent resultEvent = new NodeAddEvent(dataset);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);
	}
}



