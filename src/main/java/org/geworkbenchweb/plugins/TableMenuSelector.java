package org.geworkbenchweb.plugins;

import java.util.Map;

import org.geworkbenchweb.utils.PreferenceOperations;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public abstract class TableMenuSelector extends MenuBar {

	private static final long serialVersionUID = -8195610134056190752L;

	private TextField search;
	protected final Tabular parent;

	public TableMenuSelector(final Tabular tabular, final String tabularName) {

		setImmediate(true);
		setStyleName("transparent");

		parent = tabular;
		tabular.setSearchStr(null);

		MenuItem displayPreferences = this.addItem("Display Preferences", null);
		Map<String, Command> subItems = createDisplayPreferenceItems();
		for (String caption : subItems.keySet()) {
			MenuItem item = displayPreferences.addItem(caption,
					subItems.get(caption));
			item.setStyleName("plugin");
		}
		displayPreferences.setStyleName("plugin");

		MenuItem filterItem = this.addItem("Filter", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				Window filterWindow = createFilterWindow();
				UI.getCurrent().addWindow(filterWindow);
			}
		});
		filterItem.setStyleName("plugin");

		MenuItem exportItem = this.addItem("Export", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				tabular.export();
			}
		});
		exportItem.setStyleName("plugin");

		MenuItem searchItem = this.addItem("Search", null);
		searchItem.setStyleName("plugin");

		final MenuItem clearItem = this.addItem("Clear Search", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				tabular.setSearchStr(null);
				if (search != null)
					search.setValue("");
				tabular.resetDataSource();
				selectedItem.setEnabled(false);
			}
		});
		clearItem.setStyleName("plugin");
		clearItem.setEnabled(false);

		searchItem.setCommand(new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				showSearchWindow(tabular, clearItem);
			}
		});

		MenuItem resetItem = this.addItem("Reset", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PreferenceOperations.deleteAllPreferences(
						tabular.getDatasetId(), tabular.getUserId(),
						tabularName + "%");

				tabular.setSearchStr(null);
				if (search != null)
					search.setValue("");
				clearItem.setEnabled(false);
				tabular.resetDataSource();

			}
		});
		resetItem.setStyleName("plugin");
	} /* end of constructor */

	private static TextField createSearchTextField(final Tabular parent,
			final MenuItem clearItem) {
		TextField textField = new TextField();

		textField
				.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
		textField.setInputPrompt("Please enter search string");
		textField.setImmediate(true);

		String searchString = parent.getSearchStr();
		if (searchString != null && !searchString.isEmpty()) {
			textField.setValue(searchString);
		}
		textField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1048639156493298177L;

			public void textChange(TextChangeEvent event) {
				if (event.getText() != null && event.getText().length() > 0) {
					clearItem.setEnabled(true);
					parent.setSearchStr(event.getText().trim());
				} else {
					clearItem.setEnabled(false);
					parent.setSearchStr(null);
				}
				parent.resetDataSource();
			}
		});
		return textField;
	}

	private void showSearchWindow(final Tabular parent, final MenuItem clearItem) {
		if (search == null) {
			search = createSearchTextField(parent, clearItem);
		}

		Window searchWindow = new Window();
		searchWindow.setClosable(true);
		((AbstractOrderedLayout) searchWindow.getContent()).setSpacing(true);
		searchWindow.setWidth("300px");
		searchWindow.setHeight("120px");
		searchWindow.setResizable(false);
		searchWindow.setCaption("Search");
		searchWindow.setImmediate(true);

		searchWindow.setContent(search);
		UI.getCurrent().addWindow(searchWindow);
		searchWindow.center();
	}

	abstract protected Map<String, Command> createDisplayPreferenceItems();

	abstract protected Window createFilterWindow();
}
