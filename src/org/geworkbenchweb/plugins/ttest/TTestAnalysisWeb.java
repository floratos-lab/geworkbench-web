package org.geworkbenchweb.plugins.ttest;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbenchweb.GeworkbenchRoot; 
import org.geworkbenchweb.utils.SubSetOperations;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geworkbench.components.ttest.SignificanceMethod;
import org.geworkbench.components.ttest.data.TTestInput;
import org.geworkbench.components.ttest.data.TTestOutput;

/**
 * This class submits TTest Analysis from web application
 * @author Nikhil Reddy
 */
public class TTestAnalysisWeb {

	private static Log log = LogFactory.getLog(TTestAnalysisWeb.class);
	
	private static final String  DEFAULT_WEB_SERVICES_URL = "http://localhost:8080/axis2/services/TTestService";
	private static final String  TTEST_WEBSERVICE_URL = "ttest.webService.url";
	
	private static String url = null;
	
	private DSMicroarraySet dataSet = null;
	
	private int numGenes, numExps;
	private double alpha;
	private boolean isPermut, useWelchDf;
	private int numCombs = 0;
	private boolean useAllCombs =  false;
	
	private int numberGroupA = 0;
	private int numberGroupB = 0;
	private int m;

	private boolean isLogNormalized = false;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	public TTestAnalysisWeb(DSMicroarraySet dataSet,
			HashMap<Serializable, Serializable> params) {
		this.params = params;
		this.dataSet = dataSet;
		
		
		if(((String) params.get(TTestParameters.LOGNORMALIZED)).equalsIgnoreCase("yes")) {
			isLogNormalized = true;
		}
		
		numCombs	= 	0;
		if((Boolean) params.get(TTestParameters.ISPERMUT)) {
			isPermut	= 	true;
			if((Boolean) params.get(TTestParameters.ALLCOMBINATATIONS)) {
				useAllCombs = true;
			}else {
				numCombs = Integer.parseInt((String) params.get(TTestParameters.NUMCOMBINATIONS));
			}
		}
		alpha 		= 	Double.parseDouble((String) params.get(TTestParameters.ALPHA));
		
		if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("Just alpha (no-correction)")) {
			m = SignificanceMethod.JUST_ALPHA;
		} else if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("Adjusted Bonferroni Correction")) {
			m = SignificanceMethod.ADJ_BONFERRONI;
		} else if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("Standard Bonferroni Correction")) {
			m = SignificanceMethod.STD_BONFERRONI;
		} else if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("minP")) {
			m = SignificanceMethod.MIN_P;
		}else if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("maxT")) {
			m = SignificanceMethod.MAX_T;
		}
		
		if(((String) params.get(TTestParameters.WELCHDIFF)).equals("Unequal (Welch approximation)") ) {
			useWelchDf = true;
		}else {
			useWelchDf = false;
		}
	}

	public DSSignificanceResultSet<DSGeneMarker> execute() throws IOException {
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView =  new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
		
		String[] selectedCaseSets 		= 	(String[]) params.get(TTestParameters.CASEARRAY);
		String[] selectedControlSets 	= 	(String[]) params.get(TTestParameters.CONTROLARRAY);
		List<String> caseArrayPositions = new ArrayList<String>();
		List<String> controlArrayPositions = new ArrayList<String>();
		for (int i = 0; i < selectedCaseSets.length; i++) {			
			 
			 ArrayList<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedCaseSets[i].trim()));			 
			 for (int j = 0; j < arrays.size(); j++)  			 
				caseArrayPositions.add(arrays.get(j));				 
			 
		}
		for (int i = 0; i < selectedControlSets.length; i++) {			
			 
			 ArrayList<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedControlSets[i].trim()));			 
			 for (int j = 0; j < arrays.size(); j++)  			 
				 controlArrayPositions.add(arrays.get(j));				 
			 
		}	
	 
		numberGroupA 		= 	caseArrayPositions.size();
		numberGroupB 		= 	controlArrayPositions.size();
		
		List<String> aC = new ArrayList<String>();
		List<String> bC = new ArrayList<String>();
		
		aC.addAll(caseArrayPositions);
		bC.addAll(controlArrayPositions);
		
		aC.addAll(bC);
		
		DSPanel<DSGeneMarker> markerPanel = new CSPanel<DSGeneMarker>();
		for (int i=0; i<dataSet.getMarkers().size(); i++) {
			markerPanel.add(dataSet.getMarkers().get(i));
		} 
		dataSetView.setMarkerPanel(markerPanel);
		
		DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>();
		for (int i=0; i<aC.size(); i++) {
				DSMicroarray micraorray = dataSet.get(aC.get(i));
				panel.add(micraorray);
		} 
		dataSetView.setItemPanel(panel);
		
		final int n = dataSet.size();
		int count = 0;
		for(int i=0; i<n; i++) {			
			for(int j=0; j<aC.size(); j++) {
				if(!aC.contains(dataSet.get(i-count).getLabel())) {
					dataSet.remove(i-count);
					count++;
					break;
				} 
			}
		}

		numExps 	= 	dataSet.size();
		numGenes 	= 	dataSet.getMarkers().size();
		
		double[][] caseArray  	= 	new double[numGenes][numberGroupA];
		double[][] controlArray = 	new double[numGenes][numberGroupB];
		
		
		
		
		String[] caseLabels 	= 	new String[caseArrayPositions.size()];
		String[] controlLabels 	= 	new String[controlArrayPositions.size()];
	 
		for (int i = 0; i < numGenes; i++) {	

			int caseIndex 		=	0;
			int controlIndex 	= 	0;

			for(int j=0; j<numExps; j++) {
				for(int r=0; r<caseArrayPositions.size(); r++){
					if(caseArrayPositions.get(r).trim().equalsIgnoreCase(dataSet.get(j).getLabel())) {
						caseArray[i][caseIndex] = dataSet.getValue(i, j);
						caseIndex++;
					}
				}
				for(int r=0; r<controlArrayPositions.size(); r++){	
					if(controlArrayPositions.get(r).trim().equalsIgnoreCase(dataSet.get(j).getLabel())) {
						controlArray[i][controlIndex] = dataSet.getValue(i, j);
						controlIndex++;
					}
				}
			}
		}
		
		for(int i=0; i<caseArrayPositions.size(); i++) {
			caseLabels[i] = caseArrayPositions.get(i);
		}
		
		for(int i=0; i<controlArrayPositions.size(); i++) {
			controlLabels[i] = controlArrayPositions.get(i);
		}
		TTestInput tTestInput = new TTestInput(numGenes, numberGroupA,
				numberGroupB, caseArray, controlArray, m, alpha, isPermut,
				useWelchDf, useAllCombs, numCombs, isLogNormalized);
		
		TTestOutput ttestOutput = computeTtestRemote(tTestInput);
		if(ttestOutput==null) {
			throw new IOException("t-test failed to get result from URL "+url);
		}
		
		DSSignificanceResultSet<DSGeneMarker> sigSet = createDSSignificanceResultSet(dataSetView, ttestOutput, caseLabels, controlLabels);
		return sigSet;
	}
	
	private TTestOutput computeTtestRemote(TTestInput input) {
		TTestOutput output = null;

		try {       
			
			getWebServiceUrl();
			log.debug("url is "+url);
			
			RPCServiceClient serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();

			
			long soTimeout = 2 * 24 * 60 * 60 * 1000; // 2 days
			options.setTimeOutInMilliSeconds(soTimeout);

			EndpointReference targetEPR = new EndpointReference(url);
					 
			options.setTo(targetEPR);

			// notice that that namespace is in the required form
			QName opName = new QName(
					"http://service.ttest.components.geworkbench.org",
					"execute");
			Object[] args = new Object[] { input };

			Class<?>[] returnType = new Class[] { TTestOutput.class };

			log.debug("... before invokeBlocking "+System.currentTimeMillis());
			Object[] response = serviceClient.invokeBlocking(opName, args,
					returnType);
			log.debug("... after invokeBlocking "+System.currentTimeMillis());
			output = (TTestOutput) response[0];

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
	
	private DSSignificanceResultSet<DSGeneMarker> createDSSignificanceResultSet(
			DSMicroarraySetView<DSGeneMarker,DSMicroarray> dataSetView,
			TTestOutput output, String[] caseLabels, String[] controlLabels) {
		
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
				dataSetView.getMicroarraySet(), "T-Test", caseLabels, controlLabels,
				alpha, isLogNormalized
		);
	
		if (output.getSignificanceIndex() != null)
		{
			for (int i = 0; i < output.getSignificanceIndex().length; i++) {
			
				int index = (output.getSignificanceIndex())[i];
			    DSGeneMarker m = dataSetView.markers().get(index);
			    sigSet.setSignificance(m, (output.getpValue())[index]);
			    sigSet.setTValue(m, (output.gettValue())[index]);
			
			    sigSet.setFoldChange(m, (output.getFoldChange())[index]);
		    }
		    sigSet.sortMarkersBySignificance();
		
		}
		
		return sigSet;
	}
	
	/**
	 * Provides webservice URL
	 */
	private void getWebServiceUrl() {
		if (url == null || url.trim().equals("")) {
			url  = GeworkbenchRoot.getAppProperty(TTEST_WEBSERVICE_URL);
				if (url == null || url.trim().equals(""))
					url = DEFAULT_WEB_SERVICES_URL;
		}		
	}
}
