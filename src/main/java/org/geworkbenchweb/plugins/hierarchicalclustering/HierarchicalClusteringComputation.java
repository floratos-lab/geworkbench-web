package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.geworkbench.components.hierarchicalclustering.computation.DimensionType;
import org.geworkbench.components.hierarchicalclustering.computation.DistanceType;
import org.geworkbench.components.hierarchicalclustering.computation.HNode;
import org.geworkbench.components.hierarchicalclustering.computation.Linkage;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterInput;
import org.geworkbench.components.hierarchicalclustering.data.HierClusterOutput;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.HierarchicalClusteringResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.OverLimitException;
import org.geworkbenchweb.utils.SubSetOperations;

public class HierarchicalClusteringComputation {
	
	private static final String DEFAULT_WEB_SERVICES_URL = "http://afdev.c2b2.columbia.edu:9090/axis2/services/HierClusterService";
	private static final String  HCS_WEBSERVICE_URL = "hierClusterService.webService.url";
	 
	private static String url = null;

	private final double[][] matrix;
 
	private final int metric;
	private final int method;
	private final int dimension;

	private static Log log = LogFactory
			.getLog(HierarchicalClusteringComputation.class);

	private final int[] selectedMarkers;
	private final int[] selectedArrays;
	
	private static final int MARKER_NUMBER_LIMIT;
	static {
		String limit = GeworkbenchRoot.getAppProperty("hierarchical.clustering.limit");
		MARKER_NUMBER_LIMIT = Integer.parseInt(limit);
	}
	
	public HierarchicalClusteringComputation(Long datasetId,
			HashMap<Serializable, Serializable> params, Long userId) throws Exception {

		String[] markerSet = (String[])params.get(HierarchicalClusteringParams.MARKER_SET);
		String[] micraoarraySet = (String[])params.get(HierarchicalClusteringParams.MICROARRAY_SET);

		MicroarraySet microarrays = DataSetOperations.getMicroarraySet(datasetId);
		List<String> markerLabels = Arrays.asList( microarrays.markerLabels );
		List<String> arrayLabels = Arrays.asList( microarrays.arrayLabels );
		float[][] values = microarrays.values;
		
		if (markerSet != null) { // TODO verify null versus empty
			List<Integer> selected = new ArrayList<Integer>();
			for (String markerSetId : markerSet) {
				/* what is returned at this point is database id as long */
				List<String> positions = SubSetOperations.getMarkerData(Long.parseLong(markerSetId.trim()));
				for(String markerName : positions) {
					selected.add(markerLabels.indexOf(markerName));
				}
			}
			selectedMarkers = new int[selected.size()];
			for(int i=0; i<selected.size(); i++) {
				selectedMarkers[i] = selected.get(i);
			}
		} else {
			selectedMarkers = null;
		}

		if (micraoarraySet != null) { // TODO verify null versus empty
			List<Integer> selected = new ArrayList<Integer>();
			for (String microarraySetId : micraoarraySet) {
				List<String> positions = SubSetOperations.getArrayData(Long.parseLong(microarraySetId.trim()));
				for(String microarrayName : positions) {
					selected.add(arrayLabels.indexOf(microarrayName));
				}
			}
			selectedArrays = new int[selected.size()];
			for(int i=0; i<selected.size(); i++) {
				selectedArrays[i] = selected.get(i);
			}
		} else {
			selectedArrays = null;
		}

		this.metric = (Integer) params.get(HierarchicalClusteringParams.CLUSTER_METRIC);
		this.method = (Integer) params.get(HierarchicalClusteringParams.CLUSTER_METHOD);
		this.dimension = (Integer) params.get(HierarchicalClusteringParams.CLUSTER_DIMENSION);
		
		boolean checkMarkerNumber = (dimension==0)||(dimension==2);
		matrix = geValues(values, selectedMarkers, selectedArrays, checkMarkerNumber);
	}

	HierarchicalClusteringResult execute() throws RemoteException {		  
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

		HNode markerCluster = null;
		HNode arrayCluster = null;
		
		if (dimension == 2) {
			HierClusterInput hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, DimensionType.MARKER.name()) ;
			HierClusterOutput hierClusterOutput = computeHierarchicalClusteringRemote(hierClusterInput);
	        markerCluster = hierClusterOutput.getHnodeObject();
					 
		    hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, DimensionType.ARRAY.name()) ;
			hierClusterOutput = computeHierarchicalClusteringRemote(hierClusterInput);
	        arrayCluster = hierClusterOutput.getHnodeObject();
		} else {
			HierClusterInput hierClusterInput = new HierClusterInput(matrix, linkageType, distanceType, dimensionType) ;
			HierClusterOutput hierClusterOutput = computeHierarchicalClusteringRemote(hierClusterInput);
			HNode hNode = hierClusterOutput.getHnodeObject();	 
			if (dimension == 1) {
				arrayCluster = hNode;		 
			} else if (dimension == 0) {
				markerCluster = hNode;
			}
		}  

		return new HierarchicalClusteringResult(markerCluster, arrayCluster,
				selectedMarkers, selectedArrays);
	}

	/* construct an arrray of only the part that is selected. */
	private static double[][] geValues(float[][] data, int[] selectedMarkers, int[] selectedArrays,
			boolean checkMarkerNumber) throws OverLimitException {
		if(selectedMarkers==null) {
			selectedMarkers = new int[data.length];
			for(int i=0; i<data.length; i++) selectedMarkers[i] = i;
		}
		if(selectedArrays==null) {
			selectedArrays = new int[data[0].length];
			for(int i=0; i<data[0].length; i++) selectedArrays[i] = i;
		}
		int rows = selectedMarkers.length;
		if (checkMarkerNumber && rows > MARKER_NUMBER_LIMIT)
			throw new OverLimitException(
					"Hierarchical Clustering plugin can only handle "
							+ MARKER_NUMBER_LIMIT
							+ " markers or fewer. You are trying to process "
							+ rows + " markers.");
		int cols = selectedArrays.length;
		double[][] array = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			int markerIndex = selectedMarkers[i];
			for (int j = 0; j < cols; j++) {
				int arrayIndex = selectedArrays[j];
				array[i][j] = data[markerIndex][arrayIndex];
			}
		}
		return array;
	}
	
	 private HierClusterOutput computeHierarchicalClusteringRemote(HierClusterInput input) throws RemoteException {
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
				throw new RemoteException( "HierarchicalClustering AxisFault:" + e.getMessage() + " fault action: " + e.getFaultAction()
						+ " reason: " + e.getReason());		
				
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException( "Compute HierarchicalClustering error:" + e.getMessage());
			}
			 
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
}
