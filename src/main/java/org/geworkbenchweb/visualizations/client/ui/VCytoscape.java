package org.geworkbenchweb.visualizations.client.ui;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VCytoscape extends Widget implements Paintable {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-cytoscape";

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	/** DIV place holder which will be replaced by cytoscape flash object */
	private Element placeholder;
	
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VCytoscape() {   
		
		placeholder = DOM.createDiv();
		placeholder.setId("cy");
		setElement(placeholder);
		setStyleName(CLASSNAME);
	}

    /**
     * Called whenever an update is received from the server 
     */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		this.client = client;

		String[] nodes = new String[uidl.getStringArrayVariable("nodes").length];
		String[] edges = new String[uidl.getStringArrayVariable("edges").length];
		
		nodes = uidl.getStringArrayVariable("nodes");
		edges = uidl.getStringArrayVariable("edges");
		
		String layoutName = uidl.getStringAttribute("layoutName");
		
		createCytoscapeView(placeholder.getId(), wrapArray(nodes), wrapArray(edges), layoutName);

		String[] colors = uidl.getStringArrayVariable("colors");
		if(colors!=null) {
			setColor(placeholder.getId(), wrapArray(nodes), wrapArray(colors));
		}
	}

	public static final native void createCytoscapeView(String containerId, JsArrayString nodeArray, JsArrayString edgeArray, String layoutName)/*-{
		$wnd.$network_viewer.create(containerId, nodeArray, edgeArray, layoutName);
	}-*/;
	
	public static final native void setColor(String containerId, JsArrayString nodeArray, JsArrayString colors)/*-{
		$wnd.$network_viewer.set_color(containerId, nodeArray, colors);
	}-*/;
	
	/**
	 * Wraps a Java String Array to a JsArrayString for dev mode.
	 * 
	 * @param srcArray the array to wrap
	 * @return the wrapped array
	 */
	public static JsArrayString wrapArray(String[] srcArray) {
		JsArrayString result = JavaScriptObject.createArray().cast();
		for (int i = 0; i < srcArray.length; i++) {
			result.set(i, srcArray[i]);
		}
		return result;
	}

}