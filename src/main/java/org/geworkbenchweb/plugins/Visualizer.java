package org.geworkbenchweb.plugins;

import com.vaadin.ui.Component;

/* visualizer plugin */
public interface Visualizer extends Component {
	PluginEntry getPluginEntry();
	
	Long getDatasetId();
}
