package org.geworkbenchweb.visualizations;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

@JavaScript({ "https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js", "d3.v3.min.js", "citrus_diagram.js" })
public class CitrusDiagram extends AbstractJavaScriptComponent {

	@Override
	public CitrusDiagramState getState() {
		return (CitrusDiagramState) super.getState();
	}

	public void setCitrusData(String[] alteration, String[] samples, String[] presence,
			Integer[] preppi, Integer[] cindy,
			String[] pvalue, String[] nes) {
		getState().alteration = alteration;
		getState().samples = samples;
		getState().presence = presence;
		getState().preppi = preppi;
		getState().cindy = cindy;
		getState().pvalue = pvalue;
		getState().nes = nes;
		getState().zoom = false;
	}

	public void zoomX(double x) {
		getState().zoom = true;
		getState().xzoom = x;
	}

	public void zoomY(double y) {
		getState().zoom = true;
		getState().yzoom = y;
	}
}
