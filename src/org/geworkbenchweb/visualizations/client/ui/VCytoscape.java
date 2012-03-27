package org.geworkbenchweb.visualizations.client.ui;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VCytoscape extends Widget implements Paintable, ClickHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-cytoscape";

	public static final String CLICK_EVENT_IDENTIFIER = "click";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	static final String holderId = "canvasholder";

	static final String upgradeMessage = "Your browser does not support the HTML5 Canvas. Please upgrade your browser to view this demo.";
	  
	
	 Canvas canvas;
	 Canvas backBuffer;
	 
	 final CssColor redrawColor = CssColor.make("rgba(255,255,255,0.6)");
	 Context2d context;
	 Context2d backBufferContext;
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VCytoscape() {
		// TODO This example code is extending the GWT Widget class so it must set a root element.
		// Change to a proper element or remove this line if extending another widget.
		setElement(Document.get().createDivElement());
		
		// This method call of the Paintable interface sets the component
		// style name in DOM tree
		setStyleName(CLASSNAME);
		
		
	}

    /**
     * Called whenever an update is received from the server 
     */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// This call should be made first. 
		// It handles sizes, captions, tooltips, etc. automatically.
		if (client.updateComponent(this, uidl, true)) {
		    // If client.updateComponent returns true there has been no changes and we
		    // do not need to update anything.
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();
		
		int width = 600;
		int height = 600;
		
		 // init the canvases
	    canvas.setWidth(width + "px");
	    canvas.setHeight(height + "px");
	    canvas.setCoordinateSpaceWidth(width);
	    canvas.setCoordinateSpaceHeight(height);
	    backBuffer.setCoordinateSpaceWidth(width);
	    backBuffer.setCoordinateSpaceHeight(height);
	    RootPanel.get(holderId).add(canvas);
	    context = canvas.getContext2d();
	    backBufferContext = backBuffer.getContext2d();
	    context = canvas.getContext2d();
	    backBufferContext = backBuffer.getContext2d();
		
	}

    /**
     * Called when a native click event is fired.
     * 
     * @param event
     *            the {@link ClickEvent} that was fired
     */
     public void onClick(ClickEvent event) {
		
	}
}
