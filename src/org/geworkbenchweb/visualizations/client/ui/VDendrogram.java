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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 * 
 * @author Nikhil Reddy
 * @Note Don't even think of making any changes with out asking Nikhil
 *
 */
public class VDendrogram extends Composite implements Paintable, ClickHandler, DoubleClickHandler, MouseDownHandler, MouseUpHandler,
				MouseMoveHandler, MouseWheelHandler, KeyDownHandler, KeyUpHandler {

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
		
        canvas.addMouseMoveHandler(this);
        canvas.addDoubleClickHandler(this);
        canvas.addMouseUpHandler(this);
        canvas.addMouseDownHandler(this);
        canvas.addClickHandler(this);
        canvas.addMouseWheelHandler(this);

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
		
		final String markerTreeString 	=  	uidl.getStringVariable("markerCluster").trim();
		final String arrayTreeString	= 	uidl.getStringVariable("arrayCluster").trim();
		final int arrayNumber 			= 	uidl.getIntVariable("arrayNumber");
		final int markerNumber 			=	uidl.getIntVariable("markerNumber");
		
		width = ((arrayNumber*geneWidth) + 600);
		height = ((markerNumber*geneHeight) + 600);
		
		/**
		 * This is where we draw Marker Cluster Dendrogram.
		 * only if exists.
		 */
		if(markerTreeString.contains("(")) {
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
							.nodes(((PVDomNode) TreeData.data(markerTreeString)).nodes()).group(false).orient("left");

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
			}, 100, 300);
		}
		
		/**
		 * This is where we draw Microarray Cluster Dendrogram.
		 * Only if exists.
		 */
		if(arrayTreeString.contains("(")) {
			panel.add(new ProtovisWidget() {
				protected void onAttach() {
					super.onAttach();

					initPVPanel();
					
					PVPanel arrayTree = getPVPanel().width(arrayNumber*geneWidth).height(200).left(0).right(0).top(0).bottom(0);
					PVClusterLayout arrayTreeLayout = arrayTree
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(arrayTreeString)).nodes()).group(false).orient("top");

					arrayTreeLayout.link().add(PV.Line).lineWidth(1)
					.antialias(false); 

					getPVPanel().render();
				}
			}, 400, 100);

		}
		
		/*
		 * Building Heat map starts here.
		 */
		canvas.clear();
		panel.add(canvas, 400, 300);
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
        	/*
        	 * This is where we start building rectangles for each gene and assign corresponding color.
        	 */
        	final Rectangle geneBox = new Rectangle((i%arrayNumber)*geneWidth, ycord, geneWidth, geneHeight);
    		geneBox.setFillColor((uidl.getStringArrayVariable("color"))[i]);
    		geneBox.setStrokeColor(null);
    		geneBox.setStrokeWidth(0);
    		canvas.add(geneBox);
    	
    		/*
			 * Here we add Marker Names
			 */
    		if((i+1)%arrayNumber == 0) {
    			
    			Text markerName = new Text(((i%arrayNumber) +1)*geneWidth + 50, ycord+5, uidl.getStringArrayVariable("markerLabels")[n]);
    			markerName.setFontSize(5);
    			canvas.add(markerName);
    			n++;
    			
    		}
    		
    		/*
    		 * Here we add Array Names
    		 */
    		if ((i+1) ==(uidl.getStringArrayVariable("color")).length) {
    			int xcord = 0;
    			
    			for(int j=0; j<(uidl.getStringArrayVariable("arrayLabels")).length; j++) {
    				if(j != 0){
    					xcord = xcord + 20;
    				}
    				Text arrayName = new Text(xcord, (geneHeight*markerNumber)+25,uidl.getStringArrayVariable("arrayLabels")[j]);
    				arrayName.setFontSize(6);
    				arrayName.setRotation(270);
    				canvas.add(arrayName);
    			}
    		}
        }
	}


	@Override
	public void onKeyUp(KeyUpEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onKeyDown(KeyDownEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMouseWheel(MouseWheelEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMouseUp(MouseUpEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMouseDown(MouseDownEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onClick(ClickEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMouseMove(MouseMoveEvent event) {
		
		
	}
}