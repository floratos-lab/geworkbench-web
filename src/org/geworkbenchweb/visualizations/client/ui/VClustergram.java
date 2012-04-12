package org.geworkbenchweb.visualizations.client.ui;

import org.thechiselgroup.choosel.protovis.client.PV;
import org.thechiselgroup.choosel.protovis.client.PVBar;
import org.thechiselgroup.choosel.protovis.client.PVClusterLayout;
import org.thechiselgroup.choosel.protovis.client.PVColor;
import org.thechiselgroup.choosel.protovis.client.PVDomNode;
import org.thechiselgroup.choosel.protovis.client.PVEventHandler;
import org.thechiselgroup.choosel.protovis.client.PVLabel;
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
public class VClustergram extends Composite implements Paintable {

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


	/** Marker Cluster Tree String */
	private String markerTreeString;

	/** Array Cluster Tree String */
	private String arrayTreeString;

	/** Number of arrays/phenotypes */
	private int arrayNumber;

	/** Number of markers */
	private int markerNumber;

	/** Gene Color Array */
	private String[] colorArray;

	/** Marker Labels */
	private String[] markerLabels;

	/** Array Labels */
	private String[] arrayLabels;

	/**
	 * The constructor should first call super() to initialize the component and 
	 * then handle any initialization relevant to Vaadin. 
	 */	
	public VClustergram() {

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

		/* All the variables from the server are retrieved here */
		markerTreeString 		=  	uidl.getStringVariable("markerCluster").trim();
		arrayTreeString			= 	uidl.getStringVariable("arrayCluster").trim();
		arrayNumber 			= 	uidl.getIntVariable("arrayNumber");
		markerNumber 			=	uidl.getIntVariable("markerNumber");
		colorArray 				=	uidl.getStringArrayVariable("color");
		markerLabels			= 	uidl.getStringArrayVariable("markerLabels");
		arrayLabels				= 	uidl.getStringArrayVariable("arrayLabels");
		
		/* Marker Dendrogram */
		if(markerTreeString.contains("(")) {
			panel.add(new ProtovisWidget() {
				protected void onAttach() {
					super.onAttach();

					initPVPanel();

					final String selectedNodeIndexProperty = "selectedNodeIndex";
					final String selectedArcIndexProperty = "selectedArcIndex";

					final PVColor arcColor = PV.color("rgba(0,0,0,.2)");
					final PVColor emphasizedArcColor = PV.color("red");
					final PVColor deemphasizedArcColor = PV.color("rgba(0,0,0,.2)");

					final PVPanel vis = getPVPanel().width(150).height(markerNumber*geneHeight).left(2).right(0).top(0).bottom(0)
							.def(selectedNodeIndexProperty, -1)
							.def(selectedArcIndexProperty, null);

					PVClusterLayout layout = vis
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(markerTreeString)).nodes()).group(false).orient("left");

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
					getPVPanel().render();
				}
			}, 50, 200);
		}
		
		/* Array Dendrogram*/
		if(arrayTreeString.contains("(")) {
			panel.add(new ProtovisWidget() {
				protected void onAttach() {
					super.onAttach();

					initPVPanel();

					final String selectedNodeIndexProperty = "selectedNodeIndex";
					final String selectedArcIndexProperty = "selectedArcIndex";

					final PVColor arcColor = PV.color("rgba(0,0,0,.2)");
					final PVColor emphasizedArcColor = PV.color("red");
					final PVColor deemphasizedArcColor = PV.color("rgba(0,0,0,.2)");

					final PVPanel vis = getPVPanel().width(arrayNumber*geneWidth).height(150).left(0).right(0).top(0).bottom(0)
							.def(selectedNodeIndexProperty, -1)
							.def(selectedArcIndexProperty, null);



					PVClusterLayout arrayTreeLayout = vis
							.add(PV.Layout.Cluster())
							.nodes(((PVDomNode) TreeData.data(arrayTreeString)).nodes()).group(false).orient("top");

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
					getPVPanel().render();
				}	
			}, 200, 50);
		}		
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
		}, 200, 200);


		/* Marker Labels are printed here */
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();

				initPVPanel();
				final PVPanel vis = getPVPanel().width(400).height((markerNumber*geneHeight)).left(0).right(0).top(0).bottom(0);

				int markerPosition = 7;
				for(int i=0; i<markerLabels.length; i++) {
					@SuppressWarnings("unused")
					PVLabel markerLabel = vis.add(PV.Label).font("bold 5px sans-serif").top(markerPosition).text(markerLabels[i]);
					markerPosition = markerPosition + 5;
				}
				getPVPanel().render();
			}
		}, (225+geneWidth*arrayNumber), 200);

		/* Array Labels are printed here */
		panel.add(new ProtovisWidget() {
			protected void onAttach() {
				super.onAttach();

				initPVPanel();
				final PVPanel vis = getPVPanel().width(arrayNumber*geneWidth).height(400).left(0).right(0).top(0).bottom(0);

				int arrayPosition = 5;
				for(int i=0; i<arrayLabels.length; i++) {
					@SuppressWarnings("unused")
					PVLabel markerLabel = vis.add(PV.Label).font("bold 9px sans-serif")
					.left(arrayPosition)
					.top(5)
					.textAngle(Math.PI/2)
					.text(arrayLabels[i]);

					arrayPosition = arrayPosition + 20;
				}
				getPVPanel().render();
			}
		}, 200, (225 + (geneHeight*markerNumber)));
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

		int nodesInSelectedCluster 	= 	countMatches(markerTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()");
		int nodesBeforeCluster		=  	countMatches(markerTreeString.substring(0, selectedNodeIndex), "()");
		String[] newColorArray 		= 	new String[arrayNumber * nodesInSelectedCluster];
		String[] newMarkerLabels 	= 	new String[countMatches(markerTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()")];

		for (int i = 0 ; i < arrayNumber * nodesInSelectedCluster; i++) {

			if (i == 0) {
				int n = 0;
				for(int k=0; k<nodesInSelectedCluster; k++) {	
					newMarkerLabels[n] 	=	markerLabels[nodesBeforeCluster + k]; 
					n++;
				}
			}
			newColorArray[i] = colorArray[(arrayNumber * nodesBeforeCluster) + i];

		}

		/* Variables that are to be updated are sent to the server counterpart here */
		
		client.updateVariable(paintableId, "markerLabels", newMarkerLabels, false);
		client.updateVariable(paintableId, "markerColor", newColorArray, false);
		client.updateVariable(paintableId, "markerNumber", countMatches(markerTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()"), false);
		client.updateVariable(paintableId, "marker", updatedString.toString(), true);

	}

	/**
	 * This method is used to find the number of patterns in a given string.
	 * @param String and Pattern
	 * @return count 
	 */
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

		int nodesInSelectedCluster 	= 	countMatches(arrayTreeString.substring(selectedNodeIndex, selectedNodeIndex + positionIncrement), "()");
		int nodesBeforeCluster 		= 	countMatches(arrayTreeString.substring(0, selectedNodeIndex), "()");
		String[] newColorArray 		= 	new String[markerNumber * nodesInSelectedCluster];
		String[] newArrayLabels		= 	new String[nodesInSelectedCluster];

		int j = 0;
		int m = 0;
		try {
			for (int i = 0 ; i < colorArray.length; i++) {

				if(i%arrayNumber == 0){

					if(i == 0) {
						for(int k = 0; k<nodesInSelectedCluster; k++) {
							newArrayLabels[m] = arrayLabels[nodesBeforeCluster + k];
							m++;
						}
					}
					for(int k = 0; k<nodesInSelectedCluster; k++) {
						newColorArray[j] = colorArray[i + nodesBeforeCluster + k];
						j++;
					}
				}
			}
		} catch (Exception e) {
			VConsole.log(e);
		}
		
		/* Variables that are to be updated are sent to the server counterpart here */
		
		client.updateVariable(paintableId, "arrayLabels", newArrayLabels, false);
		client.updateVariable(paintableId, "arrayColor", newColorArray, false);
		client.updateVariable(paintableId, "arrayNumber", nodesInSelectedCluster, false);
		client.updateVariable(paintableId, "array", updatedString.toString(), true);

	}

}