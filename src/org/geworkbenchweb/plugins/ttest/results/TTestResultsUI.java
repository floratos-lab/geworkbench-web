package org.geworkbenchweb.plugins.ttest.results;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.Color.RGBA;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.ZoomType;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.ScatterConfig;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;

import com.vaadin.ui.VerticalLayout;

public class TTestResultsUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private DSSignificanceResultSet<DSGeneMarker> significance;

	@SuppressWarnings("unchecked")
	public TTestResultsUI(Long dataSetId) {

		setImmediate(true);
		setSizeFull();		

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.id=:id", parameters);

		significance =  (DSSignificanceResultSet<DSGeneMarker>) ObjectConversion.toObject(data
				.get(0).getData());

		addComponent(drawPlot());
	}

	/**
	 * This method draws the Throttle Graph using Invient Charts Add-on.
	 * 
	 */
	private InvientCharts drawPlot() {

		DSMicroarraySet set 	= 	significance.getParentDataSet();

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
		xAxis.setTitle(new AxisTitle("FlodChange Log2(ratio)"));
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

		ScatterConfig femaleScatterCfg = new ScatterConfig();
		femaleScatterCfg.setColor(new RGBA(223, 83, 83, 0.5f));
		XYSeries series = new XYSeries("Significant Markers", femaleScatterCfg);

		// First put all the gene pairs in the xyValues array
		int numMarkers = set.getMarkers().size();
		
		double validMinSigValue 	= 	Double.MAX_VALUE;
		double minPlotValue 		= 	Double.MAX_VALUE;
		double maxPlotValue 		= 	Double.MIN_VALUE;

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
       
		for (int i = 0; i < numMarkers; i++) {
			
			DSGeneMarker mark = set.getMarkers().get(i);
			double sigValue = significance.getSignificance(mark);

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
				DecimalPoint a = new DecimalPoint(series, xVal, yVal);
				a.setName(set.getMarkers().get(i).getLabel());
				points.add(a);
			} else {
				//log.debug("Marker " + i + " was infinite or NaN.");
			}

		}
		series.setSeriesPoints(points);
		chart.addSeries(series);
		return chart;
	}


}
