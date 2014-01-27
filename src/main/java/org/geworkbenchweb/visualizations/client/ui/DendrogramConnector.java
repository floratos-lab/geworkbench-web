package org.geworkbenchweb.visualizations.client.ui;

import org.geworkbenchweb.visualizations.Dendrogram;

import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("deprecation")
@Connect(Dendrogram.class)
public class DendrogramConnector extends LegacyConnector {
	private static final long serialVersionUID = 3355130555102177080L;
	
	@Override
	public VDendrogram getWidget() {
		return (VDendrogram) super.getWidget();
	}
}
