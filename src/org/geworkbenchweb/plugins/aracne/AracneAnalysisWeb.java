package org.geworkbenchweb.plugins.aracne;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import edu.columbia.c2b2.aracne.Parameter; 

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbench.components.aracne.data.AracneGraphEdge;

/**
 * 
 * This class submits ARACne Analysis from web application
 * 
 * @author Nikhil Reddy
 * 
 */
public class AracneAnalysisWeb {

	private static Log log = LogFactory.getLog(AracneAnalysisWeb.class);
	
	private static final String  DEFAULT_WEB_SERVICES_URL = "http://afdev.c2b2.columbia.edu:9090/axis2/services/AracneService";
	private static final String  ARACNE_WEBSERVICE_URL = "aracne.webService.url";
	
	private static String url = null;
	
	private DSMicroarraySet dataSet = null;

	final Parameter p = new Parameter();

	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	public AracneAnalysisWeb(DSMicroarraySet dataSet,
			HashMap<Serializable, Serializable> params) {
		this.params = params;
		this.dataSet = dataSet;
	}

	public AdjacencyMatrixDataSet execute() {

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
				dataSet);

		List<String> hubGeneList = null;
		if (params.get(AracneParameters.HUB_MARKER_SET) != null
				&& !params.get(AracneParameters.HUB_MARKER_SET).toString()
						.trim().equals("")
				&& !params.get(AracneParameters.HUB_MARKER_SET).toString()
						.trim().equals("All vs. All")) {
			Long subSetId = Long.parseLong((String) params
					.get(AracneParameters.HUB_MARKER_SET));
			hubGeneList = getMarkerData(subSetId);
		}

		final AracneInput aracneInput = new AracneInput();
		if (hubGeneList != null && hubGeneList.size() > 0) {
			aracneInput.setHubGeneList(hubGeneList.toArray(new String[0]));
		}

		aracneInput.setIsThresholdMI(((String) params
				.get(AracneParameters.T_TYPE)).equalsIgnoreCase("Mutual Info"));
		aracneInput.setThreshold(Float.valueOf((String) params
				.get(AracneParameters.T_VALUE)));
		aracneInput.setNoCorrection(((String) params
				.get(AracneParameters.CORRECTION))
				.equalsIgnoreCase("No Correction"));
		aracneInput.setIsKernelWidthSpecified(((String) params
				.get(AracneParameters.KERNEL_WIDTH))
				.equalsIgnoreCase("Specify"));
		aracneInput.setKernelWidth(Float.valueOf((String) params
				.get(AracneParameters.WIDTH_VALUE)));
		aracneInput.setIsDPIToleranceSpecified(((String) params
				.get(AracneParameters.TOL_TYPE)).equalsIgnoreCase("Apply"));
		aracneInput.setDPITolerance(Float.valueOf((String) params
				.get(AracneParameters.TOL_VALUE)));

		if (!((String) params.get(AracneParameters.DPI_LIST))
				.equalsIgnoreCase("Do Not Apply")) {
			Long subSetId = Long.parseLong((String) params
					.get(AracneParameters.MARKER_SET));
			ArrayList<String> targetGeneList = getMarkerData(subSetId);
			aracneInput
					.setTargetGeneList(targetGeneList.toArray(new String[0]));
		}

		aracneInput.setAlgorithm((String) params
				.get(AracneParameters.ALGORITHM));
		aracneInput.setMode((String) params.get(AracneParameters.MODE));
		String dataSetName = mSetView.getDataSet().getDataSetName();
		aracneInput.setDataSetName(dataSetName);
		aracneInput.setDataSetIdentifier(mSetView.getDataSet().getID());

		int bs = Integer.valueOf((String) params
				.get(AracneParameters.BOOTS_NUM));
		float pt = Float.valueOf((String) params
				.get(AracneParameters.CONSENSUS_THRESHOLD));

		aracneInput.setBootstrapNumber(bs);
		aracneInput.setConsensusThreshold(pt);

		setDSMicroarraydata(aracneInput);

		AracneOutput aracneOutput = computeAracneRemote(aracneInput);

		if (aracneOutput.getGraphEdges().length > 0) {
			boolean prune = isPrune();
			//set dataset = null to AdjacencyMatrixDataSet object
			AdjacencyMatrixDataSet adjDataSet = new AdjacencyMatrixDataSet(
					convert(aracneOutput, hubGeneList, dataSet, prune), 0,
					"Adjacency Matrix", "ARACNE Set", null);
			return adjDataSet;
		} else {
			// this.tellUserToRelaxThresholds();
			return null;
		}

	}

	/**
	 * Create Dataset for selected markerSet
	 */
	public ArrayList<String> getMarkerData(Long subSetId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getMarkerSet(subSetId);
		ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());

		return positions;
	}

	/**
	 * Create Dataset for selected markerSet
	 */
	public ArrayList<String> getArrayData(Long subSetId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getArraySet(subSetId);
		ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());

		return positions;
	}

	private void setDSMicroarraydata(AracneInput aracneInput) {

		// get selected Marker Names
		List<String> selectedMarkerNames = null;
		DSItemList<DSGeneMarker> selectedMarkers = null;
		String[] selectedMarkerSet = null;

		String selectedMarkerSetStr = params.get(AracneParameters.MARKER_SET)
				.toString();
		if (!selectedMarkerSetStr.equals("[]")
				&& !selectedMarkerSetStr.contains("All Markers")) {
			selectedMarkerSet = selectedMarkerSetStr.substring(1,
					selectedMarkerSetStr.length() - 1).split(",");

		}

		selectedMarkerNames = new ArrayList<String>();
		if (selectedMarkerSet == null) {
			selectedMarkers = dataSet.getMarkers();
			for (DSGeneMarker marker : dataSet.getMarkers())
				selectedMarkerNames.add(marker.getLabel());
		} else {
			selectedMarkers = new CSItemList<DSGeneMarker>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				ArrayList<String> temp = getMarkerData(Long
						.parseLong(selectedMarkerSet[i].trim()));

				for (int m = 0; m < temp.size(); m++) {
					String temp1 = ((temp.get(m)).split("\\s+"))[0].trim();
					DSGeneMarker marker = dataSet.getMarkers().get(temp1);
					if (marker != null) {
						selectedMarkers.add(marker);
						selectedMarkerNames.add(marker.getLabel());
					}
				}

			}
		}

		// get selected array setss
		String[] selectedArraySet = null;
		String selectedArraySetStr = params.get(AracneParameters.ARRAY_SET)
				.toString();
		if (!selectedArraySetStr.equals("[]")
				&& !selectedArraySetStr.contains("All Arrays"))
			selectedArraySet = selectedArraySetStr.substring(1,
					selectedArraySetStr.length() - 1).split(",");

		// get total SelectedArray Num
		int totalSelectedArrayNum = 0;
		if (selectedArraySet == null) {
			totalSelectedArrayNum = dataSet.size();
		} else {
			for (int i = 0; i < selectedArraySet.length; i++) {

				ArrayList<String> arrays = getArrayData(Long
						.parseLong(selectedArraySet[i].trim()));
				totalSelectedArrayNum = totalSelectedArrayNum + arrays.size();

			}
		}

		// get array names and marker values
		int selectedMarkersNum = selectedMarkers.size();
		float[][] A = new float[totalSelectedArrayNum][selectedMarkersNum];
		String[] selectedArrayNames = new String[totalSelectedArrayNum];
		int arrayIndex = 0;

		if (selectedArraySet == null) {
			for (int i = 0; i < dataSet.size(); i++) {
				for (int j = 0; j < selectedMarkersNum; j++) {
					A[i][j] = (float) (dataSet.get(i).getMarkerValue(
							selectedMarkers.get(j)).getValue());

				}
				selectedArrayNames[arrayIndex++] = dataSet.get(i).getLabel();

			}

		} else {
			for (int i = 0; i < selectedArraySet.length; i++) {
				ArrayList<String> arrayPositions = getArrayData(Long
						.parseLong(selectedArraySet[i].trim()));

				for (int j = 0; j < arrayPositions.size(); j++) {

					for (int k = 0; k < selectedMarkersNum; k++) {
						A[arrayIndex][k] = (float) (dataSet.get(arrayPositions
								.get(j)))
								.getMarkerValue(selectedMarkers.get(k))
								.getValue();

					}
					selectedArrayNames[arrayIndex++] = dataSet.get(
							arrayPositions.get(j)).getLabel();

				}
			}
		}

		aracneInput.setMarkers(selectedMarkerNames.toArray(new String[0]));
		aracneInput.setMicroarrayNames(selectedArrayNames);
		aracneInput.setMarkerValues(A);
 
	}

	private AracneOutput computeAracneRemote(AracneInput input) {
		AracneOutput output = null;
		RPCServiceClient serviceClient;

		try {       
			
			getWebServiceUrl();
			
			serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();

			
			long soTimeout = 2 * 24 * 60 * 60 * 1000; // 2 days
			options.setTimeOutInMilliSeconds(soTimeout);

			EndpointReference targetEPR = new EndpointReference(url);
					 
			options.setTo(targetEPR);

			// notice that that namespace is in the required form
			QName opName = new QName(
					"http://service.aracne.components.geworkbench.org",
					"execute");
			Object[] args = new Object[] { input };

			Class<?>[] returnType = new Class[] { AracneOutput.class };

			Object[] response = serviceClient.invokeBlocking(opName, args,
					returnType);
			output = (AracneOutput) response[0];

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

	private boolean isPrune() {
		return params.get(AracneParameters.MERGEPS).toString()
				.equalsIgnoreCase("Yes");
	}

	/**
	 * Convert the result from aracne-java to an AdjacencyMatrix object.
	 */
	private static AdjacencyMatrix convert(AracneOutput aracneOutput,
			List<String> hubGeneList, DSMicroarraySet mSet, boolean prune) {
		AdjacencyMatrix matrix = new AdjacencyMatrix(null);
		AracneGraphEdge[] aracneGraphEdges = aracneOutput.getGraphEdges();
		if (aracneGraphEdges == null || aracneGraphEdges.length == 0)
			return matrix;

		int nEdge = 0;
		for (int i = 0; i < aracneGraphEdges.length; i++) {
			DSGeneMarker marker1 = mSet.getMarkers().get(
					aracneGraphEdges[i].getNode1());
			DSGeneMarker marker2 = mSet.getMarkers().get(
					aracneGraphEdges[i].getNode2());

			if (hubGeneList != null && !hubGeneList.contains(marker1.getLabel())) {
				DSGeneMarker m = marker1;
				marker1 = marker2;
				marker2 = m;
			}

			AdjacencyMatrix.Node node1, node2;
			if (!prune) {
				node1 = new AdjacencyMatrix.Node(marker1);
				node2 = new AdjacencyMatrix.Node(marker2);
				matrix.add(node1, node2, aracneGraphEdges[i].getWeight(), null);
			} else {
				String geneName1 = marker1.getGeneName();
				if (geneName1.equals("---"))
					geneName1 = marker1.getLabel();
				node1 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,geneName1);
				 
				String geneName2 = marker2.getGeneName();
				if (geneName2.equals("---"))
					geneName2 = marker2.getLabel();
				node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,geneName2);						 
				matrix.add(node1, node2, aracneGraphEdges[i].getWeight());
			}
			nEdge++;
		}
		log.debug("edge count " + nEdge);
		return matrix;
	}
	
	
	private void getWebServiceUrl()
	{
		if (url == null || url.trim().equals(""))
		{
			 		
				url  = GeworkbenchRoot.getAppProperties().getProperty(ARACNE_WEBSERVICE_URL);
				if (url == null || url.trim().equals(""))
					url = DEFAULT_WEB_SERVICES_URL;
				
		}		
		 
	}

}
