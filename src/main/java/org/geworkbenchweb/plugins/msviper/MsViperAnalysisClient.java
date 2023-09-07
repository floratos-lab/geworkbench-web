package org.geworkbenchweb.plugins.msviper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.MsViperResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.visualizations.Barcode;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

/**
 * 
 * MsViper Analysis client.
 * 
 */
public class MsViperAnalysisClient {

	private static Log log = LogFactory.getLog(MsViperAnalysisClient.class);

	private static final Random random = new Random();

	private static final String MSVIPER_SERVICE_URL = GeworkbenchRoot
			.getAppProperty("msviper.clusterService.url");
	private static final String MSVIPER_NAMESPACE = "http://www.geworkbench.org/service/msviper";

	private static final String MSVIPER_ENDPOINT = "MsViperRequest";
	private static final String PHENOTYPE_FILE = "phenotypes.txt";
	public static final String GENE_NAME = "gene name";
	public static final String ENTREZ_ID = "entrez id";

	final private Long datasetId;
	private final Long userId;
	final MsViperParam params;

	public MsViperAnalysisClient(Long datasetId, Long userId,
			MsViperParam params) {
		this.params = params;
		this.userId = userId;
		this.datasetId = datasetId;
	}

	public MsViperResult execute() throws Exception {

		String tempdirPath = GeworkbenchRoot.getBackendDataDirectory()
				+ File.separator + "temp" + File.separator + "msviper"
				+ random.nextInt(Short.MAX_VALUE);
		File tempdir = new File(tempdirPath);
		if (!tempdir.exists() && !tempdir.mkdirs())
			throw new RemoteException("Cannot create a temp directory.");

		DataSet dataSet = FacadeFactory.getFacade().find(DataSet.class,
				datasetId);
		String datasetName = dataSet.getName();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, dataSet.getDataId());

		String networkFname = params.getNetwork();
		if (networkFname.length() == 0)
			throw new RemoteException("Network not loaded");

		if (!networkFname.endsWith(".adj"))
			networkFname = networkFname.replace(" ", "") + ".adj";

		/* copy the uploaded (and possibly processed) network file */
		String source = GeworkbenchRoot.getBackendDataDirectory()
				+ File.separator + "networks" + File.separator + "msViper"
				+ File.separator + userId + File.separator + datasetId + File.separator + networkFname;
		File target = convertNetWork(source, tempdirPath + File.separator + networkFname);
		if (target == null || !target.exists())
			throw new RemoteException("msViper convertNetWork error");
		
		File expFile = exportExp(microarray, tempdir, datasetName);
		if (expFile == null || !expFile.exists())
			throw new RemoteException("msViper exportExp error");

		File phenotypesFile = exportPhenotypesFile(microarray, tempdir);
		if (phenotypesFile == null || !phenotypesFile.exists())
			throw new RemoteException("msViper export phenotypes file error");

		try {
			org.apache.axis2.client.Options serviceOptions = new org.apache.axis2.client.Options();
			serviceOptions.setProperty(Constants.Configuration.ENABLE_MTOM,
					Constants.VALUE_TRUE);
			serviceOptions.setProperty(
					Constants.Configuration.ATTACHMENT_TEMP_DIR,
					System.getProperty("java.io.tmpdir"));
			serviceOptions.setProperty(
					Constants.Configuration.CACHE_ATTACHMENTS,
					Constants.VALUE_TRUE);
			serviceOptions.setProperty(
					Constants.Configuration.FILE_SIZE_THRESHOLD, "1024");
			// 50-hour timeout
			serviceOptions.setTimeOutInMilliSeconds(180000000);

			ServiceClient serviceClient = new ServiceClient();
			serviceClient.setOptions(serviceOptions);
			EndpointReference ref = new EndpointReference();
			ref.setAddress(MSVIPER_SERVICE_URL);
			serviceClient.setTargetEPR(ref);

			return executeMsViper(tempdirPath + File.separator + datasetName,
					tempdir + File.separator + PHENOTYPE_FILE, tempdirPath
							+ File.separator + networkFname, params,
					datasetName, networkFname, serviceClient);

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
			throw new RemoteException("MsViper AxisFault: " + e.getMessage()
					+ "\nfault action: " + e.getFaultAction() + "\nreason: "
					+ e.getReason());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("Compute msViper error: "
					+ e.getMessage());
		} finally {
			if (tempdirPath != null && tempdirPath.contains("msviper"))
				FileUtils.deleteDirectory(new File(tempdirPath));
		}
	}

	/* export the microarray data as a (temporary) .exp file */
	private File exportExp(MicroarrayDataset microarray, File tempdir,
			String datasetName) {

		String[] arrayLabels = microarray.getArrayLabels();
		String[] markerLabels = microarray.getMarkerLabels();
		float[][] values = microarray.getExpressionValues();

		File dataFile = new File(tempdir, datasetName);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("ID");
			for (int i = 0; i < arrayLabels.length; i++)
				bw.write("\t" + arrayLabels[i]);
			bw.newLine();

			for (int i = 0; i < markerLabels.length; i++) {
				bw.write(markerLabels[i]);
				for (int j = 0; j < arrayLabels.length; j++)
					bw.write("\t" + values[i][j]);
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

	private File exportPhenotypesFile(MicroarrayDataset microarray, File tempdir) {

		File dataFile = new File(tempdir, PHENOTYPE_FILE);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(dataFile));
			bw.write("Array" + "\t" + params.getContext() + "\n");
			Map<String, String> caseMap = params.getClassCase();
			Map<String, String> controlMap = params.getClassControl();

			String[] arrayLabels = microarray.getArrayLabels();
			for (int i = 0; i < arrayLabels.length; i++) {

				String s = caseMap.get(arrayLabels[i]);
				if (s == null)
					s = controlMap.get(arrayLabels[i]);
				if (s == null)
					s = "others";
				bw.write(arrayLabels[i] + "\t" + s + "\n");

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

	String resultName = null; // TODO bad design: 1. typical 'side effect'
								// pattern; 2. not consistent with other plugins

	/* Discovery */
	private MsViperResult executeMsViper(final String expFile,
			final String phenotypesFile, final String adjFile,
			MsViperParam param, final String datasetName,
			final String networkFname, final ServiceClient serviceClient)
			throws AxisFault, Exception {

		OMFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		OMNamespace namespace = omFactory.createOMNamespace(MSVIPER_NAMESPACE,
				null);
		OMElement request = omFactory.createOMElement(MSVIPER_ENDPOINT,
				namespace);

		addFileElement("expFile", expFile, omFactory, namespace, request);

		addFileElement("phenotypesFile", phenotypesFile, omFactory, namespace,
				request);
		addFileElement("adjFile", adjFile, omFactory, namespace, request);
		omFactory.createOMElement("datasetName", namespace, request).setText(
				convertToAlphaNumeric(datasetName));
		omFactory.createOMElement("context", namespace, request).setText(
				convertToAlphaNumeric(param.getContext()));
		omFactory.createOMElement("caseGroups", namespace, request).setText(
				param.getCaseGroups());
		omFactory.createOMElement("controlGroups", namespace, request).setText(
				param.getControlGroups());
		omFactory.createOMElement("networkFileName", namespace, request)
				.setText(convertToAlphaNumeric(networkFname.substring(0, networkFname.length()-4))+".adj");
		omFactory.createOMElement("gesFilter", namespace, request).setText(
				param.getGesFilter().toString().toUpperCase());
		omFactory.createOMElement("minAllowedRegulonSize", namespace, request)
				.setText(param.getMinAllowedRegulonSize().toString());
		omFactory.createOMElement("bootstrapping", namespace, request).setText(
				param.getBootstrapping().toString().toUpperCase());
		omFactory.createOMElement("method", namespace, request).setText(
				param.getMethod());
		omFactory.createOMElement("shadow", namespace, request).setText(
				param.getShadow().toString().toUpperCase());
		omFactory.createOMElement("shadowValue", namespace, request).setText(Float.toString(param.getShadowValue()));

		OMElement response = serviceClient.sendReceive(request);

		OMElement logElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE, "log"));
		String errlog = logElement.getText();
		if (errlog != null && errlog.length() > 0)
		{
			if (errlog.contains("was killed"))
				throw new Exception(errlog + ". A possible reason could be that one or more hub genes in the network have too few regulon genes. ");
			else
				throw new Exception(errlog);
		}
		OMElement nameElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"resultName"));
		resultName = nameElement.getText();

		OMElement resultElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"resultFile"));
		DataHandler resultHandler = resultElement == null ? null
				: (DataHandler) ((OMText) resultElement.getFirstOMChild())
						.getDataHandler();
		/*
		 * OMElement signaturesElement = (OMElement) response
		 * .getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
		 * "signatureFile")); DataHandler signaturesHandler = signaturesElement
		 * == null ? null : (DataHandler) ((OMText)
		 * signaturesElement.getFirstOMChild()) .getDataHandler();
		 */
		OMElement mrsElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE, "mrsFile"));
		DataHandler mrsHandler = mrsElement == null ? null
				: (DataHandler) ((OMText) mrsElement.getFirstOMChild())
						.getDataHandler();
		OMElement ledgesElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"ledgesFile"));
		DataHandler ledgesHandler = ledgesElement == null ? null
				: (DataHandler) ((OMText) ledgesElement.getFirstOMChild())
						.getDataHandler();
		OMElement regulonsElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"regulonsFile"));
		DataHandler regulonsHandler = regulonsElement == null ? null
				: (DataHandler) ((OMText) regulonsElement.getFirstOMChild())
						.getDataHandler();
		OMElement mrsSigfileElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"mrsSignatureFile"));
		DataHandler mrsSighandler = mrsSigfileElement == null ? null
				: (DataHandler) ((OMText) mrsSigfileElement.getFirstOMChild())
						.getDataHandler();
		
		OMElement shadowResultElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"shadowResultFile"));
		DataHandler shadowResulthandler = shadowResultElement == null ? null
				: (DataHandler) ((OMText) shadowResultElement.getFirstOMChild())
						.getDataHandler();

		OMElement shadowPairElement = (OMElement) response
				.getFirstChildWithName(new QName(MSVIPER_NAMESPACE,
						"shadowPairFile"));
		DataHandler shadowPairhandler = shadowPairElement == null ? null
				: (DataHandler) ((OMText) shadowPairElement.getFirstOMChild())
						.getDataHandler();

		MsViperResult result = new MsViperResult();
		result.setLabel(resultName);
		String[] mrs = getMrs(mrsHandler);

		result.setMrsResult(getMrsResult(resultHandler));
		result.setMrs_signatures(getSignatureMap(mrsSighandler));
		result.setMrs(mrs);
		result.setLeadingEdges(getGenelistMap(ledgesHandler, mrs));
		result.setRegulons(getGenelistMap(regulonsHandler, mrs));
		sortMrRanks(result.getMrs_signatures(), result);
		result.setBarcodes(getBarcodeMap(result, this.datasetId));
		if (params.getShadow())
		{
			result.setShadow_pairs(getShadowPairMap(shadowPairhandler));
			result.setShadowResult(getMrsResult(shadowResulthandler));
		}
		 
		return result;
	}

	private void addFileElement(String title, String filepath,
			OMFactory omFactory, OMNamespace namespace, OMElement request) {
		DataSource messageDataSource = null;
		messageDataSource = new FileDataSource(filepath);
		DataHandler dataHandler = new DataHandler(messageDataSource);
		OMElement fileElement = omFactory.createOMElement(title, namespace,
				request);
		OMText textData = omFactory.createOMText(dataHandler, true);
		fileElement.addChild(textData);
		;
	}
	
	private String convertToAlphaNumeric(String value)
	{
		return value.trim().replaceAll("[^A-Za-z0-9]", "_");
	}
	
	

	// TODO there will be easier ways to copy files in Java 7
	static int copyFile(String source, String dest) throws IOException {
		int byteCount = 0;
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
				byteCount += bytesRead;
			}
		} finally {
			if (input != null)
				input.close();
			if (output != null)
				output.close();
		}
		return byteCount;
	}

	// read mrs result response from the web service
	private Map<String, Double> getSignatureMap(DataHandler handler) {
		if (handler == null)
			return null;
		Map<String, Double> sMap = new HashMap<String, Double>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					handler.getInputStream()));
			String line = null;
			line = br.readLine(); // skip first line
			while ((line = br.readLine()) != null) {
				if (line.trim().equals(""))
					continue;
				String[] toks = line.trim().split("\t");
				String regulon = toks[0].trim();
				sMap.put(regulon.substring(1, regulon.length() - 1),
						Double.valueOf(toks[1].trim()));

			}

			return sMap;

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

	}

	// read mrs result response from the web service
	private String[][] getMrsResult(DataHandler handler) {
		if (handler == null)
			return null;
		Map<String, String> annotationMap = null;
		annotationMap = DataSetOperations.getAnnotationMap(this.datasetId);
		List<String[]> resultList = new ArrayList<String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					handler.getInputStream()));
			String line = null;

			String[] row = null;
			br.readLine(); // skip first line
			while ((line = br.readLine()) != null) {
				if (line.trim().equals(""))
					continue;
				String[] toks = line.trim().split("\t");
				row = new String[toks.length];
				row[0] = toks[0].trim().substring(1,
						toks[0].trim().length() - 1);
				row[1] = annotationMap.get(row[0]);
				if (row[1] == null || row[1].trim().equals(""))
					row[1] = row[0];
				for (int j = 2; j < toks.length; j++)
					row[j] = toks[j].trim();
				resultList.add(row);

			}

			if ( resultList == null || resultList.size() == 0)
				return null;
			int size = row==null ? 0: row.length;
			String[][] results = new String[resultList.size()][size];
			for (int i = 0; i < resultList.size(); i++)
				for (int j = 0; j < resultList.get(i).length; j++)
					results[i][j] = resultList.get(i)[j];

			return results;

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

	}

	// read master regulons from the web service
	private String[] getMrs(DataHandler handler) {
		if (handler == null)
			return null;
		String[] mrs;
		List<String> mrList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					handler.getInputStream()));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.trim().equals(""))
					continue;
				mrList.add(line.trim());
			}
			mrs = new String[mrList.size()];
			for (int i = 0; i < mrList.size(); i++)
				mrs[i] = mrList.get(i);

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
		return mrs;
	}

	// read leading edges or regulons result response from the web service
	private Map<String, List<String>> getGenelistMap(DataHandler handler,
			String[] mrs) {
		if (handler == null)
			return null;
		Map<String, List<String>> map = new HashMap<String, List<String>>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					handler.getInputStream()));
			String line = null;
			String ledgeLine = "";
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.trim().equals(""))
					continue;
				ledgeLine = ledgeLine + line.trim();
				if (!line.trim().endsWith(")"))
					continue;

				List<String> genes = new ArrayList<String>();
				String[] toks = ledgeLine.substring(2, ledgeLine.length() - 1)
						.split(",");

				for (int j = 0; j < toks.length; j++) {
					String tok = toks[j].trim();
					genes.add(tok.substring(1, tok.length() - 1));
				}
				map.put(mrs[i], genes);
				ledgeLine = "";
				i++;
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
		return map;
	}

	// read leading edges or regulons result response from the web service
	private Map<String, List<String>> getShadowPairMap(DataHandler handler) {
		if (handler == null)
			return null;
		Map<String, List<String>> pairMap = new HashMap<String, List<String>>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					handler.getInputStream()));
			String line = null;
			br.readLine(); // skip first line
			while ((line = br.readLine()) != null) {
				if (line.trim().equals(""))
					continue;
				String[] toks = line.trim().split("\t");
				String s1 = toks[1].trim().substring(1,
						toks[1].trim().length() - 1);
				String s2 = toks[2].trim().substring(1,
						toks[2].trim().length() - 1);
				if (pairMap.keySet().contains(s2))
					pairMap.get(s2).add(s1);
				else {
					List<String> pairList = new ArrayList<String>();
					pairList.add(s1);
					pairMap.put(s2, pairList);
				}

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
		return pairMap;
	}

	private void sortMrRanks(final Map<String, Double> values,
			MsViperResult msViperResult) {
		Map<String, Integer> rankMap = new HashMap<String, Integer>();
		List<String> genes = new ArrayList<String>();
		genes.addAll(values.keySet());
		// sort genes by value
		Collections.sort(genes, new Comparator<String>() {
			public int compare(String m1, String m2) {
				return Double.valueOf(Math.abs(values.get(m2)))
						.compareTo(Double.valueOf(Math.abs(values.get(m1))));
			}
		});
		msViperResult.setMaxVal(values.get(genes.get(0)));
		msViperResult.setMinVal(values.get(genes.get(genes.size() - 1)));
		// give same ranks to genes with same value
		// TODO we should use standard library, e.g. apache commons math to do
		// this
		rankMap.put(genes.get(0), 0);
		double lastValue = values.get(genes.get(0));
		int lastRank = 0;
		for (int i = 1; i < genes.size(); i++) {
			int rank = i;
			String gene = genes.get(i);
			double value = values.get(gene);
			if (value == lastValue) {
				rank = lastRank;
			}
			rankMap.put(gene, rank);
			lastValue = value;
			lastRank = rank;
		}

		Map<String, Integer> mrRankMap = new HashMap<String, Integer>();
		String[][] r = msViperResult.getMrsResult();
		for (int i = 0; i < r.length; i++)
			mrRankMap.put(r[i][0], rankMap.get(r[i][0]));

		msViperResult.setRanks(mrRankMap);
	}

	private Map<String, Integer> getSigRankMap(final Map<String, Double> values) {
		Map<String, Integer> rankMap = new HashMap<String, Integer>();
		List<String> genes = new ArrayList<String>();
		genes.addAll(values.keySet());
		// sort genes by value
		Collections.sort(genes, new Comparator<String>() {
			public int compare(String m1, String m2) {
				return values.get(m1).compareTo(values.get(m2));
			}
		});

		rankMap.put(genes.get(0), 1);
		// double lastValue = values.get(genes.get(0));
		// int lastRank = 0;
		for (int i = 1; i < genes.size(); i++) {
			int rank = i + 1;
			String gene = genes.get(i);
			/*
			 * double value = values.get(gene); if (value == lastValue) { rank =
			 * lastRank; }
			 */
			rankMap.put(gene, rank);
			// lastValue = value;
			// lastRank = rank;
		}
		return rankMap;
	}

	private Map<String, List<Barcode>> getBarcodeMap(
			MsViperResult msViperResult, long dataSetId) {
		Map<String, List<Barcode>> barcodeMap = new HashMap<String, List<Barcode>>();
		Map<String, List<String>> regulons = msViperResult.getRegulons();
		// Map<String, Double> signatures = msViperResult.getSignatures();
		Map<String, Double> mrs_signatures = msViperResult.getMrs_signatures();
		Map<String, Integer> ranks = getSigRankMap(mrs_signatures);
		String[][] rdata = msViperResult.getMrsResult();

		Map<String, float[]> microarrayMap = getMicroarrayMap(dataSetId);

		for (int i = 0; i < rdata.length; i++) {
			List<Barcode> barcodeList = new ArrayList<Barcode>();
			ArrayList<HashMap<Integer, Integer>> lm = new ArrayList<HashMap<Integer, Integer>>();
			lm.add(0, new HashMap<Integer, Integer>()); // SC>=0
			lm.add(1, new HashMap<Integer, Integer>()); // SC<0
			int[] maxcopy = new int[2];
			List<String> targets = regulons.get(rdata[i][0]);
			if (targets == null || targets.size() == 0)
				continue;
			for (int j = 0; j < targets.size(); j++) {
				SpearmansCorrelation SC = new SpearmansCorrelation();
				double spearCor = 0.0;

				float[] arrayData1 = microarrayMap.get(rdata[i][0]);
				float[] arrayData2 = microarrayMap.get(targets.get(j));
				spearCor = SC.correlation(convertToDouble(arrayData1),
						convertToDouble(arrayData2));

				Barcode barcode = getBarcode(targets.get(j), spearCor, ranks
						.get(targets.get(j)).intValue(), mrs_signatures.size(),
						lm, maxcopy);
				barcodeList.add(barcode);
			}

			barcodeMap.put(rdata[i][0], barcodeList);
		}
		return barcodeMap;
	}

	private Map<String, float[]> getMicroarrayMap(long dataSetId) {
		Map<String, float[]> microarrayMap = new HashMap<String, float[]>();
		DataSet dataSet = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, dataSet.getDataId());
		String[] markers = microarray.getMarkerLabels();
		float[][] data = microarray.getExpressionValues();

		for (int i = 0; i < markers.length; i++) {
			microarrayMap.put(markers[i], data[i]);
		}

		return microarrayMap;

	}

	private double[] convertToDouble(float[] inData) {
		double[] outData = new double[inData.length];
		for (int i = 0; i < inData.length; i++)
			outData[i] = (double)inData[i];
		return outData;

	}

	private Barcode getBarcode(String gene, double spearmanCor, int rank,
			int totalMarkerNumber, ArrayList<HashMap<Integer, Integer>> lm,
			int[] maxcopy) {
		int arrayIndex = spearmanCor > 0 ? 0 : 1;
		int position = (int) 400 * rank / totalMarkerNumber;	 
		HashMap<Integer, Integer> hm = lm.get(arrayIndex);
		Integer copy = hm.get(position);
		copy = copy == null ? 1 : (copy + 1);
		hm.put(position, copy);
		if (maxcopy[arrayIndex] < copy)
			maxcopy[arrayIndex] = copy;
		int ColorIndex = 255 * lm.get(arrayIndex).get(position)
				/ maxcopy[arrayIndex];

		return new Barcode(gene, position, ColorIndex, arrayIndex);
	}

	private File convertNetWork(String source, String target) {

		File sourceFile = new File(source);
		File targetFile = new File(target);
		BufferedWriter bw = null;

		BufferedReader br = null;
		try {

			Map<String, String> geneNameMap = getAnnotationMap(GENE_NAME,
					datasetId);
			Map<String, String> entezIDMap = getAnnotationMap(GENE_NAME,
					datasetId);

			br = new BufferedReader(new FileReader(sourceFile));
			bw = new BufferedWriter(new FileWriter(targetFile));
			String line = br.readLine();
			while (line != null) {
				// skip comments
				if (line.trim().equals("") || line.startsWith(">")
						|| line.startsWith("-")) {
					line = br.readLine();
					continue;
				}
				StringTokenizer tr = new StringTokenizer(line, "\t");
				String hub = getMarker(tr.nextToken().trim(), geneNameMap,
						entezIDMap);
				bw.write(hub);
				while (tr.hasMoreTokens()) {
					String strGeneId2 = tr.nextToken().trim();
					if (strGeneId2.length() == 0)
						continue;
					strGeneId2 = getMarker(strGeneId2, geneNameMap, entezIDMap);
					float value = Float.parseFloat(tr.nextToken().trim());
					if (strGeneId2.equals("null") || strGeneId2.equals("NULL"))
						continue;
					bw.write("\t" + strGeneId2);
					bw.write("\t" + value);

				} // end of the token loop for one line
				bw.write("\n");
				line = br.readLine();
			} // end of reading while loop

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

			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return targetFile;
	}

	private String getMarker(String nodeLabel, Map<String, String> geneNameMap,
			Map<String, String> entezIDMap) {
		if (geneNameMap == null) // no annotation
			return nodeLabel;
		nodeLabel = nodeLabel.trim();
		if (geneNameMap.containsKey(nodeLabel.toUpperCase()))
			return geneNameMap.get(nodeLabel.toUpperCase());
		if (entezIDMap.containsKey(nodeLabel))
			return entezIDMap.get(nodeLabel);
		return nodeLabel;
	}

	/* Maps geneSymbol/entezID to marker in original dataset if available */
	public static Map<String, String> getAnnotationMap(String type,
			long dataSetId) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory
				.getFacade()
				.find("SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId",
						parameter);
		Map<String, String> geneToMarkerMap = null;
		if (dataSetAnnotation != null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Map<String, Object> pm = new HashMap<String, Object>();
			pm.put("id", annotationId);
			List<?> entries = FacadeFactory
					.getFacade()
					.list("SELECT entries.probeSetId, entries.geneSymbol, entries.entrezId FROM Annotation a JOIN a.annotationEntries entries WHERE a.id=:id",
							pm);
			geneToMarkerMap = new HashMap<String, String>();
			if (GENE_NAME.equals(type)) {
				for (Object entry : entries) {
					Object[] obj = (Object[]) entry;
					geneToMarkerMap.put(((String) obj[1]).trim().toUpperCase(), ((String) obj[0]).trim());
				}
			} else if (ENTREZ_ID.equals(type)) {
				for (Object entry : entries) {
					Object[] obj = (Object[]) entry;
					geneToMarkerMap.put(((String) obj[2]).trim(), ((String) obj[0]).trim());
				}
			}

		}
		return geneToMarkerMap;
	}

}
