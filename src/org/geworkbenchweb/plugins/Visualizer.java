package org.geworkbenchweb.plugins;

import com.vaadin.ui.Component;

/* class Analysis needs to be renamed to cover both analysis plugin and visualizer plugin */
public interface Visualizer extends Component {
	PluginEntry getPluginEntry();
	
	Long getDatasetId();
}
