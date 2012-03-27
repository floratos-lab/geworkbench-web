package org.geworkbenchweb.analysis.hierarchicalclustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.components.hierarchicalclustering.ClusteringAlgorithm;
import org.geworkbench.components.hierarchicalclustering.HierClusterFactory;
import org.geworkbench.components.hierarchicalclustering.HierarchicalClustering;
import org.geworkbench.util.CorrelationDistance;
import org.geworkbench.util.Distance;
import org.geworkbench.util.EuclideanDistance;
import org.geworkbench.util.SpearmanRankDistance;

public class HierarchicalClusteringWrapper {

	private final DSMicroarraySetView<DSGeneMarker, DSMicroarray> data;
	private final int metric;
	private final int method;
	private final int dimension;

	private static Log log = LogFactory
			.getLog(HierarchicalClusteringWrapper.class);

	public HierarchicalClusteringWrapper(
			final DSMicroarraySetView<DSGeneMarker, DSMicroarray> data,
			final int metric, final int method, int dimension) {
		this.data = data;
		this.metric = metric;
		this.method = method;
		this.dimension = dimension;
	}

	public HierCluster[] execute() {
		double[][] matrix = geValues(data);
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

			HierClusterFactory cluster = new HierClusterFactory.Gene(
					data.markers());
			resultClusters[0] = hierarchicalClustering.compute(matrix, cluster,
					distanceMetric);

			cluster = new HierClusterFactory.Microarray(data.items());
			resultClusters[1] = hierarchicalClustering.compute(
					getTranspose(matrix), cluster, distanceMetric);
		} else if (dimension == 1) {
			HierClusterFactory cluster = new HierClusterFactory.Microarray(
					data.items());
			resultClusters[1] = hierarchicalClustering.compute(
					getTranspose(matrix), cluster, distanceMetric);
		} else if (dimension == 0) {
			HierClusterFactory cluster = new HierClusterFactory.Gene(
					data.markers());
			resultClusters[0] = hierarchicalClustering.compute(matrix, cluster,
					distanceMetric);
		}

		return resultClusters;
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
