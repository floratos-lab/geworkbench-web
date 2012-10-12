package org.geworkbenchweb.plugins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.cnkb.CNKBInteractions;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringParams;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringWrapper;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;

public class Analysis {

	private ResultSet resultSet; 

	private HashMap<Serializable, Serializable> params;

	private DSMicroarraySet dataSet = null;

	public ResultSet execute(AnalysisSubmissionEvent event) {

		this.resultSet = event.getResultSet();
		this.params = event.getParameters();
		try {
			dataSet = (DSMicroarraySet) event.getDataSet();
		} catch(Exception e) {
			//TODO
		}
		if(resultSet.getType().contains("HierarchicalClusteringResults")) {
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> data = 
					new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
			HierarchicalClusteringWrapper analysis 	= 	
					new HierarchicalClusteringWrapper(data, (Integer) params.get(HierarchicalClusteringParams.CLUSTER_METRIC), 
							(Integer) params.get(HierarchicalClusteringParams.CLUSTER_METHOD), 
							(Integer) params.get(HierarchicalClusteringParams.CLUSTER_DIMENSION));
			HierCluster[] resultClusters = analysis.execute();
			CSHierClusterDataSet results = new CSHierClusterDataSet(resultClusters, null, false,
					"Hierarchical Clustering", data);
			resultSet.setName("Hierarchical Clustering");
			resultSet.setData(ObjectConversion.convertToByte(results));
		} else if(resultSet.getType().contains("CNKBResults")) {
			CNKBInteractions cnkb = new CNKBInteractions();
			Vector<CellularNetWorkElementInformation> hits = cnkb.CNKB(dataSet, params);
			resultSet.setName("CNKB");
			resultSet.setData(ObjectConversion.convertToByte(hits));
		} else if(resultSet.getType().contains("AnovaResults")) {

		} else if(resultSet.getType().contains("AracneResults")) {

		} else {
			//Marina
		}	
		return resultSet;
	}
}
