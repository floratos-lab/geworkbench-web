package org.geworkbenchweb.plugins.tabularview;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Tabular;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.AnnotationEntry;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PagedTableView;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Displays Tabular View for Microarray Data.  
 * 
 * @author Nikhil
 */
public class TabularViewUI extends VerticalLayout implements Tabular {

	private static final long serialVersionUID = -1544215388914183715L;

	private Long userId;
	private int precisonNumber = 2;
	private String searchStr;	
	private TabularViewPreferences tabViewPreferences;
    private PagedTableView displayTable;
    
	final private Long datasetId;
	
	private final Map<String, AnnotationEntry> annotationMap;

	 
	public TabularViewUI(final Long dataSetId) {
 
		datasetId = dataSetId;
		if(dataSetId==null) {
			annotationMap = null;	 
			return;
		}
		
 
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

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("dataSetId", dataSetId);
		DataSetAnnotation dataSetAnnotation = FacadeFactory.getFacade().find(
				"SELECT d FROM DataSetAnnotation AS d WHERE d.datasetid=:dataSetId", parameter);
		annotationMap = new HashMap<String, AnnotationEntry>();
		if(dataSetAnnotation!=null) {
			Long annotationId = dataSetAnnotation.getAnnotationId();
			Annotation annotation = FacadeFactory.getFacade().find(Annotation.class, annotationId);
			for(AnnotationEntry entry : annotation.getAnnotationEntries()) {
				String probeSetId = entry.getProbeSetId();
				annotationMap.put(probeSetId, entry);
			}
		}

		
		final MenuBar toolBar = new TabularMenuSelector(this, "TabularViewUI");
		addComponent(toolBar);
		addComponent(displayTable);
		setExpandRatio(displayTable, 1);	 
	
		displayTable.setContainerDataSource(getIndexedContainer());
		displayTable.setColumnWidth(Constants.MARKER_HEADER, 150); 

		addComponent(displayTable.createControls());
	} 

	 
 
	private void loadTabViewPreferences() {

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
	
	private List<String> getDisplayPrefColHeaders() {			 
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
	 
		return colHeaders;
	}
	

	private List<Integer> getArrayColHeaders(String[] arrayLabels) {			 
		List<Integer> colHeaders = new ArrayList<Integer>();
		
		FilterInfo arrayFilter = tabViewPreferences.getArrayFilter();

		Long[] selectedArraySet = null;
		if (arrayFilter != null)
			selectedArraySet = arrayFilter.getSelectedSet();

		if (selectedArraySet == null
				|| selectedArraySet[0]==0) {
			for (int i = 0; i < arrayLabels.length; i++)
				colHeaders.add(i);
		} else {

			for (int i = 0; i < selectedArraySet.length; i++) {

				List<?> subSet = SubSetOperations.getArraySet(selectedArraySet[i]);
				ArrayList<String> positions = (((SubSet) subSet.get(0))
						.getPositions());
 
				for (int j = 0; j < arrayLabels.length; j++) {
					String array = arrayLabels[j];
					if (positions.contains(array))
						colHeaders.add(j);
				}

			}
		}

		return colHeaders;
	}

 
	private List<Integer> getTabViewMarkers(String[] markerLabels) {		 
		List<Integer> selectedMarkers = new ArrayList<Integer>();

		Long[] selectedMarkerSet = null;

		FilterInfo markerFilter = tabViewPreferences.getMarkerFilter();
		if (markerFilter != null)
			selectedMarkerSet = markerFilter.getSelectedSet();

		Map<String, String> map = DataSetOperations.getAnnotationMap(datasetId);
		
		int markerDisplayControl = tabViewPreferences.getMarkerDisplayControl();
		if (selectedMarkerSet != null && selectedMarkerSet.length > 0
				&& (selectedMarkerSet[0]!=0)) {
			
			Set<String> included = new HashSet<String>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<?> subSet = SubSetOperations.getMarkerSet(selectedMarkerSet[i]);
				if (subSet == null || subSet.size() == 0)
					continue;
				ArrayList<String> positions = (((SubSet) subSet.get(0))
						.getPositions());

				for (int m = 0; m < positions.size(); m++) {
					String temp = ((positions.get(m)).split("\\s+"))[0].trim();
					if (temp != null) {
						included.add(temp);
					}
				}
			}
			for (int i = 0; i < markerLabels.length; i++) {
				String marker = markerLabels[i];
				if (included.contains(marker) && isMatchSearch(marker, markerDisplayControl, map))
					selectedMarkers.add(i);
			}
		} else {
			for (int i = 0; i < markerLabels.length; i++) {
				String marker = markerLabels[i];
				if (isMatchSearch(marker, markerDisplayControl, map))
					selectedMarkers.add(i);
			}
		}

		return selectedMarkers;

	}

	private boolean isMatchSearch(String markerLabel, int markerDisplayControl,
			Map<String, String> map) {
		if (searchStr == null || searchStr.trim().length() == 0)
			return true;

		if (markerDisplayControl == Constants.MarkerDisplayControl.both
				.ordinal()) {
			if (markerLabel.toUpperCase().contains(searchStr.toUpperCase())) {
				return true;
			} else {
				String geneSymbol = map.get(markerLabel);
				if (geneSymbol != null
						&& geneSymbol.toUpperCase().contains(searchStr.toUpperCase())) {
					return true;
				}
			}
		} else if (markerDisplayControl == Constants.MarkerDisplayControl.marker
				.ordinal() && markerLabel.toUpperCase().contains(searchStr.toUpperCase())) {
			return true;
		}
		else if ( markerDisplayControl == Constants.MarkerDisplayControl.gene_symbol.ordinal() )  
		{			
			String geneSymbol = map.get(markerLabel);
			if (geneSymbol != null
					&& geneSymbol.toUpperCase().contains(searchStr.toUpperCase()))  
				return true;
		}
		return false;
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

	private IndexedContainer getIndexedContainer() {
		IndexedContainer dataIn = null;
		DataSet data = FacadeFactory.getFacade().find(DataSet.class, datasetId);
		Long id = data.getDataId();
		MicroarrayDataset dataset = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] arrayLabels = dataset.getArrayLabels();
		String[] markerLabels = dataset.getMarkerLabels();
		float[][] values = dataset.getExpressionValues();
		dataIn = getIndexedContainer(markerLabels, arrayLabels, values);
		return dataIn;
	}

	private IndexedContainer getIndexedContainer(String[] markerLabels, String[] arrayLabels,
			float[][] values) {

		loadTabViewPreferences();
		IndexedContainer dataIn = new IndexedContainer();
		List<String> displayPrefColHeaders = getDisplayPrefColHeaders();
		List<Integer> arrayColHeaders = getArrayColHeaders(arrayLabels);
		List<Integer> selectedMarkers = getTabViewMarkers(markerLabels);
	 
		precisonNumber = tabViewPreferences.getNumberPrecisionControl();
 
		for (Integer i : selectedMarkers)
		{		 
			Item item = dataIn.addItem(i);
			String probeSetId = markerLabels[i];
			String geneSymbol = null;
			String geneDescription = null;
			AnnotationEntry entry = annotationMap.get(probeSetId);
			if(entry!=null) { // no annotation
				geneSymbol = entry.getGeneSymbol();
				geneDescription = entry.getGeneDescription();
			}			
		 
			for (int k = 0; k < displayPrefColHeaders.size(); k++) {
				 
					dataIn.addContainerProperty(displayPrefColHeaders.get(k),
							String.class, null);
					if (selectedMarkers.size() == 0)
						continue;
					if (displayPrefColHeaders.get(k).equalsIgnoreCase(
							Constants.MARKER_HEADER))
						item.getItemProperty(displayPrefColHeaders.get(k)).setValue(
								probeSetId);
					else if (displayPrefColHeaders.get(k).equalsIgnoreCase(
							Constants.GENE_SYMBOL_HEADER))
						item.getItemProperty(displayPrefColHeaders.get(k)).setValue(
								geneSymbol);
					else if (displayPrefColHeaders.get(k).equalsIgnoreCase(
							Constants.ANNOTATION_HEADER))
					{
						String list = geneDescription;
						if (list != null && list.length() > 0)							 
						    item.getItemProperty(displayPrefColHeaders.get(k)).setValue(list);
						else
							item.getItemProperty(displayPrefColHeaders.get(k)).setValue("---");
                        
					}
			} 
			
			for (int k = 0; k < arrayColHeaders.size(); k++) {
			        String arrayName = arrayLabels[arrayColHeaders.get(k)];
					dataIn.addContainerProperty(arrayName, Float.class,
							null);
					if (selectedMarkers.size() == 0)
						continue;					 
					item.getItemProperty(arrayName).setValue(values[i][arrayColHeaders.get(k)]);
				 
			 }
			 
		}

		return dataIn;
	}
 
	@Override
	public void setSearchStr(String search) {
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

	public void setPrecisonNumber(int precisonNumber) {		 
		this.precisonNumber = precisonNumber;
	}

	public void clearTable(){
		if (displayTable != null)
			displayTable.setContainerDataSource(new IndexedContainer());
	}
 
	@Override
	public void resetDataSource() {
		displayTable.setContainerDataSource(getIndexedContainer());
	}

	@Override
	public void export() {
		final Application app = getApplication();
		final File file = new File("expression_data_" + datasetId + ".txt");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			Collection<?> properties = displayTable.getContainerDataSource()
					.getContainerPropertyIds();
			Object[] p = properties.toArray();
			pw.print(p[0]);
			for (int i = 1; i < p.length; i++) {
				pw.print("\t" + p[i]);
			}
			pw.print("\n");
			Collection<?> items = displayTable.getItemIds();
			for (Object itemId : items) {
				Item item = displayTable.getItem(itemId);
				pw.print(item.getItemProperty(p[0]).getValue());
				for (int i = 1; i < p.length; i++) {
					pw.print("\t" + item.getItemProperty(p[i]).getValue());
				}
				pw.print("\n");
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file, app);
		app.getMainWindow().open(resource, "exported");
	}
}
