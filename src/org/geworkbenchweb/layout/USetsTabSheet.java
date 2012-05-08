package org.geworkbenchweb.layout;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public class USetsTabSheet extends TabSheet{

	private static final long serialVersionUID = 1L;

	public USetsTabSheet() {
		
		
		setSizeFull();
		
		addTab(new VerticalLayout(), "Marker Sets");
		addTab(new VerticalLayout(), "Array Sets");
	}

}
