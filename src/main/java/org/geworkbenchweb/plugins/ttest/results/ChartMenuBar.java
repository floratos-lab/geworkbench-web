package org.geworkbenchweb.plugins.ttest.results;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.TTestResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartResetZoomEvent;
import com.invient.vaadin.charts.InvientCharts.ChartResetZoomListener;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.invient.vaadin.charts.InvientCharts.ChartZoomEvent;
import com.invient.vaadin.charts.InvientCharts.ChartZoomListener;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;

public class ChartMenuBar extends MenuBar {

	private static final long serialVersionUID = -6900180476335740806L;
	private static final String excelDoubleFormat = "0.00000000000000";
	private MenuItem exportItem;
	private MenuItem resetZoomItem;
	private InvientCharts chart;
	private String chartTitle;
	private TTestResultsUI tTestResultsUI;
	
	public ChartMenuBar(final InvientCharts chart, TTestResultsUI tTestResultsUI){
		setImmediate(true);
		setStyleName("transparent");
		
		this.tTestResultsUI = tTestResultsUI;
		this.chart = chart;
		chartTitle = chart.getConfig().getTitle().getText();
		chartTitle = chartTitle.replaceAll(" ", "_");
		
		if (chart.getConfig().getGeneralChartConfig().getZoomType() != null) {
			chart.addListener(new ChartZoomListener() {
				private static final long serialVersionUID = 7797202131662773606L;
	
				@Override
				public void chartZoom(ChartZoomEvent chartZoomEvent) {
					resetZoomItem.setEnabled(true);
				}
			});
			chart.addListener(new ChartResetZoomListener() {
				private static final long serialVersionUID = 7797202131662773606L;
	
				@Override
				public void chartResetZoom(ChartResetZoomEvent chartResetZoomEvent) {
					resetZoomItem.setEnabled(false);
				}
			});
		}
		
		exportItem = this.addItem("Export", null, null);
		exportItem.setStyleName("plugin");

		MenuItem exportPlotItem = exportItem.addItem("Plot", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				exportPlot();
			}
		});
		exportPlotItem.setStyleName("plugin");
		
		MenuItem exportDataItem = exportItem.addItem("Data as Excel", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				exportData("Excel");
			}
		});
		exportDataItem.setStyleName("plugin");
		
		MenuItem exportDataCSVItem = exportItem.addItem("Data as CSV", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				exportData("CSV");
			}
		});
		exportDataCSVItem.setStyleName("plugin");
				
		resetZoomItem = this.addItem("Reset Zoom", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				resetZoom();

			}
		});
		resetZoomItem.setStyleName("plugin");
		resetZoomItem.setEnabled(false);
	}

	public void exportPlot(){
		chart.addListener(new InvientCharts.ChartSVGAvailableListener() {
			private static final long serialVersionUID = 1L;
			public void svgAvailable(final ChartSVGAvailableEvent chartSVGAvailableEvent) {
				StreamResource svgResource = new StreamResource(
						new StreamSource() {
							private static final long serialVersionUID = 4459384346468205801L;
							@Override
							public InputStream getStream() {
								return new ByteArrayInputStream(chartSVGAvailableEvent.getSVG().getBytes());
							}
						}, 
						chartTitle+".svg", 
						ChartMenuBar.this.getApplication());
				getWindow().open(svgResource, "_blank");
			}
		});
	};
	
	public void exportData(String format){
		Table table = new Table();
		table.addContainerProperty("Probe Set Name", String.class,  null);
		table.addContainerProperty("Gene Name", String.class,  null);
		table.addContainerProperty("p-Value", Double.class,  null);
		table.addContainerProperty("Fold Change (log2)", Double.class,  null);

		Long dataSetId = tTestResultsUI.parentDatasetId;
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] markerLabels = microarray.getMarkerLabels();
		
		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);
		TTestResult tTestResultSet = tTestResultsUI.tTestResultSet;;
		
		for (int i = 0; i < tTestResultSet.getSignificantIndex().length; i++) {
			int index = tTestResultSet.getSignificantIndex()[i];
			String markerLabel = markerLabels[index];
			String geneSymbol  = map.get(markerLabel);
			Double foldchange  = tTestResultSet.getFoldChange()[index];
			Double sigValue    = tTestResultSet.getpValue()[index];
			table.addItem(new Object[]{markerLabel, geneSymbol, sigValue, foldchange}, new Integer(i));
		}
		table.sort(new String[]{"p-Value"}, new boolean[]{true});

		tTestResultsUI.addComponent(table);
		
		if(format.equals("Excel")){
			ExcelExport excelExport = new ExcelExport(table);
			excelExport.setExportFileName(chartTitle+".xls");
			excelExport.setDoubleDataFormat(excelDoubleFormat);
			excelExport.setDisplayTotals(false);
			excelExport.export();
		}else{
			CsvExport csvExport = new CsvExport(table);
			csvExport.setExportFileName(chartTitle+".csv");
			csvExport.setDoubleDataFormat(excelDoubleFormat);
			csvExport.setDisplayTotals(false);
			csvExport.export();
		}
		tTestResultsUI.removeComponent(table);
	}
	
	public void resetZoom(){
		for(Series series : chart.getAllSeries()){
			series.show();
		}
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("event", "chartResetZoom");
		chart.changeVariables(chart, variables);
	};
}
