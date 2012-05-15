package org.geworkbenchweb.analysis.CNKB.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.layout.UMenuBar;

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
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * This class displays CNKB results in a Table and also a graph
 * @author Nikhil Reddy
 */
@SuppressWarnings("unused")
public class UCNKBTab extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private VerticalSplitPanel tabPanel;

	ArrayList<Double> interactionConfidence =	new ArrayList<Double>();
	
	ArrayList<Double> ppConfidence 			= 	new ArrayList<Double>();
	
	ArrayList<Double> pdnaConfidence 		= 	new ArrayList<Double>();
	
	ArrayList<Double> mtfConfidence 		= 	new ArrayList<Double>();
	
	public static InvientCharts plot;
	
	private static UCNKBTab menuBarInstance;
	
	public static UCNKBTab getCNKBTabObject() {
		
		return menuBarInstance;
		
	}
	
	public UCNKBTab(Vector<CellularNetWorkElementInformation> hits) {
	
		setSizeFull();
		setImmediate(true);
		
		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(400, Sizeable.UNITS_PIXELS);
		tabPanel.setStyleName("small");
		tabPanel.setLocked(false);
		
		/* Results Table Code */
		Table dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.setImmediate(true);
		
		IndexedContainer dataIn  = 	new IndexedContainer();

		for(int j=0; j<hits.size();j++) {

			Item item 	= 	dataIn.addItem(j);

			InteractionDetail[] interactionDetail = hits.get(j).getInteractionDetails();
			if(interactionDetail != null) {
				for(InteractionDetail interaction : interactionDetail) {
					
					interactionConfidence.add(interaction.getConfidence());
					
					if(interaction.getInteractionType().equalsIgnoreCase("protein-protein")) {
					
						ppConfidence.add(interaction.getConfidence());
						
					}else if(interaction.getInteractionType().equalsIgnoreCase("protein-dna")) {
						
						pdnaConfidence.add(interaction.getConfidence());
						
					}else if(interaction.getInteractionType().equalsIgnoreCase("modulator-TF")) {
						
						mtfConfidence.add(interaction.getConfidence());
						
					}	
				}
			}	
			HashMap<String, Integer> interactionNumMap = hits.get(j).getInteractionNumMap();

			dataIn.addContainerProperty("Marker", String.class, null);
			dataIn.addContainerProperty("Gene", String.class, null);
			dataIn.addContainerProperty("Gene Type", String.class, null);
			dataIn.addContainerProperty("Annotation", String.class, null);
			dataIn.addContainerProperty("Modulator-TF #", Integer.class, null);
			dataIn.addContainerProperty("Protein-DNA #", Integer.class, null);
			dataIn.addContainerProperty("Protein-Protein #", Integer.class, null);

			item.getItemProperty("Marker").setValue(hits.get(j).getdSGeneMarker());
			if(hits.get(j).getdSGeneMarker().getShortName() == hits.get(j).getdSGeneMarker().getGeneName()) {

				item.getItemProperty("Gene").setValue("--");

			} else {

				item.getItemProperty("Gene").setValue(hits.get(j).getdSGeneMarker().getGeneName());

			}

			item.getItemProperty("Gene Type").setValue(hits.get(j).getGeneType());
			item.getItemProperty("Annotation").setValue(hits.get(j).getGoInfoStr());
			item.getItemProperty("Modulator-TF #").setValue(interactionNumMap.get("modulator-TF"));
			item.getItemProperty("Protein-DNA #").setValue(interactionNumMap.get("protein-dna"));
			item.getItemProperty("Protein-Protein #").setValue(interactionNumMap.get("protein-protein"));

		}

		dataTable.setContainerDataSource(dataIn);
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth("Annotation", 150);
		dataTable.setColumnHeaders(new String[] {"Marker", "Gene", "Gene Type", "Annotation", 
				"Modulator-TF #", "Protein-DNA #", "Protein-Protein #" });
		
		
		plot = drawPlot();
		tabPanel.setFirstComponent(plot);
		tabPanel.setSecondComponent(dataTable);
		addComponent(tabPanel);
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
	
	public void plotExportSVG() {

		plot.addListener(new InvientCharts.ChartSVGAvailableListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void svgAvailable(
					ChartSVGAvailableEvent chartSVGAvailableEvent) {

				chartSVGAvailableEvent.getSVG();
			}
		});

	}

	public void plotPrint() {

		plot.print();

	}
	
}
