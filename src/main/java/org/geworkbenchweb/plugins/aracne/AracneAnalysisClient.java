package org.geworkbenchweb.plugins.aracne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.NetworkEdges;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * 
 * ARACne Analysis client.
 */
public class AracneAnalysisClient {

	private static Log log = LogFactory.getLog(AracneAnalysisClient.class);

	private static final Random random = new Random();

	private static final String ARACNE_SERVICE_URL = GeworkbenchRoot.getAppProperty("aracne.clusterService.url");
	private static final String ARACNE_NAMESPACE = "http://www.geworkbench.org/service/aracne";

	private static final String DISCOVERY_ENDPOINT = "DiscoveryRequest";

	final private Long datasetId;
	final private HashMap<Serializable, Serializable> params;

	/*
	 * WARN problematic design:
	 * 1. typical 'side effect' pattern;
	 * 2. not consistent with other plugins
	 */
	String resultName = null;

	public AracneAnalysisClient(Long datasetId,
			HashMap<Serializable, Serializable> params) {
		this.params = params;
		this.datasetId = datasetId;
	}

	public AbstractPojo execute() throws RemoteException {

		MicroarraySet microarrays = DataSetOperations.getMicroarraySet(datasetId);

		List<String> hubGeneList = null;
		if (params.get(AracneParameters.HUB_MARKER_SET) != null
				&& !params.get(AracneParameters.HUB_MARKER_SET).toString()
						.trim().equals("")) {
			Long subSetId = Long.parseLong((String) params
					.get(AracneParameters.HUB_MARKER_SET));
			hubGeneList = SubSetOperations.getMarkerData(subSetId);
		}

		// only for the purpose of dataset name
		String datasetName = FacadeFactory.getFacade().find(DataSet.class, datasetId).getName();

		List<String> markers = Arrays.asList(microarrays.markerLabels);

		File expFile = exportExp(microarrays, hubGeneList, markers);
		if (expFile == null || !expFile.exists())
			throw new RemoteException("Aracne exportExp error");

		try {
			org.apache.axis2.client.Options serviceOptions = new org.apache.axis2.client.Options();
			serviceOptions.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
			serviceOptions.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,
					System.getProperty("java.io.tmpdir"));
			serviceOptions.setProperty(Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE);
			serviceOptions.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "1024");

			ServiceClient serviceClient = new ServiceClient();
			serviceClient.setOptions(serviceOptions);
			EndpointReference ref = new EndpointReference();
			ref.setAddress(ARACNE_SERVICE_URL);
			serviceClient.setTargetEPR(ref);

			boolean prune = params.get(AracneParameters.MERGEPS).toString()
					.equalsIgnoreCase("Yes");

			float threshold = Float.valueOf((String) params.get(AracneParameters.P_VALUE));

			OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
			OMNamespace namespace = omFactory.createOMNamespace(ARACNE_NAMESPACE, null);
			OMElement request = omFactory.createOMElement(DISCOVERY_ENDPOINT, namespace);

			OMText textData = omFactory.createOMText(new DataHandler(new FileDataSource(expFile)), true);
			omFactory.createOMElement("expFile", namespace, request).addChild(textData);
			omFactory.createOMElement("dataSetName", namespace, request).setText(datasetName);
			omFactory.createOMElement("bootstrapNumber", namespace, request)
					.setText((String) params.get(AracneParameters.BOOTS_NUM));
			omFactory.createOMElement("dataSetIdentifier", namespace, request).setText(datasetId.toString());
			omFactory.createOMElement("threshold", namespace, request).setText(Float.toString(threshold));
			omFactory.createOMElement("hubGeneList", namespace, request).setText(String.join("\n", hubGeneList));
			omFactory.createOMElement("isDPIFiltering", namespace, request).setText(
					Boolean.toString(((String) params.get(AracneParameters.DPI_FILTERING)).equalsIgnoreCase("Yes")));

			OMElement response = serviceClient.sendReceive(request);

			OMElement nameElement = (OMElement) response.getFirstChildWithName(new QName(ARACNE_NAMESPACE, "adjName"));
			resultName = nameElement.getText();

			OMElement fileElement = (OMElement) response.getFirstChildWithName(new QName(ARACNE_NAMESPACE, "adjFile"));
			DataHandler handler = fileElement == null ? null
					: (DataHandler) ((OMText) fileElement.getFirstOMChild()).getDataHandler();

			return createNetwork(getEdges(handler), hubGeneList, prune);
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
			throw new RemoteException("Aracne AxisFault: " + e.getMessage() + "\nfault action: " + e.getFaultAction()
					+ "\nreason: " + e.getReason());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("Compute Aracne error: " + e.getMessage());
		} finally {
			if (!expFile.delete())
				expFile.deleteOnExit();
		}
	}

	/* export the microarray data as a temporary) gene expression file for aracne */
	private static File exportExp(final MicroarraySet microarrays,
			final List<String> hubGeneList,
			final List<String> selectedMarkerNames) {

		// get total SelectedArray Num
		int totalSelectedArrayNum = microarrays.arrayNumber;

		int[] selectedMarkerIndex = new int[selectedMarkerNames.size()];
		int index = 0;
		for (int i = 0; i < microarrays.markerNumber; i++) {
			if (selectedMarkerNames.contains(microarrays.markerLabels[i])) {
				selectedMarkerIndex[index] = i;
				index++;
			}
		}

		// get array names and marker values
		int selectedMarkersNum = selectedMarkerNames.size();
		float[][] A = new float[totalSelectedArrayNum][selectedMarkersNum];
		String[] selectedArrayNames = new String[totalSelectedArrayNum];
		int arrayIndex = 0;

		for (int i = 0; i < microarrays.arrayNumber; i++) {
			for (int j = 0; j < selectedMarkersNum; j++) {
				A[i][j] = microarrays.values[selectedMarkerIndex[j]][i];
			}
			selectedArrayNames[arrayIndex++] = microarrays.arrayLabels[i];
		}

		File tempdir = new File(GeworkbenchRoot.getBackendDataDirectory() + "/temp/");
		if (!tempdir.exists() && !tempdir.mkdir())
			return null;
		File dataFile = new File(tempdir, "aracne_" + random.nextInt(Short.MAX_VALUE) + ".exp");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("gene");
			for (String arrayName : selectedArrayNames)
				bw.write("\t" + arrayName);
			bw.newLine();

			String[] markers = selectedMarkerNames.toArray(new String[0]);
			for (int i = 0; i < markers.length; i++) {
				String markerName = markers[i];
				bw.write(markerName);
				for (int j = 0; j < selectedArrayNames.length; j++)
					bw.write("\t" + A[j][i]);
				bw.newLine();
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return dataFile;
	}

	private static class Edge {
		final String node1, node2;
		final float weight;

		Edge(String node1, String node2, float wieght) {
			this.node1 = node1;
			this.node2 = node2;
			this.weight = wieght;
		}
	}

	// read and parse the network result response from the web service
	private static List<Edge> getEdges(DataHandler handler) {
		if (handler == null)
			return null;
		List<Edge> edges = new ArrayList<Edge>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(handler.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.trim().equals("") || line.startsWith(">"))
					continue;
				String[] toks = line.split("\t");
				String node1 = toks[0];
				int n = toks.length;
				if (n != 4 || node1.trim().equals(""))
					continue;
				String node2 = toks[1];
				float weight = Float.parseFloat(toks[2]);
				edges.add(new Edge(node1, node2, weight));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return edges;
	}

	private Network createNetwork(final List<Edge> aracneGraphEdges, final List<String> hubGeneList,
			final boolean prune) {
		Map<String, NetworkEdges> network = new HashMap<String, NetworkEdges>();
		if (aracneGraphEdges == null)
			return new Network(network);

		if (aracneGraphEdges == null || aracneGraphEdges.size() == 0)
			return new Network(network);

		final Map<String, String> map = DataSetOperations.getAnnotationMap(datasetId);

		Map<String, List<String>> node2s = new HashMap<String, List<String>>();
		Map<String, List<Double>> weights = new HashMap<String, List<Double>>();
		int nEdge = 0;
		for (Edge edge : aracneGraphEdges) {
			String marker1 = edge.node1;
			String marker2 = edge.node2;

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
				String geneName1 = map.get(marker1);
				if (geneName1.equals("---"))
					geneName1 = marker1;
				node1 = geneName1;

				String geneName2 = map.get(marker2);
				if (geneName2.equals("---"))
					geneName2 = marker2;
				node2 = geneName2;
			}
			List<String> n2 = node2s.get(node1);
			if (n2 == null) {
				n2 = new ArrayList<String>();
				node2s.put(node1, n2);
			}
			n2.add(node2);
			List<Double> w = weights.get(node1);
			if (w == null) {
				w = new ArrayList<Double>();
				weights.put(node1, w);
			}
			w.add(Double.valueOf(edge.weight));
			nEdge++;
		}
		for (String node1 : node2s.keySet()) {
			List<String> n2 = node2s.get(node1);
			List<Double> w = weights.get(node1);
			String[] types = new String[w.size()];
			for (int i = 0; i < types.length; i++) {
				types[i] = "pd";
			}
			NetworkEdges edges = new NetworkEdges(n2, w, types);
			network.put(node1, edges);
		}
		log.debug("edge count " + nEdge);
		return new Network(network);
	}
}
