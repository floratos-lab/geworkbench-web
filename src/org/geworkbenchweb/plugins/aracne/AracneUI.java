package org.geworkbenchweb.plugins.aracne;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;

public class AracneUI extends GridLayout implements AnalysisUI {

	private static final long serialVersionUID = 1L;

	private Long dataSetId;

	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private ListSelect markerSetSelect = new ListSelect("Select Marker Sets:");;
	private ListSelect arraySetSelect = new ListSelect("Select Array Sets:");;
	private ComboBox hubGeneMarkerSetBox = new ComboBox();
	private ComboBox modeBox = new ComboBox();
	private ComboBox algoBox = new ComboBox();
	private ComboBox kernelWidth = new ComboBox();
	private ComboBox thresholdType = new ComboBox();
	private ComboBox dpiTolerance = new ComboBox();
	private ComboBox dpiTargetList = new ComboBox();
	private TextField widthValue = new TextField();
	private TextField threshold = new TextField();
	private ComboBox correction = new ComboBox();
	private TextField tolerance = new TextField();
	private ComboBox dpiTargetSetBox = new ComboBox();
	private TextField bootStrapNumber = new TextField();
	private TextField consensusThreshold = new TextField();
	private ComboBox mergeProbeSets = new ComboBox();
	private Button submitButton = null;

	public AracneUI(Long dataId) {

		this.dataSetId = dataId;

		setColumns(4);
		setRows(11);
		setSpacing(true);
		setImmediate(true);

		/**
		 * Params default values
		 */
		params.put(AracneParameters.MARKER_SET, "All Markers");
		params.put(AracneParameters.ARRAY_SET, "All Arrays");
		params.put(AracneParameters.HUB_MARKER_SET, "All vs. All");
		params.put(AracneParameters.MODE, "Complete");
		params.put(AracneParameters.ALGORITHM, "Adaptive Partitioning");
		params.put(AracneParameters.KERNEL_WIDTH, "Inferred");
		params.put(AracneParameters.WIDTH_VALUE, "0.01");
		params.put(AracneParameters.TOL_TYPE, "Apply");
		params.put(AracneParameters.TOL_VALUE, "0.1");
		params.put(AracneParameters.T_TYPE, "Mutual Info");
		params.put(AracneParameters.T_VALUE, "0.01");
		params.put(AracneParameters.CORRECTION, "No Correction");
		params.put(AracneParameters.DPI_LIST, "Do Not Apply");
		params.put(AracneParameters.BOOTS_NUM, "1");
		params.put(AracneParameters.CONSENSUS_THRESHOLD, "1.e-6");
		params.put(AracneParameters.MERGEPS, "No");

		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(15);
		markerSetSelect.setImmediate(true);

		markerSetSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.MARKER_SET, String
						.valueOf(valueChangeEvent.getProperty().getValue()));
			}
		});

		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(15);
		arraySetSelect.setImmediate(true);

		arraySetSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.ARRAY_SET, String
						.valueOf(valueChangeEvent.getProperty().getValue()));
			}
		});

		hubGeneMarkerSetBox.setCaption("Hub Marker(s) From Sets");
		hubGeneMarkerSetBox.setNullSelectionAllowed(false);
		hubGeneMarkerSetBox.setInputPrompt("Select Marker Set");
		hubGeneMarkerSetBox.setImmediate(true);


		hubGeneMarkerSetBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.HUB_MARKER_SET, String
						.valueOf(valueChangeEvent.getProperty().getValue()));
			}
		});

		modeBox.setCaption("Select Mode");
		modeBox.setNullSelectionAllowed(false);
		modeBox.setImmediate(true);
		modeBox.addItem("Complete");
		modeBox.addItem("Discovery");
		modeBox.addItem("Preprocessing");
		modeBox.select("Complete");
		modeBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.MODE);
				params.put(AracneParameters.MODE, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		algoBox.setCaption("Select Algorithm");
		algoBox.setImmediate(true);
		algoBox.setNullSelectionAllowed(false);
		algoBox.addItem("Adaptive Partitioning");
		algoBox.addItem("Fixed Bandwidth");
		algoBox.select("Adaptive Partitioning");
		algoBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase("Fixed Bandwidth")) {
					kernelWidth.setEnabled(true);
					widthValue.setEnabled(true);
				} else if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase("Adaptive Partitioning")) {
					kernelWidth.setEnabled(false);
					widthValue.setEnabled(false);
				}
				params.remove(AracneParameters.ALGORITHM);
				params.put(AracneParameters.ALGORITHM, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		widthValue.setCaption(" ");
		widthValue.setValue("0.1");
		widthValue.setEnabled(false);
		widthValue.setNullSettingAllowed(false);
		widthValue.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.WIDTH_VALUE);
				params.put(AracneParameters.WIDTH_VALUE, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		kernelWidth.setCaption("Kernel Width");
		kernelWidth.setImmediate(true);
		kernelWidth.setNullSelectionAllowed(false);
		kernelWidth.addItem("Inferred");
		kernelWidth.addItem("Specify");
		kernelWidth.select("Inferred");
		kernelWidth.setEnabled(false);
		kernelWidth.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase("Specify")) {
					widthValue.setEnabled(true);
				} else {
					widthValue.setEnabled(false);
				}
				params.remove(AracneParameters.KERNEL_WIDTH);
				params.put(AracneParameters.KERNEL_WIDTH, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		correction.setCaption(" ");
		correction.setNullSelectionAllowed(false);
		correction.addItem("No Correction");
		correction.addItem("Bonferroni Correction");
		correction.select("No Correction");
		correction.setEnabled(true);
		correction.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.CORRECTION);
				params.put(AracneParameters.CORRECTION, valueChangeEvent
						.getProperty().getValue().toString());
			}

		});

		threshold.setCaption(" ");
		threshold.setValue("0.01");
		threshold.setNullSettingAllowed(false);
		threshold.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.T_VALUE);
				params.put(AracneParameters.T_VALUE, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		thresholdType.setCaption("Threshold Type");
		thresholdType.setImmediate(true);
		thresholdType.setNullSelectionAllowed(false);
		thresholdType.addItem("P-Value");
		thresholdType.addItem("Mutual Info");
		thresholdType.select("P-Value");
		thresholdType.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase("P-Value")) {
					correction.setEnabled(true);
				} else {
					correction.setEnabled(false);
				}
				params.put(AracneParameters.T_TYPE, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		tolerance.setCaption(" ");
		tolerance.setValue("0.1");
		tolerance.setNullSettingAllowed(false);
		tolerance.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.TOL_VALUE, valueChangeEvent
						.getProperty().getValue().toString());
			}

		});

		dpiTolerance.setCaption("DPI Tolerance");
		dpiTolerance.setImmediate(true);
		dpiTolerance.setNullSelectionAllowed(false);
		dpiTolerance.addItem("Apply");
		dpiTolerance.addItem("Do Not Apply");
		dpiTolerance.select("Apply");
		dpiTolerance.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase("Apply")) {
					tolerance.setEnabled(true);
				} else {
					tolerance.setEnabled(false);
				}
				params.put(AracneParameters.TOL_TYPE, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		dpiTargetSetBox.setCaption(" ");
		dpiTargetSetBox.setNullSelectionAllowed(false);
		dpiTargetSetBox.setInputPrompt("Select Marker Set");
		dpiTargetSetBox.setImmediate(true);
		dpiTargetSetBox.setEnabled(false);


		dpiTargetSetBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.DPI_SET, String
						.valueOf(valueChangeEvent.getProperty().getValue()));
			}
		});

		dpiTargetList.setCaption("DPI TargetList");
		dpiTargetList.setImmediate(true);
		dpiTargetList.setNullSelectionAllowed(false);
		dpiTargetList.addItem("From Sets");
		dpiTargetList.addItem("Do Not Apply");
		dpiTargetList.select("Do Not Apply");
		dpiTargetList.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase("From Sets")) {
					dpiTargetSetBox.setEnabled(true);
				} else {
					dpiTargetSetBox.setEnabled(false);
				}
				params.remove(AracneParameters.DPI_LIST);
				params.put(AracneParameters.DPI_LIST, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		bootStrapNumber.setCaption("Bootstrap Number");
		bootStrapNumber.setImmediate(true);
		bootStrapNumber.setValue("1");
		bootStrapNumber.setNullSettingAllowed(false);
		bootStrapNumber.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			public void textChange(TextChangeEvent event) {
				params.remove(AracneParameters.BOOTS_NUM);
				params.put(AracneParameters.BOOTS_NUM, event.getText());
				try{
				if (Integer.valueOf((String) params
						.get(AracneParameters.BOOTS_NUM)) > 1)
					consensusThreshold.setEnabled(true);
				else
					consensusThreshold.setEnabled(false);
				}catch(NumberFormatException e)
				{
					//do nothing, validate message will in validInputData()
				}
			}

		});

		consensusThreshold.setCaption("Consensus Threshold ");
		consensusThreshold.setImmediate(true);
		consensusThreshold.setValue("1.e-6");
		consensusThreshold.setEnabled(false);
		consensusThreshold.setNullSettingAllowed(false);
		consensusThreshold.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.CONSENSUS_THRESHOLD);
				params.put(AracneParameters.CONSENSUS_THRESHOLD,
						valueChangeEvent.getProperty().getValue().toString());
			}
		});

		mergeProbeSets.setCaption("Merge multiple probesets");
		mergeProbeSets.addItem("Yes");
		mergeProbeSets.addItem("No");
		mergeProbeSets.select("No");
		mergeProbeSets.setNullSelectionAllowed(false);
		mergeProbeSets.setImmediate(true);
		mergeProbeSets.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.MERGEPS);
				params.put(AracneParameters.MERGEPS, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {

					if (validInputData()) {
						DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion
								.toObject(UserDirUtils.getDataSet(dataSetId));

						ResultSet resultSet = new ResultSet();
						java.sql.Date date = new java.sql.Date(System
								.currentTimeMillis());
						resultSet.setDateField(date);
						String dataSetName = "Aracne - Pending";
						resultSet.setName(dataSetName);
						resultSet.setType(getResultType().getName());
						resultSet.setParent(dataSetId);
						resultSet.setOwner(SessionHandler.get().getId());
						FacadeFactory.getFacade().store(resultSet);

						NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
						GeworkbenchRoot.getBlackboard().fire(resultEvent);

						AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
								maSet, resultSet, params, AracneUI.this);
						GeworkbenchRoot.getBlackboard().fire(analysisEvent);
					}

				} catch (Exception e) {
				}
			}
		});

		addComponent(markerSetSelect, 0, 0);
		addComponent(arraySetSelect, 1, 0);
		addComponent(hubGeneMarkerSetBox, 0, 1);
		addComponent(modeBox, 1, 1);
		addComponent(algoBox, 0, 2);
		addComponent(kernelWidth, 1, 2);
		addComponent(widthValue, 2, 2);
		addComponent(thresholdType, 0, 3);
		addComponent(threshold, 1, 3);
		addComponent(correction, 2, 3);
		addComponent(dpiTolerance, 0, 4);
		addComponent(tolerance, 1, 4);
		addComponent(dpiTargetList, 0, 5);
		addComponent(dpiTargetSetBox, 1, 5);
		addComponent(bootStrapNumber, 0, 6);
		addComponent(consensusThreshold, 1, 6);
		addComponent(mergeProbeSets, 0, 7);
		addComponent(submitButton, 0, 8);

	}

	private boolean validInputData() {

		if (hubGeneMarkerSetBox.getValue() == null
				|| hubGeneMarkerSetBox.getValue().toString().trim().equals("")) {
			hubGeneMarkerSetBox.setComponentError(new UserError(
					"You did not load any genes as hub markers."));
			return false;
		}
		 
		hubGeneMarkerSetBox.setComponentError(null);

		float floatValue = -1;
		try {
			if (threshold.getValue() != null)
				floatValue = Float.parseFloat(threshold.getValue().toString());
		} catch (NumberFormatException e) {
		}

		if (((String) params.get(AracneParameters.T_TYPE))
				.equalsIgnoreCase("Mutual Info")) {
			if (floatValue < 0) {
				threshold
						.setComponentError(new UserError(
								"Threshold Mutual Info. should be larger than or equal to zero."));
				return false;
			}
		} else {

			if (floatValue < 0 || floatValue > 1)

			{
				threshold.setComponentError(new UserError(
						"Threshold P-Value should be between 0.0 and 1.0"));
				return false;
			}

		}
        
		threshold.setComponentError(null);
		
		floatValue = -1;
		try {
			if (widthValue.getValue() != null)
				floatValue = Float.parseFloat(widthValue.getValue().toString());
		} catch (NumberFormatException e) {
		}

		if (((String) params.get(AracneParameters.KERNEL_WIDTH))
				.equalsIgnoreCase("Specify")) {

			if (floatValue < 0 || floatValue > 1) {
				widthValue.setComponentError(new UserError(
						"Kernel Width should between 0.0 and 1.0"));
				return false;
			}
		}
		widthValue.setComponentError(null);
		
		floatValue = -1;
		try {
			if (tolerance.getValue() != null)
				floatValue = Float.parseFloat(tolerance.getValue().toString());
		} catch (NumberFormatException e) {
		}

		if (((String) params.get(AracneParameters.TOL_TYPE))
				.equalsIgnoreCase("Apply")) {
			if (floatValue < 0 || floatValue > 1) {
				tolerance
						.setComponentError(new UserError(
								"DPI Tolerance should be a float number between 0.0 and 1.0."));
				return false;
			}

		}
		tolerance.setComponentError(null); 
		
		int b = 0;
		try {
			if (params.get(AracneParameters.BOOTS_NUM) != null)
				b = Integer.parseInt(params.get(AracneParameters.BOOTS_NUM)
						.toString());
		} catch (NumberFormatException e) {

		}
		if (b <= 0) {
			bootStrapNumber.setComponentError(new UserError(
					"Must be an integer"));
			return false;
		}
		
		bootStrapNumber.setComponentError(null);

		floatValue = -1;
		try {
			if (consensusThreshold.getValue() != null)
				floatValue = Float.parseFloat(consensusThreshold.getValue()
						.toString());
		} catch (NumberFormatException e) {
		}	

		if (b > 1) {
			if (floatValue <= 0 || floatValue > 1) {
				consensusThreshold.setComponentError(new UserError(
						"Consensus threshold is not valid."));
				return false;

			}
		}
		consensusThreshold.setComponentError(null);		
		submitButton.setComponentError(null);
		
		return true;
	}

	@Override
	public void setDataSetId(Long dataSetId) {
		this.dataSetId = dataSetId;

		List<?> markerSubSets = SubSetOperations.getMarkerSets(dataSetId);

		markerSetSelect.removeAllItems();
		markerSetSelect.addItem("All Markers");
		for (int m = 0; m < (markerSubSets).size(); m++) {
			markerSetSelect.addItem(((SubSet) markerSubSets.get(m)).getId());
			markerSetSelect.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}
		markerSetSelect.select("All Markers");

		hubGeneMarkerSetBox.removeAllItems();
		hubGeneMarkerSetBox.addItem("All vs. All");
		for (int m = 0; m < (markerSubSets).size(); m++) {
			hubGeneMarkerSetBox
					.addItem(((SubSet) markerSubSets.get(m)).getId());
			hubGeneMarkerSetBox.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}
		// hubGeneMarkerSetBox.select("All vs. All");
		
		dpiTargetSetBox.removeAllItems();
		for (int m = 0; m < (markerSubSets).size(); m++) {
			dpiTargetSetBox.addItem(((SubSet) markerSubSets.get(m)).getId());
			dpiTargetSetBox.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}

		List<?> arraySubSets = SubSetOperations.getArraySets(dataSetId);

		arraySetSelect.removeAllItems();
		arraySetSelect.addItem("All Arrays");
		for (int m = 0; m < (arraySubSets).size(); m++) {
			arraySetSelect.addItem(((SubSet) arraySubSets.get(m)).getId());
			arraySetSelect.setItemCaption(
					((SubSet) arraySubSets.get(m)).getId(),
					((SubSet) arraySubSets.get(m)).getName());
		}
		arraySetSelect.select("All Arrays");
	}

	@Override
	public Class<?> getResultType() {
		return AdjacencyMatrixDataSet.class;
	}

	@Override
	public String execute(Long resultId, DSDataSet<?> dataset,
			HashMap<Serializable, Serializable> parameters) {
		AracneAnalysisWeb analyze = new AracneAnalysisWeb((DSMicroarraySet) dataset, params);
		UserDirUtils.saveResultSet(resultId, ObjectConversion.convertToByte(analyze.execute()));
		return "Aracne";
	}
}