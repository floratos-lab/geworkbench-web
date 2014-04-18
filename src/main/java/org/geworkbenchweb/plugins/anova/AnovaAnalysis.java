package org.geworkbenchweb.plugins.anova;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.anova.Anova;
import org.geworkbench.components.anova.AnovaException;
import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.pojos.AnovaResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;

 
/**
 * 
 * This class submits Anova Analysis from web application
 * 
 * @author Min You
 * 
 */
public class AnovaAnalysis {

	private static Log log = LogFactory.getLog(AnovaAnalysis.class);
	
	private static final String DEFAULT_WEB_SERVICES_URL = "http://afdev.c2b2.columbia.edu:9090/axis2/services/AnovaService";
	private static final String  ANOVA_WEBSERVICE_URL = "anova.webService.url";
	 
	private static String url = null;
	
	private Long dataSetId = null;
	private AnovaUI paramForm = null;
	private List<String> selectedMarkers = null;
	private String[] selectedArraySetNames = null;
	
	public AnovaAnalysis(Long dataSetId, AnovaUI paramForm) {
		this.dataSetId = dataSetId;
		this.paramForm = paramForm;
	}

	public AnovaResult execute() throws RemoteException {

		AnovaInput anovaInput = getAnovaInput();
		AnovaOutput output = computeAnovaRemote(anovaInput);

		int[] featuresIndexes = output.getFeaturesIndexes();
		List<String> significantMarkerNames = new ArrayList<String>();

		for (int i = 0; i < featuresIndexes.length; i++) {
			String item = selectedMarkers.get(featuresIndexes[i]);
			significantMarkerNames.add(item);
		}

		AnovaResult anovaResultSet = new AnovaResult(output, selectedArraySetNames);

		if (significantMarkerNames.size() > 0)
		{
			java.util.Collections.sort(significantMarkerNames);
			SubSetOperations.storeSignificance(significantMarkerNames, paramForm.getDataSetId(), paramForm.getUserId());
		}		
		return anovaResultSet;
	}

	private AnovaOutput computeAnovaRemote(AnovaInput input)
			throws RemoteException {
		AnovaOutput output = null;
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
					"http://service.anova.components.geworkbench.org",
					"execute");
			Object[] args = new Object[] { input };

			Class<?>[] returnType = new Class[] { AnovaOutput.class };

			Object[] response = serviceClient.invokeBlocking(opName, args,
					returnType);
			output = (AnovaOutput) response[0];

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
			
			throw new RemoteException( "Anova AxisFault:" + e.getMessage() + " fault action: " + e.getFaultAction()
					+ " reason: " + e.getReason());
			
			 
		}
		catch (Exception e) {
			throw new RemoteException( "Coumpute Anova error:" + e.getMessage());
		}

		 
	}

	private AnovaInput getAnovaInput() {
		String[] selectedMarkerSet = null;
		String[] selectedArraySet = null;

		@SuppressWarnings("unused")
		String GroupAndChipsString = "";

		selectedMarkerSet = paramForm.getSelectedMarkerSet();

		MicroarraySet microarrays = DataSetOperations.getMicroarraySet(dataSetId);
		if (selectedMarkerSet == null) {
			selectedMarkers = Arrays.asList( microarrays.markerLabels );
		} else {
			selectedMarkers = new ArrayList<String>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<String> temp = SubSetOperations.getMarkerData(Long.parseLong(selectedMarkerSet[i].trim()));
				for(int m=0; m<temp.size(); m++) {
					String temp1 = ((temp.get(m)).split("\\s+"))[0].trim();
					selectedMarkers.add(temp1);
				}
				 
			} 
		}

		int[] selectedMarkerIndex = new int[selectedMarkers.size()];
		int index = 0;
		for(int i=0; i<microarrays.markerNumber; i++) {
			if(selectedMarkers.contains( microarrays.markerLabels[i]) ) {
				selectedMarkerIndex[index] = i;
				index++;
			}
		}

		selectedArraySet = paramForm.getSelectedArraySet();
		selectedArraySetNames = paramForm.getSelectedArraySetNames();

		int selectedMarkersNum = selectedMarkers.size();
		int globleArrayIndex = 0;
		int numSelectedGroups = selectedArraySet.length;

		GroupAndChipsString += numSelectedGroups + " groups analyzed:\n";

		globleArrayIndex = paramForm.getTotalSelectedArrayNum();
		int[] groupAssignments = new int[globleArrayIndex];
		float[][] A = new float[selectedMarkersNum][globleArrayIndex];

		globleArrayIndex = 0;
		/* for each groups */

		log.debug("selectedMarkers.size() = " + selectedMarkers.size());
		for (int i = 0; i < numSelectedGroups; i++) {
			List<String> arrayPositions = SubSetOperations.getArrayData(Long
					.parseLong(selectedArraySet[i].trim()));
			
			int[] selectedArrayIndex = new int[arrayPositions.size()];
			int arrayIndex = 0;
			for(int x=0; x<microarrays.arrayNumber; x++) {
				if(arrayPositions.contains( microarrays.arrayLabels[x]) ) {
					selectedArrayIndex[arrayIndex] = x;
					arrayIndex++;
				}
			}

			String groupLabel = selectedArraySetNames[i];
			/* put group label into history */
			GroupAndChipsString += "\tGroup " + groupLabel + " (" + arrayPositions.size()
					+ " chips)" + ":\n";

			/*
			 * for each array in this group
			 */
			for (int j = 0; j < arrayPositions.size(); j++) {
				GroupAndChipsString += "\t\t"
						+ arrayPositions.get(j) + "\n";
				int aIndex = selectedArrayIndex[j];
				/* for each marker in this array */
				for (int k = 0; k < selectedMarkersNum; k++) {
					int mIndex = selectedMarkerIndex[k];
					A[k][globleArrayIndex] = microarrays.values[mIndex][aIndex];

				}
				groupAssignments[globleArrayIndex] = i + 1;
				globleArrayIndex++;
			}
		}

		AnovaInput anovaInput = new AnovaInput(A, groupAssignments,
				selectedMarkersNum, numSelectedGroups,
				paramForm.getPValThreshold(), paramForm.getPValueEstimation(),
				paramForm.getPermNumber(),
				paramForm.getFalseDiscoveryRateControl(),
				paramForm.getFalseSignificantGenesLimit());

		return anovaInput;
	}

	@SuppressWarnings("unused")
	private AnovaOutput computeAnovaLocal(AnovaInput input) {
		AnovaOutput output = null;
		try {
			Anova anova = new Anova(input);
			output = anova.execute();

		} catch (AnovaException AE) {

			log.debug(AE.getMessage());
		}
		return output;
	}
	
	 
	
	private void getWebServiceUrl()
	{
		if (url == null || url.trim().equals(""))
		{
			 		
				url  = GeworkbenchRoot.getAppProperty(ANOVA_WEBSERVICE_URL);
				if (url == null || url.trim().equals(""))
					url = DEFAULT_WEB_SERVICES_URL;
				
		}		
		 
	}
	
}
