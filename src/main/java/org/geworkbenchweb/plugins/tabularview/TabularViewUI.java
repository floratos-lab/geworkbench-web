package org.geworkbenchweb.plugins.tabularview;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
 
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Tabular; 
import org.geworkbenchweb.pojos.DataSet; 
import org.geworkbenchweb.pojos.MicroarrayDataset; 
import org.geworkbenchweb.pojos.Preference; 
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
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource; 
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar; 
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator; 
import com.vaadin.ui.themes.Reindeer;

/**
 * Displays Tabular View for Microarray Data.  
 * 
 * @author Nikhil
 */
public class TabularViewUI extends VerticalLayout implements Tabular {

	private static final long serialVersionUID = -1544215388914183715L;

	private final Long userId;
	private int precisonNumber = 2;
	private String searchStr;	
	private final TabularViewPreferences tabViewPreferences;
    private final PagedTableView displayTable;
    
	final private Long datasetId;
	
	//private final Map<String, AnnotationEntry> annotationMap;
	private final Map<String, String[]> annotationMap;

	private static Log log = LogFactory.getLog(TabularViewUI.class);
	 
	public TabularViewUI(final Long dataSetId) {
 
		datasetId = dataSetId;
		if(dataSetId==null) {
			userId = null;
			displayTable = null;
			annotationMap = null;
			tabViewPreferences = null;
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

		displayTable
				.setItemDescriptionGenerator(new ItemDescriptionGenerator() {

					private static final long serialVersionUID = -7851708164038772830L;

					public String generateDescription(Component source,
							Object itemId, Object propertyId) {
						if (propertyId == null) {
							return null;
						} else {
							Item item = ((Table) source).getItem(itemId);
							Property property = item.getItemProperty(propertyId);
							Object value = property.getValue();
							if(value!=null) {
								return value.toString();
							} else {
								return null;
							}
						}
					}

				});
 
		log.debug("Started retrieve annotation ...");
		annotationMap = DataSetOperations.getAnnotationInfoMap(dataSetId);
		log.debug("Ended retrieve annotation ...");
		
		final MenuBar toolBar = new TabularMenuSelector(this, "TabularViewUI");
		addComponent(toolBar);
		addComponent(displayTable);
		setExpandRatio(displayTable, 1);	 
		log.debug("Started get indexedContainer ...");
		displayTable.setContainerDataSource(getIndexedContainer());
		log.debug("just get indexedContainer ...");
		displayTable.setColumnWidth(Constants.MARKER_HEADER, 150); 
		List<String> displayPrefColHeaders = getDisplayPrefColHeaders();
		for (String header : displayTable.getColumnHeaders()) {
			if(displayPrefColHeaders.contains(header)) continue;
			displayTable.setColumnAlignment(header, Table.ALIGN_RIGHT);
		}

		addComponent(displayTable.createControls());
		
		log.debug("Finished addComponent for displayTable ...");
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

				List<String> positions = SubSetOperations.getArrayData(selectedArraySet[i]);
 
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
 
		int markerDisplayControl = tabViewPreferences.getMarkerDisplayControl();
		if (selectedMarkerSet != null && selectedMarkerSet.length > 0
				&& (selectedMarkerSet[0]!=0)) {
			
			Set<String> included = new HashSet<String>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<String> positions = SubSetOperations.getMarkerData(selectedMarkerSet[i]);

				for (int m = 0; m < positions.size(); m++) {
					String temp = ((positions.get(m)).split("\\s+"))[0].trim();
					if (temp != null) {
						included.add(temp);
					}
				}
			}
			for (int i = 0; i < markerLabels.length; i++) {
				String marker = markerLabels[i];
				if (included.contains(marker) && isMatchSearch(marker, markerDisplayControl))
					selectedMarkers.add(i);
			}
		} else {
			for (int i = 0; i < markerLabels.length; i++) {
				String marker = markerLabels[i];
				if (isMatchSearch(marker, markerDisplayControl))
					selectedMarkers.add(i);
			}
		}

		return selectedMarkers;

	}

	private boolean isMatchSearch(String markerLabel, int markerDisplayControl) {
		if (searchStr == null || searchStr.trim().length() == 0)
			return true;

		if (markerDisplayControl == Constants.MarkerDisplayControl.both
				.ordinal()) {
			if (markerLabel.toUpperCase().contains(searchStr.toUpperCase())) {
				return true;
			} else {
				String[] geneSymbol = annotationMap.get(markerLabel);
				if (geneSymbol != null
						&& geneSymbol[0].toUpperCase().contains(searchStr.toUpperCase())) {
					return true;
				}
			}
		} else if (markerDisplayControl == Constants.MarkerDisplayControl.marker
				.ordinal() && markerLabel.toUpperCase().contains(searchStr.toUpperCase())) {
			return true;
		}
		else if ( markerDisplayControl == Constants.MarkerDisplayControl.gene_symbol.ordinal() )  
		{			
			String geneSymbol = annotationMap.get(markerLabel)[0];
			if (geneSymbol != null
					&& geneSymbol.toUpperCase().contains(searchStr.toUpperCase()))  
				return true;
		}
		return false;
	}
	 
	TabularViewPreferences getTabViewPreferences()
	{
		return tabViewPreferences;
	} 

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

	private IndexedContainer getIndexedContainer() {
		log.debug("before load dataset ...");
		DataSet data = FacadeFactory.getFacade().find(DataSet.class, datasetId);
		Long id = data.getDataId();
		MicroarrayDataset dataset = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		String[] arrayLabels = dataset.getArrayLabels();
		String[] markerLabels = dataset.getMarkerLabels();
		float[][] values = dataset.getExpressionValues();
		log.debug("after load dataset ...");

		log.debug("before loadTabViewPreferences()...");
		loadTabViewPreferences();
		log.debug("after loadTabViewPreferences()...");
		
		IndexedContainer dataIn =  new IndexedContainer() {
		          
				private static final long serialVersionUID = 1L;

				@Override
	            public Collection<?> getSortableContainerPropertyIds() {
	                // Default implementation allows sorting only if the property
	                // type can be cast to Comparable
	                return getContainerPropertyIds();
	            }
	        };
	        
	        dataIn.setItemSorter(new DefaultItemSorter(new Comparator<Object>() {

	            public int compare(Object o1, Object o2) {
	                if (o1 instanceof PopupView && o2 instanceof PopupView) {
	                    String caption1 = ((PopupView) o1).getData().toString();
	                    String caption2 = ((PopupView) o2).getData().toString();
	                    return caption1.compareTo(caption2);

	                } 
	                else if (o1 instanceof String && o2 instanceof String) {
	                    return ((String) o1).compareTo(
	                            ((String) o2));
	                } 
	                else if (o1 instanceof Float && o2 instanceof Float) {
	                    return ((Float) o1).compareTo(
	                            (Float) o2);
	                }
	                else
	                	return 0;
	            }
	        }));
	        
	    
	     
		List<String> displayPrefColHeaders = getDisplayPrefColHeaders();
		List<Integer> arrayColHeaders = getArrayColHeaders(arrayLabels);
		List<Integer> selectedMarkers = getTabViewMarkers(markerLabels);
		 
		precisonNumber = tabViewPreferences.getNumberPrecisionControl();
		
		log.debug("before for loop ...");
		for (int k = 0; k < displayPrefColHeaders.size(); k++)  
		{
			
			if (displayPrefColHeaders.get(k).equals(Constants.GENE_SYMBOL_HEADER) && annotationMap != null && annotationMap.size() != 0)
			{
				dataIn.addContainerProperty(displayPrefColHeaders.get(k), PopupView.class, null);
			}
			else
				dataIn.addContainerProperty(displayPrefColHeaders.get(k),	String.class, null);
		} 
					
		 
		for (int k = 0; k < arrayColHeaders.size(); k++)       
		{
			String arrayName = arrayLabels[arrayColHeaders.get(k)];
			dataIn.addContainerProperty(arrayName, Float.class,null);
		}		
		
	 
		for (Integer i : selectedMarkers)
		{		 
			Item item = dataIn.addItem(i);
			String probeSetId = markerLabels[i];
			String geneSymbol = null;
			String geneDescription = null;
			String entrezId = null;
			String[] entry = null;
			if (annotationMap != null)
			    entry = annotationMap.get(probeSetId);
			if(entry!=null) { // no annotation
				geneSymbol = entry[0];
				geneDescription = entry[1];
				entrezId = entry[2];
			}			
		 
			for (int k = 0; k < displayPrefColHeaders.size(); k++) {				
					if (selectedMarkers.size() == 0)
						continue;
					if (displayPrefColHeaders.get(k).equalsIgnoreCase(
							Constants.MARKER_HEADER))
						item.getItemProperty(displayPrefColHeaders.get(k)).setValue(
								probeSetId);
					else if (displayPrefColHeaders.get(k).equalsIgnoreCase(
							Constants.GENE_SYMBOL_HEADER))
					{	
						if(geneSymbol != null)
						{
							PopupView geneView = new PopupView(new PopupWindow(geneSymbol, entrezId));					 
						    geneView.setData(geneSymbol);
						    item.getItemProperty(displayPrefColHeaders.get(k)).setValue(
								geneView);		
						}
						else
							item.getItemProperty(displayPrefColHeaders.get(k)).setValue(
									geneSymbol);		
					}
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
					if (selectedMarkers.size() == 0)
						continue;					 
					item.getItemProperty(arrayName).setValue(values[i][arrayColHeaders.get(k)]);
				 
			 }
			 
		}
		log.debug("after for loop ...");
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
		/* temporary file location */
		String dirName = GeworkbenchRoot.getBackendDataDirectory() + "/"
				+ "export";
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdirs();

		final Application app = getApplication();
		final File file = new File(dirName + "/expression_data_" + datasetId + ".tsv");
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
				if (p[0].equals(Constants.GENE_SYMBOL_HEADER))
				{
					if (item.getItemProperty(p[0]).getValue() != null && item.getItemProperty(p[0]).getValue() instanceof PopupView) 
					    pw.print(((PopupView)item.getItemProperty(p[0]).getValue()).getData().toString());
					else
						pw.print(item.getItemProperty(p[0]).getValue());
				}
				else
				   pw.print(item.getItemProperty(p[0]).getValue());
				for (int i = 1; i < p.length; i++) {
					if (p[i].equals(Constants.GENE_SYMBOL_HEADER) && item.getItemProperty(p[i]).getValue() instanceof PopupView)
					    pw.print("\t" + ((PopupView)item.getItemProperty(p[i]).getValue()).getData().toString());
					else
						pw.print("\t" + item.getItemProperty(p[i]).getValue());
				}
				pw.print("\n");
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file, app);
		app.getMainWindow().open(resource);
	}
	
	 
}
