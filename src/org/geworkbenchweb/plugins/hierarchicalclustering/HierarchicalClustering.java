package org.geworkbenchweb.plugins.hierarchicalclustering;

import org.geworkbenchweb.layout.VisualPlugin;

public class HierarchicalClustering extends VisualPlugin {

	private Long dataSetId;

	public HierarchicalClustering(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Hierarchical Clustering";
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Hierarchical clustering is a method to group arrays and/or markers together based on similarity on their expression profiles." +
				" geWorkbench implements its own code for agglomerative hierarchical clustering. Starting from individual points " +
				"(the leaves of the tree), nearest neighbors are found for individual points, and then for groups of points, " +
				"at each step building up a branched structure that converges toward a root that contains all points. " +
				"The resulting graph tends to group similar items together. " +
				"Results of hierarchical clustering are displayed in the Dendrogram component.";
	}

	@Override
	public boolean checkForVisualizer() {
		// TODO Auto-generated method stub
		return false;
	}

}
