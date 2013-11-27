package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;

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
	
	private String networkPNG;
	
	private Visualization vis;
	
	private String networkSVG;
	
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
		
		networkPNG 	= uidl.getStringVariable("networkPNG"); 
		networkSVG 	= uidl.getStringVariable("networkSVG"); 
		
		nodes = uidl.getStringArrayVariable("nodes");
		edges = uidl.getStringArrayVariable("edges");
		
		String layoutName = uidl.getStringAttribute("layoutName");
		
		if(networkPNG == "true") {
			
			String dataPNG = null;
			client.updateVariable(paintableId, "networkPNG", "false", false);
			client.updateVariable(paintableId, "networkPNGData", vis.export(dataPNG), true);
			
		} else if(networkSVG == "true") {
			
			String dataSVG = null;
			client.updateVariable(paintableId, "networkSVG", "false", false);
			client.updateVariable(paintableId, "networkSVGData", vis.exportSVG(dataSVG), true);
		
		}else {	
			
			vis = Visualization.create(placeholder.getId());
			vis.constructNetwork(wrapArray(nodes), wrapArray(edges), layoutName);
		}
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