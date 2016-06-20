package org.geworkbenchweb.plugins.ttest;

import java.util.LinkedHashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.TTestResult;
import org.geworkbenchweb.visualizations.VolcanoPlot;
import org.geworkbenchweb.visualizations.VolcanoPlot.Point;
import org.geworkbenchweb.visualizations.VolcanoPlot.VolcanoPlotData;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class TTestResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = -6720344403076533166L;
	private static Log log = LogFactory.getLog(TTestResultsUI.class);

	protected final TTestResult tTestResultSet;

	final private Long datasetId;
	final protected Long parentDatasetId;

	public TTestResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if (dataSetId == null || dataSetId == 0) {
			tTestResultSet = null;
			parentDatasetId = null;
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
			return;
		}
		tTestResultSet = FacadeFactory.getFacade().find(TTestResult.class, id);

		VolcanoPlot chart = drawPlot();
		addComponent(chart);
	}

	private VolcanoPlot drawPlot() {
		VolcanoPlot chart = new VolcanoPlot();
		chart.setWidth("100%");
		chart.setHeight("100%");

		double minPlotValue = Double.MAX_VALUE;
		double maxPlotValue = Double.MIN_VALUE;

		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, parentDatasetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] markerLabels = microarray.getMarkerLabels();

		log.debug("t-test result ID " + tTestResultSet.getId());
		int[] significantIndex = tTestResultSet.getSignificantIndex();
		if (significantIndex == null)
			significantIndex = new int[0]; // prevent the null pointer exception
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;

		LinkedHashSet<Point> points = new LinkedHashSet<Point>();
		for (int i = 0; i < significantIndex.length; i++) {

			int index = significantIndex[i];
			String mark = markerLabels[index];
			double sigValue = tTestResultSet.getpValue()[index];

			if (sigValue >= 0.0 && sigValue < 4.9E-45) {
				sigValue = 4.9E-45;
			} else if (sigValue < 0) {
				// log.debug("Significance less than 0, (" + sigValue + ")
				// setting to 1 for the moment.");
				sigValue = 1;
			}

			double xVal = tTestResultSet.getFoldChange()[index];
			if (xVal > maxX)
				maxX = xVal;
			if (xVal < minX)
				minX = xVal;

			if (!Double.isNaN(xVal) && !Double.isInfinite(xVal)) {
				double yVal = -Math.log10(sigValue);
				double plotVal = Math.abs(xVal) * Math.abs(yVal);
				if (plotVal < minPlotValue) {
					minPlotValue = plotVal;
				}
				if (plotVal > maxPlotValue) {
					maxPlotValue = plotVal;
				}
				Point a = new Point(xVal, yVal, mark);
				points.add(a);
			} else {
				// log.debug("Marker " + i + " was infinite or NaN.");
			}
		}

		VolcanoPlotData data = new VolcanoPlotData("T-Test", "Fold Change Log2(ratio)", "Significance(-Log10)", points,
				"Significant Markers");
		chart.setData(data);
		return chart;
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
