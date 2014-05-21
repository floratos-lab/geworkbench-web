package org.geworkbenchweb.plugins;

import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

public class ItemLayout extends GridLayout {

	private static final long serialVersionUID = -2801145303701009347L;
	
	private final FancyCssLayout cssLayout = new FancyCssLayout();
	
	public ItemLayout() {
		setColumns(2);
		setRows(2);
		setSizeFull();
		setImmediate(true);
		setColumnExpandRatio(1, 1.0f);

		cssLayout.setSlideEnabled(true);
		cssLayout.setWidth("95%");
		cssLayout.addStyleName("lay");
	}

	public void addDescription(String itemDescription) {
		Label tableText = new Label(
				"<p align = \"justify\">"+itemDescription+"</p>");
		tableText.setContentMode(Label.CONTENT_XHTML);
		cssLayout.addComponent(tableText);
		addComponent(cssLayout, 0, 1, 1, 1);
	}
	
	public void clearDescription() {
		cssLayout.removeAllComponents();
		removeComponent(cssLayout);
	}
}