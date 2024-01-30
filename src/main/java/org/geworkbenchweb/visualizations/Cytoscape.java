package org.geworkbenchweb.visualizations;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

@JavaScript({ "https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js", "cytoscape.min.js",
		"jquery.contextMenu.min.js", "network_viewer.js" })
public class Cytoscape extends AbstractJavaScriptComponent {

	@Override
	public CytoscapeState getState() {
		return (CytoscapeState) super.getState();
	}

	public void setNodes(String[] nodes) {
		getState().nodes = nodes;
	}

	public void setEdges(String[] edges) {
		getState().edges = edges;
	}

	public void setLayout(String layoutName) {
		getState().layoutName = layoutName;
	}

	public void setColor(String[] colors) {
		getState().colors = colors;
	}

}
