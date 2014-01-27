/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester3.Digester;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbenchweb.plugins.cnkb.CNKBResultSet;
import org.geworkbenchweb.plugins.microarray.MicroarrayUI;
import org.geworkbenchweb.plugins.proteinstructure.ProteinStructureUI;
import org.xml.sax.SAXException;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;

/**
 * Central control of all Plug-ins. 
 * 
 * @author zji
 * @version $Id$
 *
 */
public class PluginRegistry {
	
	private Map<PluginEntry, AnalysisUI> analysisUIMap = new HashMap<PluginEntry, AnalysisUI>();
	private Map<Class<?>, ThemeResource> iconMap = new HashMap<Class<?>, ThemeResource>();
	private Map<Class<?>, Class<? extends DataTypeMenuPage>> uiMap = new HashMap<Class<?>, Class<? extends DataTypeMenuPage>>(); 
	private Map<Class<? extends DSDataSet<?>>, List<PluginEntry>> analysisMap = new HashMap<Class<? extends DSDataSet<?>>, List<PluginEntry>>();
	
	// TODO for now, let's maintain a separate list for result type. this may not necessary eventually
	private Map<Class<?>, ThemeResource> resultIconMap = new HashMap<Class<?>, ThemeResource>();
	private Map<Class<?>, Class<? extends Visualizer>> resultUiMap = new HashMap<Class<?>, Class<? extends Visualizer>>();
	private Map<Class<? extends Visualizer>, PluginEntry> visualizerPluginEntry = new HashMap<Class<? extends Visualizer>, PluginEntry>(); 
	
	static private ThemeResource microarrayIcon 	=	new ThemeResource("../custom/icons/chip16x16.gif");
	static private ThemeResource proteinIcon 		=	new ThemeResource("../custom/icons/dna16x16.gif");
	// the following are for result node
	static private ThemeResource hcIcon	 		=	new ThemeResource("../custom/icons/dendrogram16x16.gif");
	static private ThemeResource networkIcon	=	new ThemeResource("../custom/icons/network16x16.gif");
	static private ThemeResource markusIcon		=	new ThemeResource("../custom/icons/icon_world.gif");
	static private ThemeResource anovaIcon		=	new ThemeResource("../custom/icons/significance16x16.gif");
	static private ThemeResource marinaIcon		=	new ThemeResource("../custom/icons/generic16x16.gif");
	// other icons
	/*
	static private ThemeResource pendingIcon	=	new ThemeResource("../custom/icons/pending.gif");
	static private ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	static private ThemeResource cancelIcon 	= 	new ThemeResource("../runo/icons/16/cancel.png");
	*/

	/** Add all the initial registry entries.*/
	public void init() {
		resultIconMap.put(org.geworkbenchweb.pojos.HierarchicalClusteringResult.class, hcIcon); // hierarchical clustering result
		resultIconMap.put(CNKBResultSet.class, networkIcon); // cnkb result
		resultIconMap.put(AdjacencyMatrix.class, networkIcon); // aracne result or 'cytoscape' result
		resultIconMap.put(MarkUsResultDataSet.class, markusIcon); // markus result
		resultIconMap.put(org.geworkbenchweb.pojos.AnovaResult.class, anovaIcon); // anova result
		resultIconMap.put(org.geworkbenchweb.pojos.TTestResult.class, anovaIcon); // t-test result
		resultIconMap.put(org.geworkbenchweb.pojos.MraResult.class, marinaIcon); // marina result

		iconMap.put(DSMicroarraySet.class, microarrayIcon);
		iconMap.put(org.geworkbenchweb.pojos.PdbFileInfo.class, proteinIcon);

		uiMap.put(DSMicroarraySet.class, MicroarrayUI.class);
		uiMap.put(org.geworkbenchweb.pojos.PdbFileInfo.class, ProteinStructureUI.class);
		
		Digester digester = new Digester();
		digester.addObjectCreate("plugins", ArrayList.class);
		digester.addObjectCreate("plugins/analysis", ArrayList.class);
		digester.addObjectCreate("plugins/analysis/inputType", DataTypeEntry.class);
		digester.addSetProperties( "plugins/analysis/inputType", "className", "inputType");
		digester.addObjectCreate("plugins/analysis/inputType/plugin", PluginInfo.class);
		digester.addBeanPropertySetter( "plugins/analysis/inputType/plugin/name", "name" );
		digester.addBeanPropertySetter( "plugins/analysis/inputType/plugin/description", "description" );
		digester.addBeanPropertySetter( "plugins/analysis/inputType/plugin/uiClass", "uiClass");
		digester.addSetNext("plugins/analysis/inputType/plugin", "add" );
		digester.addSetNext("plugins/analysis/inputType", "add" );
		digester.addSetNext("plugins/analysis", "add" );

		digester.addObjectCreate("plugins/visualizer", ArrayList.class);
		digester.addObjectCreate("plugins/visualizer/inputType", DataTypeEntry.class);
		digester.addSetProperties( "plugins/visualizer/inputType", "className", "inputType");
		digester.addObjectCreate("plugins/visualizer/inputType/plugin", PluginInfo.class);
		digester.addBeanPropertySetter( "plugins/visualizer/inputType/plugin/name", "name" );
		digester.addBeanPropertySetter( "plugins/visualizer/inputType/plugin/description", "description" );
		digester.addBeanPropertySetter( "plugins/visualizer/inputType/plugin/uiClass", "uiClass");
		digester.addSetNext("plugins/visualizer/inputType/plugin", "add" );
		digester.addSetNext("plugins/visualizer/inputType", "add" );
		digester.addSetNext("plugins/visualizer", "add" );
		
		try {
			// list.get(0) is analysis; list.get(1) is visualizer
			List<List<DataTypeEntry>> list = digester.parse(this.getClass().getResourceAsStream("/plugins.xml"));
			convert(list);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void convert(List<List<DataTypeEntry>> overall) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<DataTypeEntry> list = overall.get(0);
		for(DataTypeEntry entry : list) {
			List<PluginEntry> entryList = new ArrayList<PluginEntry>();
			for(PluginInfo info : entry.getPluginList()) {
				PluginEntry pluginEntry = new PluginEntry(info.name, info.description);
				entryList.add(pluginEntry);
				Class<?> uiClass = Class.forName(info.uiClass);
				analysisUIMap.put(pluginEntry, (AnalysisUI) uiClass.newInstance());
			}
			analysisMap.put((Class<? extends DSDataSet<?>>) Class.forName(entry.getInputType()), entryList);
		}
		
		List<DataTypeEntry> visualizerlist = overall.get(1);
		for(DataTypeEntry entry : visualizerlist) {
			PluginInfo info = entry.getPluginList().get(0);
			Class<?> inputType = Class.forName(entry.inputType);
			Class<?> uiType = Class.forName(info.uiClass);
			resultUiMap.put(inputType, (Class<? extends Visualizer>) uiType);
			visualizerPluginEntry.put((Class<? extends Visualizer>) uiType, new PluginEntry(info.name, info.description));
		}
	}

	// query on null returns all analysis plug-ins
	public List<PluginEntry> getAnalysisList(Class<?> dataType) {
		if(dataType!=null)
			return analysisMap.get(dataType);
		else
			return new ArrayList<PluginEntry>(analysisUIMap.keySet());
	}
	
	public AnalysisUI getUI(PluginEntry a) {
		return analysisUIMap.get(a);
	}

	public ThemeResource getIcon(Class<?> clazz) {
		for(Class<?> c : iconMap.keySet()) {
			if(c.isAssignableFrom(clazz)) {
				return iconMap.get(c);
			}
		}
		return null; // TODO should be a default icon instead of null
	}

	public ThemeResource getResultIcon(Class<?> clazz) {
		for(Class<?> c : resultIconMap.keySet()) {
			if(c.isAssignableFrom(clazz)) {
				return resultIconMap.get(c);
			}
		}
		return null; // TODO should be a default icon instead of null
	}
	
	// this is in fact similar to getResultUI, but returns an array instead of one visualizer
	@SuppressWarnings("unchecked")
	public Class<? extends Visualizer>[] getVisualizers(Class<?> type) {
		if(type==null) { // return all list id no type is specified
			return resultUiMap.values().toArray(new Class[0]);
		} else if(getResultUI(type)==null) { // the case of no visualizer
			return new Class[0];
		} else { // result does not have common interface
			return new Class[]{getResultUI(type)};
		}
	}

	// clazz is a data type we support
	public Class<? extends DataTypeMenuPage> getDataUI(Class<?> clazz) {
		for(Class<?> c : uiMap.keySet()) {
			if(c.isAssignableFrom(clazz)) {
				return uiMap.get(c);
			}
		}
		return null; // TODO return the complete list (like by 'tools' menu) may be the best option
	}

	// clazz is a result data type
	public Class<? extends Component> getResultUI(Class<?> clazz) {
		// TODO this is not ideal - that both perfect match and super class are allowed
		// get the perfect match first
		Class<? extends Component> uiClass = resultUiMap.get(clazz);
		if(uiClass!=null) {
			return uiClass;
		}
		
		for(Class<?> c : resultUiMap.keySet()) {
			if(c.isAssignableFrom(clazz)) {
				return resultUiMap.get(c);
			}
		}
		return null; // TODO return the complete list (like by 'tools' menu) may be the best option
	}
	
	/* this is class is for the convenience of parsing configuration file */
	public static class PluginInfo {
	    
		private String name, description, uiClass;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getUiClass() {
			return uiClass;
		}

		public void setUiClass(String uiClass) {
			this.uiClass = uiClass;
		}
		
	}
	
	/* this is class is for the convenience of parsing configuration file */
	public static class DataTypeEntry {
		private String inputType;
		private List<PluginInfo> pluginList;
		
		public DataTypeEntry() {
			inputType = null;
			pluginList = new ArrayList<PluginInfo>();
		}
		
		public String getInputType() {
			return inputType;
		}
		
		public void setInputType(String inputType) {
			this.inputType = inputType;
		}
		
		public List<PluginInfo> getPluginList() {
			return pluginList;
		}
		
		public void add(PluginInfo entry) {
			pluginList.add(entry);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(inputType+":");
			for(PluginInfo entry : pluginList) {
				sb.append(entry.getName()).append(",");
			}
			return sb.toString();
		}
	}

	public PluginEntry getVisualizerPluginEntry(Class<? extends Visualizer> visualizerClass) {
		return visualizerPluginEntry.get(visualizerClass);
	}
}
