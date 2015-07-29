package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VCytoscape widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VCytoscape.class)
public class Cytoscape extends AbstractComponent {

	private static final long serialVersionUID = -6368440900242204532L;

	private String[] nodes;
	private String[] edges;
	
	private String layoutName = "ForceDirected"; // default
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		
		target.addAttribute("layoutName", layoutName);

		target.addVariable(this, "nodes", getNodes());
		target.addVariable(this, "edges", getEdges());
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
	
}
