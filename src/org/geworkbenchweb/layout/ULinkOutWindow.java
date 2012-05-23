package org.geworkbenchweb.layout;

import com.vaadin.ui.Window;

public class ULinkOutWindow extends Window {

	private static final long serialVersionUID = 1L;
	
	public ULinkOutWindow(String geneName) {
		
		setCaption("LinkOut Marker");
		setModal(true);
		setClosable(true);
		setHeight("400px");
		setWidth("350px");
	}
}
