package org.geworkbenchweb.analysis;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.components.hierarchicalclustering.FastHierClustAnalysis;

public class HierarchicalClustering {
	
	
	public HierClusterTestResult doHierClusterAnalysis(DSMicroarraySet dataSet) {
		

		FastHierClustAnalysis analysis = new FastHierClustAnalysis();

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);

		AlgorithmExecutionResults analysisResult = analysis.execute(dataSetView);

		Object r = analysisResult.getResults();
		if(!(r instanceof CSHierClusterDataSet)) {
			System.out.println("ERROR: hierarchical clustering result is of type "+r.getClass().getName());
			return null;
		}
		
		CSHierClusterDataSet hierClusterDataSet = (CSHierClusterDataSet)r;
		int size = hierClusterDataSet.getNumberOfClusters();
		// size should always be 2
		ClusterNode clusterNode = null; // TODO try one cluster first
		for(int index=0; index<size; index++) {
			HierCluster cluster = hierClusterDataSet.getCluster(index);
			if(cluster==null) {
				System.out.println("cluster #"+index+": is null");
			} else {
				System.out.println("cluster #"+index+": "+cluster.toString()+":"+cluster.getClass().getName());
				System.out.println("LEAF CHILDREN");
				for(Cluster c: cluster.getLeafChildren()) {
					if(c instanceof MarkerHierCluster) {
						MarkerHierCluster m = (MarkerHierCluster)c;
						System.out.println(m.getMarkerInfo()+";");
					}
				}
				System.out.println();
				clusterNode = convert(cluster);
			}
		}
		
		DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();;
        int numMarkers = maSet.getMarkers().size();
        int numArray = dataSetView.items().size();
        System.out.println("numMarkers="+numMarkers+"; number of arrays="+numArray);
        
        double[][] v = new double[numMarkers][numArray];
        for(int i=0; i<numMarkers; i++) {
        	DSGeneMarker marker = maSet.getMarkers().get(i);
        	for(int j=0; j<numArray; j++) {
        		v[i][j] = maSet.getValue(marker, j);
        	}
        }

        DSRangeMarker marker = (DSRangeMarker) maSet.getMarkers().get(0);
        double maxV = marker.getRange().max;
		double minV = marker.getRange().min;
		double sigma = marker.getRange().norm.getSigma();
		HierClusterTestResult results = new HierClusterTestResult(clusterNode, v, minV, maxV, sigma);
		return results;	
	}
	
	private static ClusterNode convert(Cluster hierCluster) {
		if(hierCluster==null) return null;

		if(! (hierCluster instanceof MarkerHierCluster) ){
			// TODO
			return new ClusterNode("not implemented for array cluster yet");
		}
		
		ClusterNode cluster = null;
		if(hierCluster.isLeaf()) {
			MarkerHierCluster m = (MarkerHierCluster)hierCluster;
			cluster = new ClusterNode(m.getMarkerInfo().toString());
		} else {
			Cluster[] child = hierCluster.getChildrenNodes();
			ClusterNode c1 = convert(child[0]);
			ClusterNode c2 = convert(child[1]);
			cluster = new ClusterNode(c1, c2);
		}
		return cluster;
	}
}
