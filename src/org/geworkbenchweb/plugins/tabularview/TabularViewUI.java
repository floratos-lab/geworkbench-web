package org.geworkbenchweb.plugins.tabularview;

import java.util.List;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;

import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion; 
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.plugins.PluginEntry;
 
import org.geworkbenchweb.plugins.tabularview.Constants;
import org.geworkbenchweb.plugins.Tabular;


import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.PagedTableView;
import org.vaadin.appfoundation.authentication.SessionHandler;
 
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer; 
import com.vaadin.ui.MenuBar; 
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer; 

/**
 * Displays Tabular View for Microarray Data.  
 * 
 * @author Nikhil
 */
public class TabularViewUI extends VerticalLayout implements Tabular {

	private static final long serialVersionUID = 1L;
   
	private DSMicroarraySet maSet;
	private Long userId;
	private int precisonNumber = 2;
	private String searchStr;	
	private TabularViewPreferences tabViewPreferences;
    private PagedTableView displayTable;
    
	final private Long datasetId;

	public TabularViewUI(final Long dataSetId) {
 
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
 
		setSizeFull();
		setImmediate(true);		 
            
		userId = SessionHandler.get().getId();
		tabViewPreferences = new TabularViewPreferences();
		displayTable = new PagedTableView() {

			private static final long serialVersionUID = 5268979064889636700L;

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object value = property.getValue();
				if ((value != null) && (value instanceof Number)) {

					return String.format("%." + precisonNumber + "f", value);
				}

				return super.formatPropertyValue(rowId, colId, property);
			}
		};		 

		displayTable.setSizeFull();
		displayTable.setImmediate(true);
		displayTable.setStyleName(Reindeer.TABLE_STRONG);

		DataSet data = DataSetOperations.getDataSet(dataSetId);
		try {
			maSet = (DSMicroarraySet) UserDirUtils.deserializeDataSet(data.getId(), DSMicroarraySet.class);
			UserDirUtils.setAnnotationParser(dataSetId, maSet);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}  
		
		final MenuBar toolBar = new TabularMenuSelector(this, "TabularViewUI");
		addComponent(toolBar);
		addComponent(displayTable);
		setExpandRatio(displayTable, 1);		 
	 
		displayTable.setContainerDataSource(getIndexedContainer());
		displayTable.setColumnWidth(Constants.MARKER_HEADER, 150); 

		addComponent(displayTable.createControls());
	} 

	 
 
	void loadTabViewPreferences() {

		List<Preference> preferences = PreferenceOperations.getAllPreferences(
				datasetId, userId, "TabularViewUI%");
		if (preferences == null) {
			tabViewPreferences.reset();
			return;
		}
		for (Preference p : preferences) {
			if (p.getName().equals(Constants.MARKER_DISPLAY_CONTROL))
				tabViewPreferences
						.setMarkerDisplayControl((Integer) ObjectConversion
								.toObject(p.getValue()));
			else if (p.getName().equals(Constants.ANNOTATION_DISPLAY_CONTROL))
				tabViewPreferences
						.setAnnotationDisplayControl((Integer) ObjectConversion
								.toObject(p.getValue()));
			else if (p.getName().equals(Constants.NUMBER_PRECISION_CONTROL))
				tabViewPreferences
						.setNumberPrecisionControl((Integer) ObjectConversion
								.toObject(p.getValue()));
			else if (p.getName().equals(Constants.MARKER_FILTER_CONTROL)) {
				if (p.getValue() != null) {
					FilterInfo markerFilter = (FilterInfo) (ObjectConversion
							.toObject(p.getValue()));
					tabViewPreferences.setMarkerFilter(markerFilter);

				}
			} else if (p.getName().equals(Constants.ARRAY_FILTER_CONTROL)) {
				if (p.getValue() != null) {
					FilterInfo arrayFilter = (FilterInfo) (ObjectConversion
							.toObject(p.getValue()));
					tabViewPreferences.setArrayFilter(arrayFilter);

				}
			}

		}

	}

	private List<String> getTabViewColHeaders() {			 
		List<String> colHeaders = new ArrayList<String>();
		if (tabViewPreferences.getMarkerDisplayControl() == Constants.MarkerDisplayControl.both
				.ordinal()) {
			colHeaders.add(Constants.MARKER_HEADER);
			colHeaders.add(Constants.GENE_SYMBOL_HEADER);
		} else if (tabViewPreferences.getMarkerDisplayControl() == Constants.MarkerDisplayControl.marker
				.ordinal())
			colHeaders.add(Constants.MARKER_HEADER);

		else
			colHeaders.add(Constants.GENE_SYMBOL_HEADER);

		if (tabViewPreferences.getAnnotationDisplayControl() == Constants.AnnotationDisplayControl.on
				.ordinal())
			colHeaders.add(Constants.ANNOTATION_HEADER);

		FilterInfo arrayFilter = tabViewPreferences.getArrayFilter();

		String[] selectedArraySet = null;
		if (arrayFilter != null)
			selectedArraySet = arrayFilter.getSelectedSet();

		if (selectedArraySet == null
				|| selectedArraySet[0].equalsIgnoreCase("All Arrays")) {
			for (int i = 0; i < maSet.size(); i++)
				colHeaders.add(maSet.get(i).getLabel());
		} else {

			for (int i = 0; i < selectedArraySet.length; i++) {

				List<?> subSet = SubSetOperations.getArraySet(Long
						.parseLong(selectedArraySet[i].trim()));
				ArrayList<String> positions = (((SubSet) subSet.get(0))
						.getPositions());

				for (int j = 0; j < positions.size(); j++) {

					colHeaders.add(maSet.get(positions.get(j)).getLabel());
				}

			}
		}

		return colHeaders;
	}

	private int getDisplayPrefColunmNum(
			TabularViewPreferences tabViewPreferences) {
		int count = 0;
		if (tabViewPreferences.getMarkerDisplayControl() == Constants.MarkerDisplayControl.both
				.ordinal()) {
			count = 2;
		} else
			count = 1;

		if (tabViewPreferences.getAnnotationDisplayControl() == Constants.AnnotationDisplayControl.on
				.ordinal())
			count = count + 1;

		return count;

	}

	private DSItemList<DSGeneMarker> getTabViewMarkers() {		 
		DSItemList<DSGeneMarker> selectedMarkers = new CSItemList<DSGeneMarker>();
		;
		String[] selectedMarkerSet = null;

		FilterInfo markerFilter = tabViewPreferences.getMarkerFilter();
		if (markerFilter != null)
			selectedMarkerSet = markerFilter.getSelectedSet();

		int markerDisplayControl = tabViewPreferences.getMarkerDisplayControl();
		if (selectedMarkerSet != null && selectedMarkerSet.length > 0
				&& (!selectedMarkerSet[0].equalsIgnoreCase("All Markers"))) {

			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<?> subSet = SubSetOperations.getMarkerSet(Long
						.parseLong(selectedMarkerSet[i].trim()));
				if (subSet == null || subSet.size() == 0)
					continue;
				ArrayList<String> positions = (((SubSet) subSet.get(0))
						.getPositions());

				for (int m = 0; m < positions.size(); m++) {
					String temp = ((positions.get(m)).split("\\s+"))[0].trim();
					DSGeneMarker marker = maSet.getMarkers().get(temp);
					if (marker != null
							&& isMatchSearch(marker, 
									markerDisplayControl)) {
						selectedMarkers.add(marker);
					}
				}

			}
		} else {
			for (int i = 0; i < maSet.getMarkers().size(); i++) {
				DSGeneMarker marker = maSet.getMarkers().get(i);
				if (isMatchSearch(marker, markerDisplayControl))
					selectedMarkers.add(marker);

			}
		}

		return selectedMarkers;

	}

	private boolean isMatchSearch(DSGeneMarker marker,  
			int markerDisplayControl) {
		if (searchStr == null || searchStr.trim().length() == 0)
			return true;

		boolean isMatch = false;	 
		if (markerDisplayControl == Constants.MarkerDisplayControl.both
				.ordinal()) {
			if (marker.getLabel().toUpperCase().contains(searchStr)
					|| marker.getGeneName().toUpperCase().contains(searchStr))
				isMatch = true;
		} else if (markerDisplayControl == Constants.MarkerDisplayControl.marker
				.ordinal()) {
			if (marker.getLabel().toUpperCase().contains(searchStr))
				isMatch = true;

		} else {
			if (marker.getGeneName().toUpperCase()
					.contains(searchStr.trim().toUpperCase()))
				isMatch = true;
		}
		return isMatch;
	}
	 
	PagedTableView getDisplayTable()
	{
		return displayTable;
	}
	
	TabularViewPreferences getTabViewPreferences()
	{
		return tabViewPreferences;
	} 
	 
 
	@Override
	public PluginEntry getPluginEntry() {
		return new PluginEntry("Tabular Microarray Viewer", 
				"Presents the numerical values of the expression measurements in a table format. " +
				"One row is created per individual marker/probe and one column per microarray.");
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

	@Override
	public IndexedContainer getIndexedContainer() {

		loadTabViewPreferences();
		IndexedContainer dataIn = new IndexedContainer();
		List<String> colHeaders = getTabViewColHeaders();
		DSItemList<DSGeneMarker> selectedMarkers = getTabViewMarkers();

		int displayPrefColunmNum = getDisplayPrefColunmNum(tabViewPreferences);
		precisonNumber = tabViewPreferences.getNumberPrecisionControl();
 
		for (int i = 0; i < selectedMarkers.size(); i++)
		{		 
			Item item = dataIn.addItem(i);
			for (int k = 0; k < colHeaders.size(); k++) {
				if (k < displayPrefColunmNum) {
					dataIn.addContainerProperty(colHeaders.get(k),
							String.class, null);
					if (selectedMarkers.size() == 0)
						continue;
					if (colHeaders.get(k).equalsIgnoreCase(
							Constants.MARKER_HEADER))
						item.getItemProperty(colHeaders.get(k)).setValue(
								selectedMarkers.get(i).getLabel());
					else if (colHeaders.get(k).equalsIgnoreCase(
							Constants.GENE_SYMBOL_HEADER))
						item.getItemProperty(colHeaders.get(k)).setValue(
								selectedMarkers.get(i).getGeneName());

					else if (colHeaders.get(k).equalsIgnoreCase(
							Constants.ANNOTATION_HEADER))
					{
						String[] list = AnnotationParser.getInfo(selectedMarkers.get(i)
								.getLabel(), AnnotationParser.DESCRIPTION);
						if (list != null && list.length > 0)							 
						    item.getItemProperty(colHeaders.get(k)).setValue(list[0]);
						else
							item.getItemProperty(colHeaders.get(k)).setValue("---");
                        
					}
				} else {
					dataIn.addContainerProperty(colHeaders.get(k), Float.class,
							null);
					if (selectedMarkers.size() == 0)
						continue;
					item.getItemProperty(colHeaders.get(k)).setValue(
							(float) maSet
									.get(colHeaders.get(k))
									.getMarkerValue(
											selectedMarkers.get(i)
													.getSerial()).getValue());
				}
			}
			 
		}

		return dataIn;
	}
 

	@Override
	public PagedTableView getPagedTableView() {		 
		return this.displayTable;
	}

	@Override
	public void setSearchStr(String search) {
		this.searchStr = search;
		
	}

	@Override
	public Long getUserId() {		 
		return this.userId;
	}

	@Override
	public void setPrecisonNumber(int precisonNumber) {		 
		this.precisonNumber = precisonNumber;
	}

 
}
