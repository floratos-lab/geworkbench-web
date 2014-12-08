package org.geworkbenchweb.plugins.ttest.results;

import java.awt.Color;
import java.awt.GradientPaint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.TTestResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.vaadin.addon.JFreeChartWrapper;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Visualization for TTest Results is done in this class.
 */

public class TTestResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = -6720344403076533166L;
	private static Log log = LogFactory.getLog(TTestResultsUI.class);

	protected final TTestResult tTestResultSet;

	final private Long datasetId;
	final protected Long parentDatasetId;
	
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

		JFreeChartWrapper chart = drawPlot();
		addComponent(chart);
		setExpandRatio(chart, 1);
	}

	private static JFreeChartWrapper drawPlot() {
		JFreeChart createchart = createchart(createDataset());
		log.debug("JFreeChart is created");
        return new JFreeChartWrapper(createchart);
	}

    private static JFreeChart createchart(CategoryDataset dataset) {

        // create the chart...
        JFreeChart chart = ChartFactory.createBarChart("Bar Chart Demo 1", // chart
                // title
                "Category", // domain axis label
                "Value", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
                );

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);

        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // renderer.setDrawBarOutline(false);

        // set up gradient paints for series...
        GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f,
                0.0f, new Color(0, 0, 64));
        GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green, 0.0f,
                0.0f, new Color(0, 64, 0));
        GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f,
                0.0f, new Color(64, 0, 0));
        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        renderer.setSeriesPaint(2, gp2);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions
                .createUpRotationLabelPositions(Math.PI / 6.0));
        
        CategoryToolTipGenerator generator = new CategoryToolTipGenerator() {

			@Override
			public String generateToolTip(CategoryDataset arg0, int arg1,
					int arg2) {
				return "TEST TOOLIP HERE...";
			}
        	
        };
		renderer.setSeriesToolTipGenerator(1, generator );

        return chart;
    }

	/**
     * Returns a sample dataset.
     * 
     * @return The dataset.
     */
    private static CategoryDataset createDataset() {

        // row keys...
        String series1 = "First";
        String series2 = "Second";
        String series3 = "Third";

        // column keys...
        String category1 = "Category 1";
        String category2 = "Category 2";
        String category3 = "Category 3";
        String category4 = "Category 4";
        String category5 = "Category 5";

        // create the dataset...
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(1.0, series1, category1);
        dataset.addValue(4.0, series1, category2);
        dataset.addValue(3.0, series1, category3);
        dataset.addValue(5.0, series1, category4);
        dataset.addValue(5.0, series1, category5);

        dataset.addValue(5.0, series2, category1);
        dataset.addValue(7.0, series2, category2);
        dataset.addValue(6.0, series2, category3);
        dataset.addValue(8.0, series2, category4);
        dataset.addValue(4.0, series2, category5);

        dataset.addValue(4.0, series3, category1);
        dataset.addValue(3.0, series3, category2);
        dataset.addValue(2.0, series3, category3);
        dataset.addValue(3.0, series3, category4);
        dataset.addValue(6.0, series3, category5);

        return dataset;
    }
    
	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
