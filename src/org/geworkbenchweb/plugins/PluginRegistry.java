/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central control of all Plug-ins. 
 * 
 * TODO this will be enhanced, but a simplistic version is created for now. 
 * 
 * @author zji
 *
 */
public class PluginRegistry {
	
	private Map<Analysis, AnalysisUI> analysisMap = new HashMap<Analysis, AnalysisUI>();

	public void register(Analysis a, AnalysisUI analysisUI) {
		analysisMap.put(a, analysisUI);
	}
	
	public List<Analysis> getAnalysisList() {
		return new ArrayList<Analysis>(analysisMap.keySet());
		
		// TODO we may not want to open up the list itself
		/*
		List<String> list = new ArrayList<String>();
		for(Analysis a : analysisList) {
			list.add(a.getName());
		}
		return list;
		*/
	}
	
	public AnalysisUI getUI(Analysis a) {
		return analysisMap.get(a);
	}
}
