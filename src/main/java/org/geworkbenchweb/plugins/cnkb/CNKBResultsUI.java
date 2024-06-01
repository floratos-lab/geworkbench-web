package org.geworkbenchweb.plugins.cnkb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.AnalysisSubmission;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.NetworkViewer;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.CNKBResultSet;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.highcharts.HighChart;

import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.messagebox.MessageBox;

/**
 * Visualization of CNKB results: a plot on the top and a table on the bottom.
 */
public class CNKBResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(CNKBResultsUI.class);

	private static final String COLUMN_GO_ANNOTATIONS = "GO Annotations";

	private VerticalSplitPanel tabPanel;
	private VerticalSplitPanel throttlePanel;

	private ArrayList<Double> totalInteractionConfidence = new ArrayList<Double>();

	private Map<String, List<Double>> ConfidentDataMap = new HashMap<String, List<Double>>();

	protected HighChart plot;

	Table dataTable;
	private Map<String, String> confidentTypeMap = null;

	final private Long datasetId;

	public CNKBResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if (dataSetId == null)
			return;

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		if (id == null) { // pending node
			addComponent(new Label("Pending computation - ID " + dataSetId));
			return;
		}
		CNKBResultSet cnkbResult = FacadeFactory.getFacade().find(org.geworkbenchweb.pojos.CNKBResultSet.class, id);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.id=:id", parameters);
		Long parentId = data.get(0).getParent();

		init(cnkbResult, parentId);
	}

	private void init(final CNKBResultSet cnkbResult, final Long parentId) {
		if (confidentTypeMap == null)
			loadConfidentTypeMap();

		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.TABSHEET_SMALL);

		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(250, Unit.PIXELS);
		tabPanel.setStyleName("small");
		tabPanel.setLocked(false);

		// Results Table Code
		dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.setImmediate(true);

		dataTable.setContainerDataSource(getIndexedContainer(cnkbResult));
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth(COLUMN_GO_ANNOTATIONS, 150);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);

		dataTable.setItemDescriptionGenerator(new ItemDescriptionGenerator() {

			private static final long serialVersionUID = 1L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				try { /* there are multiple expected possibilities to get null pointers here */
					return ((Table) source).getItem(itemId).getItemProperty(propertyId).getValue().toString();
				} catch (NullPointerException e) {
					return "";
				}
			}

		});

		Short confidenceType = cnkbResult.getCellularNetworkPreference().getSelectedConfidenceType();
		double maxValue = getMaxValue(cnkbResult.getCellularNetworkPreference().getMaxConfidenceValue(confidenceType));

		plot = drawPlot(cnkbResult, 0, maxValue);

		MenuBar menuBar = new MenuBar();
		menuBar.setStyleName("transparent");
		menuBar.addItem("Create Network", new CreateNetworkCommand(parentId,
				cnkbResult)).setStyleName("plugin");
		MenuItem export = menuBar.addItem("Export", null);
		export.addItem("Export table to Excel", new Command() {

			private static final long serialVersionUID = -4510368918141762449L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				ExcelExport excelExport = new ExcelExport(dataTable);
				excelExport.excludeCollapsedColumns();
				excelExport.setDoubleDataFormat("0");
				excelExport.setDisplayTotals(false);
				excelExport.export();
			}

		}).setStyleName("plugin");
		export.addItem("Export interactions to SIF", new Command() {

			private static final long serialVersionUID = 3617735646709100783L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				Network network = createInMemoryNetwork(parentId, cnkbResult);
				if (network == null) {
					MessageBox.createInfo().withCaption("Warning")
							.withMessage("There is no interaction to create a network.").withOkButton().open();
					return;
				}
				String filename = "network_" + System.currentTimeMillis() + ".sif";
				downloadFile(network.toSIF(), filename);
			}
		}).setStyleName("plugin");
		export.addItem("Export interactions to ADJ", new Command() {

			private static final long serialVersionUID = 8434363860293572785L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				Network network = createInMemoryNetwork(parentId, cnkbResult);
				if (network == null) {
					MessageBox.createInfo().withCaption("Warning")
							.withMessage("There is no interaction to create a network.").withOkButton().open();
					return;
				}
				String filename = "network_" + System.currentTimeMillis() + ".adj";
				downloadFile(network.toString(), filename);
			}
		}).setStyleName("plugin");
		menuBar.addItem("Help", new Command() {

			private static final long serialVersionUID = -1970832620889340547L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				Page.getCurrent().open(
						"http://wiki.c2b2.columbia.edu/workbench/index.php/Cellular_Networks_KnowledgeBase_web",
						"_blank");
			}

		}).setStyleName("plugin");
		;

		addComponent(menuBar);

		ThrottleSlider slider = new ThrottleSlider(cnkbResult, 0, maxValue, this);
		slider.setStyleName("small");
		throttlePanel = new VerticalSplitPanel();
		throttlePanel.setSizeFull();
		throttlePanel.setSplitPosition(200, Unit.PIXELS);
		throttlePanel.setStyleName("small");
		throttlePanel.setLocked(false);
		throttlePanel.setFirstComponent(plot);
		throttlePanel.setSecondComponent(slider);
		slider.setMargin(true);

		tabPanel.setFirstComponent(throttlePanel);
		tabPanel.setSecondComponent(dataTable);
		addComponent(tabPanel);
		setExpandRatio(tabPanel, 1);

	}

	private static void downloadFile(final String content, final String filename) {
		String dir = GeworkbenchRoot.getBackendDataDirectory() + System.getProperty("file.separator")
				+ SessionHandler.get().getUsername() + System.getProperty("file.separator") + "export";
		if (!new File(dir).exists())
			new File(dir).mkdirs();

		final File file = new File(dir, filename);
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			pw.print(content);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file);
		Page.getCurrent().open(resource, "_blank", false);
		/* alternative approaches to be considered */
		// FileDownloader fileDownloader = new FileDownloader(resource);
		// fileDownloader.extend(target));
		// Link link = new Link("download network file", resource);
	}

	/* this version for 'orphan' result */
	public CNKBResultsUI(CNKBResultSet cnkbResult) {
		datasetId = null;
		init(cnkbResult, null);
	}

	/**
	 * Draws the Throttle Graph.
	 * 
	 */
	protected HighChart drawPlot(CNKBResultSet resultSet, double minX, double maxX) {
		if (confidentTypeMap == null) {
			log.error("CNKB server connection error");
			return null;
		}
		Short confidenceType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		String axisTile = null;
		if (confidenceType != null)
			axisTile = confidentTypeMap.get(confidenceType.toString());
		if (axisTile == null)
			axisTile = "Likelihood";

		double smallestIncrement = 0.01d;
		String xAxisMax = "";
		Double maxConfidenceValue = resultSet.getCellularNetworkPreference().getMaxConfidenceValue(confidenceType);
		if (maxConfidenceValue != null && maxConfidenceValue > 1) {
			smallestIncrement = maxX / 100;
		} else {
			xAxisMax = ", max:" + maxX;
		}

		String tooltip = "tooltip: { formatter: function() { return ";
		if (maxConfidenceValue != null && maxConfidenceValue <= 1) {
			tooltip += "'<b>' + this.series.name + '</b><br/>' +  Math.round(((this.x)*100))/100 + ' '+ ' to 1 - ' + this.y + ' interactions'";
		} else {
			tooltip += "'<b>' + this.series.name + '</b><br/>' +  Math.round(((this.x)*100))/100 + ' to max value - '+ this.y + ' interactions'";
		}
		tooltip += "} }";

		String series_for_total = getTotalDistribution(smallestIncrement);

		StringBuffer series_for_types = new StringBuffer();
		for (String interactionType : ConfidentDataMap.keySet()) {
			series_for_types.append("{name:'" + interactionType + "', data:"
					+ getDistribution(interactionType, smallestIncrement) + "},");
		}

		HighChart chart = new HighChart();
		chart.setSizeFull();
		String chartConfig = "chart: { marginRight: 30 }";
		String title = resultSet.getCellularNetworkPreference().getTitle();
		String xAxis = "xAxis: { title: {text: '" + axisTile + "'}, minPadding: 0.05, min:" + minX + xAxisMax + " }";
		String yAxis = "yAxis: { title: {text: '# Interactions'}, gridLineWidth: 1, min: 0 }";
		String options = "plotOptions: {series: {marker: {radius: 5, states: {hover: {lineColor: '#646464'} } } } }";
		String hcjs = String.format(
				"var options = { title: { text: '%s' }, series: [{ name: 'Total Distribution', %s}," +
						series_for_types.toString()
						+ "], %s, %s, %s, %s, %s };",
				title, series_for_total, chartConfig, tooltip, xAxis, yAxis, options);
		chart.setHcjs(hcjs);
		return chart;
	}

	/**
	 * Calculate the graph points for Protein-Protein Interactions
	 */
	private String getDistribution(String interactionType, double smallestIncrement) {
		StringBuffer points = new StringBuffer("[");
		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		List<Double> confidenceList = ConfidentDataMap.get(interactionType);
		for (int m = 0; m < confidenceList.size(); m++) {
			int confidence = (int) ((confidenceList.get(m)) / smallestIncrement);
			if (confidence >= distribution.length) {
				log.warn("This shall not happen: confidence = " + confidence);
				confidence = distribution.length - 1;
			}
			if (confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.0;
		for (int j = 0; j < distribution.length; j++) {
			y = (double) (distribution[j]);
			points.append("[" + x + "," + y + "],");
			x = x + smallestIncrement;
		}
		return points.append("]").toString();
	}

	/**
	 * Calculate the graph points for all Interactions
	 */
	private String getTotalDistribution(double smallestIncrement) {
		StringBuffer points = new StringBuffer("data: [");

		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		for (int m = 0; m < totalInteractionConfidence.size(); m++) {
			int confidence = (int) ((totalInteractionConfidence.get(m)) / smallestIncrement);
			if (confidence >= distribution.length) {
				confidence = distribution.length - 1;
				log.warn("This shall not happen: confidence = " + confidence);
			}
			if (confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.0d;
		for (int j = 0; j < distribution.length; j++) {
			y = (double) (distribution[j]);
			points.append("[" + x + "," + y + "],");
			x = x + smallestIncrement;
		}
		return points.append("]").toString();
	}

	private static Network createInMemoryNetwork(final Long parentId, final CNKBResultSet cnkbResultSet) {

		Short confidentType = cnkbResultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		int interactionNum = 0;
		Vector<CellularNetWorkElementInformation> hits = cnkbResultSet.getCellularNetWorkElementInformations();
		List<String> selectedTypes = cnkbResultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions(selectedTypes, confidentType);
			interactionNum = interactionNum + arrayList.size();
		}

		if (hits == null || interactionNum == 0) {
			log.warn("There is no interaction to create a network. ");
			return null;
		}
		HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
		params.put(CNKBParameters.CNKB_RESULTSET, cnkbResultSet);

		Network network = new NetworkCreation(parentId).createNetwork(params);
		return network;
	}

	private class CreateNetworkCommand implements Command {

		private static final long serialVersionUID = 831124091338570481L;

		final private Long parentId;
		final private CNKBResultSet cnkbResultSet;

		public CreateNetworkCommand(final Long parentId,
				final CNKBResultSet resultSet) {

			this.parentId = parentId;
			this.cnkbResultSet = resultSet;
		}

		private int getInteractionTotalNum(short confidentType) {

			int interactionNum = 0;
			Vector<CellularNetWorkElementInformation> hits = cnkbResultSet.getCellularNetWorkElementInformations();
			List<String> selectedTypes = cnkbResultSet.getCellularNetworkPreference()
					.getDisplaySelectedInteractionTypes();
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

				ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
						.getSelectedInteractions(selectedTypes, confidentType);

				interactionNum = interactionNum + arrayList.size();

			}

			return interactionNum;

		}

		@Override
		public void menuSelected(MenuItem selectedItem) {

			Vector<CellularNetWorkElementInformation> hits = cnkbResultSet.getCellularNetWorkElementInformations();
			if (hits == null || getInteractionTotalNum(
					cnkbResultSet.getCellularNetworkPreference().getSelectedConfidenceType()) == 0) {

				MessageBox.createInfo().withCaption("Warning")
						.withMessage("There is no interaction to create a network.").withOkButton().open();
				return;
			}
			HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
			params.put(CNKBParameters.CNKB_RESULTSET, cnkbResultSet);

			if (datasetId == null) { /* 'orphan' result of CNKB */
				Network network = new NetworkCreation(parentId).createNetwork(params);
				System.out.println(network);

				// direct show the orphan result
				Component content = (ComponentContainer) UI.getCurrent().getContent();
				if (content instanceof UMainLayout) {
					UMainLayout m = (UMainLayout) content;
					m.setPluginViewContent(new NetworkViewer(network));
				} else {
					log.error("wrong type of plugin view content: " + content);
				}
				return;
			}

			ResultSet resultSet = new ResultSet();
			java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
			resultSet.setTimestamp(timestamp);
			String dataSetName = "Cytoscape - Pending";
			resultSet.setName(dataSetName);
			resultSet.setType(Network.class.getName());
			resultSet.setParent(parentId);
			resultSet.setOwner(SessionHandler.get().getId());
			FacadeFactory.getFacade().store(resultSet);

			generateHistoryString(datasetId, hits.get(0).getThreshold(), resultSet);

			GeworkbenchRoot app = (GeworkbenchRoot) UI.getCurrent();
			app.addNode(resultSet);

			/*
			 * this is a special case of the work flow: NetworkCreation uses the interface
			 * method execute but ignore the DataSet argument.
			 */
			Component content = UI.getCurrent().getContent();
			if (content instanceof UMainLayout) {
				new AnalysisSubmission((UMainLayout)content).submit(params, new NetworkCreation(parentId), resultSet);
			} else {
				log.error("THIS SHOULD NEVER HAPPEN.");
			}
		}
	}

	private List<String> getInteractionTypes(CNKBResultSet resultSet) {

		Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
		short confidentType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		List<String> interactionTypes = resultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();

		List<String> selectedTypes = new ArrayList<String>();

		for (int j = 0; j < hits.size(); j++) {

			ArrayList<InteractionDetail> interactionDetail = hits.get(j)
					.getSelectedInteractions(interactionTypes, confidentType);
			if (interactionDetail != null) {
				for (InteractionDetail interaction : interactionDetail) {
					String interactionType = interaction.getInteractionType();
					if (selectedTypes.contains(interactionType))
						continue;
					else
						selectedTypes.add(interactionType);

				}
			}
		}

		return selectedTypes;
	}

	IndexedContainer getIndexedContainer(CNKBResultSet resultSet) {
		Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
		final Short confidentType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		IndexedContainer dataIn = new IndexedContainer();

		List<String> selectedTypes = getInteractionTypes(resultSet);

		totalInteractionConfidence.clear();
		ConfidentDataMap.clear();

		Long id = resultSet.getDatasetId();
		Map<String, String> map = DataSetOperations.getAnnotationMap(id);

		for (int j = 0; j < hits.size(); j++) {
			Item item = dataIn.addItem(j);
			ArrayList<InteractionDetail> interactionDetail = hits.get(j)
					.getSelectedInteractions(selectedTypes, confidentType);
			if (interactionDetail != null) {
				for (InteractionDetail interaction : interactionDetail) {
					totalInteractionConfidence.add(interaction
							.getConfidenceValue(interaction
									.getConfidenceTypes().get(0)));
					String interactionType = interaction.getInteractionType();
					if (ConfidentDataMap.get(interactionType) == null) {
						List<Double> confidenceList = new ArrayList<Double>();
						ConfidentDataMap.put(interactionType, confidenceList);
					}
					ConfidentDataMap.get(interactionType).add(
							interaction.getConfidenceValue(interaction
									.getConfidenceTypes().get(0)));
				}
			}

			dataIn.addContainerProperty("Marker", String.class, null);
			dataIn.addContainerProperty("Gene", String.class, null);
			dataIn.addContainerProperty("Gene Type", String.class, null);
			dataIn.addContainerProperty("Interactome", String.class, null);
			dataIn.addContainerProperty(COLUMN_GO_ANNOTATIONS, String.class, null);
			for (String selectedType : selectedTypes)
				dataIn.addContainerProperty(selectedType + " #", Integer.class,
						null);

			String label = hits.get(j).getMarkerLabel();
			String geneSymbol = map.get(label);
			item.getItemProperty("Marker").setValue(label);
			if (geneSymbol != null)
				item.getItemProperty("Gene").setValue(geneSymbol);

			item.getItemProperty("Gene Type").setValue(
					hits.get(j).getGeneType());
			item.getItemProperty("Interactome").setValue(
					hits.get(j).getInteractome());
			item.getItemProperty(COLUMN_GO_ANNOTATIONS).setValue(
					hits.get(j).getGoInfoStr());

			for (String selectedType : selectedTypes)
				item.getItemProperty(selectedType + " #").setValue(
						(hits.get(j).getSelectedInteractions(selectedType, confidentType))
								.size());

		}

		return dataIn;

	}

	private void loadConfidentTypeMap() {
		CNKBServletClient interactionsConnection = new CNKBServletClient();
		try {
			confidentTypeMap = interactionsConnection.getConfidenceTypeMap();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private double getMaxValue(Double maxConfidenceValue) {
		double maxX = 1.00001d;
		if (maxConfidenceValue != null && maxConfidenceValue > 1) {
			int a = (int) Math.log10(maxConfidenceValue);
			double b = maxConfidenceValue / (Math.pow(10, a));
			maxX = Math.ceil(b);
			maxX = maxX * (Math.pow(10, a));
			log.debug("maxConfidenceValue is " + maxConfidenceValue);
		}

		return maxX;
	}

	protected void updatePlot(CNKBResultSet resultSet, double minX, double maxX) {
		throttlePanel.replaceComponent(plot, drawPlot(resultSet, minX, maxX));
		plot = (HighChart) throttlePanel.getFirstComponent();
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

	private void generateHistoryString(long cnkbResultSetId, double threshold, ResultSet cytoscapeResultSet) {

		Map<String, Object> eParams = new HashMap<String, Object>();
		eParams.put("parent", cnkbResultSetId);
		List<AbstractPojo> histories = FacadeFactory.getFacade()
				.list("Select h from DataHistory as h where h.parent =:parent", eParams);
		DataHistory dH = (DataHistory) histories.get(0);

		String[] temp = dH.getData().split("Markers used");

		StringBuilder histBuilder = new StringBuilder();

		histBuilder.append(temp[0]);
		histBuilder.append("Threshold - " + threshold + "\n");
		histBuilder.append("Markers used" + temp[1]);

		DataHistory his = new DataHistory();
		his.setParent(cytoscapeResultSet.getId());
		his.setData(histBuilder.toString());
		FacadeFactory.getFacade().store(his);
	}

}
