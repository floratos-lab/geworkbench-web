package org.geworkbenchweb.plugins.hierarchicalclustering;

import org.geworkbenchweb.plugins.Analysis;

public class HierarchicalClustering implements Analysis {

	@Override
	public String getName() {
		return "Hierarchical Clustering";
	}

	@Override
	public String getDescription() {
		return "Hierarchical clustering is a method to group arrays and/or markers together based on similarity on their expression profiles." +
				" geWorkbench implements its own code for agglomerative hierarchical clustering. Starting from individual points " +
				"(the leaves of the tree), nearest neighbors are found for individual points, and then for groups of points, " +
				"at each step building up a branched structure that converges toward a root that contains all points. " +
				"The resulting graph tends to group similar items together. " +
				"Results of hierarchical clustering are displayed in the Dendrogram component.";
	}

}
