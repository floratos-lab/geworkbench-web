package org.geworkbenchweb.plugins.aracne;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
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

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
 
public class AracneUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = 1L;

	private Long dataSetId;
	private final HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private ComboBox hubGeneMarkerSetBox = new ComboBox();
	private ComboBox thresholdType = new ComboBox();
	private ComboBox dpiTolerance = new ComboBox();
	private ComboBox dpiTargetList = new ComboBox();
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
	
	private static String QUESTION_MARK = " \uFFFD";
	
	 

	public AracneUI() {
		this(0L);
	}

	public AracneUI(Long dataId) {

		this.dataSetId = dataId;

		setSpacing(true);
		setImmediate(true);

		final GridLayout gridLayout = new GridLayout(4, 8);

		gridLayout.setSpacing(true);
		gridLayout.setImmediate(true);

		setDefaultParameters(params);

		final GridLayout extraParameterLayout = initializeExtraParameterPanel();
		extraParameterLayout.setVisible(true);

		submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {

					final List<String> hubGeneList;
					Long subSetId;

					if (hubGeneMarkerSetBox.getValue() != null) {
						subSetId = Long.parseLong((String) hubGeneMarkerSetBox
								.getValue().toString().trim());
						hubGeneList = SubSetOperations.getMarkerData(subSetId);
					}
					else
						hubGeneList = null;
					
					if (validInputData(hubGeneList)) {

						DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
						int arrayNum = DataSetOperations.getNumber("arrayNumber", dataset.getDataId());
						if (arrayNum < 100)
						{	   
						    String theMessage = "ARACNe should not in general be run on less than 100 arrays. Do you want to continue?";

						    MessageBox mb = new MessageBox(getWindow(), "Warning", null,
								theMessage, new MessageBox.ButtonConfig(
										MessageBox.ButtonType.CUSTOM1, "Cancel"),
								new MessageBox.ButtonConfig(MessageBox.ButtonType.CUSTOM2,
										"Continue"));
						   
						    mb.show(new MessageBox.EventListener() {

							    private static final long serialVersionUID = 1L;

							  
						     	@Override
							    public void buttonClicked(ButtonType buttonType) {
								   if (buttonType == ButtonType.CUSTOM1)
									  return;
								   else
									  addPendingNode(hubGeneList);
							    }
						    });						   
						}
						else  
						{ 
							 addPendingNode(hubGeneList);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		gridLayout.addComponent(extraParameterLayout, 0, 0, 2, 6);
		gridLayout.addComponent(submitButton, 0, 7);

		addComponent(gridLayout);

	}

	private void addPendingNode(List<String> hubGeneList)
	{
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

		   AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
				resultSet, params, AracneUI.this);
		   GeworkbenchRoot.getBlackboard().fire(analysisEvent);
	}
	
	
	
	private static void setDefaultParameters(HashMap<Serializable, Serializable> params) {
		/**
		 * Params default values
		 */
		params.put(AracneParameters.TOL_TYPE, "Apply");
		params.put(AracneParameters.TOL_VALUE, "0.0");
		params.put(AracneParameters.T_TYPE, "Mutual Info");
		params.put(AracneParameters.T_VALUE, "0.01");
		params.put(AracneParameters.CORRECTION, "No Correction");
		params.put(AracneParameters.DPI_LIST, "Do Not Apply");
		params.put(AracneParameters.BOOTS_NUM, "1");
		params.put(AracneParameters.CONSENSUS_THRESHOLD, "1.e-6");
		params.put(AracneParameters.MERGEPS, "No");
	}
	
	private GridLayout initializeExtraParameterPanel() {
		hubGeneMarkerSetBox.setCaption("Hub Marker(s) From Sets" + QUESTION_MARK);
		hubGeneMarkerSetBox.setDescription("Mutual information is calculated between each hub marker and all other selected markers (default All Markers)");
		hubGeneMarkerSetBox.setTextInputAllowed(false);
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

	
		threshold.setCaption("Threshold Value" + QUESTION_MARK);
		threshold.setDescription("Enter a P-value or Mutual Information threshold value");
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

		thresholdType.setCaption("Threshold Type" + QUESTION_MARK);
		thresholdType.setDescription("Threshold for retaining edges calculated using ARACNe.  Choose to use a <b>P-value</b> threshold (calculated using the background model parameters determined during Preprocessing if a preprocessing configuration node was selected), or directly enter a <b>Mutual Information</b> threshold");
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
		
		correction.setCaption("Correction Type" + QUESTION_MARK);
		correction.setDescription("Whether to use a <b>Bonferroni</b> multiple testing correction, or <b>No Correction</b>");
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

		tolerance.setCaption("Tolerance Value" + QUESTION_MARK);
		tolerance.setDescription("Value for DPI Tolerance");
		tolerance.setValue("0.0");
		tolerance.setNullSettingAllowed(false);
		tolerance.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.TOL_VALUE, valueChangeEvent
						.getProperty().getValue().toString());
			}

		});

		dpiTolerance.setCaption("DPI Tolerance" + QUESTION_MARK);
		dpiTolerance.setDescription("Choose <b>Apply</b> to use the <b>Data Processing Inequality</b> to remove indirect connections between network nodes, otherwise choose <b>Do Not Apply</b>");
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

		dpiTargetSetBox.setCaption("DPI Target List Selection" + QUESTION_MARK);
		dpiTargetSetBox.setDescription("Choose a marker set to protect from DPI removal");
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

		dpiTargetList.setCaption("DPI Target List" + QUESTION_MARK);
		dpiTargetList.setDescription("if <b>Apply</b> is chosen, allows a set of hub markers (e.g. transcription factors) to be given whose interactions will not be removed by use of the DPI, even if any such interaction is weaker than other network edges with which it forms a triangle");
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

		bootStrapNumber.setCaption(defaultBootsNum + " rounds of Bootstrapping" + QUESTION_MARK);
		bootStrapNumber.setDescription("If checked, run 100 rounds of ARACNe bootstrapping and generate a consensus network, otherwise perform a single run of ARACNe");
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

		consensusThreshold.setCaption("Consensus Threshold " + QUESTION_MARK);
		consensusThreshold.setDescription("Set a consensus threshold for retaining network edges after ARACNe bootstrapping");
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

		mergeProbeSets.setCaption("Merge multiple probesets" + QUESTION_MARK);
		mergeProbeSets.setDescription("If Yes, summarize each particular gene-gene edge by the strongest connection between their individual probesets, when more than one probeset per gene is present in the network. Requires that an appropriate annotation file linking probesets to gene symbols was loaded along with the expression matrix.  The network is output in terms of gene symbols.");
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
		
		final GridLayout layout = new GridLayout(4, 7);
		layout.setSpacing(true);
		layout.setImmediate(true);
		
		layout.addComponent(hubGeneMarkerSetBox, 0, 1);
		layout.addComponent(thresholdType, 0, 2);
		layout.addComponent(threshold, 1, 2);
		layout.addComponent(correction, 2, 2);
		layout.addComponent(dpiTolerance, 0, 3);
		layout.addComponent(tolerance, 1, 3);
		layout.addComponent(dpiTargetList, 0, 4);
		layout.addComponent(dpiTargetSetBox, 1, 4);
		layout.addComponent(bootStrapNumber, 0, 5);
		layout.addComponent(consensusThreshold, 1, 5);
		layout.addComponent(mergeProbeSets, 0, 6);
		
		return layout;
	}

	private boolean validInputData(List<String> hubGeneList) {

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
				builder.append("100 Bootstrapping is not checked\n");

		builder.append("Merge multiple probesets - "
					+ mergeProbeSets.getItemCaption(mergeProbeSets.getValue())
					+ "\n");

		DataHistory his = new DataHistory();
		his.setParent(resultSetId);
		his.setData(builder.toString());
		FacadeFactory.getFacade().store(his);
	}

	@Override
	public void setDataSetId(Long dataSetId) {
		this.dataSetId = dataSetId;

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
	}

	@Override
	public Class<?> getResultType() {
		return Network.class;
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws IOException, Exception {
		AracneAnalysisClient analyze = new AracneAnalysisClient(dataSetId, params);
		AbstractPojo output = analyze.execute();
		FacadeFactory.getFacade().store(output);
		ResultSet result = FacadeFactory.getFacade().find(ResultSet.class,
				resultId);
		result.setDataId(output.getId());
		if(output instanceof Network) { // FIXME binding two different types together horrible idea
			Network n = (Network)output;
			result.setDescription("# of nodes: "+n.getNodeNumber()+", # of edges: "+n.getEdgeNumber());
		}
		FacadeFactory.getFacade().store(result);

		String resultName = analyze.resultName;
		if (resultName != null && resultName.trim().length() > 0)
			return "Aracne - " + resultName;
		return "Aracne";
	}
}