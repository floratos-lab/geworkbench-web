package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List; 
import java.util.Map;

 
import org.geworkbenchweb.pojos.Preference;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/**
 * Purpose of this class is to have all the operations on the DataSet table
 * @author Nikhil
 */

public class PreferenceOperations {

	 
	public static Preference getData(String name, Long userId) {
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("owner", userId);
		parameters.put("name", name);

		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from Preference as p where p.owner=:owner and p.name=:name ", parameters);

		if (data == null || data.size() ==0)
			return null;
		else
			return  (Preference)data.get(0);
	}
	
	public static Preference getData(Long dataSetId, String name, Long userId) {
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("owner", userId);
		parameters.put("name", name);
		parameters.put("dataSet", dataSetId);

		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from Preference as p where p.owner=:owner and p.dataSet=:dataSet and p.name=:name ", parameters);
		 
		if (data == null || data.size() ==0)
			return null;
		else
			return  (Preference)data.get(0);
	}
	
	 
	public static List<Preference> getAllPreferences(Long dataSetId, Long userId, String namelike) {
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("owner", userId);		 
		parameters.put("dataSet", dataSetId);
		parameters.put("name", namelike);
		
		List<Preference> data = FacadeFactory.getFacade().list("Select p from Preference as p where p.owner=:owner and (p.dataSet=:dataSet or p.dataSet is null) and p.name like :name  ", parameters);

		if (data == null || data.size() ==0)
			return null;
		else
			return  (List<Preference>)data;
	}
	
	 
	public static List<AbstractPojo> getAllPreferences(Long dataSetId) {
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		 	 
		parameters.put("dataSet", dataSetId);	 
		
		List<AbstractPojo> data = FacadeFactory.getFacade().list("Select p from Preference as p where p.dataSet=:dataSet ", parameters);

		if (data == null || data.size() ==0)
			return null;
		else
			return  (List<AbstractPojo>)data;
	}
	
	public static void deleteAllPreferences(Long dataSetId, Long userId, String namelike) {
		
		List<Preference> preferences = PreferenceOperations.getAllPreferences(dataSetId, userId,namelike);
		if ( preferences == null)
			return;
		FacadeFactory.getFacade().deleteAll(preferences);
	 
	}
	
	
public static void deleteAllPreferences(Long dataSetId) {
		
		List<AbstractPojo> preferences = PreferenceOperations.getAllPreferences(dataSetId);
		if ( preferences == null)
			return;
		FacadeFactory.getFacade().deleteAll(preferences);
	 
}
	
	
	public static void setValue(Object value, Preference pref) {
		
		pref.setValue(ObjectConversion.convertToByte(value));
		FacadeFactory.getFacade().store(pref);
	}
	
	
	
	public static boolean storeData(Object value, String type, String name, Long dataSetId, Long userId)
    {

		Preference preference  	= 	new Preference();

		preference.setValue(ObjectConversion.convertToByte(value));
		preference.setName(name);
		preference.setType(type);
		preference.setOwner(userId);
		preference.setDataSet(dataSetId);
		 
		try {
			FacadeFactory.getFacade().store(preference);
		} catch (Exception e) {
			return false;	
		}

		return true;
	}
	
	
	
}
