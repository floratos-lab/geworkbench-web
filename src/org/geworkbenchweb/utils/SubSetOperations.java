package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Purpose of this class is to have all the operations on the subset table
 * @author Nikhil
 */

public class SubSetOperations {

	
	public static boolean storeData(String selectedValues, String setType,
			String name, long l) {
		
		SubSet subset  	= 	new SubSet();
		
		subset.setName(name);
		subset.setType(setType);
		subset.setOwner(SessionHandler.get().getId());
	    subset.setParent(l);
	    subset.setPositions(selectedValues);
		
		try {

		    FacadeFactory.getFacade().store(subset);
			
		} catch (Exception e) {
		
			return false;
			
		}
		
		return true;
	}

	public static List<?> getMarkerSet(String setName) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("name", setName);
		parameters.put("type", "marker");
		
		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.name=:name and p.type=:type ", parameters);
		return data;
	}
	
	public static List<?> getArraySet(String setName) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("name", setName);
		parameters.put("type", "microarray");
		
		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.name=:name and p.type=:type ", parameters);
		return data;
	}
	
	public static List<?> getMarkerSets(Long dataSetId) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("parent", dataSetId);
		parameters.put("type", "marker");
		
		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);
		
		return data;
	}
	

	public static List<?> getArraySets(Long dataSetId) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("parent", dataSetId);
		parameters.put("type", "Microarray");
		
		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);
		
		return data;
	}

	/**
	 * This method is used to delete all the Marker and Array sets for given dataSet
	 * @input dataSet ID
	 * 
	 */
	public static void deleteAllSets(Long dataSetId) {
		
		
		
	}
	
	public static boolean checkForDataSet(String subSetName) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("name", subSetName);
		
		List<?> data =  FacadeFactory.getFacade().list("Select p from SubSet as p where p.name=:name", parameters);
		
		if(data.isEmpty()) {
			return false;
		}else {
			return true;
		}
	
	}
}
