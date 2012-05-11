package org.geworkbenchweb.visualizations.client.ui;

import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVBar;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.ProtovisWidget;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VHeatMap extends Composite implements Paintable {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-heatmap";

	/**
	 * Value for height of marker in pixels
	 */
	static int geneHeight = 10;

	/**
	 * Value for width of marker in pixels
	 */
	static int geneWidth = 30;

	/** The client side widget identifier. */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	/** Abolute panel to hold the Clustergram. */
	private AbsolutePanel panel;

	/** Number of arrays/phenotypes */
	private int arrayNumber;

	/** Number of markers */
	private int markerNumber;

	/** Gene Color Array */
	private String[] colorArray;

	/** Marker Labels */
	@SuppressWarnings("unused")
	private String[] markerLabels;

	/** Array Labels */
	@SuppressWarnings("unused")
	private String[] arrayLabels;


	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VHeatMap() {
		
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
		panel.clear();

		arrayNumber 			= 	uidl.getIntVariable("arrayNumber");
		markerNumber 			=	uidl.getIntVariable("markerNumber");
		colorArray 				=	uidl.getStringArrayVariable("color");
		markerLabels			= 	uidl.getStringArrayVariable("markerLabels");
		arrayLabels				= 	uidl.getStringArrayVariable("arrayLabels");
		
		/* Heat Map */
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();

				initPVPanel();
				final PVPanel vis = getPVPanel().width((arrayNumber*geneWidth)).height((markerNumber*geneHeight)).left(0).right(0).top(0).bottom(0);
				
				int topCordinate 	=  	0;
				int leftCordinate	= 	0; 
				
				for(int i=0; i<colorArray.length; i++) {
					if(i%arrayNumber == 0) {
						if(i != 0) {
							topCordinate 	= 	topCordinate + geneHeight;
							leftCordinate 	=	0;
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
				getPVPanel().render();
			}
		}, 0, 0);
				
	}

}
