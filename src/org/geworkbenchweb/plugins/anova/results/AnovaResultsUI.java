package org.geworkbenchweb.plugins.anova.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.TableView;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.Reindeer;

public class AnovaResultsUI extends VerticalLayout {

	private static final long serialVersionUID = 3115606230292029231L;

	private TableView dataTable;
	// preferences
	private static boolean fStat = true;
	private static boolean pVal = true;
	private static boolean mean = true;
	private static boolean std = true;

	private CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;

	Button submitButton;

	@SuppressWarnings("unchecked")
	public AnovaResultsUI(Long dataSetId) {

		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id", parameters);
		
		anovaResultSet = (CSAnovaResultSet<DSGeneMarker>) ObjectConversion.toObject(data.get(0).getData());
	
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
			dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(
					((DSGeneMarker) anovaResultSet.getSignificantMarkers().get(
							cx)).getShortName());
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
}
