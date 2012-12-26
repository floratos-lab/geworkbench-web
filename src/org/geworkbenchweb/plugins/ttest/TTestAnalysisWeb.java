package org.geworkbenchweb.plugins.ttest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.SubSet;
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
import org.geworkbench.components.ttest.TTestException;
import org.geworkbench.components.ttest.data.TTestInput;
import org.geworkbench.components.ttest.data.TTestOutput;

/**
 * 
 * This class submits TTest Analysis from web application
 * 
 * @author Nikhil Reddy
 * 
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
	private int numCombs;
	private boolean useAllCombs;
	
	private int numberGroupA = 0;
	private int numberGroupB = 0;
	private SignificanceMethod m;

	private boolean isLogNormalized = false;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	public TTestAnalysisWeb(DSMicroarraySet dataSet,
			HashMap<Serializable, Serializable> params) {
		this.params = params;
		this.dataSet = dataSet;
		
		if(((String) params.get(TTestParameters.LOGNORMALIZED)).equalsIgnoreCase("yes")) {
			isLogNormalized = true;
		}
		
		useAllCombs = 	false;
		numCombs	= 	0;
		isPermut	= 	false;
		alpha 		= 	Double.parseDouble((String) params.get(TTestParameters.ALPHA));
		
		if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("Just alpha (no-correction)")) {
			m = SignificanceMethod.JUST_ALPHA;
		} else if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("Adjusted Bonferroni Correction")) {
			m = SignificanceMethod.ADJ_BONFERRONI;
		} else if(((String) params.get(TTestParameters.CORRECTIONMETHOD)).equals("Standard Bonferroni Correction")) {
			m = SignificanceMethod.STD_BONFERRONI;
		}
		
		if(((String) params.get(TTestParameters.WELCHDIFF)).equals("Unequal (Welch approximation)") ) {
			useWelchDf = true;
		}else {
			useWelchDf = false;
		}
	}

	public DSSignificanceResultSet<DSGeneMarker> execute() {
		
		Long caseSetId 		= 	(Long) params.get(TTestParameters.CASEARRAY);
		Long controlSetId 	= 	(Long) params.get(TTestParameters.CONTROLARRAY);
		
		SubSet caseSet 		= 	((SubSet) (SubSetOperations.getArraySet(caseSetId)).get(0));
		SubSet controlSet 	= 	((SubSet) (SubSetOperations.getArraySet(controlSetId)).get(0));
		
		numberGroupA 		= 	caseSet.getPositions().size();
		numberGroupB 		= 	controlSet.getPositions().size();
		
		ArrayList<String> aC = new ArrayList<String>();
		ArrayList<String> bC = new ArrayList<String>();
		
		aC = caseSet.getPositions();
		bC = controlSet.getPositions();
		
		aC.addAll(bC);
		
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
		
		String[] caseLabels 	= 	new String[caseSet.getPositions().size()];
		String[] controlLabels 	= 	new String[controlSet.getPositions().size()];
		
		ArrayList<String> caseSet1 		= 	((SubSet) (SubSetOperations.getArraySet(caseSetId)).get(0)).getPositions();
		ArrayList<String> controlSet1 	= 	((SubSet) (SubSetOperations.getArraySet(controlSetId)).get(0)).getPositions();
		
		for (int i = 0; i < numGenes; i++) {	

			int caseIndex 		=	0;
			int controlIndex 	= 	0;

			for(int j=0; j<numExps; j++) {
				for(int r=0; r<caseSet1.size(); r++){
					if(caseSet1.get(r).trim().equalsIgnoreCase(dataSet.get(j).getLabel())) {
						caseArray[i][caseIndex] = dataSet.getValue(i, j);
						caseIndex++;
					}
				}
				for(int r=0; r<controlSet1.size(); r++){	
					if(controlSet1.get(r).trim().equalsIgnoreCase(dataSet.get(j).getLabel())) {
						controlArray[i][controlIndex] = dataSet.getValue(i, j);
						controlIndex++;
					}
				}
			}
		}
		
		for(int i=0; i<caseSet.getPositions().size(); i++) {
			caseLabels[i] = caseSet.getPositions().get(i);
		}
		
		for(int i=0; i<controlSet.getPositions().size(); i++) {
			controlLabels[i] = controlSet.getPositions().get(i);
		}
		TTestInput tTestInput = new TTestInput(numGenes, numberGroupA,
				numberGroupB, caseArray, controlArray, m, alpha, isPermut,
				useWelchDf, useAllCombs, numCombs, isLogNormalized);
		
		TTestOutput ttestOutput = null;
		try {
			ttestOutput = new org.geworkbench.components.ttest.TTest(tTestInput).execute();
		} catch (TTestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DSSignificanceResultSet<DSGeneMarker> sigSet = createDSSignificanceResultSet(dataSet, ttestOutput, caseLabels, controlLabels);
		return sigSet;
	}
	
	@SuppressWarnings("unused")
	private TTestOutput computeTtestRemote(TTestInput input) {
		TTestOutput output = null;
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
					"http://service.ttest.components.geworkbench.org",
					"execute");
			Object[] args = new Object[] { input };

			Class<?>[] returnType = new Class[] { TTestOutput.class };

			Object[] response = serviceClient.invokeBlocking(opName, args,
					returnType);
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
			DSMicroarraySet data,
			TTestOutput output, String[] caseLabels, String[] controlLabels) {
		
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
				data, "T-Test", caseLabels, controlLabels,
				alpha, isLogNormalized
		);
		
		for (int i = 0; i < output.significanceIndex.length; i++) {
			int index = output.significanceIndex[i];
			DSGeneMarker m = data.getMarkers().get(index);
			sigSet.setSignificance(m, output.pValue[index]);
			sigSet.setTValue(m, output.tValue[index]);
			
			sigSet.setFoldChange(m, output.foldChange[index]);
		}
		
		sigSet.sortMarkersBySignificance();
		
		return sigSet;
	}
	
	/**
	 * Provides webservice URL
	 */
	private void getWebServiceUrl() {
		if (url == null || url.trim().equals("")) {
			url  = GeworkbenchRoot.getAppProperties().getProperty(TTEST_WEBSERVICE_URL);
				if (url == null || url.trim().equals(""))
					url = DEFAULT_WEB_SERVICES_URL;
		}		
	}
}
