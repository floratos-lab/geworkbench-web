package org.geworkbenchweb.plugins.ttest.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSTTestResultSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.Color.RGBA;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.ZoomType;
import com.invient.vaadin.charts.InvientChartsConfig.HorzAlign;
import com.invient.vaadin.charts.InvientChartsConfig.Legend.Layout;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.Position;
import com.invient.vaadin.charts.InvientChartsConfig.ScatterConfig;
import com.invient.vaadin.charts.InvientChartsConfig.VertAlign;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;

import com.vaadin.ui.VerticalLayout;

public class TTestResultsUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private DSSignificanceResultSet<DSGeneMarker> significance;
	private LinkedHashSet<DecimalPoint> scatterMaleData = null;

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
		String[] caseLabels 	= 	significance.getLabels(DSTTestResultSet.CASE);
		String[] controlLabels 	= 	significance.getLabels(DSTTestResultSet.CONTROL);

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(set);

		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setType(SeriesType.SCATTER);
		chartConfig.getGeneralChartConfig().setZoomType(ZoomType.XY);

		chartConfig.getTitle().setText(
				"T-Test");

		chartConfig.getTooltip().setFormatterJsFunc(
				"function() {"
						+ " return '' + this.x + ' cm, ' + this.y + ' kg'; "
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

		Legend legend = new Legend();
		legend.setLayout(Layout.VERTICAL);
		Position legendPos = new Position();
		legendPos.setAlign(HorzAlign.LEFT);
		legendPos.setVertAlign(VertAlign.TOP);
		legendPos.setX(100);
		legendPos.setY(70);
		legend.setPosition(legendPos);
		legend.setFloating(true);
		legend.setBorderWidth(1);
		legend.setBackgroundColor(new RGB(255, 255, 255));
		chartConfig.setLegend(legend);

		ScatterConfig scatterCfg = new ScatterConfig();

		SymbolMarker marker = new SymbolMarker(5);
		scatterCfg.setMarker(marker);
		marker.setHoverState(new MarkerState());
		marker.getHoverState().setEnabled(true);
		marker.getHoverState().setLineColor(new RGB(100, 100, 100));
		chartConfig.addSeriesConfig(scatterCfg);

		InvientCharts chart = new InvientCharts(chartConfig);

		ScatterConfig femaleScatterCfg = new ScatterConfig();
		femaleScatterCfg.setColor(new RGBA(223, 83, 83, 0.5f));
		XYSeries series = new XYSeries("", femaleScatterCfg);

		// First put all the gene pairs in the xyValues array
		int numMarkers = dataSetView.getMarkerPanel().size();
		System.out.println(numMarkers);
		double validMinSigValue 	= 	Double.MAX_VALUE;
		double minPlotValue 		= 	Double.MAX_VALUE;
		double maxPlotValue 		= 	Double.MIN_VALUE;

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
       
		for (int i = 0; i < numMarkers; i++) {
			
			DSGeneMarker mark = dataSetView.getMarkerPanel().get(i);
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
				System.out.println("Nikhil");
				points.add(new DecimalPoint(series, xVal, yVal));
			} else {
				//log.debug("Marker " + i + " was infinite or NaN.");
			}

		}
		System.out.println(points.size());
		series.setSeriesPoints(points);
		chart.addSeries(series);
		return chart;
	}


}
