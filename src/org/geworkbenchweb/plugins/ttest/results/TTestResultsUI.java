package org.geworkbenchweb.plugins.ttest.results;

import java.awt.Color;
import java.util.LinkedHashSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.ZoomType;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.PointConfig;
import com.invient.vaadin.charts.InvientChartsConfig.ScatterConfig;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;

import com.vaadin.ui.VerticalLayout;

/**
 * Visualization for TTest Results is done in this class.
 * @author Nikhil
 */

public class TTestResultsUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private DSSignificanceResultSet<DSGeneMarker> significance;

	@SuppressWarnings("unchecked")
	public TTestResultsUI(Long dataSetId) {

		setImmediate(true);
		setSizeFull();	

		significance =  (DSSignificanceResultSet<DSGeneMarker>) ObjectConversion.toObject(
				UserDirUtils.getResultSet(dataSetId));

		addComponent(drawPlot());
	}

	/**
	 * This method draws the Volcano plot using Invient Charts Add-on.
	 */
	private InvientCharts drawPlot() {

		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setType(SeriesType.SCATTER);
		chartConfig.getGeneralChartConfig().setZoomType(ZoomType.XY);

		chartConfig.getTitle().setText(
				"T-Test");

		chartConfig.getTooltip().setFormatterJsFunc(
				"function() {"
						+ " return '' + this.point.name + ', ' +  this.x + ', ' + this.y + ''; "
						+ "}");

		NumberXAxis xAxis = new NumberXAxis();
		xAxis.setTitle(new AxisTitle("Fold Change Log2(ratio)"));
		xAxis.setStartOnTick(true);
		xAxis.setEndOnTick(true);
		xAxis.setShowLastLabel(true);
		LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxesSet.add(xAxis);
		chartConfig.setXAxes(xAxesSet);

		NumberYAxis yAxis = new NumberYAxis();
		yAxis.setTitle(new AxisTitle("Significance(-Log10)"));
		LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxesSet.add(yAxis);
		chartConfig.setYAxes(yAxesSet);

		ScatterConfig scatterCfg = new ScatterConfig();

		SymbolMarker marker = new SymbolMarker(5);
		scatterCfg.setMarker(marker);
		marker.setHoverState(new MarkerState());
		marker.getHoverState().setEnabled(true);
		marker.getHoverState().setLineColor(new RGB(100, 100, 100));
		chartConfig.addSeriesConfig(scatterCfg);

		InvientCharts chart = new InvientCharts(chartConfig);
		chart.setWidth("100%");
		chart.setHeight("100%");

		ScatterConfig sCfg = new ScatterConfig();
		XYSeries series = new XYSeries("Significant Markers", sCfg);
		
		double validMinSigValue 	= 	Double.MAX_VALUE;
		double minPlotValue 		= 	Double.MAX_VALUE;
		double maxPlotValue 		= 	Double.MIN_VALUE;

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		
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
				points.add(new DecimalPoint(series, xVal, yVal));
			} else {
				//log.debug("Marker " + i + " was infinite or NaN.");
			}

		}
		
		GMTColorPalette.ColorRange[] range = {new GMTColorPalette.ColorRange(minPlotValue, Color.BLUE.brighter(), maxPlotValue - (maxPlotValue / 3), Color.BLUE),
                new GMTColorPalette.ColorRange(maxPlotValue - (maxPlotValue / 3), Color.BLUE, maxPlotValue, Color.RED)};
		GMTColorPalette colormap = new GMTColorPalette(range);

		LinkedHashSet<DecimalPoint> newPoints = new LinkedHashSet<DecimalPoint>();
		for(int i=0; i<points.size(); i++) {
			
			DecimalPoint a =  (DecimalPoint) (points.toArray())[i] ;
			Color aC = colormap.getColor(Math.abs(a.getX()) * Math.abs(a.getY()));

			SymbolMarker pMarker = new SymbolMarker(5);
			pMarker.setFillColor(new RGB(aC.getRed(), aC.getGreen(), aC.getBlue()));

			PointConfig aCfg = new PointConfig(pMarker);
			a.setConfig(aCfg);
			a.setName(significance.getSignificantMarkers().get(i).getLabel());
			newPoints.add(a);
		}
		series.setSeriesPoints(newPoints);
		chart.addSeries(series);
		return chart;
	}
}
