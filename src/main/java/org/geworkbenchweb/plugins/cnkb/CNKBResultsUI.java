package org.geworkbenchweb.plugins.cnkb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
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

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.Grid;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Margin;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * This class displays CNKB results in a Table and also a graph
 * 
 * @author Nikhil Reddy
 */
public class CNKBResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(CNKBResultsUI.class);

	private static final String COLUMN_GO_ANNOTATIONS = "GO Annotations";

	private VerticalSplitPanel tabPanel;
	private VerticalSplitPanel throttlePanel;

	private ArrayList<Double> totalInteractionConfidence = new ArrayList<Double>();

	private Map<String, List<Double>> ConfidentDataMap = new HashMap<String, List<Double>>();

	protected InvientCharts plot;

	Table dataTable;
	private Map<String, String> confidentTypeMap = null;

	final private Long datasetId;

	public CNKBResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		if(id==null) { // pending node
			addComponent(new Label("Pending computation - ID "+ dataSetId));
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
	
	private void init(final CNKBResultSet cnkbResult, Long parentId) {
		if (confidentTypeMap == null)
			loadConfidentTypeMap();
		
		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.TABSHEET_SMALL);

		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(250, Sizeable.UNITS_PIXELS);
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
					return ((Table)source).getItem(itemId).getItemProperty(propertyId).getValue().toString();
				} catch (NullPointerException e) {
					return "";
				}
			}                                                          
			 
		});
		dataTable.addListener(new ItemClickListener() {

			private static final long serialVersionUID = 3329109033365762704L;

			@Override
			public void itemClick(ItemClickEvent event) {
				Item item = dataTable.getItem(event.getItemId());
				String gene = (String) item.getItemProperty("Gene").getValue();
				String markerLabel = (String) item.getItemProperty("Marker").getValue();
				new DetailedInteractionsView(cnkbResult).display(gene, markerLabel, CNKBResultsUI.this, confidentTypeMap);
			}

		});
		
		Short confidenceType = cnkbResult.getCellularNetworkPreference().getSelectedConfidenceType();
		double maxValue = getMaxValue(cnkbResult.getCellularNetworkPreference().getMaxConfidenceValue(confidenceType));
		
		plot = drawPlot(cnkbResult, 0, maxValue);
	 
		
		MenuBar menuBar = new MenuBar();
		menuBar.setStyleName("transparent");
		menuBar.addItem("Create Network", new CreateNetworkCommand(parentId,
				cnkbResult)).setStyleName("plugin");
		menuBar.addItem("Export", new Command() {

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
		menuBar.addItem("Help", new Command() {

			private static final long serialVersionUID = -1970832620889340547L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getWindow().open(new ExternalResource("http://wiki.c2b2.columbia.edu/workbench/index.php/Cellular_Networks_KnowledgeBase_web"), "_blank");
			}
			
		}).setStyleName("plugin");;
		
		addComponent(menuBar);		
	
		ThrottleSlider slider = new ThrottleSlider(cnkbResult, 0, maxValue, this);	
		slider.setStyleName("small");
		throttlePanel = new VerticalSplitPanel();
		throttlePanel.setSizeFull();	 
		throttlePanel.setSplitPosition(200, Sizeable.UNITS_PIXELS);
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
	
	/* this version for 'orphan' result */
	public CNKBResultsUI(CNKBResultSet cnkbResult) {
		datasetId = null;
		init(cnkbResult, null);
	}

	/**
	 * This method draws the Throttle Graph using Invient Charts Add-on.
	 * 
	 */
	protected InvientCharts drawPlot(CNKBResultSet resultSet, double minX, double maxX) {

		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setMargin(new Margin());
		chartConfig.getGeneralChartConfig().getMargin().setRight(30);	 
		chartConfig.getTitle().setText(resultSet.getCellularNetworkPreference().getTitle());

		NumberXAxis numberXAxis = new NumberXAxis();
		Short confidenceType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		String axisTile = null;
		if (confidenceType != null)
			axisTile = confidentTypeMap.get(confidenceType.toString());
		if (axisTile != null)
			numberXAxis.setTitle(new AxisTitle(axisTile));
		else			
		   numberXAxis.setTitle(new AxisTitle("Likelihood"));
		numberXAxis.setMinPadding(0.05);
		LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxesSet.add(numberXAxis);
		chartConfig.setXAxes(xAxesSet);		
		
		NumberYAxis numberYAxis = new NumberYAxis();
		numberYAxis.setGrid(new Grid());
		numberYAxis.getGrid().setLineWidth(1);
		numberYAxis.setMin(0d);
		
		numberYAxis.setTitle(new AxisTitle("# Interactions"));
		LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxesSet.add(numberYAxis);
		chartConfig.setYAxes(yAxesSet);	 
		chartConfig.getTooltip().setEnabled(true);
		// Series data label formatter
		LineConfig lineCfg = new LineConfig();
		chartConfig.addSeriesConfig(lineCfg);
       
	  
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
		    chartConfig.getTooltip().setFormatterJsFunc(
				"function() { "
						+ " return '<b>' + this.series.name + '</b><br/>' +  "
						+ "Math.round(((this.x)*100))/100 + ' '+ "
						+ "' to 1 - ' + " + "this.y + ' interactions'" + "}");

		else //this need to be fixed. Don't know how to pass maxX to function
			 chartConfig.getTooltip().setFormatterJsFunc(
						"function() { "
								+ " return '<b>' + this.series.name + '</b><br/>' +  "
								+ "Math.round(((this.x)*100))/100 + ' to max value - '+ "
								+ "this.y + ' interactions'" + "}");
		 
		InvientCharts chart = new InvientCharts(chartConfig);	
		chart.setHeight("100%");
		XYSeries seriesData = new XYSeries("Total Distribution");
		seriesData.setSeriesPoints(getTotalDistribution(seriesData, smallestIncrement));
		chart.addSeries(seriesData);

		for (String interactionType : ConfidentDataMap.keySet()) {
			seriesData = new XYSeries(interactionType);			 
			seriesData.setSeriesPoints(getDistribution(interactionType, smallestIncrement));
			chart.addSeries(seriesData);
		}

		return chart;
	}

	/**
	 * Method is used to calculate the graph points for Protein-Protein
	 * Interactions
	 */
	private LinkedHashSet<DecimalPoint> getDistribution(String interactionType, double smallestIncrement) {

		XYSeries seriesData = new XYSeries(interactionType);
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
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
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + smallestIncrement;
		}
		return points;
	}

	/**
	 * Method is used to calculate the graph points for all Interactions
	 */
	private LinkedHashSet<DecimalPoint> getTotalDistribution(XYSeries seriesData, double smallestIncrement) {

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();

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
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + smallestIncrement;
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
			List<String> selectedTypes = cnkbResultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();
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
			if (hits == null || getInteractionTotalNum(cnkbResultSet.getCellularNetworkPreference().getSelectedConfidenceType()) == 0) {

				MessageBox mb = new MessageBox(getWindow(), "Warning",
						MessageBox.Icon.INFO,
						"There is no interaction to create a network. ",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
			params.put(CNKBParameters.CNKB_RESULTSET, cnkbResultSet);
			
			if (datasetId == null) { /* 'orphan' result of CNKB */
				Network network = new NetworkCreation(parentId).createNetwork(params);
				System.out.println(network);

				// direct show the orphan result
				Window w = getApplication().getMainWindow();
				ComponentContainer content = w.getContent();
				if (content instanceof UMainLayout) {
					UMainLayout m = (UMainLayout) content;
					m.setPluginViewContent(new NetworkViewer(network));
				} else {
					log.error("wrong type of plugin view content: " + content);
				}
				return;
			}
			 
			ResultSet resultSet = new ResultSet();
			java.sql.Timestamp timestamp =	new java.sql.Timestamp(System.currentTimeMillis());
			resultSet.setTimestamp(timestamp);
			String dataSetName = "Cytoscape - Pending";
			resultSet.setName(dataSetName);
			resultSet.setType(Network.class.getName());
			resultSet.setParent(parentId);
			resultSet.setOwner(SessionHandler.get().getId());
			FacadeFactory.getFacade().store(resultSet);
			
			generateHistoryString(datasetId, hits.get(0).getThreshold(), resultSet);

			GeworkbenchRoot app = (GeworkbenchRoot) CNKBResultsUI.this
					.getApplication();
			app.addNode(resultSet);

			/* this is a special case of the work flow: NetworkCreation uses the interface method execute but ignore the DataSet argument. */
			AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
					resultSet, params, new NetworkCreation(parentId));
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
			dataIn.addContainerProperty("Interactome", String.class, null);
			dataIn.addContainerProperty(COLUMN_GO_ANNOTATIONS, String.class, null);
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
	
	private void loadConfidentTypeMap()
	{
		if (ResultSetlUtil.getUrl() == null || ResultSetlUtil.getUrl().trim().equals(""))
		{
			String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";			 
			ResultSetlUtil.setUrl(interactionsServletUrl);
			ResultSetlUtil.setTimeout(3000);
		}
		
		CNKBServletClient interactionsConnection = new CNKBServletClient();
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
		plot = (InvientCharts)throttlePanel.getFirstComponent();
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
	
	private void generateHistoryString(long cnkbResultSetId, double threshold, ResultSet cytoscapeResultSet) {
		
		Map<String, Object> eParams = new HashMap<String, Object>();
		eParams.put("parent", cnkbResultSetId); 
		List<AbstractPojo> histories =  FacadeFactory.getFacade().list("Select h from DataHistory as h where h.parent =:parent", eParams);
		DataHistory dH = (DataHistory) histories.get(0);			 
		
		String[] temp = dH.getData().split("Markers used");	
		
		StringBuilder histBuilder = new StringBuilder();		
		
		histBuilder.append(temp[0]);
		histBuilder.append("Threshold - " +  threshold + "\n");
		histBuilder.append("Markers used" + temp[1]);
		
		 

		DataHistory his = new DataHistory();
		his.setParent(cytoscapeResultSet.getId());
		his.setData(histBuilder.toString());
		FacadeFactory.getFacade().store(his);
	}

}
