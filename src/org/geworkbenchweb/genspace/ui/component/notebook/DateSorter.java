package org.geworkbenchweb.genspace.ui.component.notebook;

import java.util.Comparator;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;

public class DateSorter implements Comparator<AnalysisEvent>{
	
	public int compare(AnalysisEvent ev1, AnalysisEvent ev2) {
		return ev2.getCreatedAt().compare(ev1.getCreatedAt());
	}
} 
