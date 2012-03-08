package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Purpose of this class is to have all the operations on the subset table
 * @author Nikhil
 */

public class SetOperations {

	User user 		= 	SessionHandler.get();
	
	public boolean storeData(String selectedValues, String setType,
			String name, long l) {
		
		SubSet subset  	= 	new SubSet();
		
		subset.setName(name);
		subset.setType(setType);
		subset.setOwner(user.getId());
	    subset.setParent(l);
	    subset.setPositions(selectedValues);
		
		try {

		    FacadeFactory.getFacade().store(subset);
			
		} catch (Exception e) {
		
			return false;
			
		}
		
		return true;
	}

	public List<?> getMarkerSets(Long dataSetId) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("parent", dataSetId);
		parameters.put("type", "marker");
		
		List<?> data = FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent=:parent and p.type=:type ", parameters);
		
		return data;
	}
	

	public List<?> getArraySets(Long dataSetId) {
		
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
	
	public void deleteAllSets(Long dataSetId) {
		
		
		
	}
}
