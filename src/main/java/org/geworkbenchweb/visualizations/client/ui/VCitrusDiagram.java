package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
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

		boolean zoom = uidl.getBooleanAttribute("zoom");
		if(zoom) {
			double xzoom = uidl.getDoubleAttribute("xzoom");
			zoomX(xzoom);
			return;
		}

		String[] alteration = uidl.getStringArrayAttribute("alteration");
		String[] samples = uidl.getStringArrayAttribute("samples");
		String[] presence = uidl.getStringArrayAttribute("presence");
		int[] preppi = uidl.getIntArrayAttribute("preppi");
		int[] cindy = uidl.getIntArrayAttribute("cindy");
		String[] pvalue = uidl.getStringArrayAttribute("pvalue");
		String[] nes = uidl.getStringArrayAttribute("nes");

		createInstance(placeholder.getId(), wrapArray(alteration), wrapArray(samples),
				wrapArray(presence), wrapArray(preppi), wrapArray(cindy), wrapArrayNumber(pvalue),
				wrapArrayNumber(nes));
	}

	public static native void createInstance(String containerId, JsArrayString alteration, JsArrayString samples,
			JsArrayString presence, JsArrayInteger preppi, JsArrayInteger cindy, JsArrayNumber pvalue,
			JsArrayNumber nes)/*-{
		$wnd.$citrus_diagram.create(containerId, alteration, samples, presence, preppi, cindy, pvalue, nes);
	}-*/;

	public static final native void zoomX(double xzoom)/*-{
		$wnd.$citrus_diagram.zoom_x(xzoom);
	}-*/;
	
	public static JsArrayNumber wrapArrayNumber(String[] srcArray) {
		JsArrayNumber result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, Double.parseDouble(srcArray[i]) );
		}
		return result;
	}

	public static JsArrayInteger wrapArray(int[] srcArray) {
		JsArrayInteger result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, srcArray[i]);
		}
		return result;
	}

	public static JsArrayString wrapArray(String[] srcArray) {
		JsArrayString result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, srcArray[i]);
		}
		return result;
	}
}
