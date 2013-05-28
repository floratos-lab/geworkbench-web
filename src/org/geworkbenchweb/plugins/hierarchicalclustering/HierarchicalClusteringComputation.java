package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.components.hierarchicalclustering.ClusteringAlgorithm;
import org.geworkbench.components.hierarchicalclustering.HierClusterFactory;
import org.geworkbench.components.hierarchicalclustering.HierarchicalClustering;
import org.geworkbench.util.CorrelationDistance;
import org.geworkbench.util.Distance;
import org.geworkbench.util.EuclideanDistance;
import org.geworkbench.util.SpearmanRankDistance;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;

public class HierarchicalClusteringComputation {

	private final double[][] matrix;
//	private final List<DSGeneMarker> markers = null;
//	private final List<DSMicroarray> arrays = null;
	// TODO using standard collection is preferred to DSItemList
	private final DSItemList<DSGeneMarker> markers;
	private final DSItemList<DSMicroarray> arrays;
	private final int metric;
	private final int method;
	private final int dimension;

	private static Log log = LogFactory
			.getLog(HierarchicalClusteringComputation.class);

	private transient DSMicroarraySetView<DSGeneMarker, DSMicroarray> datasetView; // FIXME this is temporary. don't make it member variable. this can be avoided when the return type of execute is changed
	public HierarchicalClusteringComputation(Long datasetId,
			HashMap<Serializable, Serializable> params, Long userId) throws Exception {
		DSMicroarraySet dataSet = (DSMicroarraySet) UserDirUtils.deserializeDataSet(datasetId, DSMicroarraySet.class, userId);

//		DSMicroarraySetView<DSGeneMarker, DSMicroarray> datasetView = 
		datasetView = 
				new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
		
		String[] markerSet = (String[])params.get(HierarchicalClusteringParams.MARKER_SET);
		String[] micraoarraySet = (String[])params.get(HierarchicalClusteringParams.MICROARRAY_SET);

		if (markerSet != null) { // TODO verify null versus empty
			DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>();
			for (String markerSetId : markerSet) {
				// TODO note what is returned at this point is database id as long. nasty
				List<?> subSet = SubSetOperations.getMarkerSet(Long.parseLong(markerSetId.trim()));
				ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions()); // only the first one is used
				for(String position : positions) {
					String markerName = position; // only the first field 
					DSGeneMarker marker = dataSet.getMarkers().get(markerName);
					panel.add(marker);
				}
			} 
			datasetView.setMarkerPanel(panel);
		}
		if (micraoarraySet != null) { // TODO verify null versus empty
			DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>();
			for (String microarraySetId : micraoarraySet) {
				// TODO note what is returned at this point is database id as long. nasty
				List<?> subSet = SubSetOperations.getArraySet(Long.parseLong(microarraySetId.trim()));
				ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions()); // only the first one is used
				for(String position : positions) {
					String microarrayName = position; // only the first field 
					DSMicroarray micraorray = dataSet.get(microarrayName);
					panel.add(micraorray);
				}
			} 
			datasetView.setItemPanel(panel);
		}

		this.metric = (Integer) params.get(HierarchicalClusteringParams.CLUSTER_METRIC);
		this.method = (Integer) params.get(HierarchicalClusteringParams.CLUSTER_METHOD);
		this.dimension = (Integer) params.get(HierarchicalClusteringParams.CLUSTER_DIMENSION);
		
		matrix = geValues(datasetView);
		arrays = datasetView.items();
		markers = datasetView.markers();
	}

	CSHierClusterDataSet execute() {
		final Distance[] distances = { EuclideanDistance.instance,
				CorrelationDistance.instance, SpearmanRankDistance.instance };
		Distance distanceMetric = distances[metric];
		ClusteringAlgorithm.Linkage linkageType = null;
		switch (method) {
		case 0:
			linkageType = ClusteringAlgorithm.Linkage.SINGLE;
			break;
		case 1:
			linkageType = ClusteringAlgorithm.Linkage.AVERAGE;
			break;
		case 2:
			linkageType = ClusteringAlgorithm.Linkage.COMPLETE;
			break;
		default:
			log.error("error in linkage type");
		}
		final HierarchicalClustering hierarchicalClustering = new HierarchicalClustering(
				linkageType);

		// one for marker; one for array
		HierCluster[] resultClusters = new HierCluster[2];

		if (dimension == 2) {

			HierClusterFactory cluster = new HierClusterFactory.Gene(markers);
			resultClusters[0] = hierarchicalClustering.compute(matrix, cluster,
					distanceMetric);

			cluster = new HierClusterFactory.Microarray(arrays);
			resultClusters[1] = hierarchicalClustering.compute(
					getTranspose(matrix), cluster, distanceMetric);
		} else if (dimension == 1) {
			HierClusterFactory cluster = new HierClusterFactory.Microarray(
					arrays);
			resultClusters[1] = hierarchicalClustering.compute(
					getTranspose(matrix), cluster, distanceMetric);
		} else if (dimension == 0) {
			HierClusterFactory cluster = new HierClusterFactory.Gene(
					markers);
			resultClusters[0] = hierarchicalClustering.compute(matrix, cluster,
					distanceMetric);
		}

		return new CSHierClusterDataSet(resultClusters, null, false,
				"Hierarchical Clustering", datasetView);
	}

	// TODO these duplicate methods should be refactored in geWorkbench so it does
	// not have to be here any more
	private static double[][] geValues(
			final DSMicroarraySetView<DSGeneMarker, DSMicroarray> data) {
		int rows = data.markers().size();
		int cols = data.items().size();
		double[][] array = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			array[i] = data.getRow(i);
		}
		return array;
	}

	private static double[][] getTranspose(final double[][] input) {
		double d[][] = new double[input[0].length][input.length];
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				d[i][j] = input[j][i];
			}
		}
		return d;
	}
}
