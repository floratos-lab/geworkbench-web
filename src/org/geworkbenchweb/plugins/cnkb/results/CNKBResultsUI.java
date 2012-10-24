package org.geworkbenchweb.plugins.cnkb.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.visualizations.Cytoscape;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

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

/**
 * This class displays CNKB results in a Table and also a graph
 * @author Nikhil Reddy
 */
@SuppressWarnings("unused")
public class CNKBResultsUI extends TabSheet {

	private static final long serialVersionUID = 1L;

	private VerticalSplitPanel tabPanel;

	ArrayList<Double> interactionConfidence =	new ArrayList<Double>();
	
	ArrayList<Double> ppConfidence 			= 	new ArrayList<Double>();
	
	ArrayList<Double> pdnaConfidence 		= 	new ArrayList<Double>();
	
	ArrayList<Double> mtfConfidence 		= 	new ArrayList<Double>();
	
	public static InvientCharts plot;
	
	private static CNKBResultsUI menuBarInstance;
	
	private static Table dataTable;
	
	private Cytoscape cy;
	
	public CNKBResultsUI(Long dataSetId) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id", parameters);
		
		@SuppressWarnings("unchecked")
		Vector<CellularNetWorkElementInformation> hits = (Vector<CellularNetWorkElementInformation>) ObjectConversion.toObject(data.get(0).getData());
	
		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.TABSHEET_SMALL);
		
		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(400, Sizeable.UNITS_PIXELS);
		tabPanel.setStyleName("small");
		tabPanel.setLocked(false);
		
		 //Results Table Code 
		dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.setImmediate(true);
		
		/* Preparing data for cytoscape */
		ArrayList<String> nodes = new ArrayList<String>();
		ArrayList<String> edges = new ArrayList<String>();
		
		IndexedContainer dataIn  = 	new IndexedContainer();

		List<String> selectedTypes = new ArrayList<String>();
		selectedTypes.add("protein-protein");
		selectedTypes.add("protein-dna");
		selectedTypes.add("modulator-TF");
		
		for(int j=0; j<hits.size();j++) {
			Item item 								= 	dataIn.addItem(j);
			ArrayList<InteractionDetail> interactionDetail 	= 	hits.get(j).getSelectedInteractions(selectedTypes);
			if(interactionDetail != null) {
				for(InteractionDetail interaction : interactionDetail) {				
					interactionConfidence.add(interaction.getConfidenceValue(interaction.getConfidenceTypes().get(0)));
					if(interaction.getInteractionType().equalsIgnoreCase("protein-protein")) {
						ppConfidence.add(interaction.getConfidenceValue(interaction.getConfidenceTypes().get(0)));
					}else if(interaction.getInteractionType().equalsIgnoreCase("protein-dna")) {
						pdnaConfidence.add(interaction.getConfidenceValue(interaction.getConfidenceTypes().get(0)));
					}else if(interaction.getInteractionType().equalsIgnoreCase("modulator-TF")) {
						mtfConfidence.add(interaction.getConfidenceValue(interaction.getConfidenceTypes().get(0)));
					}	
				}
			}	

			dataIn.addContainerProperty("Marker", String.class, null);
			dataIn.addContainerProperty("Gene", String.class, null);
			dataIn.addContainerProperty("Gene Type", String.class, null);
			dataIn.addContainerProperty("Annotation", String.class, null);
			dataIn.addContainerProperty("Modulator-TF #", Integer.class, null);
			dataIn.addContainerProperty("Protein-DNA #", Integer.class, null);
			dataIn.addContainerProperty("Protein-Protein #", Integer.class, null);

			item.getItemProperty("Marker").setValue(hits.get(j).getdSGeneMarker().getLabel());
			if(hits.get(j).getdSGeneMarker().getShortName() == hits.get(j).getdSGeneMarker().getGeneName()) {
				item.getItemProperty("Gene").setValue("--");
			} else {
				item.getItemProperty("Gene").setValue(hits.get(j).getdSGeneMarker().getGeneName());
			}

			item.getItemProperty("Gene Type").setValue(hits.get(j).getGeneType());
			item.getItemProperty("Annotation").setValue(hits.get(j).getGoInfoStr());
			item.getItemProperty("Modulator-TF #").setValue((hits.get(j).getSelectedInteractions("modulator-TF")).size());
			item.getItemProperty("Protein-DNA #").setValue(hits.get(j).getSelectedInteractions("protein-dna").size());
			item.getItemProperty("Protein-Protein #").setValue(hits.get(j).getSelectedInteractions("protein-protein").size());
			
			
			
			if(interactionDetail.size() != 0) {
				for(InteractionDetail interaction: interactionDetail) {
					String edge = hits.get(j).getdSGeneMarker().getGeneName() 
							+ ","
							+ interaction.getdSGeneName();
					String node1 = 	 hits.get(j).getdSGeneMarker().getGeneName()
							+ ","
							+  hits.get(j).getdSGeneMarker().getGeneName();
					String node2 =	interaction.getdSGeneName()
							+ ","
							+ interaction.getdSGeneName();
					if(edges.isEmpty()) {
						edges.add(edge);
					}else if(!edges.contains(edge)) {
						edges.add(edge);
					}
					if(node1 == node2) {
						if(!nodes.contains(node1 + ",1")){
							nodes.add(node1+",1");
							if(nodes.contains(node1+",0")) {
								nodes.remove(node1 + ",0");
							}
						}else if(!nodes.contains(node1 + ",0")) {
							nodes.add(node1+",0");
						}
					}else if(nodes.isEmpty()) {
						nodes.add(node1 + ",1");
						nodes.add(node2 + ",0");
					} else { 
						if(!nodes.contains(node1 + ",1")) {
							nodes.add(node1 + ",1");
							if(nodes.contains(node1+",0")) {
								nodes.remove(node1 + ",0");
							}
						}
						if(!nodes.contains(node2 + ",1")) {	
							if(!nodes.contains(node2 + ",0")) {
								nodes.add(node2 + ",0");
							}
						}
					}
				}
			}
		}

		dataTable.setContainerDataSource(dataIn);
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth("Annotation", 150);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);
		
		plot = drawPlot();
		tabPanel.setFirstComponent(plot);
		tabPanel.setSecondComponent(dataTable);
		addTab(tabPanel, "CNKB Results");

		cy = new Cytoscape();
		cy.setImmediate(true);
		cy.setSizeFull();
		cy.setCaption("Cytoscape");
		
		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
		edgeArray = edges.toArray(edgeArray);

		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);
		cy.setNetwork("false");

		addTab(cy);
		addTab(null, "Cytoscape");
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
        chartConfig
                .getTooltip()
                .setFormatterJsFunc(
                        "function() { " 
                                + " return '<b>' + this.series.name + '</b><br/>' +  " +
                                "Math.round(((this.x+0.005)*100))/100 + ' '+ " +
                                "' to 1 - ' + " +
                                "this.y + ' interactions'"
                                + "}");

        InvientCharts chart = new InvientCharts(chartConfig);

		XYSeries seriesData = new XYSeries("Total Distribution");
		seriesData.setSeriesPoints(getTotalDistribution(seriesData));
		chart.addSeries(seriesData);

		seriesData = new XYSeries("Modulator-TF");
		seriesData.setSeriesPoints(getModTFDistribution(seriesData));
		chart.addSeries(seriesData);

		seriesData = new XYSeries("Protein-DNA");
		seriesData.setSeriesPoints(getProteinDNADistribution(seriesData));
		chart.addSeries(seriesData);

		seriesData = new XYSeries("Protein-Protein");
		seriesData.setSeriesPoints(getPPDistribution(seriesData));
		chart.addSeries(seriesData);
		return chart;
	}
	
	/**
	 * Method is used to calculate the graph points for Protein-Protein Interactions
	 */
	private LinkedHashSet<DecimalPoint> getPPDistribution(XYSeries seriesData) {

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		Double x 			= 	null;
		Double y 			=	null;
		int[] distribution 	= 	new int[101];
		for(int m=0; m<ppConfidence.size(); m++) {
			int confidence = (int) ((ppConfidence.get(m)) * 100);
			if (confidence <= distribution.length && confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.005d;
		for(int j=0; j<distribution.length; j++) {
			y = (double) (distribution[j]);
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + 0.01d;
		}
		return points;
	}

	/**
	 * Method is used to calculate the graph points for Protein-DNA interactions
	 */
	private LinkedHashSet<DecimalPoint> getProteinDNADistribution(
			XYSeries seriesData) {
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();

		Double x 			= 	null;
		Double y 			=	null;
		int[] distribution 	= 	new int[101];
		for(int m=0; m<pdnaConfidence.size(); m++) {
			int confidence = (int) ((pdnaConfidence.get(m)) * 100);
			if (confidence <= distribution.length && confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.005d;
		for(int j=0; j<distribution.length; j++) {
			y = (double) (distribution[j]);
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + 0.01d;
		}
		return points;
	}

	/**
	 * Method is used to calculate the graph points for Modulator-TF Interactions
	 */
	private LinkedHashSet<DecimalPoint> getModTFDistribution(XYSeries seriesData) {
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();

		Double x 			= 	null;
		Double y 			=	null;
		int[] distribution 	= 	new int[101];
		for(int m=0; m<mtfConfidence.size(); m++) {
			int confidence = (int) ((mtfConfidence.get(m)) * 100);
			if (confidence <= distribution.length && confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.005d;
		for(int j=0; j<distribution.length; j++) {
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
		
		Double x 			= 	null;
		Double y 			=	null;
		int[] distribution 	= 	new int[101];
		
		for(int m=0; m<interactionConfidence.size(); m++) {
			int confidence = (int) ((interactionConfidence.get(m)) * 100);
			if (confidence <= distribution.length && confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.005d;
		for(int j=0; j<distribution.length; j++) {
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
		
		if(format.equalsIgnoreCase("excel")) {
			
			ExcelExport excelExport = new ExcelExport(dataTable);
			excelExport.excludeCollapsedColumns();
			excelExport.setExportFileName("CNKBInteractions.xls");
			excelExport.export();
			
		}else {
			
			CsvExport csvExport = new CsvExport(dataTable);
			csvExport.excludeCollapsedColumns();
			csvExport.setExportFileName("CNKBInteractions.csv");
			csvExport.export();
		
		}
		
	}
}
