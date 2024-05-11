package org.geworkbenchweb.plugins.ttest.results;

import java.util.Map;

import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.TTestResult;
import org.geworkbenchweb.utils.DataSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;

public class ChartMenuBar extends MenuBar {

	private static final long serialVersionUID = -6900180476335740806L;
	private static final String excelDoubleFormat = "0.00000000000000";
	private MenuItem exportItem;
	private MenuItem resetZoomItem;
	final private String chartTitle = "T-Test";
	private TTestResultsUI tTestResultsUI;

	public ChartMenuBar(TTestResultsUI tTestResultsUI) {
		setImmediate(true);
		setStyleName("transparent");

		this.tTestResultsUI = tTestResultsUI;

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
				tTestResultsUI.resetZoom();
			}
		});
		resetZoomItem.setStyleName("plugin");
		resetZoomItem.setEnabled(false);
	}

	public void exportPlot() {
		JavaScript.getCurrent().execute("const svg=document.getElementsByTagName('svg')[0];" +
				"svg.setAttribute(\"xmlns\", \"http://www.w3.org/2000/svg\");" +
				"const svgData = svg.outerHTML;" + //
				"const preface = '<?xml version=\"1.0\" standalone=\"no\"?>\\r\\n';" +
				"const svgBlob = new Blob([preface, svgData], {type:\"image/svg+xml;charset=utf-8\"});" +
				"const svgUrl = URL.createObjectURL(svgBlob);" +
				"const downloadLink = document.createElement(\"a\");" +
				"downloadLink.href = svgUrl;" +
				"downloadLink.download = '" + chartTitle + "';" +
				"document.body.appendChild(downloadLink);" +
				"downloadLink.click();" +
				"document.body.removeChild(downloadLink);");
	};

	public void exportData(String format) {
		Table table = new Table();
		table.addContainerProperty("Probe Set Name", String.class, null);
		table.addContainerProperty("Gene Name", String.class, null);
		table.addContainerProperty("p-Value", Double.class, null);
		table.addContainerProperty("Fold Change (log2)", Double.class, null);

		Long dataSetId = tTestResultsUI.parentDatasetId;
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] markerLabels = microarray.getMarkerLabels();

		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);
		TTestResult tTestResultSet = tTestResultsUI.tTestResultSet;
		;

		for (int i = 0; i < tTestResultSet.getSignificantIndex().length; i++) {
			int index = tTestResultSet.getSignificantIndex()[i];
			String markerLabel = markerLabels[index];
			String geneSymbol = map.get(markerLabel);
			Double foldchange = tTestResultSet.getFoldChange()[index];
			Double sigValue = tTestResultSet.getpValue()[index];
			table.addItem(new Object[] { markerLabel, geneSymbol, sigValue, foldchange }, new Integer(i));
		}
		table.sort(new String[] { "p-Value" }, new boolean[] { true });

		tTestResultsUI.addComponent(table);

		if (format.equals("Excel")) {
			ExcelExport excelExport = new ExcelExport(table);
			excelExport.setExportFileName(chartTitle + ".xls");
			excelExport.setDoubleDataFormat(excelDoubleFormat);
			excelExport.setDisplayTotals(false);
			excelExport.export();
		} else {
			CsvExport csvExport = new CsvExport(table);
			csvExport.setExportFileName(chartTitle + ".csv");
			csvExport.setDoubleDataFormat(excelDoubleFormat);
			csvExport.setDisplayTotals(false);
			csvExport.export();
		}
		tTestResultsUI.removeComponent(table);
	}

	public void enableReset() {
		resetZoomItem.setEnabled(true);
	}

	public void disableReset() {
		resetZoomItem.setEnabled(false);
	};
}
