package org.geworkbenchweb.plugins.tabularview;

import java.util.List;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion; 
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.plugins.tabularview.Constants;


import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.PagedTableView;
import org.vaadin.appfoundation.authentication.SessionHandler;
 
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button; 
 
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
 * 
 * @author Nikhil
 */
public class TabularViewUI extends VerticalLayout implements Visualizer {

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
	 
		final MenuBar toolBar = new MenuBar();
		toolBar.setStyleName("transparent");

		MenuBar.MenuItem displayPreferences = toolBar.addItem(
				"Display Preferences", null);
		displayPreferences.setStyleName("plugin");

		MenuBar.MenuItem geneOrMarkerItem = displayPreferences.addItem(
				"Gene Symbol/Marker ID", new Command() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("deprecation")
					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						((AbstractOrderedLayout) displayPrefWindow.getLayout())
								.setSpacing(true);
						displayPrefWindow.setWidth("300px");
						displayPrefWindow.setHeight("200px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Display Preference");
						displayPrefWindow.setImmediate(true);

						final OptionGroup og;
						og = new OptionGroup();
						og.setImmediate(true);
						og.addItem(Constants.MarkerDisplayControl.marker
								.ordinal());
						og.addItem(Constants.MarkerDisplayControl.gene_symbol
								.ordinal());
						og.addItem(Constants.MarkerDisplayControl.both
								.ordinal());
						og.setItemCaption(
								Constants.MarkerDisplayControl.marker.ordinal(),
								"Marker ID");
						og.setItemCaption(
								Constants.MarkerDisplayControl.gene_symbol
										.ordinal(), "Gene Symbol");
						og.setItemCaption(
								Constants.MarkerDisplayControl.both.ordinal(),
								"Both");

						og.select(tabViewPreferences.getMarkerDisplayControl());

						final Window mainWindow = getApplication()
								.getMainWindow();

						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {

											Object value = og.getValue();
											Preference p = PreferenceOperations
													.getData(
															Constants.MARKER_DISPLAY_CONTROL,
															userId);
											if (p != null)
												PreferenceOperations.setValue(
														value, p);
											else
												PreferenceOperations.storeData(
														value,
														Integer.class.getName(),
														Constants.MARKER_DISPLAY_CONTROL,
														null, userId);

											displayTable
													.setContainerDataSource(tabularView());				 
													 
											mainWindow
													.removeWindow(displayPrefWindow);
									 
										} catch (Exception e) {
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

		MenuBar.MenuItem annotationsItem = displayPreferences.addItem(
				"Annotations", new Command() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("deprecation")
					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						((AbstractOrderedLayout) displayPrefWindow.getLayout())
								.setSpacing(true);
						displayPrefWindow.setWidth("300px");
						displayPrefWindow.setHeight("200px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Display Preference");
						displayPrefWindow.setImmediate(true);

						final OptionGroup og;
						og = new OptionGroup();
						og.setImmediate(true);
						og.addItem(Constants.AnnotationDisplayControl.on
								.ordinal());
						og.addItem(Constants.AnnotationDisplayControl.off
								.ordinal());
						og.setItemCaption(
								Constants.AnnotationDisplayControl.on.ordinal(),
								"On");
						og.setItemCaption(
								Constants.AnnotationDisplayControl.off
										.ordinal(), "Off");

						og.select(tabViewPreferences
								.getAnnotationDisplayControl());

						final Window mainWindow = getApplication()
								.getMainWindow();

						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {

											Object value = og.getValue();
											Preference p = PreferenceOperations
													.getData(
															Constants.ANNOTATION_DISPLAY_CONTROL,
															userId);
											if (p != null)
												PreferenceOperations.setValue(
														value, p);
											else
												PreferenceOperations.storeData(
														value,
														Integer.class.getName(),
														Constants.ANNOTATION_DISPLAY_CONTROL,
														null, userId);

											displayTable
													.setContainerDataSource(tabularView());		 
															 
											mainWindow
													.removeWindow(displayPrefWindow);
										    
										} catch (Exception e) {
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

		MenuBar.MenuItem precisionItem = displayPreferences.addItem(
				"Precision", new Command() {

					private static final long serialVersionUID = 1L;

					@SuppressWarnings("deprecation")
					@Override
					public void menuSelected(MenuItem selectedItem) {
						final Window displayPrefWindow = new Window();
						displayPrefWindow.setModal(true);
						displayPrefWindow.setClosable(true);
						((AbstractOrderedLayout) displayPrefWindow.getLayout())
								.setSpacing(true);
						displayPrefWindow.setWidth("300px");
						displayPrefWindow.setHeight("200px");
						displayPrefWindow.setResizable(false);
						displayPrefWindow.setCaption("Display Preference");
						displayPrefWindow.setImmediate(true);

						final TextField precision;
						precision = new TextField();
						precision.setCaption("Precision");
						precision.setValue(tabViewPreferences
								.getNumberPrecisionControl());

						final Window mainWindow = getApplication()
								.getMainWindow();

						Button submit = new Button("Submit",
								new Button.ClickListener() {

									private static final long serialVersionUID = -4799561372701936132L;

									@Override
									public void buttonClick(ClickEvent event) {
										try {

											Object value = precision.getValue();
											Preference p = PreferenceOperations
													.getData(
															Constants.NUMBER_PRECISION_CONTROL,
															userId);
											if (p != null)
												PreferenceOperations.setValue(
														new Integer(value
																.toString()
																.trim()), p);
											else
												PreferenceOperations
														.storeData(
																new Integer(
																		value.toString()
																				.trim()),
																Integer.class
																		.getName(),
																Constants.NUMBER_PRECISION_CONTROL,
																null, userId);
											
											precisonNumber = new Integer(value.toString().trim());											
											displayTable
											.setContainerDataSource(displayTable.getContainerDataSource());
											tabViewPreferences.setNumberPrecisionControl(precisonNumber);
											mainWindow
													.removeWindow(displayPrefWindow);
										 
										} catch (NumberFormatException nfe) {
											MessageBox mb = new MessageBox(
													getWindow(),
													"Warning",
													MessageBox.Icon.WARN,
													"Please enter a number. ",
													new MessageBox.ButtonConfig(
															ButtonType.OK, "Ok"));
											mb.show();

										} catch (Exception e) {
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

		MenuBar.MenuItem filterItem = toolBar.addItem("Filter", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {

				final FilterWindow filterWindow = new FilterWindow(TabularViewUI.this);			 
				getApplication().getMainWindow().addWindow(filterWindow);

			}
		});

		filterItem.setStyleName("plugin");

		MenuBar.MenuItem exportItem = toolBar.addItem("Export", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {				 
				displayTable.csvExport("tabularViewTable.csv");				 
				 
			}
		});
		exportItem.setStyleName("plugin");

		MenuBar.MenuItem searchItem = toolBar.addItem("Search", new Command() {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window searchWindow = new Window();			 
				searchWindow.setClosable(true);
				((AbstractOrderedLayout) searchWindow.getLayout())
						.setSpacing(true);
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

					public void textChange(TextChangeEvent event) {
						if (event.getText() != null
								&& event.getText().length() > 0) {
							toolBar.getItems().get(4).setEnabled(true);
							searchStr = event.getText().trim().toUpperCase();
						} else {
							toolBar.getItems().get(4).setEnabled(false);
							searchStr = null;
						}
						displayTable.setContainerDataSource(tabularView());				 
							 

					}
				});

				searchWindow.addComponent(search);
				mainWindow.addWindow(searchWindow);
				searchWindow.center();
			}
		});
		searchItem.setStyleName("plugin");

		final MenuBar.MenuItem clearItem = toolBar.addItem("Clear Search",
				new Command() {

					private static final long serialVersionUID = 1L;

					@Override
					public void menuSelected(MenuItem selectedItem) {
						searchStr = null;
					 
						displayTable.setContainerDataSource(tabularView());						 
						selectedItem.setEnabled(false);
					}
				});

		clearItem.setStyleName("plugin");
		clearItem.setEnabled(false);
		searchStr = null;

		MenuBar.MenuItem resetItem = toolBar.addItem("Reset", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PreferenceOperations.deleteAllPreferences(dataSetId, userId,
						"TabularViewUI%");
				 
				displayTable.setContainerDataSource(tabularView());			 
				clearItem.setEnabled(false);
				searchStr = null;
			}
		});

		resetItem.setStyleName("plugin");

		displayTable.setSizeFull();
		displayTable.setImmediate(true);
		displayTable.setStyleName(Reindeer.TABLE_STRONG);

		DataSet data = DataSetOperations.getDataSet(dataSetId);
		try {
			maSet = (DSMicroarraySet) UserDirUtils.deserializeDataSet(data.getId(), DSMicroarraySet.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		addComponent(toolBar);
		addComponent(displayTable);
		setExpandRatio(displayTable, 1);		 
	 
		displayTable.setContainerDataSource(tabularView());
		displayTable.setColumnWidth(Constants.MARKER_HEADER, 150); 

		addComponent(displayTable.createControls());
	} // end of constructor FIXMEME: too long

	/**
	 * Method is called everytime user wants to to see more items in the table.
	 * Implements lazy loading principle.
	 * 
	 * @param pageIndex
	 * @return IndexedContainer with Table Items
	 */
	 IndexedContainer tabularView() {

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
	
	Long getUserId()
	{
		return userId;
	}

 
	@Override
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

 
}
