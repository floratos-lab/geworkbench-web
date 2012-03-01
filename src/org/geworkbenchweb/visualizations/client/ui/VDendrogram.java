package org.geworkbenchweb.visualizations.client.ui;

import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVClusterLayout;
import org.thechiselgroup.choosel.protovis.client.PVDomNode;
import org.thechiselgroup.choosel.protovis.client.PVEventHandler;
import org.thechiselgroup.choosel.protovis.client.PVLinearScale;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.PVTransform;
import org.thechiselgroup.choosel.protovis.client.ProtovisWidget;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VDendrogram extends Composite implements Paintable {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-dendrogram";

	/**
     * Value for height of marker in pixels
     */
    static int geneHeight = 5;

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
		
		this.client	= client;
		paintableId = uidl.getId();
		
		final String treeString 	=  	uidl.getStringVariable("markerCluster").trim();
		final String treeString1	= 	uidl.getStringVariable("arrayCluster").trim();
		int arrayNumber 			= 	uidl.getIntVariable("arrayNumber");
		final int markerNumber 		=	uidl.getIntVariable("markerNumber");
		
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();
				
				/* Sizing and scales. */
		        final int w = 800;
		        final int h = 400;
		        final double kx = w / h;
		        final double ky = 1;
		        final PVLinearScale x = PV.Scale.linear(-kx, kx).range(0, w);
		        final PVLinearScale y = PV.Scale.linear(-ky, ky).range(0, h);
				
				initPVPanel();
				 
				PVPanel vis = getPVPanel().width(300).height(markerNumber*geneHeight).left(0).right(0).top(0).bottom(0);
				PVClusterLayout layout = vis
				.add(PV.Layout.Cluster())
				.nodes(((PVDomNode) TreeData.data(treeString)).nodes()).group(false).orient("left");

				layout.link().add(PV.Line).lineWidth(1)
				.antialias(false);
				
				/** Update the x- and y-scale domains per the new transform. */
		        PVEventHandler transform = new PVEventHandler() {
		            public void onEvent(Event e, String pvEventType, JsArgs args) {
		                PVPanel _this = args.getThis();
		                PVTransform t = _this.transform().invert();
		                x.domain(t.x() / w * 2 * kx - kx, (t.k() + t.x() / w) * 2 * kx
		                        - kx);
		                y.domain(t.y() / h * 2 * ky - ky, (t.k() + t.y() / h) * 2 * ky
		                        - ky);
		                getPVPanel().render();
		            }
		        };
			
				/* Use an invisible panel to capture pan & zoom events. */
		        vis.add(PV.Panel).events(PV.Events.ALL)
		                .event(PV.Event.MOUSEDOWN, PV.Behavior.pan())
		                .event(PV.Event.MOUSEWHEEL, PV.Behavior.zoom())
		                .event(PV.Behavior.PAN, transform)
		                .event(PV.Behavior.ZOOM, transform);
				getPVPanel().render();
			}
		}, 100, 100);
		
		canvas.clear();
		
		width = ((arrayNumber*geneWidth) + 600);
		height = ((markerNumber*geneHeight) + 400);
		
		panel.add(canvas, 400, 100);
        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.getElement().getStyle().setPropertyPx("width", width);
        canvas.getElement().getStyle().setPropertyPx("height", height);

        
        int ycord 	= 0;
        int n = 0;
        
        for(int i=0; i<(uidl.getStringArrayVariable("color")).length; i++) {
        	
        	if(i%arrayNumber == 0) {
        		
        		if(i != 0) {
        			
        			ycord = ycord + geneHeight;
        		
        		}	
        	
        	}
        	
        	final Rectangle geneBox = new Rectangle((i%arrayNumber)*geneWidth, ycord, geneWidth, geneHeight);
    		geneBox.setFillColor((uidl.getStringArrayVariable("color"))[i]);
    		geneBox.setStrokeColor(null);
    		geneBox.setStrokeWidth(0);
    		canvas.add(geneBox);
    	
    		if((i+1)%arrayNumber == 0) {
    			
    			Text markerName = new Text(((i%arrayNumber) +1)*geneWidth + 50, ycord+4, uidl.getStringArrayVariable("markerLabels")[n]);
    			markerName.setFontSize(5);
    			canvas.add(markerName);
    			n++;
    			
    		}
        }
	}
}