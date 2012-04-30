package org.geworkbenchweb.visualizations.client.ui;

import org.geworkbenchweb.visualizations.client.ui.Visualization;

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

	/** The client side widget identifier */
	protected String paintableId;

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

		paintableId = uidl.getId();
		placeholder.setId(paintableId + "-swupph");
		
		String[] nodes = new String[uidl.getStringArrayVariable("nodes").length];
		String[] edges = new String[uidl.getStringArrayVariable("edges").length];
		
		nodes = uidl.getStringArrayVariable("nodes");
		edges = uidl.getStringArrayVariable("edges");
		
		Visualization vis = Visualization.create(placeholder.getId());
		vis.constructNetwork(wrapArray(nodes), wrapArray(edges));
	}

	

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