/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbenchweb.plugins.anova.AnovaUI;
import org.geworkbenchweb.plugins.anova.results.AnovaResultsUI;
import org.geworkbenchweb.plugins.aracne.AracneUI;
import org.geworkbenchweb.plugins.aracne.results.AracneResultsUI;
import org.geworkbenchweb.plugins.cnkb.CNKBResultSet;
import org.geworkbenchweb.plugins.cnkb.CNKBUI;
import org.geworkbenchweb.plugins.cnkb.results.CNKBResultsUI;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringResultsUI;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringUI;
import org.geworkbenchweb.plugins.marina.MarinaUI;
import org.geworkbenchweb.plugins.marina.results.MarinaResultsUI;
import org.geworkbenchweb.plugins.markus.MarkUsUI;
import org.geworkbenchweb.plugins.markus.results.MarkusResultsUI;
import org.geworkbenchweb.plugins.microarray.MicroarrayUI;
import org.geworkbenchweb.plugins.proteinstructure.ProteinStructureUI;
import org.geworkbenchweb.plugins.tabularview.TabularViewUI;
import org.geworkbenchweb.plugins.ttest.TTestUI;
import org.geworkbenchweb.plugins.ttest.results.TTestResultsUI;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;

/**
 * Central control of all Plug-ins. 
 * 
 * TODO this will be enhanced, but a simplistic version is created for now. 
 * 
 * @author zji
 *
 */
public class PluginRegistry {
	
	private Map<PluginEntry, AnalysisUI> analysisUIMap = new HashMap<PluginEntry, AnalysisUI>();
	private Map<Class<? extends DSDataSet<?>>, ThemeResource> iconMap = new HashMap<Class<? extends DSDataSet<?>>, ThemeResource>();
	private Map<Class<? extends DSDataSet<?>>, Class<? extends DataTypeUI>> uiMap = new HashMap<Class<? extends DSDataSet<?>>, Class<? extends DataTypeUI>>(); 
	private Map<Class<? extends DSDataSet<?>>, List<PluginEntry>> analysisMap = new HashMap<Class<? extends DSDataSet<?>>, List<PluginEntry>>();
	
	// TODO for now, let maintain a separate list for result type. this may not necessary eventually
	private Map<Class<?>, ThemeResource> resultIconMap = new HashMap<Class<?>, ThemeResource>();
	private Map<Class<?>, Class<? extends Visualizer>> resultUiMap = new HashMap<Class<?>, Class<? extends Visualizer>>(); 
	
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

	// TODO compare whether registration of class is a better idea than doing it for instance; 
	// TODO use configuration file (say, plugins.xml) to control the registration, so this class does not have to know each analysis plug-in
	/** Add all the initial registry entries.*/
	public void init() {
		resultIconMap.put(CSHierClusterDataSet.class, hcIcon); // hierarchical clustering result
		resultIconMap.put(CNKBResultSet.class, networkIcon); // cnkb result // FIXME this result type is too generic
		resultIconMap.put(AdjacencyMatrixDataSet.class, networkIcon); // aracne result or 'cytoscape' result
		resultIconMap.put(MarkUsResultDataSet.class, markusIcon); // markus result
		resultIconMap.put(CSAnovaResultSet.class, anovaIcon); // anova result
		resultIconMap.put(DSSignificanceResultSet.class, anovaIcon); // t-test result
		resultIconMap.put(CSMasterRegulatorTableResultSet.class, marinaIcon); // marina result

		iconMap.put(DSMicroarraySet.class, microarrayIcon);
		iconMap.put(DSProteinStructure.class, proteinIcon);

		resultUiMap.put(CSHierClusterDataSet.class, HierarchicalClusteringResultsUI.class);
		resultUiMap.put(CNKBResultSet.class, CNKBResultsUI.class);
		resultUiMap.put(AdjacencyMatrixDataSet.class, AracneResultsUI.class);
		resultUiMap.put(MarkUsResultDataSet.class, MarkusResultsUI.class);
		resultUiMap.put(CSAnovaResultSet.class, AnovaResultsUI.class);
		resultUiMap.put(DSSignificanceResultSet.class, TTestResultsUI.class);
		resultUiMap.put(CSMasterRegulatorTableResultSet.class, MarinaResultsUI.class);
		resultUiMap.put(DSMicroarraySet.class, TabularViewUI.class);
		
		uiMap.put(DSMicroarraySet.class, MicroarrayUI.class);
		uiMap.put(DSProteinStructure.class, ProteinStructureUI.class);
		
		PluginEntry anova = new PluginEntry("ANOVA", "The geWorkbench ANOVA component implements a one-way analysis of variance calculation " +
				"derived from TIGR's MeV (MultiExperiment Viewer) (Saeed, 2003). At least three groups of " +
				"arrays must be specified by defining and activating them in the Arrays/Phenotypes component.");
		PluginEntry aracne = new PluginEntry("ARACNe", "ARACNe (Algorithm for the Reconstruction of Accurate Cellular Networks) " +
				"\n(Basso 2005, Margolin 2006a, 2006b) is an information-theoretic algorithm used " +
				"\nto identify transcriptional interactions between gene products using microarray " +
				"\ngene expression profile data.\n\n");
		PluginEntry cnkb = new PluginEntry("Cellular Network Knowledge Base", "The Cellular Network Knowledge Base (CNKB) is a repository of molecular interactions, " +
				"including ones both computationally and experimentally derived. Sources for interactions " +
				"include both publicly available databases such as BioGRID and HPRD, as well as reverse-engineered " +
				"cellular regulatory interactomes developed in the lab of Dr. Andrea Califano at Columbia University.");
		PluginEntry hierarchicalClustering = new PluginEntry("Hierarchical Clustering", "Hierarchical clustering is a method to group arrays and/or markers together based on similarity on their expression profiles." +
				" geWorkbench implements its own code for agglomerative hierarchical clustering. Starting from individual points " +
				"(the leaves of the tree), nearest neighbors are found for individual points, and then for groups of points, " +
				"at each step building up a branched structure that converges toward a root that contains all points. " +
				"The resulting graph tends to group similar items together. " +
				"Results of hierarchical clustering are displayed in the Dendrogram component.");
		PluginEntry marina = new PluginEntry("MARINa", "MARINa Analysis");
		PluginEntry ttest = new PluginEntry("Differential Expression (T-Test)", "A t-Test analysis can be used to identify markers with statistically " +
				"significant differential expression between two sets of microarrays.");
		List<PluginEntry> microarrayAnalysis = new ArrayList<PluginEntry>();
		microarrayAnalysis.add(anova);
		microarrayAnalysis.add(aracne);
		microarrayAnalysis.add(cnkb);
		microarrayAnalysis.add(hierarchicalClustering);
		microarrayAnalysis.add(marina);
		microarrayAnalysis.add(ttest);
		
		PluginEntry markus = new PluginEntry("MarkUs", "MarkUs is a web server to assist the assessment of the biochemical function " +
				"for a given protein structure. MarkUs identifies related protein structures " +
				"and sequences, detects protein cavities, and calculates the surface electrostatic " +
				"potentials and amino acid conservation profile.");
		List<PluginEntry> proteinStrcutureAnalysis = new ArrayList<PluginEntry>();
		proteinStrcutureAnalysis.add(markus);
		
		analysisMap.put(DSMicroarraySet.class, microarrayAnalysis );
		analysisMap.put(DSProteinStructure.class, proteinStrcutureAnalysis);

		analysisUIMap.put(anova, new AnovaUI(0L));
		analysisUIMap.put(aracne, new AracneUI(0L));
		analysisUIMap.put(cnkb, new CNKBUI(0L));
		analysisUIMap.put(hierarchicalClustering, new HierarchicalClusteringUI(0L));
		analysisUIMap.put(marina, new MarinaUI(0L));
		analysisUIMap.put(ttest, new TTestUI(0L)); 
		
		analysisUIMap.put(markus, new MarkUsUI(0L));
	}

	// query on null returns all analysis plug-ins
	public List<PluginEntry> getAnalysisList(Class<? extends DSDataSet<?>> dataType) {
		if(dataType!=null)
			return analysisMap.get(dataType);
		else
			return new ArrayList<PluginEntry>(analysisUIMap.keySet());
	}
	
	public AnalysisUI getUI(PluginEntry a) {
		return analysisUIMap.get(a);
	}

	public ThemeResource getIcon(Class<?> clazz) {
		for(Class<? extends DSDataSet<?>> c : iconMap.keySet()) {
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
	public Class<? extends DataTypeUI> getDataUI(Class<?> clazz) {
		for(Class<? extends DSDataSet<?>> c : uiMap.keySet()) {
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
}
