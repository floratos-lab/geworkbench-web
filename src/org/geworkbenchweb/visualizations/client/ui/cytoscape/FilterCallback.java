package org.geworkbenchweb.visualizations.client.ui.cytoscape;

import com.google.gwt.core.client.JavaScriptObject;

public interface FilterCallback {
	public boolean invoke(JavaScriptObject element);
}
