/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.ui.AbstractComponentContainer;

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
	private Map<Analysis, AbstractComponentContainer> analysisMap = new HashMap<Analysis, AbstractComponentContainer>();

	private PluginRegistry() {
		
	}
	
	static public PluginRegistry getInstance() {
		return INSTANCE;
	}
	
	public void register(Analysis a, AbstractComponentContainer analysisUI) {
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
	
	public AbstractComponentContainer getUI(Analysis a) {
		return analysisMap.get(a);
	}
}
