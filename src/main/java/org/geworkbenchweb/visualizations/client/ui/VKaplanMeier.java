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

public class VKaplanMeier extends Widget implements Paintable {

    private Element placeholder = DOM.createDiv();

    public VKaplanMeier() {
        setElement(placeholder);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        placeholder.setId(uidl.getId());

        int subtypes = uidl.getIntAttribute("subtypes");
        String[] y = uidl.getStringArrayAttribute("y");
        int[] series_name = uidl.getIntArrayAttribute("series_name");
        int[] series_count = uidl.getIntArrayAttribute("series_count");
        String title = uidl.getStringAttribute("title");
        String xtitle = uidl.getStringAttribute("xtitle");
        String ytitle = uidl.getStringAttribute("ytitle");

        createInstance(placeholder.getId(), title, xtitle, ytitle, subtypes, wrapArrayNumber(y),
                wrapArray(series_name), wrapArray(series_count));
    }

    public static native void createInstance(String containerId, String title, String xtitle, String ytitle,
            int subtypes, JsArrayNumber y, JsArrayInteger series_name, JsArrayInteger series_count)
    /*-{
        $wnd.$kaplan_meier.create(containerId, title, xtitle, ytitle, subtypes, y, series_name, series_count);
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
