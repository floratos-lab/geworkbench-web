package org.geworkbenchweb.plugins.anova.results;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.utils.TableView;
import org.geworkbenchweb.utils.UserDirUtils;

import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class AnovaResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 3115606230292029231L;

	private TableView dataTable;
	// preferences
	private static boolean fStat = true;
	private static boolean pVal = true;
	private static boolean mean = true;
	private static boolean std = true;

	private CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;

	final private Long datasetId;

	@SuppressWarnings("unchecked")
	public AnovaResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		}
		if(! (object instanceof CSAnovaResultSet)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			addComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			return;
		}
		anovaResultSet = (CSAnovaResultSet<DSGeneMarker>) object;
	
		setSpacing(true);
		setImmediate(true);
		setSizeFull();
		
		/* Results Table Code */

		dataTable = new TableView() {

			private static final long serialVersionUID = 9129226094428040540L;

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object value = property.getValue();
				if ((value != null) && (value instanceof Number)) {
					if (((Number) value).doubleValue() < 0.1)
						return String.format("%.2E", value);
					else
						return String.format("%.2f", value);
				}

				return super.formatPropertyValue(rowId, colId, property);
			}
		};

		 
		dataTable.setContainerDataSource(getIndexedContainer());
		dataTable.setSizeFull();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);
		 
		
		Button exportButton;
		exportButton = new Button("Export", new ExportListener());
		
		addComponent(exportButton);
		addComponent(dataTable);
		setExpandRatio(dataTable, 1);
		
		

	}

	private IndexedContainer getIndexedContainer() {

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

		for (int i=0; i<header.length; i++) {
			if (header[i].equals("Marker Name"))
				dataIn.addContainerProperty(header[i], String.class, "");
			else
				dataIn.addContainerProperty(header[i], Float.class, "");

		}

		double[][] result2DArray = anovaResultSet.getResult2DArray();
		int significantMarkerNumbers = anovaResultSet.getSignificantMarkers()
				.size();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			Object id = dataIn.addItem();
			fieldIndex = 0;
			String s  = ((DSGeneMarker) anovaResultSet.getSignificantMarkers().get(
					cx)).getShortName();
			if (s.equals("---"))
				s =  ((DSGeneMarker) anovaResultSet.getSignificantMarkers().get(
						cx)).getLabel();
			dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(s);
			if (pVal) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(
						result2DArray[0][cx]);
			}
			if (fStat) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(
						result2DArray[2][cx]);
			}
			for (int gc = 0; gc < groupNum; gc++) {
				if (mean) {
					dataIn.getContainerProperty(
							id,
							header[meanStdStartAtIndex + gc
									* ((mean ? 1 : 0) + (std ? 1 : 0)) + 0])
							.setValue(result2DArray[3 + gc * 2][cx]);
				}
				if (std) {
					dataIn.getContainerProperty(
							id,
							header[meanStdStartAtIndex + gc
									* ((mean ? 1 : 0) + (std ? 1 : 0))
									+ (mean ? 1 : 0)]).setValue(
							result2DArray[4 + gc * 2][cx]);
				}
			}

		}

		return dataIn;
	}

	private class ExportListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;

		public ExportListener() {
		};

		@Override
		public void buttonClick(ClickEvent event) {

			dataTable.csvExport("anovaTable.csv");

		}
	}

	@Override
	public PluginEntry getPluginEntry() {
		return new PluginEntry("ANOVA Result Viewer", "Show ANOVA result as a table");
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
