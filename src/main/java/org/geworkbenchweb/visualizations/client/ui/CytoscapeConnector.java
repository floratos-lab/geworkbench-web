package org.geworkbenchweb.visualizations.client.ui;

import org.geworkbenchweb.visualizations.Cytoscape;

import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("deprecation")
@Connect(Cytoscape.class)
public class CytoscapeConnector extends LegacyConnector {
	private static final long serialVersionUID = -4196760645845781967L;
	
	@Override
	public VCytoscape getWidget() {
		return (VCytoscape) super.getWidget();
	}
}
