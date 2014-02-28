package org.geworkbenchweb.plugins;

import java.util.Map;

import org.geworkbenchweb.utils.PreferenceOperations;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
 
public abstract class TableMenuSelector extends MenuBar {

	private static final long serialVersionUID = -8195610134056190752L;

	private TextField search;
	protected final Tabular parent;

	public TableMenuSelector(final Tabular tabular, final String tabularName) {

		setImmediate(true);
		setStyleName("transparent");

		parent = tabular;

		MenuItem displayPreferences = this.addItem("Display Preferences", null);
		Map<String, Command> subItems = createDisplayPreferenceItems();
		for(String caption: subItems.keySet()) {
			MenuItem item = displayPreferences.addItem(caption, subItems.get(caption));
			item.setStyleName("plugin");
		}
		displayPreferences.setStyleName("plugin");

		MenuItem filterItem = this.addItem("Filter", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				Window filterWindow = createFilterWindow();
				getApplication().getMainWindow().addWindow(filterWindow);
			}
		});

		filterItem.setStyleName("plugin");

		MenuItem exportItem = this.addItem("Export", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				parent.export();
			}
		});
		exportItem.setStyleName("plugin");

		MenuItem searchItem = this.addItem("Search", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				if (search == null) {
					search = new TextField();
					search.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
					search.setInputPrompt("Please enter search string");
					search.setImmediate(true);
					if (parent.getSearchStr() != null
							&& !parent.getSearchStr().isEmpty()) {
						search.setValue(parent.getSearchStr());
					}
					search.addListener(new TextChangeListener() {
						private static final long serialVersionUID = 1048639156493298177L;

						public void textChange(TextChangeEvent event) {
							if (event.getText() != null
									&& event.getText().length() > 0) {
								TableMenuSelector.this.getItems().get(4)
										.setEnabled(true);
								parent.setSearchStr(event.getText().trim());
							} else {
								TableMenuSelector.this.getItems().get(4)
										.setEnabled(false);
								parent.setSearchStr(null);
							}
							parent.resetDataSource();

						}
					});

				}

				Window searchWindow = new Window();
				searchWindow.setClosable(true);
				((AbstractOrderedLayout) searchWindow.getContent())
							.setSpacing(true);
				searchWindow.setWidth("300px");
				searchWindow.setHeight("120px");
				searchWindow.setResizable(false);
				searchWindow.setCaption("Search");
				searchWindow.setImmediate(true); 

				searchWindow.addComponent(search);

				final Window mainWindow = getApplication().getMainWindow();
				mainWindow.addWindow(searchWindow);
				searchWindow.center();
			}
		});
		searchItem.setStyleName("plugin");

		final MenuItem clearItem = this.addItem("Clear Search", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				parent.setSearchStr(null);
				if (search != null)
				   search.setValue("");
				parent.resetDataSource();
				selectedItem.setEnabled(false);
			}
		});

		clearItem.setStyleName("plugin");
		clearItem.setEnabled(false);
		parent.setSearchStr(null);

		MenuItem resetItem = this.addItem("Reset", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PreferenceOperations.deleteAllPreferences(
						parent.getDatasetId(), parent.getUserId(), tabularName
								+ "%");

				parent.setSearchStr(null);
				if (search != null)
				   search.setValue("");
				clearItem.setEnabled(false);
				parent.resetDataSource();

			}
		});

		resetItem.setStyleName("plugin");

	} /* end of constructor TODO need refactoring */

	abstract protected Map<String, Command> createDisplayPreferenceItems();

	abstract protected Window createFilterWindow();

}
