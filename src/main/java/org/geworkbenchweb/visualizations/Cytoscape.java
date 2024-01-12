package org.geworkbenchweb.visualizations;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VCytoscape widget.
 */
public class Cytoscape extends AbstractComponent {

	private static final long serialVersionUID = -6368440900242204532L;

	private String[] nodes;
	private String[] edges;

	private String layoutName = "concentric"; // default

	// FIXME this should be replaced by vaadin 7 communication mechanism
	public void paintContent(PaintTarget target) throws PaintException {

		target.addAttribute("layoutName", layoutName);

		target.addVariable(this, "nodes", getNodes());
		target.addVariable(this, "edges", getEdges());

		if (colors != null)
			target.addVariable(this, "colors", colors);
	}

	public void setNodes(String[] nodes) {
		this.nodes = nodes;
		requestRepaint();
	}

	public String[] getNodes() {

		return nodes;

	}

	public void setEdges(String[] edges) {
		this.edges = edges;
		requestRepaint();
	}

	public String[] getEdges() {

		return edges;

	}

	public void setLayout(String layoutName) {
		this.layoutName = layoutName;
	}

	private String[] colors = null;

	public void setColor(String[] colors) {
		this.colors = colors;
		requestRepaint();
	}

}
