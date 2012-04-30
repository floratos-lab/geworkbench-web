package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * Purpose of this class is to have all the operations on the subset table
 * @author Nikhil
 */

public class DataSetOperations {

	public static List<?> getDataSet(String dataSetName) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("name", dataSetName);
		parameters.put("owner", SessionHandler.get().getId());
		
		List<?> data = FacadeFactory.getFacade().list("Select p from DataSet as p where p.name=:name and p.owner=:owner ", parameters);
				
		return data;
	}
	
}
