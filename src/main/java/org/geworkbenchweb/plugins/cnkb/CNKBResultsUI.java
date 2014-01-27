package org.geworkbenchweb.plugins.cnkb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.plugins.cnkb.CNKBParameters;
import org.geworkbenchweb.plugins.cnkb.CNKBResultSet;
import org.geworkbenchweb.plugins.cnkb.NetworkCreation;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;

/**
 * This class displays CNKB results in a Table and also a graph
 * 
 * @author Nikhil Reddy
 */
@SuppressWarnings("unused")
public class CNKBResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(CNKBResultsUI.class);

	private VerticalSplitPanel tabPanel;
	private VerticalSplitPanel throttlePanel;

	private ArrayList<Double> totalInteractionConfidence = new ArrayList<Double>();

	private Map<String, List<Double>> ConfidentDataMap = new HashMap<String, List<Double>>();

	protected Chart plot;

	private CNKBResultsUI menuBarInstance;

	protected Table dataTable;
	private Map<String, String> confidentTypeMap = null;

	final private Long datasetId;

	public CNKBResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;

		Object object;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		final CNKBResultSet  resultSet = (CNKBResultSet)object;
	 
		if (confidentTypeMap == null)
			loadConfidentTypeMap();
		
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
		tabPanel.setSplitPosition(250, Unit.PIXELS);
		tabPanel.setStyleName("small");
		tabPanel.setLocked(false);

		// Results Table Code
		dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.setImmediate(true);
		
		dataTable.setContainerDataSource(getIndexedContainer(resultSet));
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth("Annotation", 150);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);		
	 
		
		Short confidenceType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		double maxValue = getMaxValue(resultSet.getCellularNetworkPreference().getMaxConfidenceValue(confidenceType));
		
		plot = drawPlot(resultSet, 0, maxValue);
	 
		
		MenuBar menuBar = new MenuBar();
		menuBar.setStyleName("transparent");
		menuBar.addItem("Create Network", new CreateNetworkCommand(parentId,
				resultSet)).setStyleName("plugin");
		menuBar.addItem("Export", new Command() {

			private static final long serialVersionUID = -4510368918141762449L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				new ExcelExport(dataTable).export();	
			}
			
		}).setStyleName("plugin");
		
		addComponent(menuBar);		
	
		ThrottleSlider slider = new ThrottleSlider(resultSet, 0, maxValue, this);	
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

	/**
	 * This method draws the Throttle Graph using Invient Charts Add-on.
	 * 
	 */
	protected Chart drawPlot(CNKBResultSet resultSet, double minX, double maxX) {

		Chart chart = new Chart();
		chart.setHeight("100%");

		Configuration chartConfig = chart.getConfiguration();
		chartConfig.getChart().setMarginRight(30);
		chartConfig.getTitle().setText(resultSet.getCellularNetworkPreference().getTitle());

		XAxis numberXAxis = new XAxis();
		Short confidenceType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		String axisTile = null;
		if (confidenceType != null)
			axisTile = confidentTypeMap.get(confidenceType.toString());
		if (axisTile != null)
			numberXAxis.setTitle(axisTile);
		else			
		   numberXAxis.setTitle("Likelihood");
		numberXAxis.setMinPadding(0.05);
		chartConfig.addxAxis(numberXAxis);
		
		YAxis numberYAxis = new YAxis();
		numberYAxis.setGridLineWidth(1);
		numberYAxis.setMin(0d);
		
		numberYAxis.setTitle("# Interactions");
		chartConfig.addyAxis(numberYAxis);
		chartConfig.getTooltip().setEnabled(true);

		double smallestIncrement = 0.01d;	   
		Double maxConfidenceValue = resultSet.getCellularNetworkPreference().getMaxConfidenceValue(confidenceType);
		if (maxConfidenceValue != null && maxConfidenceValue > 1) {	 
			smallestIncrement =  maxX / 100;			 
		}
		else		
		    numberXAxis.setMax(maxX);
		
		numberXAxis.setMin(minX);
	 
		/* Tooltip formatter */
		if (maxConfidenceValue != null && maxConfidenceValue <= 1 )
		    chartConfig.getTooltip().setFormatter(
				"function() { "
						+ " return '<b>' + this.series.name + '</b><br/>' +  "
						+ "Math.round(((this.x+0.005)*100))/100 + ' '+ "
						+ "' to 1 - ' + " + "this.y + ' interactions'" + "}");

		else //this need to be fixed. Don't know how to pass maxX to function
			 chartConfig.getTooltip().setFormatter(
						"function() { "
								+ " return '<b>' + this.series.name + '</b><br/>' +  "
								+ "Math.round(((this.x+0.005)*100))/100 + ' to max value - '+ "
								+ "this.y + ' interactions'" + "}");
		 
		DataSeries seriesData = new DataSeries("Total Distribution");
		seriesData.setData(getTotalDistribution(smallestIncrement));
		chartConfig.addSeries(seriesData);

		for (String interactionType : ConfidentDataMap.keySet()) {
			seriesData = new DataSeries(interactionType);			 
			seriesData.setData(getDistribution(interactionType, smallestIncrement));
			chartConfig.addSeries(seriesData);
		}

		return chart;
	}

	/**
	 * Method is used to calculate the graph points for Protein-Protein
	 * Interactions
	 */
	private LinkedList<DataSeriesItem> getDistribution(String interactionType, double smallestIncrement) {

		LinkedHashSet<DataSeriesItem> points = new LinkedHashSet<DataSeriesItem>();
		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		List<Double> confidenceList = ConfidentDataMap.get(interactionType);
		for (int m = 0; m < confidenceList.size(); m++) {
			int confidence = (int) ((confidenceList.get(m))  / smallestIncrement);	
			if (confidence >= distribution.length )
			{				
				log.warn("This shall not happen: confidence = " + confidence );
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
			points.add(new DataSeriesItem(x, y));
			x = x + smallestIncrement;
		}
		return new LinkedList<DataSeriesItem>(points);
	}

	/**
	 * Method is used to calculate the graph points for all Interactions
	 */
	private LinkedList<DataSeriesItem> getTotalDistribution(double smallestIncrement) {

		LinkedHashSet<DataSeriesItem> points = new LinkedHashSet<DataSeriesItem>();

		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		for (int m = 0; m < totalInteractionConfidence.size(); m++) {
			int confidence = (int) ((totalInteractionConfidence.get(m))   / smallestIncrement);		 		
			if (confidence >= distribution.length)
			{
				confidence = distribution.length - 1;
				log.warn("This shall not happen: confidence = " + confidence );
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
			points.add(new DataSeriesItem(x, y));
			x = x + smallestIncrement;
		}
		return new LinkedList<DataSeriesItem>(points);
	}

	/**
	 * Called to Export SVG of the Throttle Graph
	 */
	public void plotExportSVG() {
		/*FIXME
		plot.addListener(new InvientCharts.ChartSVGAvailableListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void svgAvailable(
					ChartSVGAvailableEvent chartSVGAvailableEvent) {

				System.out.println(chartSVGAvailableEvent.getSVG());
			}
		});*/
	}

	/**
	 * Used to print the Throttle Graph of the CNKB Component
	 */
	public void plotPrint() {

		/*FIXME
		plot.print();*/

	}

	/**
	 * Called to export Table to Excel sheet or CSV
	 */
	public  void exportInteractionTable(String format) {

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

	private class CreateNetworkCommand implements Command {

		private static final long serialVersionUID = 831124091338570481L;

		final private Long parentId;		
		final private CNKBResultSet resultSet;		 
	 
		public CreateNetworkCommand(final Long parentId,
				final CNKBResultSet resultSet) {

			this.parentId = parentId;
			this.resultSet = resultSet;            
		}

		private int getInteractionTotalNum(short confidentType) {

			int interactionNum = 0;
			Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
			List<String> selectedTypes = resultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

				ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
						.getSelectedInteractions(selectedTypes, confidentType);

				interactionNum = interactionNum + arrayList.size();

			}

			return interactionNum;

		}

		@Override
		public void menuSelected(MenuItem selectedItem) {

			Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
			if (hits == null || getInteractionTotalNum(resultSet.getCellularNetworkPreference().getSelectedConfidenceType()) == 0) {

				MessageBox.showPlain(Icon.INFO, "Warning",
						"There is no interaction to create a network. ",
						ButtonId.OK);
				return;
			}
			HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
			params.put(CNKBParameters.CNKB_RESULTSET, resultSet);
			 
			ResultSet resultSet = new ResultSet();
			java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
			resultSet.setDateField(date);
			String dataSetName = "Cytoscape - Pending";
			resultSet.setName(dataSetName);
			resultSet.setType(AdjacencyMatrix.class.getName());
			resultSet.setParent(parentId);
			resultSet.setOwner(SessionHandler.get().getId());
			FacadeFactory.getFacade().store(resultSet);

			NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
			GeworkbenchRoot.getBlackboard().fire(resultEvent);

			/* this is a special case of the work flow: NetworkCreation uses the interface method execute but ignore the DataSet argument. */
			AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
					0L, resultSet, params, new NetworkCreation());
			GeworkbenchRoot.getBlackboard().fire(analysisEvent);
		}
	}

	private List<String> getInteractionTypes(CNKBResultSet  resultSet)
	{

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
	
	IndexedContainer getIndexedContainer(CNKBResultSet  resultSet)
	{
		Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
		final Short confidentType  = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
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
			dataIn.addContainerProperty("Annotation", String.class, null);
			for (String selectedType : selectedTypes)
				dataIn.addContainerProperty(selectedType + " #", Integer.class,
						null);

			String label = hits.get(j).getMarkerLabel();
			String geneSymbol = map.get(label);
			item.getItemProperty("Marker").setValue(label);
			if(geneSymbol!=null)
				item.getItemProperty("Gene").setValue(geneSymbol);

			item.getItemProperty("Gene Type").setValue(
					hits.get(j).getGeneType());
			item.getItemProperty("Annotation").setValue(
					hits.get(j).getGoInfoStr());

			for (String selectedType : selectedTypes)
				item.getItemProperty(selectedType + " #").setValue(
						(hits.get(j).getSelectedInteractions(selectedType, confidentType))
								.size());

		}
		
		return dataIn;

	}
	
	private void loadConfidentTypeMap()
	{
		if (ResultSetlUtil.getUrl() == null || ResultSetlUtil.getUrl().trim().equals(""))
		{
			String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";			 
			ResultSetlUtil.setUrl(interactionsServletUrl);
			ResultSetlUtil.setTimeout(3000);
		}
		
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		try{
		   confidentTypeMap =  interactionsConnection.getConfidenceTypeMap();
		}
		   catch(Exception ex)
		{
			   ex.printStackTrace();
		}
		
		 
	}
	
	private double getMaxValue(Double maxConfidenceValue)
	{
	    double maxX  = 1.00001d;	  	 
		if (maxConfidenceValue != null && maxConfidenceValue > 1) {
			int a = (int) Math.log10(maxConfidenceValue);
			double b = maxConfidenceValue / (Math.pow(10, a));
			maxX  = Math.ceil(b);
			maxX = maxX * (Math.pow(10, a));		 
			log.debug("maxConfidenceValue is " + maxConfidenceValue);			 
		}
		 
		return maxX;
	}
	
	protected void updatePlot(CNKBResultSet resultSet, double minX, double maxX)
	{
		throttlePanel.replaceComponent(plot, drawPlot(resultSet, minX, maxX));
		plot = (Chart)throttlePanel.getFirstComponent();
	}

	@Override
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

}
