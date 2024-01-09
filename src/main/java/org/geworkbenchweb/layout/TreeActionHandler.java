package org.geworkbenchweb.layout;

import com.vaadin.data.Item;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public abstract class TreeActionHandler implements Handler {

	private static final long serialVersionUID = -5169931657971091101L;

	static final Action ACTION_ADD_SET = new Action("Add Set");

	static final Action ACTION_FILTER = new Action("Filter");
	static final Action ACTION_REMOVE_FILTER = new Action("Remove Filter");

	static final Action[] ACTIONS = new Action[] { ACTION_ADD_SET,
			ACTION_FILTER, ACTION_REMOVE_FILTER };

	final long dataSetId;

	public TreeActionHandler(long dataSetId) {

		this.dataSetId = dataSetId;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}

	@SuppressWarnings("deprecation")
	void filterAction(final Tree sender) {

		final Window nameWindow = new Window();
		// nameWindow.setModal(true);
		nameWindow.setClosable(true);
		nameWindow.setWidth("300px");
		nameWindow.setHeight("120px");
		nameWindow.setResizable(false);

		nameWindow.setCaption("Filter " + sender.getDescription());

		nameWindow.setImmediate(true);

		final TextField search = new TextField();
		search.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
		search.setInputPrompt("Please enter filter string");
		search.setImmediate(true);

		search.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1048639156493298177L;

			Filter filter = null;

			public void textChange(TextChangeEvent event) {

				HierarchicalContainer hc = ((HierarchicalContainer) sender
						.getContainerDataSource());
				hc.removeAllContainerFilters();

				// Set new filter for the "Name" column
				filter = new SimpleStringFilter("Labels", event.getText(),
						true, false);

				hc.addContainerFilter(filter);
				String label = sender.getDescription();
				label = sender.getDescription();
				Item mainItem = hc.getItem(label);
				if (mainItem == null) {

					hc.removeAllContainerFilters();
					filter = new SimpleStringFilter("Labels", label + " [",
							false, true);

					hc.addContainerFilter(filter);
					mainItem = hc.getItem(label);

				}

				mainItem.getItemProperty("Labels").setValue(
						label + " [" + (hc.getItemIds().size() - 1) + "]");

				for (Object itemId : sender.getItemIds())
					sender.expandItem(itemId);

			}
		});

		nameWindow.setContent(search);
		UI.getCurrent().addWindow(nameWindow);
		nameWindow.center();
	}

	void removeFilterAction(final Tree sender) {
		HierarchicalContainer hc = ((HierarchicalContainer) sender
				.getContainerDataSource());
		hc.removeAllContainerFilters();
		String label = sender.getDescription();

		Item mainItem = hc.getItem(label);
		if (mainItem != null)
			mainItem.getItemProperty("Labels").setValue(label + " [" + (hc.getItemIds().size() - 1) + "]");

	}

}
