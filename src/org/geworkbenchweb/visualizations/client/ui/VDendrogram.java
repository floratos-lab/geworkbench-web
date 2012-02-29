package org.geworkbenchweb.visualizations.client.ui;

import java.util.Comparator;

import org.geworkbenchweb.visualizations.client.ui.FlareData.Unit;
import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVClusterLayout;
import org.thechiselgroup.choosel.protovis.client.PVDom;
import org.thechiselgroup.choosel.protovis.client.PVDomNode;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.ProtovisWidget;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
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

		final String treeString =  "((())())";//uidl.getStringVariable("cluster").trim();
		
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();
				initPVPanel();
				PVPanel vis = getPVPanel().width(300).height(968).left(0).right(0).top(0).bottom(0);
				PVClusterLayout layout = vis
				.add(PV.Layout.Cluster())
				.nodes(PVDom.create(FlareData.data(treeString), new FlareData.UnitDomAdapter())
                        .sort(new Comparator<PVDomNode>() {
                            public int compare(PVDomNode o1, PVDomNode o2) {
                                return o1.nodeName().compareTo(o2.nodeName());
                            }
                        }).nodes()).group(true).orient("left");

				layout.link().add(PV.Line).strokeStyle("#ccc").lineWidth(1)
				.antialias(false);
			
				getPVPanel().render();
			}
		}, 100, 100);
		
		paintableId = uidl.getId();
		canvas.clear();
		
		width = 3000;
		height = 1500;
		panel.add(canvas, 400, 100);
        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.getElement().getStyle().setPropertyPx("width", width);
        canvas.getElement().getStyle().setPropertyPx("height", height);

        int arrayNumber = uidl.getIntVariable("arrayNumber");
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
    			
    			Text markerName = new Text(((i%arrayNumber) +1)*geneWidth + 50, ycord+5, uidl.getStringArrayVariable("markerLabels")[n]);
    			markerName.setFontSize(5);
    			canvas.add(markerName);
    			n++;
    			
    		}
        }
	}
	
	protected native Iterable<Unit> data(String treeString)/*-{
	
		
		
	}-*/;

}