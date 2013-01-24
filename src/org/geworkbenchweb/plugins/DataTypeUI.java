package org.geworkbenchweb.plugins;

import com.vaadin.ui.ComponentContainer;

/**
 * UI for a selected data type.
 * 
 * Initial version only handles the 'menu page' case, not the individual visualizer yet.
 * 
 * @author zji
 *
 */
public interface DataTypeUI extends ComponentContainer {

	String getDescription();
}
