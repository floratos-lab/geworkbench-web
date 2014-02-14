package org.geworkbenchweb.plugins.ttest.results;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartResetZoomEvent;
import com.invient.vaadin.charts.InvientCharts.ChartResetZoomListener;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.invient.vaadin.charts.InvientCharts.ChartZoomEvent;
import com.invient.vaadin.charts.InvientCharts.ChartZoomListener;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.MenuBar;

public class ChartMenuBar extends MenuBar {

	private static final long serialVersionUID = -6900180476335740806L;
	private MenuItem exportPlotItem;
	private MenuItem exportDataItem;
	private MenuItem resetZoomItem;
	private InvientCharts chart;
	private String chartTitle;
	
	public ChartMenuBar(final InvientCharts chart){
		setImmediate(true);
		setStyleName("transparent");
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
		
		exportPlotItem = this.addItem("Export Plot", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				exportPlot();
			}
		});
		exportPlotItem.setStyleName("plugin");
		
		exportDataItem = this.addItem("Export Data", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				exportData();
			}
		});
		exportDataItem.setStyleName("plugin");
		
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
	
	public void exportData(){
		
	}
	
	public void resetZoom(){
		/*Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("event", "chartResetZoom");
		chart.changeVariables(chart, variables);
		chart.requestRepaint();
		chart.refresh();
		GeworkbenchRoot.getPusher().push();*/
	};
}
