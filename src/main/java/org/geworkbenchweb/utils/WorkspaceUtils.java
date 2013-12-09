package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Workspace;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/**
 * Purpose of this class is to have all the operations on the subset table
 * @author Nikhil
 */

public class WorkspaceUtils {

	public static Long getActiveWorkSpace() {
		
		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", SessionHandler.get().getId());
		
		List<AbstractPojo> workspaces =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);	
				
		return ((ActiveWorkspace) workspaces.get(0)).getWorkspace();
	}
	
	
	public static List<Workspace> getAvailableWorkspaces() {
		
		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", SessionHandler.get().getId());
		
		List<Workspace> workspaces =  FacadeFactory.getFacade().list("Select p from Workspace as p where p.owner=:owner", param);	
		
		return workspaces;
	}
	
}
