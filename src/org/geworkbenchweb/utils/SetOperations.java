package org.geworkbenchweb.utils;

import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class SetOperations {

	public boolean storeData(String selectedValues, String setType,
			String name, long l) {
		
		
		User user 		= 	SessionHandler.get();
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


}
