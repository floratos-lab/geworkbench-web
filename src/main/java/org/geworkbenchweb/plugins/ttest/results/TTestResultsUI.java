package org.geworkbenchweb.plugins.ttest.results;

import java.awt.Color;
import java.util.LinkedHashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.TTestResult;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.MarkerStates;
import com.vaadin.addon.charts.model.PlotOptionsScatter;
import com.vaadin.addon.charts.model.State;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.ZoomType;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Visualization for TTest Results is done in this class.
 * @author Nikhil
 */

public class TTestResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = -6720344403076533166L;
	private static Log log = LogFactory.getLog(TTestResultsUI.class);

	private final TTestResult tTestResultSet;

	final private Long datasetId;
	final private Long parentDatasetId;
	
	public TTestResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null || dataSetId==0) {
			tTestResultSet = null;
			parentDatasetId = null;
			return;
		}

		setImmediate(true);
		setSizeFull();	

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		parentDatasetId = resultSet.getParent();
		Long id = resultSet.getDataId();
		if(id==null) { // pending node
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			tTestResultSet = null;
			return;
		}
		tTestResultSet = FacadeFactory.getFacade().find(TTestResult.class, id);

		addComponent(drawPlot());
	}

	/**
	 * This method draws the Volcano plot using Invient Charts Add-on.
	 */
	private Chart drawPlot() {

		Chart chart = new Chart();
		chart.setWidth("100%");
		chart.setHeight("100%");
		
		Configuration chartConfig = chart.getConfiguration();
		chartConfig.getChart().setType(ChartType.SCATTER);
		chartConfig.getChart().setZoomType(ZoomType.XY);

		chartConfig.getTitle().setText(
				"T-Test");

		chartConfig.getTooltip().setFormatter(
				"function() {"
						+ " return '' + this.point.name + ', ' +  this.x + ', ' + this.y + ''; "
						+ "}");

		XAxis xAxis = new XAxis();
		xAxis.setTitle("Fold Change Log2(ratio)");
		xAxis.setStartOnTick(true);
		xAxis.setEndOnTick(true);
		xAxis.setShowLastLabel(true);
		chartConfig.addxAxis(xAxis);

		YAxis yAxis = new YAxis();
		yAxis.setTitle("Significance(-Log10)");
		chartConfig.addyAxis(yAxis);

		Marker marker = new Marker(true);
		marker.setRadius(5);

		State hover = new State();
		hover.setEnabled(true);
		/*FIXME
		hover.setLineColor(new RGB(100, 100, 100));*/
		
		PlotOptionsScatter plotOptions = new PlotOptionsScatter();
		plotOptions.setMarker(marker);
		plotOptions.setStates(new MarkerStates(hover));

		DataSeries series = new DataSeries("Significant Markers");
		series.setPlotOptions(plotOptions);
		
		double validMinSigValue 	= 	Double.MAX_VALUE;
		double minPlotValue 		= 	Double.MAX_VALUE;
		double maxPlotValue 		= 	Double.MIN_VALUE;

		LinkedHashSet<DataSeriesItem> points 	= 	new LinkedHashSet<DataSeriesItem>();

		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, parentDatasetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] markerLabels = microarray.getMarkerLabels();

		log.debug("t-test result ID "+tTestResultSet.getId());
		/* Logic in this loop is copied from geWorkbench(swing) volcano plot*/
		for (int i = 0; i < tTestResultSet.getSignificantIndex().length; i++) {
			
			int index = tTestResultSet.getSignificantIndex()[i];
			String mark 	= 	markerLabels[index];
			double sigValue 	= 	tTestResultSet.getpValue()[index];
		
			if (sigValue >= 0.0 && sigValue < 4.9E-45  ) {
				sigValue = 4.9E-45;
			} 
			else if (sigValue < 0) {
				//log.debug("Significance less than 0, (" + sigValue + ") setting to 1 for the moment.");
				sigValue = 1;
			}

			if (sigValue < validMinSigValue) {
				validMinSigValue = sigValue;
			}


			double xVal = tTestResultSet.getFoldChange()[index];

			if (!Double.isNaN(xVal) && !Double.isInfinite(xVal)) {
				double yVal = -Math.log10(sigValue);
				double plotVal = Math.abs(xVal) * Math.abs(yVal);
				if (plotVal < minPlotValue) {
					minPlotValue = plotVal;
				}
				if (plotVal > maxPlotValue) {
					maxPlotValue = plotVal;
				}
				DataSeriesItem a = new DataSeriesItem(xVal, yVal);
				a.setName(mark);
				points.add(a);
			} else {
				//log.debug("Marker " + i + " was infinite or NaN.");
			}

		}
		
		GMTColorPalette.ColorRange[] range = {new GMTColorPalette.ColorRange(minPlotValue, Color.BLUE.brighter(), maxPlotValue - (maxPlotValue / 3), Color.BLUE),
                new GMTColorPalette.ColorRange(maxPlotValue - (maxPlotValue / 3), Color.BLUE, maxPlotValue, Color.RED)};
		GMTColorPalette colormap = new GMTColorPalette(range);

		LinkedHashSet<DataSeriesItem > newPoints = new LinkedHashSet<DataSeriesItem >();
		for(int i=0; i<points.size(); i++) {
			
			DataSeriesItem  a =  (DataSeriesItem ) (points.toArray())[i] ;
			Color aC = colormap.getColor(Math.abs(a.getX().doubleValue()) * Math.abs(a.getY().doubleValue()));
			
			Marker pMarker = new Marker(true);
			pMarker.setRadius(5);
			pMarker.setFillColor(new SolidColor(aC.getRed(), aC.getGreen(), aC.getBlue()));

			a.setMarker(pMarker);
			newPoints.add(a);
		}
		for(DataSeriesItem point : newPoints){
			series.add(point);
		}
		chartConfig.setSeries(series);
		return chart;
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
