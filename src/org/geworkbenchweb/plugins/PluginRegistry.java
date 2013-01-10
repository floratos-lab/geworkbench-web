/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * Central control of all Plug-ins. 
 * 
 * TODO this will be enhanced, but a simplistic version is created for now. 
 * 
 * @author zji
 *
 */
public class PluginRegistry {
	
	static private PluginRegistry INSTANCE = new PluginRegistry();
	private List<Analysis> analysisList = new ArrayList<Analysis>();

	private PluginRegistry() {
		
	}
	
	static public PluginRegistry getInstance() {
		return INSTANCE;
	}
	
	public void register(Analysis a) {
		analysisList.add(a);
	}
	
	public List<Analysis> getAnalysisList() {
		return analysisList;
		
		// TODO we may not want to open up the list itself
		/*
		List<String> list = new ArrayList<String>();
		for(Analysis a : analysisList) {
			list.add(a.getName());
		}
		return list;
		*/
	}
}
