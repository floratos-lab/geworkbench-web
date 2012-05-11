package org.geworkbenchweb.layout;

import com.vaadin.ui.MenuBar;

/**
 * Menu Bar class which will be common for all the Visual Plugins
 * @author Nikhil
 */
public class UMenuBar extends MenuBar {

	private static final long serialVersionUID = 1L;
	
	private static UMenuBar menuBarInstance;

	private UMenuBar() {
		
		setImmediate(true);
		setSizeFull();
		
	}
	
	public static UMenuBar getMenuBarObject() {
		if (menuBarInstance == null) {
			menuBarInstance = new UMenuBar();
		}
		return menuBarInstance;
	}
	
}
