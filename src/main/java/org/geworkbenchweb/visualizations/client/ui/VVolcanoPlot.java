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

public class VVolcanoPlot extends Widget implements Paintable {

	private Element placeholder = DOM.createDiv();

	public VVolcanoPlot() {
		setElement(placeholder);
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

		if (client.updateComponent(this, uidl, true)) {
			return;
		}
		placeholder.setId(uidl.getId());

		String[] x = uidl.getStringArrayAttribute("x");
		String[] y = uidl.getStringArrayAttribute("y");
		String[] name = uidl.getStringArrayAttribute("name");
		String series_name = uidl.getStringAttribute("series_name");
		String title = uidl.getStringAttribute("title");
		String xtitle = uidl.getStringAttribute("xtitle");
		String ytitle = uidl.getStringAttribute("ytitle");

		createInstance(placeholder.getId(), title, xtitle, ytitle, wrapArrayNumber(x), wrapArrayNumber(y),
				wrapArray(name), series_name);
	}

	public static native void createInstance(String containerId, String title, String xtitle, String ytitle,
			JsArrayNumber x, JsArrayNumber y, JsArrayString name, String series_name)
	/*-{
		$wnd.$volcano_plot.create(containerId, title, xtitle, ytitle, x, y, name, series_name);
	}-*/;

	public static JsArrayNumber wrapArrayNumber(String[] srcArray) {
		JsArrayNumber result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, Double.parseDouble(srcArray[i]));
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
