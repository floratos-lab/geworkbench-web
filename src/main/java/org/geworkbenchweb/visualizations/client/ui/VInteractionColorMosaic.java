package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VInteractionColorMosaic extends Widget implements Paintable {

	private Element placeholder = DOM.createDiv();

	public VInteractionColorMosaic() {
		setElement(placeholder);
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}		
		placeholder.setId(uidl.getId());

		String interactome = uidl.getStringAttribute("interactome");
		String[] geneSymbol = uidl.getStringArrayAttribute("geneSymbol");
		String[] pValue = uidl.getStringArrayAttribute("pValue");
		String[] color = uidl.getStringArrayAttribute("color");

		createInstance(placeholder.getId(), interactome, wrapArray(geneSymbol), wrapArray(pValue), wrapArray(color));
	}

	public static native void createInstance(String containerId, String interactome, JsArrayString geneSymbol, JsArrayString pValue, JsArrayString color)/*-{
    	$wnd.$interaction_color_mosaic.create(containerId, interactome, geneSymbol, pValue, color);
	}-*/;
	
	public static JsArrayString wrapArray(String[] srcArray) {
		JsArrayString result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, srcArray[i]);
		}
		return result;
	}
}
