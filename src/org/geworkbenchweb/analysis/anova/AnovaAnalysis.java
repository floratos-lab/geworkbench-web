package org.geworkbenchweb.analysis.anova;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.geworkbenchweb.analysis.anova.ui.UAnovaParamForm;

import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;

import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;

/**
 * 
 * This class submits Anova Analysis from web application
 * 
 * @author Min You
 * 
 */
public class AnovaAnalysis {

	private static Log log = LogFactory.getLog(AnovaAnalysis.class);
	private User user = SessionHandler.get();
	private DSMicroarraySet dataSet = null;
	private UAnovaParamForm paramForm = null;
	private DSItemList<DSGeneMarker> selectedMarkers = null;
	private String[] selectedArraySetNames = null;
	private Long dataSetId;

	public AnovaAnalysis(DSMicroarraySet dataSet, UAnovaParamForm paramForm,
			Long dataSetId) {
		this.dataSet = dataSet;
		this.paramForm = paramForm;
		this.dataSetId = dataSetId;
	}

	public void execute() {

		String[] selectedMarkerSet = null;
		String[] selectedArraySet = null;

		@SuppressWarnings("unused")
		String GroupAndChipsString = "";

		selectedMarkerSet = paramForm.getSelectedMarkerSet();

		if (selectedMarkerSet == null)
			selectedMarkers = dataSet.getMarkers();
		else {
			selectedMarkers = new CSItemList<DSGeneMarker>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				String markers = getMarkerData(Long
						.parseLong(selectedMarkerSet[i].trim()));
				String[] temp = (markers.substring(1, markers.length() - 1))
						.split(",");
				for (int j = 0; j < temp.length; j++)
					selectedMarkers.add(dataSet.getMarkers().get(
							Integer.parseInt(temp[j].trim())));

			}
		}

		selectedArraySet = paramForm.getSelectedArraySet();
		selectedArraySetNames = paramForm.getSelectedArraySetNames();

		int selectedMarkersNum = selectedMarkers.size();
		int globleArrayIndex = 0;
		int numSelectedGroups = selectedArraySet.length;

		GroupAndChipsString += numSelectedGroups + " groups analyzed:\n";

		/* for each group */
		for (int i = 0; i < numSelectedGroups; i++) {
			String arrayPositions = getArrayData(Long
					.parseLong(selectedArraySet[i].trim()));
			String[] temp = (arrayPositions.substring(1,
					arrayPositions.length() - 1)).split(",");

			String groupLabel = selectedArraySetNames[i];
			/* put group label into history */
			GroupAndChipsString += "\tGroup " + groupLabel + " (" + temp.length
					+ " chips)" + ":\n";

			/*
			 * for each array in this group
			 */
			for (int j = 0; j < temp.length; j++) {
				/*
				 * put member of each group into history
				 */

				GroupAndChipsString += "\t\t"
						+ dataSet.get(Integer.parseInt(temp[j].trim())) + "\n";

				/*
				 * count total arrays in selected groups.
				 */
				globleArrayIndex++;
			}

		}

		int[] groupAssignments = new int[globleArrayIndex];
		float[][] A = new float[selectedMarkersNum][globleArrayIndex];

		globleArrayIndex = 0;
		/* for each groups */

		log.debug("selectedMarkers.size() = " + selectedMarkers.size());
		for (int i = 0; i < numSelectedGroups; i++) {
			String arrayPositions = getArrayData(Long
					.parseLong(selectedArraySet[i].trim()));
			String[] temp = (arrayPositions.substring(1,
					arrayPositions.length() - 1)).split(",");

			/*
			 * for each array in this group
			 */
			for (int j = 0; j < temp.length; j++) {
				/* for each marker in this array */
				for (int k = 0; k < selectedMarkersNum; k++) {
					A[k][globleArrayIndex] = (float) dataSet
							.get(Integer.parseInt(temp[j].trim()))
							.getMarkerValue(selectedMarkers.get(k)).getValue();

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

		ResultSet resultSet = storePendingResultSet();

		AnovaThread anovaThread = new AnovaThread(anovaInput, resultSet);
		anovaThread.start();

	}

	private AnovaOutput computeAnova(AnovaInput input) {
		AnovaOutput output = null;
		RPCServiceClient serviceClient;

		try {
			serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();

			EndpointReference targetEPR = new EndpointReference(
					"http://localhost:8080/axis2/services/AnovaService");
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
			log.debug(output.toString());
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

	/**
	 * Create Marker Data for selected markerSet
	 */
	public String getMarkerData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getMarkerSet(setNameId);
		String positions = (((SubSet) subSet.get(0)).getPositions()).trim();
		return positions;
	}

	/**
	 * Create Array Data for selected markerSet
	 */
	public String getArrayData(long setNameId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getArraySet(setNameId);

		String positions = (((SubSet) subSet.get(0)).getPositions()).trim();

		return positions;
	}

	public ResultSet storePendingResultSet() {

		ResultSet resultSet = new ResultSet();
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName = "Anova - " + new java.util.Date();
		resultSet.setName(dataSetName);
		resultSet.setType("Anova-pending");
		resultSet.setParent(dataSetId);
		resultSet.setOwner(user.getId());
		FacadeFactory.getFacade().store(resultSet);

		NodeAddEvent resultEvent = new NodeAddEvent(resultSet.getId(),
				dataSetName, "Result Node");
		GeworkbenchRoot.getBlackboard().fire(resultEvent);

		return resultSet;
	}

	public ResultSet storeResultSet(ResultSet resultSet,
			CSAnovaResultSet<DSGeneMarker> anovaResultSet) {

		resultSet.setType("Anova");
		resultSet.setData(ObjectConversion.convertToByte(anovaResultSet));
		FacadeFactory.getFacade().store(resultSet);

		return resultSet;
	}

	private class AnovaThread extends Thread {

		AnovaInput input = null;
		ResultSet resultSet = null;

		public AnovaThread(AnovaInput input, ResultSet resultSet) {
			this.input = input;
			this.resultSet = resultSet;
		}

		public void run() {

			try {
				sleep(5000); // Sleep for 50 milliseconds
			} catch (InterruptedException ie) {

			}

			AnovaOutput output = computeAnova(input);

			/* Create panels and significant result sets to store results */
			DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
					dataSet, "Anova Analysis", new String[0],
					selectedArraySetNames, paramForm.getPValThreshold());

			CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;

			int[] featuresIndexes = output.getFeaturesIndexes();
			double[] significances = output.getSignificances();
			String[] significantMarkerNames = new String[featuresIndexes.length];

			for (int i = 0; i < featuresIndexes.length; i++) {
				DSGeneMarker item = selectedMarkers.get(featuresIndexes[i]);
				log.debug("SignificantMarker: " + item.getLabel()
						+ ", with apFM: " + significances[i]);

				sigSet.setSignificance(item, significances[i]);
				significantMarkerNames[i] = item.getLabel();
			}

			DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					dataSet);

			anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(dataView,
					"Anova Analysis Result Set", selectedArraySetNames,
					significantMarkerNames, output.getResult2DArray());
			log.debug(significantMarkerNames.length
					+ " Markers added to anovaResultSet.");
			anovaResultSet.getSignificantMarkers().addAll(
					sigSet.getSignificantMarkers());
			log.debug(sigSet.getSignificantMarkers().size()
					+ " Markers added to anovaResultSet.getSignificantMarkers().");
			anovaResultSet.sortMarkersBySignificance();

			storeResultSet(resultSet, anovaResultSet);
		}
	}

}
