package org.geworkbenchweb.plugins.tabularview;
 
import java.util.List;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 
 
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.utils.PreferenceOperations;

 
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.TableView;
import org.vaadin.appfoundation.authentication.SessionHandler;

import com.host900.PaginationBar.PaginationBar;
import com.host900.PaginationBar.PaginationBarListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
 
 
import com.vaadin.data.Container.Filter; 
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
 
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Displays Tabular View for Microarray Data.
 * For the begining display it shows 50 markers and then loaded
 * using lazy loading principle as per user request.
 * Uses PaginationBar Addon from Vaadin.
 * @author Nikhil
 */
public class TabularViewUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	 
	private PaginationBarListener paginationBarListener;
	private int paginationBarIndex;
	private int totalPages;
	private int currentPageIndex;
 
	private DSMicroarraySet maSet;
	private int pageSize = 50;
	
	private TabularViewPreferences tabViewPreferences = new TabularViewPreferences();
	private Long userId;
	private long currentContextId;
	private int precisonNumber =2;
	 
	public TabularViewUI(final Long dataSetId) {
		setSizeFull();
		setImmediate(true);
		
		userId = SessionHandler.get().getId();
		Context context = SubSetOperations.getCurrentContext(dataSetId);
		if (context != null)
		     currentContextId = context.getId();
		
		
		final TableView table = new TableView(){
  
			private static final long serialVersionUID = 5268979064889636700L;

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object value = property.getValue();
				if ((value != null) && (value instanceof Number)) {
					 
						return String.format("%." + precisonNumber +"f", value);
				}

				return super.formatPropertyValue(rowId, colId, property);
			}
		};

		final MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");
		
		MenuBar.MenuItem displayPreferences = toolBar.addItem("Display Preferences",
				null);
		displayPreferences.setStyleName("plugin");
	
		MenuBar.MenuItem geneOrMarkerItem = displayPreferences.addItem("Gene Symbol/Marker ID", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window displayPrefWindow = new Window();
				displayPrefWindow.setModal(true);
				displayPrefWindow.setClosable(true);
				((AbstractOrderedLayout) displayPrefWindow.getLayout()).setSpacing(true);
				displayPrefWindow.setWidth("300px");
				displayPrefWindow.setHeight("200px");
				displayPrefWindow.setResizable(false);
				displayPrefWindow.setCaption("Display Preference");
				displayPrefWindow.setImmediate(true);

				final OptionGroup og;
				og = new OptionGroup();
		        og.setImmediate(true);
				og.addItem(Constants.MarkerDisplayControl.marker.ordinal());
				og.addItem(Constants.MarkerDisplayControl.gene_symbol.ordinal());
				og.addItem(Constants.MarkerDisplayControl.both.ordinal());				 
				og.setItemCaption(Constants.MarkerDisplayControl.marker.ordinal(), "Marker ID");
				og.setItemCaption(Constants.MarkerDisplayControl.gene_symbol.ordinal(), "Gene Symbol");
				og.setItemCaption(Constants.MarkerDisplayControl.both.ordinal(), "Both");
			 
				og.select(tabViewPreferences.getMarkerDisplayControl());				
			 
				
				final Window mainWindow = getApplication().getMainWindow();
				
				Button submit = new Button("Submit", new Button.ClickListener() {
  
					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {							 
					
							Object value = og.getValue();
							Preference p = PreferenceOperations.getData(Constants.MARKER_DISPLAY_CONTROL, userId);
							if (p != null)
								PreferenceOperations.setValue(value, p);
							else
								PreferenceOperations.storeData(value, Integer.class.getName(), Constants.MARKER_DISPLAY_CONTROL, null, userId);
							 
							table.setContainerDataSource(tabularView(currentPageIndex, dataSetId));
							mainWindow.removeWindow(displayPrefWindow);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				displayPrefWindow.addComponent(og);
				displayPrefWindow.addComponent(submit);
				mainWindow.addWindow(displayPrefWindow);
			}
		});
	
		geneOrMarkerItem.setStyleName("plugin");
		
		MenuBar.MenuItem annotationsItem = displayPreferences.addItem("Annotations", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window displayPrefWindow = new Window();
				displayPrefWindow.setModal(true);
				displayPrefWindow.setClosable(true);
				((AbstractOrderedLayout) displayPrefWindow.getLayout()).setSpacing(true);
				displayPrefWindow.setWidth("300px");
				displayPrefWindow.setHeight("200px");
				displayPrefWindow.setResizable(false);
				displayPrefWindow.setCaption("Display Preference");
				displayPrefWindow.setImmediate(true);

				final OptionGroup og;
				og = new OptionGroup();
		        og.setImmediate(true);
				og.addItem(Constants.AnnotationDisplayControl.on.ordinal());
				og.addItem(Constants.AnnotationDisplayControl.off.ordinal());					 
				og.setItemCaption(Constants.AnnotationDisplayControl.on.ordinal(), "On");
				og.setItemCaption(Constants.AnnotationDisplayControl.off.ordinal(), "Off");
			 
				og.select(tabViewPreferences.getAnnotationDisplayControl());				
			 
				
				final Window mainWindow = getApplication().getMainWindow();
				
				Button submit = new Button("Submit", new Button.ClickListener() {
  
					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {							 
					
							Object value = og.getValue();
							Preference p = PreferenceOperations.getData(Constants.ANNOTATION_DISPLAY_CONTROL, userId);
							if (p != null)
								PreferenceOperations.setValue(value, p);
							else
								PreferenceOperations.storeData(value, Integer.class.getName(), Constants.ANNOTATION_DISPLAY_CONTROL, null, userId);
							 
							table.setContainerDataSource(tabularView(currentPageIndex, dataSetId));
							mainWindow.removeWindow(displayPrefWindow);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				displayPrefWindow.addComponent(og);
				displayPrefWindow.addComponent(submit);
				mainWindow.addWindow(displayPrefWindow);
			}
		});
	
		annotationsItem.setStyleName("plugin");
		
		MenuBar.MenuItem  precisionItem	= displayPreferences.addItem("Precision", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window displayPrefWindow = new Window();
				displayPrefWindow.setModal(true);
				displayPrefWindow.setClosable(true);
				((AbstractOrderedLayout) displayPrefWindow.getLayout()).setSpacing(true);
				displayPrefWindow.setWidth("300px");
				displayPrefWindow.setHeight("200px");
				displayPrefWindow.setResizable(false);
				displayPrefWindow.setCaption("Display Preference");
				displayPrefWindow.setImmediate(true);

				final TextField precision;
				precision = new TextField();
				precision.setCaption("Precision");
				precision.setValue(tabViewPreferences.getNumberPrecisionControl());			
				
				final Window mainWindow = getApplication().getMainWindow();
				
				Button submit = new Button("Submit", new Button.ClickListener() {
  
					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {							 
					
							Object value = precision.getValue();
							Preference p = PreferenceOperations.getData(Constants.NUMBER_PRECISION_CONTROL, userId);
							if (p != null)
								PreferenceOperations.setValue(new Integer(value.toString().trim()), p);
							else
								PreferenceOperations.storeData(new Integer(value.toString().trim()), Integer.class.getName(), Constants.NUMBER_PRECISION_CONTROL, null, userId);
							 
							table.setContainerDataSource(tabularView(currentPageIndex, dataSetId));							
							mainWindow.removeWindow(displayPrefWindow);
						}catch(NumberFormatException nfe)
						{
							MessageBox mb = new MessageBox(getWindow(), "Warning",
									MessageBox.Icon.WARN,
									"Please enter a number. ",
									new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
							mb.show();
							 
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				displayPrefWindow.addComponent(precision);
				displayPrefWindow.addComponent(submit);
				mainWindow.addWindow(displayPrefWindow);
			}
		});
		precisionItem.setStyleName("plugin");
		
		MenuBar.MenuItem  filterItem = toolBar.addItem("Filter", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window filterWindow = new Window();
				filterWindow.setModal(true);
				filterWindow.setClosable(true);
				((AbstractOrderedLayout) filterWindow.getLayout()).setSpacing(true);
				filterWindow.setWidth("420px");
				filterWindow.setHeight("320px");
				filterWindow.setResizable(false);
				filterWindow.setCaption("Filter Setting");
				filterWindow.setImmediate(true);

				final ListSelect markerSetSelect = new ListSelect("Select Marker Sets:");
				final ListSelect arraySetSelect = new ListSelect("Select Array Sets:");
				markerSetSelect.setMultiSelect(true);
				markerSetSelect.setRows(5);
				markerSetSelect.setColumns(15);
				markerSetSelect.setImmediate(true);
				
				List<?> markerSubSets = SubSetOperations.getMarkerSets(dataSetId);

				markerSetSelect.removeAllItems();
				markerSetSelect.addItem("All Markers");
				for (int m = 0; m < (markerSubSets).size(); m++) {
					markerSetSelect.addItem(((SubSet) markerSubSets.get(m)).getId());
					markerSetSelect.setItemCaption(
							((SubSet) markerSubSets.get(m)).getId(),
							((SubSet) markerSubSets.get(m)).getName());
				}				
				
				String[] selectedMarkerSet = null;
				if (tabViewPreferences.getMarkerFilter() != null)
					selectedMarkerSet= tabViewPreferences.getMarkerFilter().getSelectedSet();
				if (selectedMarkerSet != null && selectedMarkerSet.length > 0)
				{
					int startIndex =0;
					if (selectedMarkerSet[0].equalsIgnoreCase("All Markers"))
					{
						startIndex = startIndex +1;
						markerSetSelect.select("All Markers");
					}
					for(int i=startIndex; i<selectedMarkerSet.length; i++)
					{
						markerSetSelect.select(new Long(selectedMarkerSet[i].trim()));
					}
				}
			 
				
				
				arraySetSelect.setMultiSelect(true);
				arraySetSelect.setRows(5);
				arraySetSelect.setColumns(15);
				arraySetSelect.setImmediate(true);

				List<?> arraySubSets = SubSetOperations.getArraySetsForCurrentContext(dataSetId);
				arraySetSelect.removeAllItems();
				arraySetSelect.addItem("All Arrays");
				for (int m = 0; m < (arraySubSets).size(); m++) {					 
					arraySetSelect.addItem(((SubSet) arraySubSets.get(m)).getId());
					arraySetSelect.setItemCaption(
							((SubSet) arraySubSets.get(m)).getId(),
							((SubSet) arraySubSets.get(m)).getName());
				}
				 
				String[] selectedArraySet = null;
				if (tabViewPreferences.getArrayFilter() != null)
					selectedArraySet= tabViewPreferences.getArrayFilter().getSelectedSet();
				if (selectedArraySet != null && selectedArraySet.length > 0 && tabViewPreferences.getArrayFilter().getContextId() == currentContextId)
				{
					int startIndex =0;
					if (selectedArraySet[0].equalsIgnoreCase("All Arrays"))
					{
						startIndex = startIndex +1;
						arraySetSelect.select("All Arrays");
					}
					for(int i=startIndex; i<selectedArraySet.length; i++)
						arraySetSelect.select(new Long(selectedArraySet[i].trim()));
				}
			 			 
				
				final Window mainWindow = getApplication().getMainWindow();
				
				Button submit = new Button("Submit", new Button.ClickListener() {
  
					private static final long serialVersionUID = -4799561372701936132L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {							 
							 String value = markerSetSelect.getValue().toString();						   
							 FilterInfo markerFilter = new FilterInfo(currentContextId, getSelectedSet(value));
							 
							 
							 Preference p = PreferenceOperations.getData(dataSetId, Constants.MARKER_FILTER_CONTROL, userId);
							 if (p != null)
								PreferenceOperations.setValue(markerFilter, p);
							 else
								PreferenceOperations.storeData(markerFilter, FilterInfo.class.getName(), Constants.MARKER_FILTER_CONTROL, dataSetId, userId);
							 
							 value = arraySetSelect.getValue().toString();	
							 FilterInfo arrayFilter = new FilterInfo(currentContextId, getSelectedSet(value));
							 p = PreferenceOperations.getData(dataSetId, Constants.ARRAY_FILTER_CONTROL, userId);
							 if (p != null)
								PreferenceOperations.setValue(arrayFilter, p);
							 else
								PreferenceOperations.storeData(arrayFilter, FilterInfo.class.getName(), Constants.ARRAY_FILTER_CONTROL, dataSetId, userId);
							 currentPageIndex = 1;
							 table.setContainerDataSource(tabularView(currentPageIndex, dataSetId));
							 setPaginationBar();
							 mainWindow.removeWindow(filterWindow);
							 
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				filterWindow.addComponent(markerSetSelect);
				filterWindow.addComponent(arraySetSelect);
				filterWindow.addComponent(submit);
				mainWindow.addWindow(filterWindow);
			}
		});
	
		filterItem.setStyleName("plugin");
		
		 
		MenuBar.MenuItem  exportItem = toolBar.addItem("Export", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				table.csvExport("TabularViewTable.csv");				 
			}
		});
		exportItem.setStyleName("plugin");		
		
		MenuBar.MenuItem  searchItem = toolBar.addItem("search", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window searchWindow = new Window();
				// searchWindow.setModal(true);
				searchWindow.setClosable(true);
				((AbstractOrderedLayout) searchWindow.getLayout()).setSpacing(true);
				searchWindow.setWidth("300px");
				searchWindow.setHeight("120px");
				searchWindow.setResizable(false);

				searchWindow.setCaption("Search");

				searchWindow.setImmediate(true);

				final TextField search = new TextField();
				search.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
				search.setInputPrompt("Please enter search string");
				search.setImmediate(true);
				
				final Window mainWindow = getApplication().getMainWindow();
				
				search.addListener(new TextChangeListener() {
					private static final long serialVersionUID = 1048639156493298177L;

					Filter filter = null;

					public void textChange(TextChangeEvent event) {

						IndexedContainer hc = (IndexedContainer)table.getContainerDataSource();
						hc.removeAllContainerFilters();

					 
						filter = new Or(new SimpleStringFilter(Constants.MARKER_HEADER, event.getText(),
								true, false), new SimpleStringFilter(Constants.GENE_SYMBOL_HEADER, event.getText(),
										true, false));

						hc.addContainerFilter(filter);
						if (event.getText() != null && event.getText().length() > 0 )					 
							toolBar.getItems().get(4).setEnabled(true);					 
						else
							toolBar.getItems().get(4).setEnabled(false);	
 
					}
				});

				searchWindow.addComponent(search);
				mainWindow.addWindow(searchWindow);
				searchWindow.center();
			}
		});
		searchItem.setStyleName("plugin");
		 
		final MenuBar.MenuItem clearItem = toolBar.addItem("Clear Search", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				IndexedContainer hc = (IndexedContainer)table.getContainerDataSource();
				hc.removeAllContainerFilters();
				selectedItem.setEnabled(false);
			}
		});
		
		
		clearItem.setStyleName("plugin");
		clearItem.setEnabled(false); 
		 
		
		MenuBar.MenuItem  resetItem = toolBar.addItem("Reset", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PreferenceOperations.deleteAllPreferences(dataSetId, userId, "TabularViewUI%");
				currentPageIndex = 1;
				table.setContainerDataSource(tabularView(currentPageIndex, dataSetId));
				setPaginationBar();
				clearItem.setEnabled(false);
			}
		});
		
		resetItem.setStyleName("plugin");
		
		
		

	
		table.setSizeFull();
		table.setImmediate(true);
		table.setStyleName(Reindeer.TABLE_STRONG);

		DataSet data 	= 	DataSetOperations.getDataSet(dataSetId);
		maSet 			= 	(DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(data.getId()));

		addComponent(toolBar);
		addComponent(table);
		setExpandRatio(table, 1);		
		currentPageIndex = 1;
		table.setContainerDataSource(tabularView(currentPageIndex, dataSetId));
		table.setColumnWidth(Constants.MARKER_HEADER, 150);
		

	    
		paginationBarListener=new PaginationBarListener() {
			@Override
			public void pageRequested(int pageIndexRequested) {
				table.setContainerDataSource(tabularView(pageIndexRequested, dataSetId));
				currentPageIndex = pageIndexRequested;
			}
		};	
		
		setPaginationBar();	
		 
		
	}

	/**
	 * Method is called everytime user wants to to see more items in the table.
	 * Implements lazy loading principle.
	 * @param pageIndex
	 * @return IndexedContainer with Table Items
	 */
	private IndexedContainer tabularView(int pageIndex, Long dataSetId) {

		getTabViewPreferences(dataSetId);		
		IndexedContainer dataIn = new IndexedContainer();		
		List<String> colHeaders = getTabViewColHeaders();		 
		DSItemList<DSGeneMarker> selectedMarkers = getTabViewMarkers();
		
	    int displayPrefColunmNum = getDisplayPrefColunmNum();
	    precisonNumber = tabViewPreferences.getNumberPrecisionControl();
		
	    totalPages = (int) Math.ceil((double) selectedMarkers.size()/(double) pageSize);
	 
		/* Last page might not have all 50 elements */
		int flag = selectedMarkers.size() + 1;
		if(pageIndex == totalPages) {
			flag = selectedMarkers.size();
		}
		for(int i=((pageIndex-1)*pageSize+1);i<=(pageIndex-1)*pageSize+pageSize;i++){
			Item item = dataIn.addItem(i-1);
			for (int k = 0; k < colHeaders.size(); k++) {
				if (k < displayPrefColunmNum) {					 
					dataIn.addContainerProperty(colHeaders.get(k), String.class,
							null);
					if (colHeaders.get(k).equalsIgnoreCase(Constants.MARKER_HEADER))
					     item.getItemProperty(colHeaders.get(k)).setValue(
					    		 selectedMarkers.get(i-1).getLabel());
					else if (colHeaders.get(k).equalsIgnoreCase(Constants.GENE_SYMBOL_HEADER))
					     item.getItemProperty(colHeaders.get(k)).setValue(
					    		 selectedMarkers.get(i-1).getGeneName());
					
					else if (colHeaders.get(k).equalsIgnoreCase(Constants.ANNOTATION_HEADER))
					     item.getItemProperty(colHeaders.get(k)).setValue(
					    		 selectedMarkers.get(i-1).getDescription());
					
				} else {					 
					dataIn.addContainerProperty(colHeaders.get(k), Float.class,
							null);
					item.getItemProperty(colHeaders.get(k)).setValue(
							(float) maSet.get(colHeaders.get(k)).getMarkerValue(selectedMarkers.get(i-1).getSerial()).getValue());
				}
			}
			if (i == flag) break;
		}		
		 
		return dataIn;
	}
	
	
	private void getTabViewPreferences(Long dataSetId)
	{
		
		List<Preference> preferences = PreferenceOperations.getAllPreferences(dataSetId, userId, "TabularViewUI%");
		if ( preferences == null)
		{
			tabViewPreferences.reset();
			return;
		}
		for(Preference p:  preferences)
		{
			if (p.getName().equals(Constants.MARKER_DISPLAY_CONTROL))
				tabViewPreferences.setMarkerDisplayControl((Integer)ObjectConversion.toObject(p.getValue()));
			else if(p.getName().equals(Constants.ANNOTATION_DISPLAY_CONTROL))
				tabViewPreferences.setAnnotationDisplayControl((Integer)ObjectConversion.toObject(p.getValue()));
			else if(p.getName().equals(Constants.NUMBER_PRECISION_CONTROL))
				tabViewPreferences.setNumberPrecisionControl((Integer)ObjectConversion.toObject(p.getValue()));
			else if(p.getName().equals(Constants.MARKER_FILTER_CONTROL))
			{
				if (p.getValue() != null)
				{
					FilterInfo markerFilter = (FilterInfo)(ObjectConversion.toObject(p.getValue()));				 
		      	    tabViewPreferences.setMarkerFilter(markerFilter);
		             
				}
			}
			else if(p.getName().equals(Constants.ARRAY_FILTER_CONTROL))
			{
				if (p.getValue() != null)
				{
				   FilterInfo arrayFilter = (FilterInfo)(ObjectConversion.toObject(p.getValue()));				 
				 
				   if (arrayFilter != null && arrayFilter.getContextId() == currentContextId)
					   tabViewPreferences.setArrayFilter(arrayFilter);
		            else
		            {
		            	tabViewPreferences.setArrayFilter(null);
		            	if (arrayFilter != null)
		            	   PreferenceOperations.setValue(null, p);
		            }
				}
			}
			 
		}
	
	}
	
	
	private List<String> getTabViewColHeaders()
	{
		List<String> colHeaders = new ArrayList<String>();
		if (tabViewPreferences.getMarkerDisplayControl() == Constants.MarkerDisplayControl.both.ordinal())
		{
			colHeaders.add(Constants.MARKER_HEADER);
			colHeaders.add(Constants.GENE_SYMBOL_HEADER);
		}
		else if (tabViewPreferences.getMarkerDisplayControl() == Constants.MarkerDisplayControl.marker.ordinal())	 
			colHeaders.add(Constants.MARKER_HEADER);
		 
		else
			colHeaders.add(Constants.GENE_SYMBOL_HEADER);
		
		if (tabViewPreferences.getAnnotationDisplayControl() == Constants.AnnotationDisplayControl.on.ordinal())
			colHeaders.add(Constants.ANNOTATION_HEADER);
		
		FilterInfo arrayFilter = tabViewPreferences.getArrayFilter();
		
		String[] selectedArraySet = null;
		if (arrayFilter != null)
			selectedArraySet = arrayFilter.getSelectedSet();
		 
		if (selectedArraySet == null || selectedArraySet[0].equalsIgnoreCase("All Arrays") || arrayFilter.getContextId() != currentContextId)
		{
			 for (int i = 0; i < maSet.size(); i++)  
				 colHeaders.add(maSet.get(i).getLabel());			
		}
		else
		{			
			 
			for (int i = 0; i < selectedArraySet.length; i++) {

				List<?> subSet = SubSetOperations.getArraySet(Long
						.parseLong(selectedArraySet[i].trim()));
				ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());

				for (int j = 0; j < positions.size(); j++) {
 
					colHeaders.add(maSet.get(
							positions.get(j)).getLabel());					 
				}

			}
		}
		
		return colHeaders;
	}
	
	
	private int getDisplayPrefColunmNum()
	{
		int count = 0;
		if (tabViewPreferences.getMarkerDisplayControl() == Constants.MarkerDisplayControl.both.ordinal())
		{
			 count = 2;
		}
		else  
			count = 1;		 
		
		if (tabViewPreferences.getAnnotationDisplayControl() == Constants.AnnotationDisplayControl.on.ordinal())
			count = count + 1 ;
		
		return count;
		
	}
	
	
	private DSItemList<DSGeneMarker> getTabViewMarkers()
	{		 
		DSItemList<DSGeneMarker> selectedMarkers = null;
		String[] selectedMarkerSet = null;
		
		FilterInfo markerFilter = tabViewPreferences.getMarkerFilter();
		if(markerFilter != null)
		selectedMarkerSet = markerFilter.getSelectedSet();
		
		if (selectedMarkerSet != null && selectedMarkerSet.length >0 && (!selectedMarkerSet[0].equalsIgnoreCase("All Markers"))) {		 
			selectedMarkers = new CSItemList<DSGeneMarker>();
			for (int i = 0; i < selectedMarkerSet.length; i++) {
				List<?> subSet = SubSetOperations.getMarkerSet(Long
						.parseLong(selectedMarkerSet[i].trim()));
				if (subSet == null ||  subSet.size() == 0)
					continue;
				ArrayList<String> positions = (((SubSet) subSet.get(0)).getPositions());
				for (int m = 0; m < positions.size(); m++) {
					String temp = ((positions.get(m)).split("\\s+"))[0].trim();
					DSGeneMarker marker = maSet.getMarkers().get(temp);
					if (marker != null) 
						selectedMarkers.add(marker);				 
				}

			}
		}
		
		if (selectedMarkers == null || selectedMarkers.size() == 0)  
			selectedMarkers = maSet.getMarkers();		
		
		
		return selectedMarkers;
		

	} 
	 
	
	private String[] getSelectedSet(String temp)
	{
		String[] selectedSet = null;
		
		 
			if (!temp.equals("[]"))					 
				selectedSet = temp.substring(1,
						temp.length() - 1).split(",");	        
		 
		
		return selectedSet;
	}
	
	private void setPaginationBar()
	{
		   if (paginationBarIndex != 0)
			    this.removeComponent(getComponent(paginationBarIndex));
		    PaginationBar paginationBar=new PaginationBar(totalPages, paginationBarListener);
			HorizontalLayout pageBar=paginationBar.createPagination();				
			addComponent(pageBar);
			paginationBarIndex = getComponentIndex(pageBar);

	}
	
	 
	 
}




