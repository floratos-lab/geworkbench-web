package org.geworkbenchweb.genspace.ui.component.notebook;

import java.util.Comparator;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;

public class ToolSorter implements Comparator<AnalysisEvent>{
	
	public int compare(AnalysisEvent ev1, AnalysisEvent ev2) {
		return ev1.getToolname().compareTo(ev2.getToolname());
	}

}
