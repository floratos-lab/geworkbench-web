package org.geworkbenchweb.analysis.anova;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
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
import org.geworkbench.components.anova.Anova;
import org.geworkbench.components.anova.AnovaException;
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

	public AnovaAnalysis(DSMicroarraySet dataSet, UAnovaParamForm paramForm) {
		this.dataSet = dataSet;
		this.paramForm = paramForm;
	}

	public void execute() {

		String GroupAndChipsString = "";
		DSItemList<DSGeneMarker> selectedMarkers = null;		 

		long parentSetId =  DataSetOperations.getDataSetID(dataSet.getDataSetName());
		String[] selectedMarkerSet = paramForm.getSelectedMarkerSet();

		if (selectedMarkerSet == null)
			selectedMarkers = dataSet.getMarkers();
		else {
			selectedMarkers = new CSItemList<DSGeneMarker>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				String markers = getMarkerData(selectedMarkerSet[i], parentSetId);
				String[] temp = (markers.substring(1, markers.length() - 1))
						.split(",");
				for (int j = 0; j < temp.length; j++)
					selectedMarkers.add(dataSet.getMarkers().get(
							Integer.parseInt(temp[j].trim())));

			}
		}

		String[] selectedArraySet = paramForm.getSelectedArraySet();	 
 
		int selectedMarkersNum = selectedMarkers.size();
		int globleArrayIndex = 0;
		int numSelectedGroups = selectedArraySet.length;

		GroupAndChipsString += numSelectedGroups + " groups analyzed:\n";
 
		/* for each group */
		for (int i = 0; i < numSelectedGroups; i++) {			 
			String arrayPositions = getArrayData(selectedArraySet[i], parentSetId);
			String[] temp = (arrayPositions.substring(1,
					arrayPositions.length() - 1)).split(",");

			String groupLabel = selectedArraySet[i];
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

				GroupAndChipsString += "\t\t" + dataSet.get(Integer.parseInt(temp[j].trim())) + "\n";

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
		
		log.debug(selectedMarkers.size());
		for (int i = 0; i < numSelectedGroups; i++) {
			String arrayPositions = getArrayData(selectedArraySet[i], parentSetId);
			String[] temp = (arrayPositions.substring(1,
					arrayPositions.length() - 1)).split(",");

			/*
			 * for each array in this group
			 */
			for (int j = 0; j < temp.length; j++) {
				/* for each marker in this array */			
				for (int k = 0; k < selectedMarkersNum; k++) {				 
					A[k][globleArrayIndex] = (float) dataSet.get(Integer.parseInt(temp[j].trim()))
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

		final Anova anova = new Anova(anovaInput);

		/* Create panels and significant result sets to store results */
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
				dataSet, "Anova Analysis", new String[0], selectedArraySet, paramForm.getPValThreshold());
		
		CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;
       
		try {

			AnovaOutput output = anova.execute();

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
					"Anova Analysis Result Set", selectedArraySet,
					significantMarkerNames, output.getResult2DArray());
			log.debug(significantMarkerNames.length
					+ " Markers added to anovaResultSet.");
			anovaResultSet.getSignificantMarkers().addAll(
					sigSet.getSignificantMarkers());
			log.debug(sigSet.getSignificantMarkers().size()
					+ " Markers added to anovaResultSet.getSignificantMarkers().");
			anovaResultSet.sortMarkersBySignificance();

			storeResultSet(anovaResultSet);

		} catch (AnovaException e) {

			e.printStackTrace();

		}

	}

	/**
	 * Create Marker Data for selected markerSet
	 */
	public String getMarkerData(String setName, long parentSetId) {

		@SuppressWarnings("rawtypes")
		List subSet = SubSetOperations.getMarkerSet(setName,parentSetId);			 
		String positions = (((SubSet) subSet.get(0)).getPositions()).trim();

		return positions;
	}

	/**
	 * Create Array Data for selected markerSet
	 */
	public String getArrayData(String setName, long parentSetId) {
 
		 
		log.debug(setName);
		
		@SuppressWarnings("rawtypes")
		
		
		List subSet = SubSetOperations.getArraySet(setName.trim(),	parentSetId);
		
		String positions = (((SubSet) subSet.get(0)).getPositions()).trim();

		return positions;
	}

	public ResultSet storeResultSet(
			CSAnovaResultSet<DSGeneMarker> anovaResultSet) {

		ResultSet resultSet = new ResultSet();
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName = "Anova - " + new java.util.Date();
		resultSet.setName(dataSetName);
		resultSet.setType("Anova");
		resultSet.setParent(DataSetOperations.getDataSetID(dataSet.getDataSetName()));
		resultSet.setOwner(user.getId());
		resultSet.setData(ObjectConversion.convertToByte(anovaResultSet));
		FacadeFactory.getFacade().store(resultSet);

		NodeAddEvent resultEvent = new NodeAddEvent(resultSet.getId(), dataSetName, "Result Node");
		GeworkbenchRoot.getBlackboard().fire(resultEvent);

		return resultSet;
	}

}
