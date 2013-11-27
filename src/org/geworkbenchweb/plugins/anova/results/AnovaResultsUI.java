package org.geworkbenchweb.plugins.anova.results;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry; 
import org.geworkbenchweb.plugins.Tabular;
 
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PagedTableView; 
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
 
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

	private CSAnovaResultSet<DSGeneMarker> anovaResultSet = null; 
    private AnovaTablePreferences anovaTablePref = new AnovaTablePreferences();
	
	final private Long datasetId;
	private Long userId;

	@SuppressWarnings("unchecked")
	public AnovaResultsUI(Long dataSetId) {
		datasetId = dataSetId;		
		if(dataSetId==null) return;
		
		userId = SessionHandler.get().getId();
		
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
	

	 @Override
	 public IndexedContainer getIndexedContainer() {

		loadAnovaTablePreferences();		 
		String[] header;
		IndexedContainer dataIn = new IndexedContainer();

		int groupNum = anovaResultSet.getLabels(0).length;
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
						.getLabels(0)[cx] + "_Mean";
			}
			if (anovaTablePref.selectStd()) {
				header[meanStdStartAtIndex + cx
						* ((anovaTablePref.selectMean() ? 1 : 0) + (anovaTablePref.selectStd() ? 1 : 0)) + (anovaTablePref.selectMean() ? 1 : 0)] = anovaResultSet
						.getLabels(0)[cx] + "_Std";
			}
		}

		for (int i=0; i<header.length; i++) {
			if (header[i].equals("Marker") || header[i].equals("Gene Symbol") )
				dataIn.addContainerProperty(header[i], String.class, "");
			else
				dataIn.addContainerProperty(header[i], Double.class, "");

		}

		double[][] result2DArray = anovaResultSet.getResult2DArray();
		int significantMarkerNumbers = anovaResultSet.getSignificantMarkers()
				.size();
		for (int cx = 0; cx < significantMarkerNumbers; cx++) {
			DSGeneMarker m = ((DSGeneMarker) anovaResultSet.getSignificantMarkers().get(
					cx));
			if (!isMatchSearch(m))
				continue;
			if ((anovaTablePref.getThresholdControl() == Constants.ThresholdDisplayControl.p_value.ordinal()) && (result2DArray[0][cx] > anovaTablePref.getThresholdValue()))
			    continue;
			if ((anovaTablePref.getThresholdControl() == Constants.ThresholdDisplayControl.f_statistic.ordinal()) && (Float.parseFloat(String.format("%.2f", result2DArray[2][cx])) < anovaTablePref.getThresholdValue()))
			    continue;
			
			Object id = dataIn.addItem();
			fieldIndex = 0;
		
			if (anovaTablePref.selectMarker()) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(m.getLabel());
			}
			if (anovaTablePref.selectGeneSymbol()) {
				dataIn.getContainerProperty(id, header[fieldIndex++]).setValue(m.getGeneName());
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
	 
	private boolean isMatchSearch(DSGeneMarker marker) {
			if (searchStr == null || searchStr.trim().length() == 0)
				return true;

			boolean isMatch = false;	 
			if (anovaTablePref.selectMarker() &&  anovaTablePref.selectGeneSymbol()) {
				if (marker.getLabel().toUpperCase().contains(searchStr)
						|| marker.getGeneName().toUpperCase().contains(searchStr))
					isMatch = true;
			} else if (anovaTablePref.selectMarker()) {
				if (marker.getLabel().toUpperCase().contains(searchStr))
					isMatch = true;

			} else if (anovaTablePref.selectGeneSymbol()){
				if (marker.getGeneName().toUpperCase()
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
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
	
	@Override 
	public PagedTableView getPagedTableView()
	{return dataTable;}
	
	@Override	 
	public void setSearchStr(String search)
	{
		this.searchStr = search;
	}

	@Override
	public Long getUserId() {
		 
		return this.userId;
	}

	@Override
	public void setPrecisonNumber(int precisonNumber) {
		// TODO Auto-generated method stub
		
	}
}
