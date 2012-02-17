package org.geworkbenchweb.analysis.hierarchicalclustering.client.ui;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VDendrogram extends Composite implements Paintable, ClickHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-dendrogram";

	/**
     * Value for height of marker in pixels
     */
    static int geneHeight = 8;

    /**
     * Value for width of marker in pixels
     */
    static int geneWidth = 20;
	
	public static final String CLICK_EVENT_IDENTIFIER = "click";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private DrawingArea canvas;
	
	private int width = 0;
	
	private int height = 0;
	
	private AbsolutePanel panel;
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	
	public VDendrogram() {
		panel = new AbsolutePanel();
		initWidget(panel);
		canvas = new DrawingArea(width, height);
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
		canvas.clear();
		
		width = 3000;
		height = 2000;
		panel.add(canvas, 100, 100);
        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.getElement().getStyle().setPropertyPx("width", width);
        canvas.getElement().getStyle().setPropertyPx("height", height);

        int arrayNumber = uidl.getIntVariable("arrayNumber");
        int counter = 0;
        int ycord 	= 0;
        int n = 0;
        
        for(int i=0; i<(uidl.getStringArrayVariable("color")).length; i++) {
        	
        	if(counter%arrayNumber == 0) {
        		if(counter != 0) {
        			ycord = ycord + geneHeight;
        		}	
        	}
        	
        	Rectangle geneBox = new Rectangle((i%arrayNumber)*geneWidth, ycord, geneWidth, geneHeight);
    		geneBox.setFillColor((uidl.getStringArrayVariable("color"))[i]);
    		geneBox.setStrokeColor(null);
    		geneBox.setStrokeWidth(0);
    		canvas.add(geneBox);
    		
    		if((counter+1)%arrayNumber == 0) {
    			
    			Text markerName = new Text(((i%arrayNumber)*geneWidth) + 50, ycord+5, uidl.getStringArrayVariable("markerLabels")[n]);
    			markerName.setFontSize(5);
    			canvas.add(markerName);
    			n++;
    			
    		}
    		counter++;
        }
	}

    /**
     * Called when a native click event is fired.
     * 
     * @param event
     *            the {@link ClickEvent} that was fired
     */
    public void onClick(ClickEvent event) {
		// Send a variable change to the server side component so it knows the widget has been clicked
		String button = "left click";
		// The last parameter (immediate) tells that the update should be sent to the server
		// right away
		client.updateVariable(paintableId, CLICK_EVENT_IDENTIFIER, button, true);
    }
	     
}
