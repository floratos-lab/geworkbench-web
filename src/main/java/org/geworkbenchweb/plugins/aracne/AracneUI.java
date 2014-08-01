package org.geworkbenchweb.plugins.aracne;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ConfigResult;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.MarkerArraySelector;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AracneUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = 1L;

	private Long dataSetId;
	private Long userId = null;
	private final HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private MarkerArraySelector markerArraySelector;
	private ComboBox hubGeneMarkerSetBox = new ComboBox();
	private ComboBox modeBox = new ComboBox();
	private ComboBox algoBox = new ComboBox();
	private ComboBox configBox = new ComboBox();
	private ComboBox kernelWidth = new ComboBox();
	private ComboBox thresholdType = new ComboBox();
	private ComboBox dpiTolerance = new ComboBox();
	private ComboBox dpiTargetList = new ComboBox();
	private TextField widthValue = new TextField();
	private TextField threshold = new TextField();
	private ComboBox correction = new ComboBox();
	private TextField tolerance = new TextField();
	private ComboBox dpiTargetSetBox = new ComboBox();
	private CheckBox bootStrapNumber = new CheckBox();
	private TextField consensusThreshold = new TextField();
	private ComboBox mergeProbeSets = new ComboBox();
	private Button submitButton = null;
	private static final String defaultBootsNum = "100";
	private static final String adaptivePartitioning = "Adaptive Partitioning";
	private static final String fixedBandwidth = "Fixed Bandwidth";

	public AracneUI() {
		this(0L);
	}

	public AracneUI(Long dataId) {

		this.dataSetId = dataId;
		User user = SessionHandler.get();
		if (user != null)
			userId = user.getId();

		setSpacing(true);
		setImmediate(true);

		final GridLayout gridLayout = new GridLayout(4, 10);

		gridLayout.setSpacing(true);
		gridLayout.setImmediate(true);

		/**
		 * Params default values
		 */
		params.put(AracneParameters.MARKER_SET, "All Markers");
		params.put(AracneParameters.ARRAY_SET, "All Arrays");
		params.put(AracneParameters.MODE, AracneParameters.DISCOVERY);
		params.put(AracneParameters.CONFIG, "Default");
		params.put(AracneParameters.ALGORITHM, "Adaptive Partitioning");
		params.put(AracneParameters.KERNEL_WIDTH, "Inferred");
		params.put(AracneParameters.WIDTH_VALUE, "0.01");
		params.put(AracneParameters.TOL_TYPE, "Apply");
		params.put(AracneParameters.TOL_VALUE, "0.0");
		params.put(AracneParameters.T_TYPE, "Mutual Info");
		params.put(AracneParameters.T_VALUE, "0.01");
		params.put(AracneParameters.CORRECTION, "No Correction");
		params.put(AracneParameters.DPI_LIST, "Do Not Apply");
		params.put(AracneParameters.BOOTS_NUM, "1");
		params.put(AracneParameters.CONSENSUS_THRESHOLD, "1.e-6");
		params.put(AracneParameters.MERGEPS, "No");

		markerArraySelector = new MarkerArraySelector(dataSetId, userId,
				"AracneUI");

		hubGeneMarkerSetBox.setTextInputAllowed(false);
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
		modeBox.addItem(AracneParameters.COMPLETE);
		modeBox.addItem(AracneParameters.DISCOVERY);
		modeBox.addItem(AracneParameters.PREPROCESSING);
		modeBox.select(AracneParameters.DISCOVERY);
		modeBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.MODE);
				params.put(AracneParameters.MODE, valueChangeEvent
						.getProperty().getValue().toString());
				if (params.get(AracneParameters.MODE).equals(
						AracneParameters.DISCOVERY))
					configBox.setEnabled(true);
				else
					configBox.setEnabled(false);
			}
		});

		configBox.setCaption("Select Configuration");
		configBox.setNullSelectionAllowed(false);
		configBox.setImmediate(true);
		configBox.addItem("Default");
		configBox.select("Default");
		configBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.CONFIG);
				Object o = valueChangeEvent.getProperty().getValue();
				if (o != null)
					params.put(AracneParameters.CONFIG, o.toString());
			}
		});

		algoBox.setCaption("Select Algorithm");
		algoBox.setImmediate(true);
		algoBox.setNullSelectionAllowed(false);
		algoBox.addItem(adaptivePartitioning);
		algoBox.addItem(fixedBandwidth);
		algoBox.select(adaptivePartitioning);
		algoBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase(fixedBandwidth)) {
					kernelWidth.setEnabled(true);
					widthValue.setEnabled(true);
				} else if (valueChangeEvent.getProperty().getValue().toString()
						.equalsIgnoreCase(adaptivePartitioning)) {
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
		thresholdType.select("P-Value");

		tolerance.setCaption(" ");
		tolerance.setValue("0.0");
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

		bootStrapNumber.setCaption(defaultBootsNum + " Bootstrapping");
		bootStrapNumber.setImmediate(true);
		bootStrapNumber.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent event) {
				if (bootStrapNumber.booleanValue())
					params.put(AracneParameters.BOOTS_NUM, defaultBootsNum);
				else
					params.put(AracneParameters.BOOTS_NUM, "1");
				try {
					if (Integer.valueOf((String) params
							.get(AracneParameters.BOOTS_NUM)) > 1)
						consensusThreshold.setEnabled(true);
					else
						consensusThreshold.setEnabled(false);
				} catch (NumberFormatException e) {
					// do nothing, validate message will in validInputData()
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

					List<String> hubGeneList = null;
					Long subSetId;

					if (hubGeneMarkerSetBox.getValue() != null) {
						subSetId = Long.parseLong((String) hubGeneMarkerSetBox
								.getValue().toString().trim());
						hubGeneList = SubSetOperations.getMarkerData(subSetId);
					}
					if (validInputData(hubGeneList)) {

						ResultSet resultSet = new ResultSet();
						java.sql.Timestamp timestamp = new java.sql.Timestamp(
								System.currentTimeMillis());
						resultSet.setTimestamp(timestamp);
						String dataSetName = "Aracne - Pending";
						resultSet.setName(dataSetName);
						resultSet.setType(getResultType().getName());
						resultSet.setParent(dataSetId);
						resultSet.setOwner(SessionHandler.get().getId());
						FacadeFactory.getFacade().store(resultSet);

						generateHistoryString(resultSet.getId(), hubGeneList);

						GeworkbenchRoot app = (GeworkbenchRoot) AracneUI.this
								.getApplication();
						app.addNode(resultSet);

						params.put(AracneParameters.MARKER_SET,
								markerArraySelector.getSelectedMarkerSet());
						params.put(AracneParameters.ARRAY_SET,
								markerArraySelector.getSelectedArraySet());
						AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
								resultSet, params, AracneUI.this);
						GeworkbenchRoot.getBlackboard().fire(analysisEvent);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		gridLayout.addComponent(hubGeneMarkerSetBox, 0, 0);
		gridLayout.addComponent(modeBox, 1, 0);
		gridLayout.addComponent(configBox, 2, 0);
		gridLayout.addComponent(algoBox, 0, 1);
		gridLayout.addComponent(kernelWidth, 1, 1);
		gridLayout.addComponent(widthValue, 2, 1);
		gridLayout.addComponent(thresholdType, 0, 2);
		gridLayout.addComponent(threshold, 1, 2);
		gridLayout.addComponent(correction, 2, 2);
		gridLayout.addComponent(dpiTolerance, 0, 3);
		gridLayout.addComponent(tolerance, 1, 3);
		gridLayout.addComponent(dpiTargetList, 0, 4);
		gridLayout.addComponent(dpiTargetSetBox, 1, 4);
		gridLayout.addComponent(bootStrapNumber, 0, 5);
		gridLayout.addComponent(consensusThreshold, 1, 5);
		gridLayout.addComponent(mergeProbeSets, 0, 6);
		gridLayout.addComponent(submitButton, 0, 7);

		addComponent(markerArraySelector);
		addComponent(gridLayout);

	}

	private boolean validInputData(List<String> hubGeneList) {
		if (params.get(AracneParameters.MODE).equals(
				AracneParameters.PREPROCESSING)) {
			return true;
		}

		if (hubGeneMarkerSetBox.getValue() == null
				|| hubGeneMarkerSetBox.getValue().toString().trim().equals("")) {
			hubGeneMarkerSetBox.setComponentError(new UserError(
					"You did not load any genes as hub markers."));
			return false;
		}
		// check empty hub set
		if (hubGeneList == null || hubGeneList.size() == 0) {
			hubGeneMarkerSetBox.setComponentError(new UserError(
					"You did not load any genes as hub markers."));
			return false;
		}

		hubGeneMarkerSetBox.setComponentError(null);

		String alg = algoBox.getValue().toString();
		String config = configBox.getItemCaption(configBox.getValue());
		if (configBox.isEnabled()
				&& ((alg.equalsIgnoreCase(adaptivePartitioning) && config
						.contains("FB")) || (alg
						.equalsIgnoreCase(fixedBandwidth) && config
						.contains("AP")))) {
			configBox
					.setComponentError(new UserError(
							"The selected configuration does not match the selected Algorithm."));
			return false;
		}
		configBox.setComponentError(null);

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

		if (dpiTargetSetBox.isEnabled() && dpiTargetSetBox.getValue() == null) {
			dpiTargetSetBox.setComponentError(new UserError(
					"Please select DPI Target set."));
			return false;
		}

		dpiTargetSetBox.setComponentError(null);

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

	private void generateHistoryString(Long resultSetId,
			List<String> hubGeneList) {
		StringBuilder builder = new StringBuilder();

		builder.append("Aracne Parameters : \n");

		if (hubGeneList != null) {
			builder.append("Hub Marker(s) from Sets - "
					+ hubGeneMarkerSetBox.getItemCaption(hubGeneMarkerSetBox
							.getValue()) + ": \n");
			for (String gene : hubGeneList)
				builder.append(gene + "\n");
		}

		String mode = modeBox.getItemCaption(modeBox.getValue());
		builder.append("Mode - " + mode + "\n");
		if (!mode.equalsIgnoreCase(AracneParameters.PREPROCESSING)) {
			if (configBox.isEnabled())
				builder.append("Configuration - "
						+ configBox.getItemCaption(configBox.getValue()) + "\n");
			builder.append("Algorithm - "
					+ algoBox.getItemCaption(algoBox.getValue()) + "\n");
			if (kernelWidth.isEnabled() && !widthValue.isEnabled())
				builder.append("Kernel Width - "
						+ kernelWidth.getItemCaption(kernelWidth.getValue())
						+ "\n");
			else if (widthValue.isEnabled() && widthValue.isEnabled())
				builder.append("Kernel Width - "
						+ widthValue.getValue().toString() + "\n");

			builder.append("Threshold Type - "
					+ thresholdType.getItemCaption(thresholdType.getValue())
					+ ": " + threshold.getValue().toString() + "\n");

			if (correction.isEnabled())
				builder.append("Correction Type - "
						+ correction.getItemCaption(correction.getValue())
						+ "\n");

			builder.append("DPI Tolerance - "
					+ dpiTolerance.getItemCaption(dpiTolerance.getValue())
					+ "  ");
			if (tolerance.isEnabled())
				builder.append(": " + tolerance.getValue() + "\n");
			else
				builder.append("\n");

			builder.append("DPI Target List - "
					+ dpiTargetList.getItemCaption(dpiTargetList.getValue())
					+ "\n");

			if (dpiTargetSetBox.isEnabled()
					&& (dpiTargetSetBox.getValue() != null)) {
				builder.append(" : "
						+ dpiTargetSetBox.getItemCaption(dpiTargetSetBox
								.getValue()) + "\n");
				List<String> targetGeneList = null;
				Long subSetId = Long.parseLong((String) dpiTargetSetBox
						.getValue().toString().trim());
				targetGeneList = SubSetOperations.getMarkerData(subSetId);
				for (String gene : targetGeneList)
					builder.append(gene + "\n");
			}

			if (bootStrapNumber.booleanValue() == true) {
				builder.append("100 Bootstrapping is checked, Consensus Threshold - "
						+ consensusThreshold.getValue() + "\n");
			} else
				builder.append("100 Bootstrapping is not checked");

			builder.append("Merge multiple probesets - "
					+ mergeProbeSets.getItemCaption(mergeProbeSets.getValue())
					+ "\n");

		} else
			builder.append("Algorithm - "
					+ algoBox.getItemCaption(algoBox.getValue()) + "\n");

		builder.append(markerArraySelector.generateHistoryString());

		DataHistory his = new DataHistory();
		his.setParent(resultSetId);
		his.setData(builder.toString());
		FacadeFactory.getFacade().store(his);
	}

	@Override
	public void setDataSetId(Long dataSetId) {
		User user = SessionHandler.get();
		if (user != null) {
			userId = user.getId();
		}

		this.dataSetId = dataSetId;
		markerArraySelector.setData(dataSetId, userId);

		List<?> markerSubSets = SubSetOperations.getMarkerSets(dataSetId);
		hubGeneMarkerSetBox.removeAllItems();
		for (int m = 0; m < (markerSubSets).size(); m++) {
			hubGeneMarkerSetBox
					.addItem(((SubSet) markerSubSets.get(m)).getId());
			hubGeneMarkerSetBox.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}

		dpiTargetSetBox.removeAllItems();
		for (int m = 0; m < (markerSubSets).size(); m++) {
			dpiTargetSetBox.addItem(((SubSet) markerSubSets.get(m)).getId());
			dpiTargetSetBox.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}

		configBox.removeAllItems();
		configBox.addItem("Default");
		configBox.select("Default");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		parameter.put("type", ConfigResult.class.getName());
		List<ResultSet> list = FacadeFactory.getFacade().list(
				"SELECT r FROM ResultSet AS r WHERE "
						+ "r.parent=:dataSetId and "
						+ "r.dataId is not null and " + "r.type=:type",
				parameter);
		for (ResultSet r : list) {
			configBox.addItem(r.getDataId());
			configBox.setItemCaption(r.getDataId(), r.getName());
		}

	}

	@Override
	public Class<?> getResultType() {
		if (params.get(AracneParameters.MODE).equals(
				AracneParameters.PREPROCESSING))
			return ConfigResult.class;
		return Network.class;
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		AracneAnalysisWeb analyze = new AracneAnalysisWeb(dataSetId, params);
		AbstractPojo output = analyze.execute();
		FacadeFactory.getFacade().store(output);
		ResultSet result = FacadeFactory.getFacade().find(ResultSet.class,
				resultId);
		result.setDataId(output.getId());
		FacadeFactory.getFacade().store(result);

		String resultName = analyze.serviceClient.resultName;
		if (resultName != null && resultName.trim().length() > 0)
			return "Aracne - " + resultName;
		return "Aracne";
	}
}