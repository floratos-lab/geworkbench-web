package org.geworkbenchweb.visualizations.client.ui;

import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVBar;
import org.thechiselgroup.choosel.protovis.client.PVClusterLayout;
import org.thechiselgroup.choosel.protovis.client.PVDomNode;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.ProtovisWidget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
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
public class VDendrogram extends Composite implements Paintable, ClickHandler, 
					MouseMoveHandler {

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

	/** The client side widget identifier. */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	/** Abolute panel to hold the Clustergram. */
	private AbsolutePanel panel;
	
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */	
	public VDendrogram() {
		
		panel = new AbsolutePanel();
		initWidget(panel);
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
		
		final int width 	= 	((arrayNumber*geneWidth) + 600);
		final int height 	= 	((markerNumber*geneHeight) + 600);
		
		final String[] colorArray 		=	uidl.getStringArrayVariable("color");
		
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();

				initPVPanel();
				
				final PVPanel vis = getPVPanel().width(width).height(height).left(0).right(0).top(0).bottom(0);
				
				/* Marker Dendrogram */
				if(markerTreeString.contains("(")) {
					PVClusterLayout layout = vis
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(markerTreeString)).nodes()).group(false).orient("left")
							.left(25).top(175).height(markerNumber*geneHeight).width(200);
					layout.link().add(PV.Line).lineWidth(1)
					.antialias(false);
				}
				
				/* Array Dendrogram*/
				if(arrayTreeString.contains("(")) {
					PVClusterLayout arrayTreeLayout = vis
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(arrayTreeString)).nodes()).group(false).orient("top")
							.left(225).top(25).width(arrayNumber*geneWidth).height(150);
					arrayTreeLayout.link().add(PV.Line).lineWidth(1)
					.antialias(false); 
				}
				
				/* Heatmap*/
				int topCordinate 	=  	175;
				int leftCordinate	= 	225; 
				for(int i=0; i<colorArray.length; i++) {
					if(i%arrayNumber == 0) {
						if(i != 0) {
							topCordinate 	= 	topCordinate + geneHeight;
							leftCordinate 	=	225;
						}	
					} else {
						
						leftCordinate = leftCordinate + geneWidth;
						
					}
					@SuppressWarnings("unused")
					PVBar bar = vis.add(PV.Bar)
							.top(topCordinate)
							.left(leftCordinate)
							.height(geneHeight)
							.width(geneWidth)
							.fillStyle("#" + colorArray[i]);
				}
				/* capture pan & zoom events on main panel */
		        //getPVPanel().event(PV.Event.MOUSEWHEEL, PV.Behavior.zoom());
		        getPVPanel().render();
			}
		}, 0, 0);
        
	}

	@Override
	public void onClick(ClickEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onMouseMove(MouseMoveEvent event) {
		
		
	}
}