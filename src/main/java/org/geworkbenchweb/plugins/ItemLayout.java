package org.geworkbenchweb.plugins;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ItemLayout extends GridLayout {

	private static final long serialVersionUID = -2801145303701009347L;

	private final VerticalLayout cssLayout = new VerticalLayout();

	public ItemLayout() {
		setColumns(2);
		setRows(2);
		setSizeFull();
		setImmediate(true);
		setColumnExpandRatio(1, 1.0f);

		cssLayout.setWidth("95%");
		cssLayout.addStyleName("lay");
	}

	public void addDescription(String itemDescription) {
		Label tableText = new Label(
				"<p align = \"justify\">" + itemDescription + "</p>");
		tableText.setContentMode(ContentMode.HTML);
		cssLayout.addComponent(tableText);
		addComponent(cssLayout, 0, 1, 1, 1);
	}

	public void clearDescription() {
		cssLayout.removeAllComponents();
		removeComponent(cssLayout);
	}
}