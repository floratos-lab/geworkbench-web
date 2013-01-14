package org.geworkbenchweb.plugins.cnkb.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Cytoscape;
import org.geworkbenchweb.plugins.cnkb.CNKBParameters;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.cnkb.CNKBParameters;
import org.vaadin.appfoundation.authentication.SessionHandler;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.Grid;
import com.invient.vaadin.charts.InvientChartsConfig.DataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Margin;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.Reindeer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * This class displays CNKB results in a Table and also a graph
 * 
 * @author Nikhil Reddy
 */
@SuppressWarnings("unused")
public class CNKBResultsUI extends VerticalLayout { // TabSheet {

	private static final long serialVersionUID = 1L;

	private VerticalSplitPanel tabPanel;

	private ArrayList<Double> totalInteractionConfidence = new ArrayList<Double>();

	private Map<String, List<Double>> confidenceMap = new HashMap<String, List<Double>>();

	private static InvientCharts plot;

	private static CNKBResultsUI menuBarInstance;

	private static Table dataTable;

	private Cytoscape cy;

	public CNKBResultsUI(Long dataSetId) {

		@SuppressWarnings("unchecked")
		Vector<CellularNetWorkElementInformation> hits = (Vector<CellularNetWorkElementInformation>) ObjectConversion
				.toObject(UserDirUtils.getResultSet(dataSetId));

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.id=:id", parameters);
		Long parentId = data.get(0).getParent();

		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.TABSHEET_SMALL);

		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(400, Sizeable.UNITS_PIXELS);
		tabPanel.setStyleName("small");
		tabPanel.setLocked(false);

		// Results Table Code
		dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.setImmediate(true);

		IndexedContainer dataIn = new IndexedContainer();

		List<String> selectedTypes = getInteractionTypes(hits);

		for (int j = 0; j < hits.size(); j++) {
			Item item = dataIn.addItem(j);
			ArrayList<InteractionDetail> interactionDetail = hits.get(j)
					.getSelectedInteractions(selectedTypes);
			if (interactionDetail != null) {
				for (InteractionDetail interaction : interactionDetail) {
					totalInteractionConfidence.add(interaction
							.getConfidenceValue(interaction
									.getConfidenceTypes().get(0)));
					String interactionType = interaction.getInteractionType();
					if (confidenceMap.get(interactionType) == null) {
						List<Double> confidenceList = new ArrayList<Double>();
						confidenceMap.put(interactionType, confidenceList);
					}
					confidenceMap.get(interactionType).add(
							interaction.getConfidenceValue(interaction
									.getConfidenceTypes().get(0)));
				}
			}

			dataIn.addContainerProperty("Marker", String.class, null);
			dataIn.addContainerProperty("Gene", String.class, null);
			dataIn.addContainerProperty("Gene Type", String.class, null);
			dataIn.addContainerProperty("Annotation", String.class, null);
			for (String selectedType : selectedTypes)
				dataIn.addContainerProperty(selectedType + " #", Integer.class,
						null);

			item.getItemProperty("Marker").setValue(
					hits.get(j).getdSGeneMarker().getLabel());
			if (hits.get(j).getdSGeneMarker().getShortName() == hits.get(j)
					.getdSGeneMarker().getGeneName()) {
				item.getItemProperty("Gene").setValue("--");
			} else {
				item.getItemProperty("Gene").setValue(
						hits.get(j).getdSGeneMarker().getGeneName());
			}

			item.getItemProperty("Gene Type").setValue(
					hits.get(j).getGeneType());
			item.getItemProperty("Annotation").setValue(
					hits.get(j).getGoInfoStr());

			for (String selectedType : selectedTypes)
				item.getItemProperty(selectedType + " #").setValue(
						(hits.get(j).getSelectedInteractions(selectedType))
								.size());

		}

		dataTable.setContainerDataSource(dataIn);
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth("Annotation", 150);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);

		plot = drawPlot();
		tabPanel.setFirstComponent(plot);
		tabPanel.setSecondComponent(dataTable);

		Button createNetworkButton;
		createNetworkButton = new Button("Create Network");
		createNetworkButton.addListener(new CreateNetworkListener(parentId,
				hits, selectedTypes));

		addComponent(createNetworkButton);
		addComponent(tabPanel);
		setExpandRatio(tabPanel, 1);

	}

	/**
	 * This method draws the Throttle Graph using Invient Charts Add-on.
	 * 
	 */
	private InvientCharts drawPlot() {

		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setMargin(new Margin());
		chartConfig.getGeneralChartConfig().getMargin().setRight(30);

		chartConfig.getTitle().setText("CNKB - Throttle Graph");

		NumberXAxis numberXAxis = new NumberXAxis();
		numberXAxis.setTitle(new AxisTitle("Likelihood"));

		LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxesSet.add(numberXAxis);
		chartConfig.setXAxes(xAxesSet);

		NumberYAxis numberYAxis = new NumberYAxis();
		numberYAxis.setGrid(new Grid());
		numberYAxis.getGrid().setLineWidth(1);

		numberYAxis.setTitle(new AxisTitle("# Interactions"));
		LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxesSet.add(numberYAxis);
		chartConfig.setYAxes(yAxesSet);

		chartConfig.getTooltip().setEnabled(true);
		// Series data label formatter
		LineConfig lineCfg = new LineConfig();
		chartConfig.addSeriesConfig(lineCfg);

		/* Tooltip formatter */
		chartConfig.getTooltip().setFormatterJsFunc(
				"function() { "
						+ " return '<b>' + this.series.name + '</b><br/>' +  "
						+ "Math.round(((this.x+0.005)*100))/100 + ' '+ "
						+ "' to 1 - ' + " + "this.y + ' interactions'" + "}");

		InvientCharts chart = new InvientCharts(chartConfig);

		XYSeries seriesData = new XYSeries("Total Distribution");
		seriesData.setSeriesPoints(getTotalDistribution(seriesData));
		chart.addSeries(seriesData);

		for (String interactionType : confidenceMap.keySet()) {
			seriesData = new XYSeries(interactionType);
			seriesData.setSeriesPoints(getDistribution(interactionType));
			chart.addSeries(seriesData);
		}

		return chart;
	}

	/**
	 * Method is used to calculate the graph points for Protein-Protein
	 * Interactions
	 */
	private LinkedHashSet<DecimalPoint> getDistribution(String interactionType) {

		XYSeries seriesData = new XYSeries(interactionType);
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		List<Double> confidenceList = confidenceMap.get(interactionType);
		for (int m = 0; m < confidenceList.size(); m++) {
			int confidence = (int) ((confidenceList.get(m)) * 100);
			//To do: need to fix later for those confidence value > 1
			if (confidence > 100) confidence = 100;
			if (confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.005d;
		for (int j = 0; j < distribution.length; j++) {
			y = (double) (distribution[j]);
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + 0.01d;
		}
		return points;
	}

	/**
	 * Method is used to calculate the graph points for all Interactions
	 */
	private LinkedHashSet<DecimalPoint> getTotalDistribution(XYSeries seriesData) {

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();

		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		for (int m = 0; m < totalInteractionConfidence.size(); m++) {
			int confidence = (int) ((totalInteractionConfidence.get(m)) * 100);
			//To do: need to fix later for those confidence value > 1
			if (confidence > 100) confidence = 100;
			if (confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.005d;
		for (int j = 0; j < distribution.length; j++) {
			y = (double) (distribution[j]);
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + 0.01d;
		}
		return points;
	}

	/**
	 * Called to Export SVG of the Throttle Graph
	 */
	public void plotExportSVG() {
		plot.addListener(new InvientCharts.ChartSVGAvailableListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void svgAvailable(
					ChartSVGAvailableEvent chartSVGAvailableEvent) {

				System.out.println(chartSVGAvailableEvent.getSVG());
			}
		});
	}

	/**
	 * Used to print the Throttle Graph of the CNKB Component
	 */
	public void plotPrint() {

		plot.print();

	}

	/**
	 * Called to export Table to Excel sheet or CSV
	 */
	public static void exportInteractionTable(String format) {

		if (format.equalsIgnoreCase("excel")) {

			ExcelExport excelExport = new ExcelExport(dataTable);
			excelExport.excludeCollapsedColumns();
			excelExport.setExportFileName("CNKBInteractions.xls");
			excelExport.export();

		} else {

			CsvExport csvExport = new CsvExport(dataTable);
			csvExport.excludeCollapsedColumns();
			csvExport.setExportFileName("CNKBInteractions.csv");
			csvExport.export();

		}

	}

	private class CreateNetworkListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;

		private Long parentId;
		private Vector<CellularNetWorkElementInformation> hits;
		ArrayList<String> selectedTypes;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public CreateNetworkListener(Long parentId,
				Vector<CellularNetWorkElementInformation> hits,
				List<String> selectedTypes) {

			this.parentId = parentId;
			this.hits = hits;
			this.selectedTypes = (ArrayList) selectedTypes;

		}

		private int getInteractionTotalNum() {

			int interactionNum = 0;
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

				ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
						.getSelectedInteractions(selectedTypes);

				interactionNum = interactionNum + arrayList.size();

			}

			return interactionNum;

		}

		@Override
		public void buttonClick(ClickEvent event) {

			if (hits == null || getInteractionTotalNum() == 0) {

				MessageBox mb = new MessageBox(getWindow(), "Warning",
						MessageBox.Icon.INFO,
						"There is no interaction to create a network. ",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
			params.put(CNKBParameters.NETWORK_ELEMENT_INFO, hits);
			params.put(CNKBParameters.SELECTED_INTERACTION_TYPES, selectedTypes);

			ResultSet resultSet = new ResultSet();
			java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
			resultSet.setDateField(date);
			String dataSetName = "Cytoscape - Pending";
			resultSet.setName(dataSetName);
			resultSet.setType("CytoscapeResults");
			resultSet.setParent(parentId);
			resultSet.setOwner(SessionHandler.get().getId());
			FacadeFactory.getFacade().store(resultSet);

			NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
			GeworkbenchRoot.getBlackboard().fire(resultEvent);

			AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
					null, resultSet, params);
			GeworkbenchRoot.getBlackboard().fire(analysisEvent);
		}
	}

	private List<String> getInteractionTypes(
			Vector<CellularNetWorkElementInformation> hits) {

		if (ResultSetlUtil.getUrl() == null)
			loadApplicationProperty();
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		List<String> allInteractionTypes = null;
		try {
			allInteractionTypes = interactionsConnection.getInteractionTypes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> selectedTypes = new ArrayList<String>();

		for (int j = 0; j < hits.size(); j++) {

			ArrayList<InteractionDetail> interactionDetail = hits.get(j)
					.getSelectedInteractions(allInteractionTypes);
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
	
	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";		 
		ResultSetlUtil.setUrl(interactionsServletUrl);
		ResultSetlUtil.setTimeout(3000);
		 
	}
	
	

}
