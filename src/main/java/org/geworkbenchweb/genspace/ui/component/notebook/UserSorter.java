package org.geworkbenchweb.genspace.ui.component.notebook;

import java.util.Comparator;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;

public class UserSorter implements Comparator<AnalysisEvent>{
	
	public int compare(AnalysisEvent ev1, AnalysisEvent ev2) {
		return ev1.getTransaction().getUser().getUsername().compareTo(ev2.getTransaction().getUser().getUsername());
	}

}
