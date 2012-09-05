package org.geworkbenchweb.analysis.anova.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.layout.UMenuBar;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.Grid;
import com.invient.vaadin.charts.InvientChartsConfig.DataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Margin;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * This class displays CNKB results in a Table and also a graph
 * @author Nikhil Reddy
 */
@SuppressWarnings("unused")
public class UAnovaTab extends VerticalLayout {

   
	private static final long serialVersionUID = -8549838022827406480L;
	
	private Table dataTable;
	// preferences
	private boolean fStat = true;
	private boolean pVal = true;
	private boolean mean = true;
	private boolean std = true;
		 
	 
	
	public UAnovaTab(CSAnovaResultSet<DSGeneMarker>  anovaResultSet) {
	
		setSizeFull();
		setImmediate(true);
		 
		
		/* Results Table Code */
		 
		dataTable  = new Table();		 
		dataTable.setSizeFull();
		dataTable.setImmediate(true);		 
		dataTable.setContainerDataSource(tabularView(anovaResultSet));	 
	 
		addComponent(dataTable);
	 
	}
	
	private IndexedContainer tabularView(CSAnovaResultSet<DSGeneMarker> anovaResultSet) {

		String[] header;
		IndexedContainer dataIn = new IndexedContainer();
		
		int groupNum = anovaResultSet.getLabels(0).length;
		int meanStdStartAtIndex = 1 + (fStat ? 1 : 0) + (pVal ? 1 : 0);
		header = new String[meanStdStartAtIndex + groupNum
				* ((mean ? 1 : 0) + (std ? 1 : 0))];
		int fieldIndex = 0;
		header[fieldIndex++] = "Marker Name";
		if (pVal) {
			header[fieldIndex++] = "P-Value";
		}
		if (fStat) {
			header[fieldIndex++] = "F-statistic";
		}
		for (int cx = 0; cx < groupNum; cx++) {
			if (mean) {
				header[meanStdStartAtIndex + cx
						* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0] = anovaResultSet
						.getLabels(0)[cx] + "_Mean";
			}
			if (std) {
				header[meanStdStartAtIndex + cx
						* ((mean ? 1 : 0) + (std ? 1 : 0)) + (mean ? 1 : 0)] = anovaResultSet
						.getLabels(0)[cx] + "_Std";
			}
		}
		
		for (String p : header) 
		{ 
			if (p.equals("Marker Name"))
		       dataIn.addContainerProperty(p, String.class, "");   
			else
				dataIn.addContainerProperty(p, Float.class, "");  
			  
		}
		
		
		double[][] result2DArray = anovaResultSet.getResult2DArray();
		int significantMarkerNumbers = anovaResultSet.getSignificantMarkers()
				.size();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			Object id = dataIn.addItem();
			fieldIndex = 0;
			dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(((DSGeneMarker) anovaResultSet
					.getSignificantMarkers().get(cx)).getShortName());
			if (pVal) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(convertDouble(result2DArray[0][cx]));
			}
			if (fStat) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(convertDouble(result2DArray[2][cx]));
			}
			for (int gc = 0; gc < groupNum; gc++) {
				if (mean) {
					dataIn.getContainerProperty(id, header[meanStdStartAtIndex + gc
					           							* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0]).setValue(convertDouble(result2DArray[3 + gc * 2][cx]));
				}
				if (std) {
					dataIn.getContainerProperty(id, header[meanStdStartAtIndex + gc
							* ((mean ? 1 : 0) + (std ? 1 : 0)) + (mean ? 1 : 0)]).setValue(convertDouble(result2DArray[4 + gc * 2][cx]));
				}
			}
			 
		}
		
		
		return dataIn;
	}
	
	private Object convertDouble(Object value) {
		     
		if ((value != null) && (value instanceof Number)) {
			if (((Number) value).doubleValue() < 0.1)
				value = String.format("%.2E", value);
			else
				value = String.format("%.2f", value);
		}
			
			return value;
			
	}
		 
	 
	
	
	 
}
