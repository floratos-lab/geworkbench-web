package org.geworkbenchweb.plugins.aracne;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.aracne.data.AracneGraphEdge;
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.NetworkEdges;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

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
	private static final String  ARACNE_CLUSTERSERVICE_URL = "aracne.clusterService.url";
	private static final Pattern pattern = Pattern.compile("^ara\\d+$");
	
	private static String url = null;
	private static final Random random = new Random();
	final private Long datasetId;

	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	public AracneAnalysisWeb(Long datasetId,
			HashMap<Serializable, Serializable> params) {
		this.params = params;
		this.datasetId = datasetId;
	}

	public Network execute(boolean cluster) throws RemoteException {

		List<String> hubGeneList = null;
		if (params.get(AracneParameters.HUB_MARKER_SET) != null
				&& !params.get(AracneParameters.HUB_MARKER_SET).toString()
						.trim().equals("")
				&& !params.get(AracneParameters.HUB_MARKER_SET).toString()
						.trim().equals("All vs. All")) {
			Long subSetId = Long.parseLong((String) params
					.get(AracneParameters.HUB_MARKER_SET));
			hubGeneList = SubSetOperations.getMarkerData(subSetId);
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
					.get(AracneParameters.DPI_SET));
			List<String> targetGeneList = SubSetOperations.getMarkerData(subSetId);
			aracneInput
					.setTargetGeneList(targetGeneList.toArray(new String[0]));
		}

		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, datasetId);
		MicroarraySet microarrays = DataSetOperations.getMicroarraySet(datasetId);
		Map<String, String> map = DataSetOperations.getAnnotationMap(datasetId);

		aracneInput.setAlgorithm((String) params
				.get(AracneParameters.ALGORITHM));
		aracneInput.setMode((String) params.get(AracneParameters.MODE));
		aracneInput.setDataSetName(dataset.getName());
		aracneInput.setDataSetIdentifier(datasetId.toString());

		int bs = Integer.valueOf((String) params
				.get(AracneParameters.BOOTS_NUM));
		float pt = Float.valueOf((String) params
				.get(AracneParameters.CONSENSUS_THRESHOLD));

		aracneInput.setBootstrapNumber(bs);
		aracneInput.setConsensusThreshold(pt);

		setDSMicroarraydata(aracneInput, microarrays, hubGeneList);

		AracneOutput aracneOutput = null;
		if(!cluster) aracneOutput = computeAracneRemote(aracneInput);
		else aracneOutput = computeAracneRemoteCluster(aracneInput);

		boolean prune = isPrune();
		//set dataset = null to AdjacencyMatrixDataSet object
		return convert(aracneOutput, hubGeneList, map, prune);
	} 

	private void setDSMicroarraydata(AracneInput aracneInput, final MicroarraySet microarrays, List<String> hubGeneList) {

		// get selected Marker Names
		List<String> selectedMarkerNames = new ArrayList<String>();
		String[] selectedMarkerSet = null;
		if (params.get(AracneParameters.MARKER_SET) !=  null)
			selectedMarkerSet = (String[])params.get(AracneParameters.MARKER_SET);	 

		if (selectedMarkerSet == null) {
			selectedMarkerNames = Arrays.asList( microarrays.markerLabels );
		} else {
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<String> temp = SubSetOperations.getMarkerData(Long
						.parseLong(selectedMarkerSet[i].trim()));

				for (int m = 0; m < temp.size(); m++) {
					String temp1 = ((temp.get(m)).split("\\s+"))[0].trim();
					selectedMarkerNames.add(temp1);
				}

			}
			if(hubGeneList!=null){
				for(String hubmarker : hubGeneList) {
					if(!selectedMarkerNames.contains(hubmarker)) {
						selectedMarkerNames.add(hubmarker);
					}
				}
			}
		}

		// get selected array setss
		String[] selectedArraySet = null;
		if (params.get(AracneParameters.ARRAY_SET) !=  null)
			selectedArraySet = (String[])params.get(AracneParameters.ARRAY_SET);	
		// get total SelectedArray Num
		int totalSelectedArrayNum = 0;
		if (selectedArraySet == null) {
			totalSelectedArrayNum = microarrays.arrayNumber;
		} else {
			for (int i = 0; i < selectedArraySet.length; i++) {

				List<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedArraySet[i].trim()));
				totalSelectedArrayNum = totalSelectedArrayNum + arrays.size();

			}
		}

		int[] selectedMarkerIndex = new int[selectedMarkerNames.size()];
		int index = 0;
		for(int i=0; i<microarrays.markerNumber; i++) {
			if(selectedMarkerNames.contains( microarrays.markerLabels[i]) ) {
				selectedMarkerIndex[index] = i;
				index++;
			}
		}

		// get array names and marker values
		int selectedMarkersNum = selectedMarkerNames.size();
		float[][] A = new float[totalSelectedArrayNum][selectedMarkersNum];
		String[] selectedArrayNames = new String[totalSelectedArrayNum];
		int arrayIndex = 0;

		if (selectedArraySet == null) {
			for (int i = 0; i < microarrays.arrayNumber; i++) {
				for (int j = 0; j < selectedMarkersNum; j++) {
					A[i][j] = microarrays.values[selectedMarkerIndex[j]][i];

				}
				selectedArrayNames[arrayIndex++] = microarrays.arrayLabels[i];

			}

		} else {
			for (int i = 0; i < selectedArraySet.length; i++) {
				List<String> arrayPositions = SubSetOperations.getArrayData(Long
						.parseLong(selectedArraySet[i].trim()));
				
				int[] selectedArrayIndex = new int[arrayPositions.size()];
				int c = 0;
				for(int x=0; x<microarrays.arrayNumber; x++) {
					if(arrayPositions.contains( microarrays.arrayLabels[x]) ) {
						selectedArrayIndex[c] = x;
						c++;
					}
				}

				for (int j = 0; j < arrayPositions.size(); j++) {
					int aIndex = selectedArrayIndex[j];
					for (int k = 0; k < selectedMarkersNum; k++) {
						int mIndex = selectedMarkerIndex[k];
						A[arrayIndex][k] = microarrays.values[mIndex][aIndex];
					}
					selectedArrayNames[arrayIndex++] = microarrays.arrayLabels[aIndex];
				}
			}
		}

		aracneInput.setMarkers(selectedMarkerNames.toArray(new String[0]));
		aracneInput.setMicroarrayNames(selectedArrayNames);
		aracneInput.setMarkerValues(A);
 
	}

	private File exportExp(AracneInput input){
		File tempdir = new File(GeworkbenchRoot.getBackendDataDirectory() + "/temp/");
		if (!tempdir.exists() && !tempdir.mkdir()) return null;
		File dataFile = new File(tempdir, "aracne_"+random.nextInt(Short.MAX_VALUE)+".exp");
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("AffyID\tAnnotation");
			String[] arrays = input.getMicroarrayNames();
			for(String arrayName : arrays) bw.write("\t" + arrayName);
			bw.newLine();
			
			String[] markers = input.getMarkers();
			float[][] A = input.getMarkerValues();
			for(int i = 0; i < markers.length; i++){
				String markerName = markers[i];
				bw.write(markerName + "\t" + markerName);
				for(int j = 0; j < arrays.length; j++) bw.write("\t" + A[j][i]);
				bw.newLine();
			}
			bw.flush();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			if(bw != null){
				try{ bw.close(); }catch(Exception e){ e.printStackTrace(); }
			}
		}
		return dataFile;
	}
	
	private static final String aracne_cluster_service_url = GeworkbenchRoot.getAppProperty(ARACNE_CLUSTERSERVICE_URL);
	private AracneOutput computeAracneRemoteCluster(AracneInput input) throws RemoteException {

		File expFile = exportExp(input);
		if(expFile == null || !expFile.exists())
			throw new RemoteException("Aracne exportExp error");
		
		AracneAxisClient serviceClient = new AracneAxisClient();
		try {       			
			AracneOutput output = serviceClient.executeAracne(aracne_cluster_service_url, input, expFile);
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
			throw new RemoteException( "Aracne AxisFault: " + e.getMessage() + "\nfault action: " + e.getFaultAction()
					+ "\nreason: " + e.getReason());			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException( "Coumpute Aracne error: " + e.getMessage());
		} finally {
			if(!expFile.delete()) expFile.deleteOnExit();
		}
	}
	
	private AracneOutput computeAracneRemote(AracneInput input) throws RemoteException {
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
			throw new RemoteException( "Aracne AxisFault:" + e.getMessage() + " fault action: " + e.getFaultAction()
					+ " reason: " + e.getReason());		
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException( "Coumpute Aracne error:" + e.getMessage());
		}
		 
	}

	private boolean isPrune() {
		return params.get(AracneParameters.MERGEPS).toString()
				.equalsIgnoreCase("Yes");
	}

	/**
	 * Convert the result from aracne-java to an AdjacencyMatrix object.
	 */
	private static Network convert(AracneOutput aracneOutput,
			List<String> hubGeneList, final Map<String, String> map, boolean prune) {
		Map<String, NetworkEdges> network = new HashMap<String, NetworkEdges>();
		if(aracneOutput == null) return new Network(network);
		
		String networkName = null;
		String desc = aracneOutput.getParamterDescription();
		if(desc != null && pattern.matcher(desc).find()) networkName = desc;

		AracneGraphEdge[] aracneGraphEdges = aracneOutput.getGraphEdges();
		if (aracneGraphEdges == null || aracneGraphEdges.length == 0)
			return new Network(networkName, network);

		Map<String, List<String>> node2s = new HashMap<String, List<String>>();
		Map<String, List<Double>> weights = new HashMap<String, List<Double>>();
		int nEdge = 0;
		for (int i = 0; i < aracneGraphEdges.length; i++) {
			String marker1 = aracneGraphEdges[i].getNode1();
			String marker2 = aracneGraphEdges[i].getNode2();

			if (hubGeneList != null && !hubGeneList.contains(marker1)) {
				String m = marker1;
				marker1 = marker2;
				marker2 = m;
			}

			String node1, node2;
			if (!prune) {
				node1 = marker1;
				node2 = marker2;
			} else {
				String geneName1 = map.get( marker1 );
				if (geneName1.equals("---"))
					geneName1 = marker1;
				node1 = geneName1;
				 
				String geneName2 = map.get( marker2 );
				if (geneName2.equals("---"))
					geneName2 = marker2;
				node2 = geneName2;
			}
			List<String> n2 = node2s.get(node1);
			if(n2==null) {
				n2 = new ArrayList<String>();
				node2s.put(node1, n2);
			}
			n2.add(node2);
			List<Double> w = weights.get(node1);
			if(w==null) {
				w = new ArrayList<Double>();
				weights.put(node1, w);
			}
			w.add( new Double(aracneGraphEdges[i].getWeight()) );
			nEdge++;
		}
		for(String node1 : node2s.keySet()) {
			List<String> n2 = node2s.get(node1);
			List<Double> w = weights.get(node1);
			NetworkEdges edges = new NetworkEdges(n2, w); 
			network.put(node1, edges);
		}
		log.debug("edge count " + nEdge);
		return new Network(networkName, network);
	}
	
	
	private void getWebServiceUrl()
	{
		if (url == null || url.trim().equals(""))
		{
			 		
				url  = GeworkbenchRoot.getAppProperty(ARACNE_WEBSERVICE_URL);
				if (url == null || url.trim().equals(""))
					url = DEFAULT_WEB_SERVICES_URL;
				
		}		
		 
	}

}
