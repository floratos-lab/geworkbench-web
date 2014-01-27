package org.geworkbenchweb.utils;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class LayoutUtil {
	/**
	 * create vertical layout with margin&spacing, then add component to it
	 * 
	 * @param comp
	 * @return vertical layout
	 */
	public static VerticalLayout addComponent(Component component) {
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(component);
		return layout;
	}
}
