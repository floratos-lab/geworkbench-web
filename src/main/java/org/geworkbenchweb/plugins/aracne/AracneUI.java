package org.geworkbenchweb.plugins.aracne;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class AracneUI extends VerticalLayout implements AnalysisUI {

	private final Log log = LogFactory.getLog(AracneUI.class);
	private static final long serialVersionUID = 1L;

	private Long dataSetId;
	private final HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	private ComboBox hubGeneMarkerSetBox = new ComboBox();
	private ComboBox dpiFiltering = new ComboBox();
	private TextField threshold = new TextField();
	private ComboBox bootStrapNumber = new ComboBox();
	private ComboBox mergeProbeSets = new ComboBox();
	private Button submitButton = null;
	private static final String defaultBootsNum = "100";

	private static String QUESTION_MARK = " \uFFFD";

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
					} else
						hubGeneList = null;

					if (validInputData(hubGeneList)) {

						DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
						int arrayNum = DataSetOperations.getNumber("arrayNumber", dataset.getDataId());
						if (arrayNum < 100) {
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
						} else {
							addPendingNode(hubGeneList);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		gridLayout.addComponent(extraParameterLayout, 0, 0, 2, 5);
		gridLayout.addComponent(submitButton, 0, 6);

		addComponent(gridLayout);

		setDataSetId(dataId);
	}

	private void addPendingNode(List<String> hubGeneList) {
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
		app.getBlackboard().fire(analysisEvent);
	}

	private static void setDefaultParameters(HashMap<Serializable, Serializable> params) {
		params.put(AracneParameters.DPI_FILTERING, "Yes");
		params.put(AracneParameters.P_VALUE, "0.01");
		params.put(AracneParameters.BOOTS_NUM, "1");
		params.put(AracneParameters.MERGEPS, "No");
	}

	private GridLayout initializeExtraParameterPanel() {
		hubGeneMarkerSetBox.setCaption("Hub Marker(s) From Sets" + QUESTION_MARK);
		hubGeneMarkerSetBox.setDescription(
				"Mutual information is calculated between each hub marker and all other selected markers (default All Markers)");
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

		threshold.setCaption("P-Value" + QUESTION_MARK);
		threshold.setDescription("Enter a P-value");
		threshold.setValue("0.01");
		threshold.setNullSettingAllowed(false);
		threshold.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.P_VALUE);
				params.put(AracneParameters.P_VALUE, valueChangeEvent
						.getProperty().getValue().toString());
			}
		});

		dpiFiltering.setCaption("DPI-based interaction filtering" + QUESTION_MARK);
		dpiFiltering.setDescription("to run ARACNe with or without DPI filtering");
		dpiFiltering.setImmediate(true);
		dpiFiltering.setNullSelectionAllowed(false);
		dpiFiltering.addItem("Yes");
		dpiFiltering.addItem("No");
		dpiFiltering.select("Yes");

		bootStrapNumber.setCaption(defaultBootsNum + " rounds of Bootstrapping" + QUESTION_MARK);
		bootStrapNumber.setDescription(
				"the number of rounds of ARACNe bootstrapping");
		bootStrapNumber.setImmediate(true);
		bootStrapNumber.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent event) {
				params.put(AracneParameters.BOOTS_NUM, (String)bootStrapNumber.getValue());
			}

		});
		bootStrapNumber.addItem("10");
		bootStrapNumber.addItem("20");
		bootStrapNumber.addItem("50");
		bootStrapNumber.addItem("100");
		bootStrapNumber.select("100");

		mergeProbeSets.setCaption("Merge multiple probesets" + QUESTION_MARK);
		mergeProbeSets.setDescription(
				"If Yes, summarize each particular gene-gene edge by the strongest connection between their individual probesets, when more than one probeset per gene is present in the network. Requires that an appropriate annotation file linking probesets to gene symbols was loaded along with the expression matrix.  The network is output in terms of gene symbols.");
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

		final GridLayout layout = new GridLayout(4, 6);
		layout.setSpacing(true);
		layout.setImmediate(true);

		layout.addComponent(hubGeneMarkerSetBox, 0, 1);
		layout.addComponent(threshold, 0, 2);
		layout.addComponent(dpiFiltering, 0, 3);
		layout.addComponent(bootStrapNumber, 0, 4);
		layout.addComponent(mergeProbeSets, 0, 5);

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

		if (floatValue < 0 || floatValue > 1) {
			threshold.setComponentError(new UserError(
					"Threshold P-Value should be between 0.0 and 1.0"));
			return false;
		}

		threshold.setComponentError(null);

		floatValue = -1;

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
							.getValue())
					+ ": \n");
			for (String gene : hubGeneList)
				builder.append(gene + "\n");
		}

		builder.append("P-value: " + threshold.getValue().toString() + "\n");

		builder.append("DPI Filtering - "
				+ dpiFiltering.getItemCaption(dpiFiltering.getValue())
				+ "  ");

		builder.append("Bootstrapping number " + bootStrapNumber.getValue() + "\n");

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
		if (markerSubSets.size() > 0)
			hubGeneMarkerSetBox.select(((SubSet) (markerSubSets.get(0))).getId());
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
		ResultSet result = FacadeFactory.getFacade().find(ResultSet.class,
				resultId);
		if (result==null) { // if the pending node is deleted by the user
			log.debug("Job is cancelled. resultId=" + resultId);
			return null;
		}
		FacadeFactory.getFacade().store(output);
		result.setDataId(output.getId());
		if (output instanceof Network) { // FIXME binding two different types together horrible idea
			Network n = (Network) output;
			result.setDescription("# of nodes: " + n.getNodeNumber() + ", # of edges: " + n.getEdgeNumber());
		}
		FacadeFactory.getFacade().store(result);

		String resultName = analyze.resultName;
		if (resultName != null && resultName.trim().length() > 0)
			return "Aracne - " + resultName;
		return "Aracne";
	}
}