package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VCitrusDiagram extends Widget implements Paintable {

	private Element placeholder = DOM.createDiv();

	public VCitrusDiagram() {
		setElement(placeholder);
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}		
		placeholder.setId(uidl.getId());

		String[] alteration = uidl.getStringArrayAttribute("alteration");
		String[] nes = uidl.getStringArrayAttribute("nes");

		createInstance(placeholder.getId(), wrapArray(alteration), wrapArray(nes));
	}

	public static native void createInstance(String containerId, JsArrayString alteration, JsArrayString nes)/*-{
		$wnd.$citrus_diagram.create(containerId, alteration, nes);
	}-*/;

	public static JsArrayString wrapArray(String[] srcArray) {
		JsArrayString result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, srcArray[i]);
		}
		return result;
	}
}
