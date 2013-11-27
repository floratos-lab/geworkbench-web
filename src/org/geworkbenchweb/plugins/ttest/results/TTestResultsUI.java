package org.geworkbenchweb.plugins.ttest.results;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.utils.UserDirUtils;

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

	private static final long serialVersionUID = 1L;

	private DSSignificanceResultSet<DSGeneMarker> significance;

	final private Long datasetId;
	
	@SuppressWarnings("unchecked")
	public TTestResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;

		setImmediate(true);
		setSizeFull();	

		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		}
		if(! (object instanceof DSSignificanceResultSet)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			addComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			return;
		}
		significance =  (DSSignificanceResultSet<DSGeneMarker>)object;

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
		
		/* Logic in this loop is copied from geWorkbench(swing) volcano plot*/
		for (int i = 0; i < significance.getSignificantMarkers().size(); i++) {
			
			DSGeneMarker mark 	= 	significance.getSignificantMarkers().get(i);
			double sigValue 	= 	significance.getSignificance(mark);
		
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


			double xVal = significance.getFoldChange(mark);

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
				a.setName(mark.getLabel());
				points.add(a);
			} else {
				//log.debug("Marker " + i + " was infinite or NaN.");
			}

		}
		
		GMTColorPalette.ColorRange[] range = {new GMTColorPalette.ColorRange(minPlotValue, Color.BLUE.brighter(), maxPlotValue - (maxPlotValue / 3), Color.BLUE),
                new GMTColorPalette.ColorRange(maxPlotValue - (maxPlotValue / 3), Color.BLUE, maxPlotValue, Color.RED)};
		GMTColorPalette colormap = new GMTColorPalette(range);

		for(int i=0; i<points.size(); i++) {
			
			DataSeriesItem a =  (DataSeriesItem) (points.toArray())[i] ;
			Color aC = colormap.getColor(Math.abs(a.getX().doubleValue()) * Math.abs(a.getY().doubleValue()));
			
			Marker pMarker = new Marker(true);
			pMarker.setRadius(5);
			pMarker.setFillColor(new SolidColor(aC.getRed(), aC.getGreen(), aC.getBlue()));

			a.setMarker(pMarker);
			series.add(a);
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
