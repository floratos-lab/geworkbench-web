package org.geworkbenchweb.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.Project;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class ProjectOperations {
	
public static List<Project> getProjects(Long workspaceId) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("workspace", workspaceId);
		parameters.put("owner", SessionHandler.get().getId());

		List<Project> projects = FacadeFactory.getFacade().list("Select p from Project as p where p.owner=:owner and p.workspace=:workspace ", parameters);
		return projects;
	}

}
