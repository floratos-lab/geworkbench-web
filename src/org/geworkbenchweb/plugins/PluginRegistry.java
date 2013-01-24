/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbenchweb.plugins.microarray.MicroarrayUI;
import org.geworkbenchweb.plugins.proteinstructure.ProteinStructureUI;

import com.vaadin.terminal.ThemeResource;

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
	private Map<Class<? extends DSDataSet<?>>, ThemeResource> iconMap = new HashMap<Class<? extends DSDataSet<?>>, ThemeResource>();
	// FIXME use AbstractComponentContainer for now. we may create a interface, like DataTypeUI for this purpose
	private Map<Class<? extends DSDataSet<?>>, Class<? extends DataTypeUI>> uiMap = new HashMap<Class<? extends DSDataSet<?>>, Class<? extends DataTypeUI>>(); 
	
	static private ThemeResource microarrayIcon 	=	new ThemeResource("../custom/icons/chip16x16.gif");
	static private ThemeResource proteinIcon 		=	new ThemeResource("../custom/icons/dna16x16.gif");
	// the following are for result node
	/*
	static private ThemeResource hcIcon	 		=	new ThemeResource("../custom/icons/dendrogram16x16.gif");
	static private ThemeResource pendingIcon	 	=	new ThemeResource("../custom/icons/pending.gif");
	static private ThemeResource networkIcon	 	=	new ThemeResource("../custom/icons/network16x16.gif");
	static private ThemeResource markusIcon		=	new ThemeResource("../custom/icons/icon_world.gif");
	static private ThemeResource anovaIcon			=	new ThemeResource("../custom/icons/significance16x16.gif");
	static private ThemeResource marinaIcon		=	new ThemeResource("../custom/icons/generic16x16.gif");
	static private ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	static private ThemeResource CancelIcon 		= 	new ThemeResource("../runo/icons/16/cancel.png");
	*/

	public PluginRegistry() {
		// these don't have to be hard-coded like this. it could be done through registration method like the case of Analysis
		iconMap.put(DSMicroarraySet.class, microarrayIcon);
		iconMap.put(DSProteinStructure.class, proteinIcon);

		// FIXME this approach (reuse one instance of ...UI) does not apply well to many VisualPlugin that is the actual visualization, which associates with a particular data set.
		// it is ok to do so for now when we change the mechanism only for the types of microarray set and protein structure, whose UI is really the menu,
		// that are the same for any dataset
		uiMap.put(DSMicroarraySet.class, MicroarrayUI.class);
		uiMap.put(DSProteinStructure.class, ProteinStructureUI.class);
	}

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

	public ThemeResource getIcon(Class<?> clazz) {
		for(Class<? extends DSDataSet<?>> c : iconMap.keySet()) {
			if(c.isAssignableFrom(clazz)) {
				return iconMap.get(c);
			}
		}
		return null; // TODO should be a default icon instead of null
	}

	// clazz is a data type we support
	public Class<? extends DataTypeUI> getDataUI(Class<?> clazz) {
		for(Class<? extends DSDataSet<?>> c : uiMap.keySet()) {
			if(c.isAssignableFrom(clazz)) {
				return uiMap.get(c);
			}
		}
		return null; // TODO return the complete list (like by 'tools' menu) may be the best option
	}
}
