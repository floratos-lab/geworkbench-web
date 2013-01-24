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
import org.geworkbenchweb.plugins.anova.AnovaUI;
import org.geworkbenchweb.plugins.aracne.AracneUI;
import org.geworkbenchweb.plugins.cnkb.CNKBUI;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringUI;
import org.geworkbenchweb.plugins.marina.MarinaUI;
import org.geworkbenchweb.plugins.markus.MarkUsUI;
import org.geworkbenchweb.plugins.microarray.MicroarrayUI;
import org.geworkbenchweb.plugins.proteinstructure.ProteinStructureUI;
import org.geworkbenchweb.plugins.ttest.TTestUI;

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
	
	private Map<Analysis, AnalysisUI> analysisUIMap = new HashMap<Analysis, AnalysisUI>();
	private Map<Class<? extends DSDataSet<?>>, ThemeResource> iconMap = new HashMap<Class<? extends DSDataSet<?>>, ThemeResource>();
	private Map<Class<? extends DSDataSet<?>>, Class<? extends DataTypeUI>> uiMap = new HashMap<Class<? extends DSDataSet<?>>, Class<? extends DataTypeUI>>(); 
	private Map<Class<? extends DSDataSet<?>>, List<Analysis>> analysisMap = new HashMap<Class<? extends DSDataSet<?>>, List<Analysis>>();
	
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

	// TODO compare whether registration of class is a better idea than doing it for instance; 
	// TODO use configuration file (say, plugins.xml) to control the registration, so this class does not have to know each analysis plug-in
	/** Add all the initial registry entries.*/
	public void init() {
		iconMap.put(DSMicroarraySet.class, microarrayIcon);
		iconMap.put(DSProteinStructure.class, proteinIcon);

		uiMap.put(DSMicroarraySet.class, MicroarrayUI.class);
		uiMap.put(DSProteinStructure.class, ProteinStructureUI.class);
		
		Analysis anova = new Analysis("ANOVA", "The geWorkbench ANOVA component implements a one-way analysis of variance calculation " +
				"derived from TIGR's MeV (MultiExperiment Viewer) (Saeed, 2003). At least three groups of " +
				"arrays must be specified by defining and activating them in the Arrays/Phenotypes component.");
		Analysis aracne = new Analysis("ARACNe", "ARACNe (Algorithm for the Reconstruction of Accurate Cellular Networks) " +
				"\n(Basso 2005, Margolin 2006a, 2006b) is an information-theoretic algorithm used " +
				"\nto identify transcriptional interactions between gene products using microarray " +
				"\ngene expression profile data.\n\n");
		Analysis cnkb = new Analysis("Cellular Network Knowledge Base", "The Cellular Network Knowledge Base (CNKB) is a repository of molecular interactions, " +
				"including ones both computationally and experimentally derived. Sources for interactions " +
				"include both publicly available databases such as BioGRID and HPRD, as well as reverse-engineered " +
				"cellular regulatory interactomes developed in the lab of Dr. Andrea Califano at Columbia University.");
		Analysis hierarchicalClustering = new Analysis("Hierarchical Clustering", "Hierarchical clustering is a method to group arrays and/or markers together based on similarity on their expression profiles." +
				" geWorkbench implements its own code for agglomerative hierarchical clustering. Starting from individual points " +
				"(the leaves of the tree), nearest neighbors are found for individual points, and then for groups of points, " +
				"at each step building up a branched structure that converges toward a root that contains all points. " +
				"The resulting graph tends to group similar items together. " +
				"Results of hierarchical clustering are displayed in the Dendrogram component.");
		Analysis marina = new Analysis("MARINa", "MARINa Analysis");
		Analysis ttest = new Analysis("Differential Expression (T-Test)", "A t-Test analysis can be used to identify markers with statistically " +
				"significant differential expression between two sets of microarrays.");
		List<Analysis> microarrayAnalysis = new ArrayList<Analysis>();
		microarrayAnalysis.add(anova);
		microarrayAnalysis.add(aracne);
		microarrayAnalysis.add(cnkb);
		microarrayAnalysis.add(hierarchicalClustering);
		microarrayAnalysis.add(marina);
		microarrayAnalysis.add(ttest);
		
		Analysis markus = new Analysis("MarkUs", "MarkUs is a web server to assist the assessment of the biochemical function " +
				"for a given protein structure. MarkUs identifies related protein structures " +
				"and sequences, detects protein cavities, and calculates the surface electrostatic " +
				"potentials and amino acid conservation profile.");
		List<Analysis> proteinStrcutureAnalysis = new ArrayList<Analysis>();
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
	public List<Analysis> getAnalysisList(Class<? extends DSDataSet<?>> dataType) {
		if(dataType!=null)
			return analysisMap.get(dataType);
		else
			return new ArrayList<Analysis>(analysisUIMap.keySet());
	}
	
	public AnalysisUI getUI(Analysis a) {
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
