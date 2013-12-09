package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel; 
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;  
import org.geworkbench.components.hierarchicalclustering.HierClusterFactory;
 
import org.geworkbench.components.hierarchicalclustering.computation.Linkage;
import org.geworkbench.components.hierarchicalclustering.computation.DistanceType;
import org.geworkbench.components.hierarchicalclustering.computation.DimensionType;
import org.geworkbench.components.hierarchicalclustering.computation.HNode;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterInput;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterOutput;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;

public class HierarchicalClusteringComputation {
	
	private static final String DEFAULT_WEB_SERVICES_URL = "http://afdev.c2b2.columbia.edu:9090/axis2/services/HierClusterService";
	//private static final String DEFAULT_WEB_SERVICES_URL = "http://localhost:8080/axis2/services/HierClusterService";
	private static final String  HCS_WEBSERVICE_URL = "hierClusterService.webService.url";
	 
	private static String url = null;

	private final double[][] matrix;
 
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
	 
	}
	

	CSHierClusterDataSet execute() {		  
		String distanceType = null;
		String linkageType = null;
		String dimensionType = null;
		
		switch(method) {
		case 0: linkageType = Linkage.SINGLE.name(); break;
		case 1: linkageType = Linkage.AVERAGE.name(); break;
		case 2: linkageType = Linkage.COMPLETE.name(); break;
		default: log.error("error in linkage type");
		}
		
		switch(metric) {
		case 0: distanceType = DistanceType.EUCLIDEAN.name(); break;
		case 1: distanceType = DistanceType.CORRELATION.name(); break;
		case 2: distanceType = DistanceType.SPEARMANRANK.name(); break;
		default: log.error("error in distance type");
		}
		 
		switch(dimension) {
		case 0: dimensionType = DimensionType.MARKER.name(); break;
		case 1: dimensionType = DimensionType.ARRAY.name(); break;
		case 2: dimensionType = DimensionType.BOTH.name(); break;
		default: log.error("error in dimension type");
		}
		
	
		// one for marker; one for array
		HierCluster[] resultClusters = new HierCluster[2];

		if (dimension == 2) {
			
			HierClusterFactory cluster = new HierClusterFactory.Gene(datasetView.markers());
			HierClusterInput hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, DimensionType.MARKER.name()) ;
			HierClusterOutput hierClusterOutput = computeHierarchicalClusteringRemote(hierClusterInput);
	        HNode hNode = hierClusterOutput.getHnodeObject();			
			resultClusters[0] = convertCluster(cluster, hNode);
					 
			cluster = new HierClusterFactory.Microarray(datasetView.items());
		    hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, DimensionType.ARRAY.name()) ;
			hierClusterOutput = computeHierarchicalClusteringRemote(hierClusterInput);
	        hNode = hierClusterOutput.getHnodeObject();		 
			resultClusters[1] = convertCluster(cluster,hNode);
					 
		} else  
		{		
			HierClusterInput hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, dimensionType) ;
			HierClusterOutput hierClusterOutput = computeHierarchicalClusteringRemote(hierClusterInput);			 	       
			HNode hNode = hierClusterOutput.getHnodeObject();	 
			if (dimension == 1) {
				HierClusterFactory cluster = new HierClusterFactory.Microarray(datasetView.items());			 
				resultClusters[1] = convertCluster(cluster,hNode);
						 
			} else if (dimension == 0) {
				HierClusterFactory cluster = new HierClusterFactory.Gene(datasetView.markers());
				resultClusters[0] = convertCluster(cluster,hNode);
				
				
				
				
			}		 
					 
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

	 
	
	 private HierCluster convertCluster(HierClusterFactory factory, HNode node) {
	        if (node.isLeafNode()) {
	            return factory.newLeaf(Integer.parseInt(node.getLeafItem()));
	        } else {
	        	
	        	HierCluster left = convertCluster(factory, node.getLeft());
	            HierCluster right = convertCluster(factory, node.getRight());
	            HierCluster cluster = factory.newCluster();
	            cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
	            cluster.setHeight(node.getHeight());
	            cluster.addNode(left, 0);
	            cluster.addNode(right, 0);
	            return cluster;
	        }
	  } 
	 
	
	 private HierClusterOutput computeHierarchicalClusteringRemote(HierClusterInput input) {
		 HierClusterOutput output = null;
			RPCServiceClient serviceClient;

			try {
				
				getWebServiceUrl();
				
				serviceClient = new RPCServiceClient();

				Options options = serviceClient.getOptions();

				long soTimeout =  24 * 60 * 60 * 1000; // 24 hours
				options.setTimeOutInMilliSeconds(soTimeout);


				EndpointReference targetEPR = new EndpointReference(url);
						 
				options.setTo(targetEPR);

				// notice that that namespace is in the required form
				QName opName = new QName(
						"http://service.hierarchicalclustering.components.geworkbench.org",
						"execute");
				Object[] args = new Object[] { input };

				Class<?>[] returnType = new Class[] { HierClusterOutput.class };

				Object[] response = serviceClient.invokeBlocking(opName, args,
						returnType);
				output = (HierClusterOutput) response[0];

				return output;
			} catch (AxisFault e) {
				OMElement x = e.getDetail();
				if (x != null)
					log.debug(x);

				Throwable y = e.getCause();
				while (y != null) {
					y.printStackTrace();
					y = y.getCause();
				}

				log.debug("message: " + e.getMessage());
				log.debug("fault action: " + e.getFaultAction());
				log.debug("reason: " + e.getReason());
				e.printStackTrace();
			}

			return output;
		}
	 
		
	    private void getWebServiceUrl()
		{
			if (url == null || url.trim().equals(""))
			{
				 		
					url  = GeworkbenchRoot.getAppProperty(HCS_WEBSERVICE_URL);
					if (url == null || url.trim().equals(""))
						url = DEFAULT_WEB_SERVICES_URL;
					
			}		
			 
		}
	    
	    //for testing purpose
	   /* private HierClusterOutput computeHierarchicalClusteringLocal(HierClusterInput input) {
			 HierClusterOutput output = null;
			 HierClusterService hcs = new HierClusterService();
			 output = hcs.execute(input);
			 
			 return output;
		}*/
		 
	    
	    
	
}
