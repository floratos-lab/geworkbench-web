package org.geworkbenchweb.plugins.ttest.results;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.TTestResult;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.highcharts.HighChart;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Visualization of TTest Results.
 */
public class TTestResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = -6720344403076533166L;
	private static Log log = LogFactory.getLog(TTestResultsUI.class);

	protected final TTestResult tTestResultSet;

	final private Long datasetId;
	final protected Long parentDatasetId;

	final private ChartMenuBar chartMenuBar;
	final private HighChart chart;

	public TTestResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if (dataSetId == null || dataSetId == 0) {
			tTestResultSet = null;
			parentDatasetId = null;
			chartMenuBar = null;
			chart = null;
			return;
		}

		setImmediate(true);
		setSizeFull();

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		parentDatasetId = resultSet.getParent();
		Long id = resultSet.getDataId();
		if (id == null) { // pending node
			addComponent(new Label("Pending computation - ID " + dataSetId));
			tTestResultSet = null;
			chartMenuBar = null;
			chart = null;
			return;
		}
		tTestResultSet = FacadeFactory.getFacade().find(TTestResult.class, id);

		chart = drawPlot();
		chartMenuBar = new ChartMenuBar(this);
		addComponent(chartMenuBar);
		addComponent(chart);
		setExpandRatio(chart, 1);
	}

	/**
	 * Draws the Volcano plot.
	 */
	private HighChart drawPlot() {
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, parentDatasetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] markerLabels = microarray.getMarkerLabels();

		log.debug("t-test result ID " + tTestResultSet.getId());
		int[] significantIndex = tTestResultSet.getSignificantIndex();
		if (significantIndex == null)
			significantIndex = new int[0]; // prevent the null pointer exception

		double minPlotValue = Double.MAX_VALUE;
		double maxPlotValue = Double.MIN_VALUE;
		double[] x = new double[significantIndex.length];
		double[] y = new double[significantIndex.length];
		String[] name = new String[significantIndex.length];
		// Logic in this loop is copied from geWorkbench(swing) volcano plot
		for (int i = 0; i < significantIndex.length; i++) {
			int index = significantIndex[i];
			String mark = markerLabels[index];
			double sigValue = tTestResultSet.getpValue()[index];

			if (sigValue >= 0.0 && sigValue < 4.9E-45) {
				sigValue = 4.9E-45;
			} else if (sigValue < 0) {
				// log.debug("Significance less than 0, (" + sigValue + ") setting to 1 for the
				// moment.");
				sigValue = 1;
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
				x[i] = xVal;
				y[i] = yVal;
				name[i] = mark;
			} else {
				// log.debug("Marker " + i + " was infinite or NaN.");
			}
		}
		GMTColorPalette.ColorRange[] range = {
				new GMTColorPalette.ColorRange(minPlotValue, Color.BLUE.brighter(), maxPlotValue - (maxPlotValue / 3),
						Color.BLUE),
				new GMTColorPalette.ColorRange(maxPlotValue - (maxPlotValue / 3), Color.BLUE, maxPlotValue,
						Color.RED) };
		GMTColorPalette colormap = new GMTColorPalette(range);
		// round two: colors have to be calculated in the second round because we need
		// the minimum value and maximum value from the first round
		StringBuffer data = new StringBuffer("data: [");
		for (int i = 0; i < significantIndex.length; i++) {
			if (name[i] != null) {
				Color aC = colormap.getColor(Math.abs(x[i]) * Math.abs(y[i]));
				String colorString = String.format("#%02X%02X%02X", aC.getRed(), aC.getGreen(), aC.getBlue());
				data.append("{x:" + x[i] + ",y:" + y[i] + ",name:'" + name[i] + "',color:'" + colorString
						+ "',marker:{radius:5}},");
			}

		}
		data.append("]");

		/* It is better to add the function in connector instead of global,
		but we need a little more change regarding highchart addon for that. */
		JavaScript.getCurrent().addFunction("enableReset", (JavaScriptFunction) args -> {
				// args is elemental.json.impl.JreJsonArray
				System.out.println("args: " + args.asString() + "; length: " + args.length());
				if(args.getBoolean(0)) // is there not a reset button?
					chartMenuBar.disableReset();
				else
					chartMenuBar.enableReset();
		  });
		HighChart chart = new HighChart() {{
			/* a better approach. see the comment above */
			/*
			addFunction("function1", (JavaScriptFunction) args -> {
				System.out.println("function1 called from client side .......");
			});
			registerRpc(rpc);
			*/
		}};
		chart.setSizeFull();
		String chartConfig = "chart: { type: 'scatter', zoomType: 'xy', events: {redraw: function (event) { enableReset(this.resetZoomButton===undefined); console.debug(this); console.debug(event); } } }";
		String title = "T-Test";
		String tooltip = "tooltip: { formatter: function() { return '' + this.point.name + ', ' +  this.x + ', ' + this.y + ''; } }";
		String xAxis = "xAxis: { title: {text: 'Fold Change Log2(ratio)'}, startOnTick: true, endOnTick: true }";
		String yAxis = "yAxis: { title: {text: 'Significance(-Log10)'} }";
		String options = "plotOptions: {series: {marker: {radius: 5, states: {hover: {lineColor: '#646464'} } } } }";
		String hcjs = String.format(
				"var options = { title: { text: '%s' }, series: [{ name: 'Significant Markers', %s}], %s, %s, %s, %s, %s };",
				title, data.toString(), chartConfig, tooltip, xAxis, yAxis, options);
		chart.setHcjs(hcjs);
		return chart;
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

	public void resetZoom() {
		chartMenuBar.disableReset();
		chart.markAsDirty(); // force repaint
	}
}
