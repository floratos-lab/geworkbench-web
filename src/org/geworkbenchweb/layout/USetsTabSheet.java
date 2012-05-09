package org.geworkbenchweb.layout;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class USetsTabSheet extends TabSheet{

	private static final long serialVersionUID = 1L;

	public USetsTabSheet() {
		
		setStyleName(Reindeer.TABSHEET_SMALL);
		setSizeFull();
		VerticalLayout l1 = new VerticalLayout();
		VerticalLayout l2 = new VerticalLayout();
		
		l1.setSizeFull();
		l1.setStyleName(Reindeer.LAYOUT_WHITE);
		l2.setSizeFull();
		l2.setStyleName(Reindeer.LAYOUT_WHITE);
		addTab(l1, "Marker Sets");
		addTab(l2, "Array Sets");
	}

}
