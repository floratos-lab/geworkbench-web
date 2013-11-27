package org.geworkbenchweb.plugins;

  
import org.geworkbenchweb.utils.LayoutUtil;
import org.geworkbenchweb.utils.PreferenceOperations;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public abstract class TableMenuSelector extends MenuBar {
	
	 
	private static final long serialVersionUID = -8195610134056190752L; 
	 
	private MenuItem displayPreferences;
	private MenuItem filterItem;
	private MenuItem exportItem;
	private MenuItem searchItem;
	private MenuItem clearItem;
	private MenuItem resetItem;
	private String tabularName;
	Tabular parent;
	
	public TableMenuSelector(Tabular tabular, String name) {	
		 
		setImmediate(true);
		setStyleName("transparent");
		
		parent = tabular;
		tabularName = name;
		
		displayPreferences = this.addItem(
				"Display Preferences", null);
		displayPreferences.setStyleName("plugin"); 

		filterItem = this.addItem("Filter", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
			     createFilterWindow();
			}
		});

		filterItem.setStyleName("plugin");

		exportItem = this.addItem("Export", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {				 
				parent.getPagedTableView().csvExport("tabularViewTable.csv");				 
				 
			}
		});
		exportItem.setStyleName("plugin");

		searchItem = this.addItem("Search", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window searchWindow = new Window();			 
				searchWindow.setClosable(true);
				searchWindow.setWidth("300px");
				searchWindow.setHeight("120px");
				searchWindow.setResizable(false);

				searchWindow.setCaption("Search");

				searchWindow.setImmediate(true);

				final TextField search = new TextField();
				search.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
				search.setInputPrompt("Please enter search string");
				search.setImmediate(true);

				search.addTextChangeListener(new TextChangeListener() {
					private static final long serialVersionUID = 1048639156493298177L;

					public void textChange(TextChangeEvent event) {
						if (event.getText() != null
								&& event.getText().length() > 0) {
							getThisInstance().getItems().get(4).setEnabled(true);
							parent.setSearchStr(event.getText().trim().toUpperCase());
						} else {
							getThisInstance().getItems().get(4).setEnabled(false);
							parent.setSearchStr(null);
						}
						parent.getPagedTableView().setContainerDataSource(parent.getIndexedContainer());				 
							 

					}
				});

				searchWindow.setContent(LayoutUtil.addComponent(search));
				UI.getCurrent().addWindow(searchWindow);
				searchWindow.center();
			}
		});
		searchItem.setStyleName("plugin");

		clearItem = this.addItem("Clear Search",
				new Command() {

					private static final long serialVersionUID = 1L;

					@Override
					public void menuSelected(MenuItem selectedItem) {
						parent.setSearchStr(null);
					 
						parent.getPagedTableView().setContainerDataSource(parent.getIndexedContainer());						 
						selectedItem.setEnabled(false);
					}
				});

		clearItem.setStyleName("plugin");
		clearItem.setEnabled(false);
		parent.setSearchStr(null);

		resetItem = this.addItem("Reset", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PreferenceOperations.deleteAllPreferences(parent.getDatasetId(), parent.getUserId(),
						tabularName + "%");
				 
				parent.getPagedTableView().setContainerDataSource(parent.getIndexedContainer());			 
				clearItem.setEnabled(false);
				parent.setSearchStr(null);
			}
		});

		resetItem.setStyleName("plugin");	
		
	}
	
	private TableMenuSelector getThisInstance()
	{
		return this;
	}
	
	 
    public Tabular getTabular()
    {
    	return parent;
    }
    
    public MenuItem getDisplayPreferences()
    {
    	return this.displayPreferences;
    }
    
	
    abstract public void createDisplayPreferenceItems(MenuItem displayPreferences);
	
	abstract public void createFilterWindow();	 
	
	
}
