package org.geworkbenchweb.plugins.anova.results;

import java.util.Map;

import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.plugins.Tabular;
import org.geworkbenchweb.pojos.AnovaResult;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PagedTableView;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class AnovaResultsUI extends VerticalLayout implements Tabular {

	private static final long serialVersionUID = 3115606230292029231L;

	private PagedTableView dataTable;
	private String searchStr;	

	final private AnovaResult anovaResultSet; 
    private AnovaTablePreferences anovaTablePref = new AnovaTablePreferences();
	
	final private Long datasetId;
	final private Long parentDatasetId;
	private Long userId;

	public AnovaResultsUI(Long dataSetId) {
		datasetId = dataSetId;		
		if(dataSetId==null) {
			anovaResultSet = null;
			parentDatasetId = null;
			return;
		}
		
		userId = SessionHandler.get().getId();
		
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		parentDatasetId = resultSet.getParent();
		if(id==null) { // pending node
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			anovaResultSet = null;
			return;
		}
		anovaResultSet = FacadeFactory.getFacade().find(AnovaResult.class, id);
	
		setSpacing(true);
		setImmediate(true);
		setSizeFull();
		
		/* Results Table Code */

		dataTable = new PagedTableView() {

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

		 
		
		dataTable.setSizeFull();
		dataTable.setImmediate(true);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);
		dataTable.setContainerDataSource(getIndexedContainer());
		Object [] properties={"F-statistic"};
		boolean [] ordering={false,true};		 
		dataTable.sort(properties, ordering);
		final MenuBar toolBar = new AnovaTableMenuSelector(this, "AnovaResultsUI");
		
		addComponent(toolBar);
		addComponent(dataTable);
		setExpandRatio(dataTable, 1);
		
		addComponent(dataTable.createControls());

	}
	
	private void loadAnovaTablePreferences() {

		Preference p = PreferenceOperations
				.getData(datasetId,
						Constants.DISPLAY_CONTROL,
						userId);
	 
		if (p == null) {
			anovaTablePref.reset();
			return;
		}
		 
		anovaTablePref = (AnovaTablePreferences) (ObjectConversion
							.toObject(p.getValue()));
					 

	}

	 private IndexedContainer getIndexedContainer() {

		loadAnovaTablePreferences();		 
		String[] header;
		IndexedContainer dataIn = new IndexedContainer();

		int groupNum = anovaResultSet.getSelectedArraySetNames().length;
		int meanStdStartAtIndex =  (anovaTablePref.selectMarker() ? 1 : 0) + (anovaTablePref.selectGeneSymbol() ? 1 : 0) +(anovaTablePref.selectFStat() ? 1 : 0) + (anovaTablePref.selectPVal() ? 1 : 0);
		header = new String[meanStdStartAtIndex + groupNum
				* ((anovaTablePref.selectMean() ? 1 : 0) + (anovaTablePref.selectStd() ? 1 : 0))];
		int fieldIndex = 0;
		if (anovaTablePref.selectMarker())
		   header[fieldIndex++] = "Marker";
		if (anovaTablePref.selectGeneSymbol())
			   header[fieldIndex++] = "Gene Symbol";
		if (anovaTablePref.selectPVal()) {
			header[fieldIndex++] = "P-Value";
		}
		if (anovaTablePref.selectFStat()) {
			header[fieldIndex++] = "F-statistic";
		}
		for (int cx = 0; cx < groupNum; cx++) {
			if (anovaTablePref.selectMean()) {
				header[meanStdStartAtIndex + cx
						* ((anovaTablePref.selectMean() ? 1 : 0) + (anovaTablePref.selectStd() ? 1 : 0)) + 0] = anovaResultSet
						.getSelectedArraySetNames()[cx] + "_Mean";
			}
			if (anovaTablePref.selectStd()) {
				header[meanStdStartAtIndex + cx
						* ((anovaTablePref.selectMean() ? 1 : 0) + (anovaTablePref.selectStd() ? 1 : 0)) + (anovaTablePref.selectMean() ? 1 : 0)] = anovaResultSet
						.getSelectedArraySetNames()[cx] + "_Std";
			}
		}

		for (int i=0; i<header.length; i++) {
			if (header[i].equals("Marker") || header[i].equals("Gene Symbol") )
				dataIn.addContainerProperty(header[i], String.class, "");
			else
				dataIn.addContainerProperty(header[i], Float.class, "");

		}
		
		MicroarraySet microarrays = DataSetOperations.getMicroarraySet(parentDatasetId);
		Map<String, String> map = DataSetOperations.getAnnotationMap(parentDatasetId);

		double[][] result2DArray = anovaResultSet.getResult2DArray();
		int significantMarkerNumbers = anovaResultSet.getFeaturesIndexes().length;
		int[] markerIndex = anovaResultSet.getFeaturesIndexes();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			String markerLabel = microarrays.markerLabels[markerIndex[cx]];
			String geneSymbol = map.get(markerLabel);
			if (!isMatchSearch(markerLabel, geneSymbol))
				continue;
			if ((anovaTablePref.getThresholdControl() == Constants.ThresholdDisplayControl.p_value.ordinal()) && (result2DArray[0][cx] > anovaTablePref.getThresholdValue()))
			    continue;
			if ((anovaTablePref.getThresholdControl() == Constants.ThresholdDisplayControl.f_statistic.ordinal()) && (Float.parseFloat(String.format("%.2f", result2DArray[2][cx])) < anovaTablePref.getThresholdValue()))
			    continue;
			
			Object id = dataIn.addItem();
			fieldIndex = 0;
		
			if (anovaTablePref.selectMarker()) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(markerLabel);
			}
			if (anovaTablePref.selectGeneSymbol()) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(geneSymbol);
			}	 
			 
			if (anovaTablePref.selectPVal()) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(
						result2DArray[0][cx]);
			}
			if (anovaTablePref.selectFStat()) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(
						result2DArray[2][cx]);
			}
			for (int gc = 0; gc < groupNum; gc++) {
				if (anovaTablePref.selectMean()) {
					dataIn.getContainerProperty(
							id,
							header[meanStdStartAtIndex + gc
									* ((anovaTablePref.selectMean() ? 1 : 0) + (anovaTablePref.selectStd() ? 1 : 0)) + 0])
							.setValue(result2DArray[3 + gc * 2][cx]);
				}
				if (anovaTablePref.selectStd()) {
					dataIn.getContainerProperty(
							id,
							header[meanStdStartAtIndex + gc
									* ((anovaTablePref.selectMean() ? 1 : 0) + (anovaTablePref.selectStd() ? 1 : 0))
									+ (anovaTablePref.selectMean() ? 1 : 0)]).setValue(
							result2DArray[4 + gc * 2][cx]);
				}
			}

		}

		return dataIn;
	}
	 
	private boolean isMatchSearch(String markerLabel, String geneSymbol) {
			if (searchStr == null || searchStr.trim().length() == 0)
				return true;

			boolean isMatch = false;	 
			if (anovaTablePref.selectMarker() &&  anovaTablePref.selectGeneSymbol()) {
				if (markerLabel.toUpperCase().contains(searchStr.toUpperCase())
						|| geneSymbol.toUpperCase().contains(searchStr.toUpperCase()))
					isMatch = true;
			} else if (anovaTablePref.selectMarker()) {
				if (markerLabel.toUpperCase().contains(searchStr.toUpperCase()))
					isMatch = true;

			} else if (anovaTablePref.selectGeneSymbol()){
				if (geneSymbol.toUpperCase()
						.contains(searchStr.trim().toUpperCase()))
					isMatch = true;
			}
			return isMatch;
		}
	 

	public AnovaTablePreferences getAnovaTablePreferences()
	{
		return this.anovaTablePref;
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

	@Override	 
	public void setSearchStr(String search)
	{
		this.searchStr = search;
	}
	
	@Override
	public String getSearchStr() {
		 
		return this.searchStr;
	}


	@Override
	public Long getUserId() {
		 
		return this.userId;
	}

	@Override
	public void resetDataSource() {
		dataTable.setContainerDataSource(getIndexedContainer());
	}

	@Override
	public void export() {
		CsvExport csvExport = new CsvExport(dataTable);
		csvExport.excludeCollapsedColumns();
		csvExport.setExportFileName("tabularViewTable.csv");
		csvExport.setDisplayTotals(false);
		csvExport.setDoubleDataFormat("General");
		csvExport.setExcelFormatOfProperty("P-Value", "0.00E+00");
		csvExport.export();
	}
}
