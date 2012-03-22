package org.geworkbenchweb.visualizations.client.ui;

import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVBar;
import org.thechiselgroup.choosel.protovis.client.PVClusterLayout;
import org.thechiselgroup.choosel.protovis.client.PVColor;
import org.thechiselgroup.choosel.protovis.client.PVDomNode;
import org.thechiselgroup.choosel.protovis.client.PVEventHandler;
import org.thechiselgroup.choosel.protovis.client.PVLink;
import org.thechiselgroup.choosel.protovis.client.PVPanel;
import org.thechiselgroup.choosel.protovis.client.ProtovisWidget;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsFunction;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
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

	/** The client side widget identifier. */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	/** Abolute panel to hold the Clustergram. */
	private AbsolutePanel panel;
	
	
	/**
	 * Marker Cluster Tree String
	 */
	private String markerTreeString;
	
	/**
	 * Array Cluster Tree String
	 */
	private String arrayTreeString;
	
	/**
	 * Number of arrays/phenotypes 
	 */
	private int arrayNumber;
	
	/**
	 * Number of markers
	 */
	private int markerNumber;
	/**
	 * Gene Color Array
	 */
	private String[] colorArray;
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
		panel.clear();
		/* All the variables from the server are handled here */
		markerTreeString 		=  	uidl.getStringVariable("markerCluster").trim();
		arrayTreeString			= 	uidl.getStringVariable("arrayCluster").trim();
		arrayNumber 			= 	uidl.getIntVariable("arrayNumber");
		markerNumber 			=	uidl.getIntVariable("markerNumber");
		colorArray 				=	uidl.getStringArrayVariable("color");
		
		/* Width of the dendrogram panel*/
		final int width 	= 	((arrayNumber*geneWidth) + 600);
		
		/* height of the dendrogram panel*/
		final int height 	= 	((markerNumber*geneHeight) + 600);
		
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();

				initPVPanel();
				
				final String selectedNodeIndexProperty = "selectedNodeIndex";
		        final String selectedArcIndexProperty = "selectedArcIndex";
				
		        final PVColor arcColor = PV.color("rgba(0,0,0,.2)");
		        final PVColor emphasizedArcColor = PV.color("red");
		        final PVColor deemphasizedArcColor = PV.color("rgba(0,0,0,.075)");
		        
		        final PVPanel vis = getPVPanel().width(width).height(height).left(0).right(0).top(0).bottom(0)
						.def(selectedNodeIndexProperty, -1)
		                .def(selectedArcIndexProperty, null);;
				
				/* Marker Dendrogram */
				if(markerTreeString.contains("(")) {
					PVClusterLayout layout = vis
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(markerTreeString)).nodes()).group(false).orient("left")
							.left(25).top(175).height(markerNumber*geneHeight).width(200);
					layout.link().add(PV.Line).lineWidth(1)
					.antialias(false)
					.event(PV.Event.CLICK, new PVEventHandler() {

						@Override
						public void onEvent(Event e, String pvEventType,
								JsArgs args) {
							
							PVLink link = args.getObject(1);
							markerDendrogramUpdate(link.sourceNode().nodeName());
						}
						
					})
					.strokeStyle(new JsFunction<PVColor>() {
						public PVColor f(JsArgs args) {
							PVLink d = args.getObject(1); // 0 is PVNode

							PVLink selectedArc = vis.getObject(selectedArcIndexProperty);
							if (selectedArc != null) {
								return (d == selectedArc) ? emphasizedArcColor
										: deemphasizedArcColor;
							}

							int selectedNodeIndex = vis.getInt(selectedNodeIndexProperty);
							if (selectedNodeIndex == -1) {
								return arcColor;
							}

							if (d.source() == selectedNodeIndex
									|| d.target() == selectedNodeIndex) {
								return emphasizedArcColor;
							}
							return deemphasizedArcColor;
						}
					})
					.event(PV.Event.MOUSEOVER, new PVEventHandler() {
						public void onEvent(Event e, String pvEventType, JsArgs args) {
							PVLink d = args.getObject(1);
							vis.set(selectedArcIndexProperty, d);
							vis.render();
						}
					}).event(PV.Event.MOUSEOUT, new PVEventHandler() {
						public void onEvent(Event e, String pvEventType, JsArgs args) {
							vis.set(selectedArcIndexProperty, null);
							vis.render();
						}
					});
				}
				
				/* Array Dendrogram*/
				if(arrayTreeString.contains("(")) {
					
					PVClusterLayout arrayTreeLayout = vis
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(arrayTreeString)).nodes()).group(false).orient("top")
							.left(225).top(25).width(arrayNumber*geneWidth).height(150);
					
					arrayTreeLayout.link().add(PV.Line).lineWidth(1)
					.antialias(false)
					.event(PV.Event.CLICK, new PVEventHandler() {

						@Override
						public void onEvent(Event e, String pvEventType,
								JsArgs args) {	
							PVLink link = args.getObject(1);
							arrayDendrogramUpdate(link.sourceNode().nodeName());
						}	
					})
					.strokeStyle(new JsFunction<PVColor>() {
						public PVColor f(JsArgs args) {
							PVLink d = args.getObject(1); // 0 is PVNode

							PVLink selectedArc = vis.getObject(selectedArcIndexProperty);
							if (selectedArc != null) {
								return (d == selectedArc) ? emphasizedArcColor
										: deemphasizedArcColor;
							}

							int selectedNodeIndex = vis.getInt(selectedNodeIndexProperty);
							if (selectedNodeIndex == -1) {
								return arcColor;
							}

							if (d.source() == selectedNodeIndex
									|| d.target() == selectedNodeIndex) {
								return emphasizedArcColor;
							}
							return deemphasizedArcColor;
						}
					})
					.event(PV.Event.MOUSEOVER, new PVEventHandler() {
						public void onEvent(Event e, String pvEventType, JsArgs args) {
							PVLink d = args.getObject(1);
							vis.set(selectedArcIndexProperty, d);
							vis.render();
						}
					}).event(PV.Event.MOUSEOUT, new PVEventHandler() {
						public void onEvent(Event e, String pvEventType, JsArgs args) {
							vis.set(selectedArcIndexProperty, null);
							vis.render();
						}
					});
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
		        getPVPanel().event(PV.Event.MOUSEWHEEL, PV.Behavior.zoom());
		        getPVPanel().render();
			}
		}, 0, 0);
        
	}
	/**
	 * Handles selecting marker subclusters 
	 * @param selected Marker Node name
	 */
	public void markerDendrogramUpdate(String string) {
		
		int selectedNodeIndex = Integer.parseInt(string);
		boolean flag = false;
		int counter = 1;
		int positionIncrement = 1;
		StringBuffer updatedString = new StringBuffer("(");
		while(!flag) {
			
			if(markerTreeString.charAt(selectedNodeIndex+positionIncrement) == '(' ) {
				updatedString.append("(");
				counter++; 
				
			}else {
				updatedString.append(")");
				counter--;
				
			}
			if(counter == 0) {
				flag = true;
			}
			positionIncrement++;
		}
		
		
		String[] newColorArray = new String[arrayNumber * countMatches(markerTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()")];
		
		for (int i = 0 ; i < arrayNumber * countMatches(markerTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()"); i++) {
			
			newColorArray[i] = colorArray[(arrayNumber * countMatches(markerTreeString.substring(0, selectedNodeIndex), "()")) + (i)];
		
		}
		  
		client.updateVariable(paintableId, "markerColor", newColorArray, false);
		client.updateVariable(paintableId, "markerNumber", countMatches(markerTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()"), false);
		client.updateVariable(paintableId, "marker", updatedString.toString(), true);
		
	}
	
	public static int countMatches(String str, String sub) {
		if (isEmpty(str) || isEmpty(sub)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++;
			idx += sub.length();
		}
		return count;
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	/**
	 * Handles selecting arraysubclusters
	 * @param Selected Array Node Name 
	 */
	public void arrayDendrogramUpdate(String string) {
		
		int selectedNodeIndex = Integer.parseInt(string);
		boolean flag = false;
		int counter = 1;
		int positionIncrement = 1;
		StringBuffer updatedString = new StringBuffer("(");
		while(!flag) {
			
			if(arrayTreeString.charAt(selectedNodeIndex+positionIncrement) == '(' ) {
				updatedString.append("(");
				counter++; 
				
			}else {
				updatedString.append(")");
				counter--;
				
			}
			if(counter == 0) {
				flag = true;
			}
			positionIncrement++;
		}
		
		String[] newColorArray = new String[markerNumber * countMatches(arrayTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()")];
		int j = 0;
		for (int i = 0 ; i < colorArray.length; i++) {
			
			if(i%markerNumber == 0){
				for(int k = 0; k<countMatches(arrayTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()"); k++) {
					
					newColorArray[j] = colorArray[i + (countMatches(arrayTreeString.substring(0, selectedNodeIndex), "()")-1) + k];
					j++;
				}
			}
		}
		VConsole.log(newColorArray.length + " ");
		client.updateVariable(paintableId, "arayColor", newColorArray, false);
		client.updateVariable(paintableId, "arrayNumber", countMatches(arrayTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()"), false);
		client.updateVariable(paintableId, "array", updatedString.toString(), true);
		
	}
	
}