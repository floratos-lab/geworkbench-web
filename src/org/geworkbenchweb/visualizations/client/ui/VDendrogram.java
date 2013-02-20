package org.geworkbenchweb.visualizations.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side code for Dendrogram widget.
 */
public class VDendrogram extends Widget implements Paintable {

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	/** width and height of each cell in the heat map */
	private int cellWidth = 10, cellHeight = 5;
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VDendrogram() {   
		
		setElement(DOM.createDiv());

		/** Set the CSS class name to allow styling. */
//		setStyleName(CLASSNAME);
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

		//xxx = uidl.getStringArrayVariable("some_variable_name");
		//yyy = uidl.getStringVariable("another_variable_name"); 
		
		// use xxx and yyy for updating(?) something

		//client.updateVariable(paintableId, "variablName", "newValue", isImmediate);
		
		Canvas canvas = Canvas.createIfSupported();
		
		if (canvas == null) {
            // "Sorry, your browser doesn't support the HTML5 Canvas element";
            return;
		}

		// TODO what is the difference between ...Attribute and ... Variable
		int arrayNumber = uidl.getIntAttribute("arrayNumber");
		int markerNumber = uidl.getIntAttribute("markerNumber");
		String arrayCluster = uidl.getStringAttribute("arrayCluster");
		String markerCluster = uidl.getStringAttribute("markerCluster");
		int[] colors = uidl.getIntArrayAttribute("colors");

		int canvasWidth = cellWidth*arrayNumber; // TODO this should be decided by the available space from container, not the entire heatmap
//		canvas.setWidth(canvasWidth + "px");
        canvas.setCoordinateSpaceWidth(canvasWidth);
         
        final int CANVAS_HEIGHT_LIMIT = 8000; // canvas has browser specific size limit
        int canvasHeight = Math.min(cellHeight*markerNumber, CANVAS_HEIGHT_LIMIT);
//		canvas.setHeight(canvasHeight + "px");     
        canvas.setCoordinateSpaceHeight(canvasHeight);
		
		Context2d context = canvas.getContext2d();

		int valueIndex = 0;
		
		for (int y = 0; y < canvasHeight; y+=cellHeight) {

			for (int x = 0; x < cellWidth*arrayNumber; x+=cellWidth) {

				int color = colors[valueIndex++];
				int r, g, b;
				if(color>0) {
					r = 255;
					g = 255 - color;
					b = 255 - color;
				} else {
					r = 255 + color;
					g = 255 + color;
					b = 255;
				}

				CssColor cellColor = CssColor.make("rgb(" + r + ", " + g
						+ "," + b + ")");

				context.setFillStyle(cellColor);
				context.fillRect(x, y, cellWidth, cellHeight);
			}
		}

		// canvas for microarray dendrogram
		Canvas arrayDendrogramCanvas = Canvas.createIfSupported();
		arrayDendrogramCanvas.setCoordinateSpaceWidth(canvasWidth);
		Context2d arrayDendrogramContext = arrayDendrogramCanvas.getContext2d();
		
		index = 0; // this must be reset to start reading the cluster string
		List<Double> bracketCoordinates = new ArrayList<Double>();
		final char[] clusters = arrayCluster.toCharArray();
		MidPoint midPoint = prepareBrackets(0, clusters.length-1, clusters, bracketCoordinates); // ignore the top level return value?

		final int EXTA_SPACE = 5;
		int arrayClusterHeight = (int)midPoint.height + EXTA_SPACE;
		arrayDendrogramCanvas.setCoordinateSpaceHeight(arrayClusterHeight);
		arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down

		// draw brackets
		arrayDendrogramContext.beginPath();
		for(int i=0; i<bracketCoordinates.size(); i+=5) {
			double x1 = bracketCoordinates.get(i);
			double x2 = bracketCoordinates.get(i+1);
			double y1 = bracketCoordinates.get(i+2);
			double y = bracketCoordinates.get(i+3);
			double y2 = bracketCoordinates.get(i+4);
			arrayDendrogramContext.moveTo(x1, y1);
			arrayDendrogramContext.lineTo(x1, y);
			arrayDendrogramContext.lineTo(x2, y);
			arrayDendrogramContext.lineTo(x2, y2);
		}
		arrayDendrogramContext.stroke();

		// canvas for marker dendrogram
		Canvas markerDendrogramCanvas = Canvas.createIfSupported();
		int markerClusterHeight = 0;
		if(markerCluster.length()>0) {
		markerDendrogramCanvas.setCoordinateSpaceHeight(canvasHeight); // note this is heatmap canvas height
		Context2d markerDendrogramContext = markerDendrogramCanvas.getContext2d();
		
		index = 0; // this must be reset to start reading the cluster string
		List<Double> bracketCoordinates2 = new ArrayList<Double>();
		final char[] clusters2 = markerCluster.toCharArray();
		MidPoint midPoint2 = prepareBrackets(0, clusters2.length-1, clusters2, bracketCoordinates2); // ignore the top level return value?

		markerClusterHeight = (int)midPoint2.height + EXTA_SPACE;
		markerDendrogramCanvas.setCoordinateSpaceWidth(markerClusterHeight);
		// rotate and move it to the left hand side area
		markerDendrogramContext.rotate(0.5*Math.PI);
		markerDendrogramContext.translate(0, -markerClusterHeight);

		// draw brackets
		markerDendrogramContext.beginPath();
		for(int i=0; i<bracketCoordinates2.size(); i+=5) {
			double x1 = bracketCoordinates2.get(i);
			double x2 = bracketCoordinates2.get(i+1);
			double y1 = bracketCoordinates2.get(i+2);
			double y = bracketCoordinates2.get(i+3);
			double y2 = bracketCoordinates2.get(i+4);
			markerDendrogramContext.moveTo(x1, y1);
			markerDendrogramContext.lineTo(x1, y);
			markerDendrogramContext.lineTo(x2, y);
			markerDendrogramContext.lineTo(x2, y2);
		}
		markerDendrogramContext.stroke();
		}

		// <div><canvas id=array_dendrogram></canvas><canvas id=array_heatmap></canvas><canvas id=array_labels></canvas></div>
		// place things in place
		CanvasElement arrayDendrogram = arrayDendrogramCanvas.getCanvasElement();
		Style style = arrayDendrogram.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(0, Unit.PX);
		style.setLeft(markerClusterHeight, Unit.PX);
		this.getElement().appendChild(arrayDendrogram);
		
		CanvasElement markerDendrogram = markerDendrogramCanvas.getCanvasElement();
		style = markerDendrogram.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(arrayClusterHeight, Unit.PX);
		style.setLeft(0, Unit.PX);
		this.getElement().appendChild(markerDendrogram);

		CanvasElement heatmap = canvas.getCanvasElement();
		style = heatmap.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(arrayClusterHeight, Unit.PX);
		style.setLeft(markerClusterHeight, Unit.PX);
		this.getElement().appendChild(heatmap);
		
		// array labels on the bottom
		Canvas canvas3 = Canvas.createIfSupported();
		Context2d context3 = canvas3.getContext2d();
		context3.rotate(0.5*Math.PI);
		context3.translate(0, -canvasWidth);
		//context3.setFont("20px sans-serif");
		context3.fillText("... microarray labels go here ..."+context3.getFont(), 10, 30); // TODO
		CanvasElement arrayLabels = canvas3.getCanvasElement();
		style = arrayLabels.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(arrayClusterHeight+canvasHeight, Unit.PX);
		style.setLeft(markerClusterHeight, Unit.PX);
		this.getElement().appendChild(arrayLabels);
		
		// markers labels on the right
		Canvas canvas4 = Canvas.createIfSupported();
		Context2d context4 = canvas4.getContext2d();
		//context4.setFont("20px sans-serif");
		context4.fillText("... marker labels go here ..."+context4.getFont(), 10, 30); // TODO
		CanvasElement markerLabels = canvas4.getCanvasElement();
		style = markerLabels.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(arrayClusterHeight, Unit.PX);
		style.setLeft(markerClusterHeight+canvasWidth, Unit.PX);
		this.getElement().appendChild(markerLabels);
	}
	
	transient static private int index;
	
	private static class MidPoint {
		final double mid;
		final double height;
		MidPoint(double mid, double height) { this.mid = mid; this.height = height;}
	}
	
	private static double deltaH = 5; // the increment of the dendrogram height
	
	// FIXME temporary
	static double x0 = 0;
	static double deltaX = 10;
	
	/**
	 * Prepare the collection of three points coordinates to draw the brackets,
	 * and calculate the height so the container know the size of the resulted dendrogram.
	 * 
	 * precondition: clusters[left]=='(', clusters[right]=')'
	 */
	static private MidPoint prepareBrackets(int left, int right, final char[] clusters, final List<Double> coordinates) {
		if(right-left==1) { 
			MidPoint m = new MidPoint(x0+(index+0.5)*deltaX, 0);
			index++;
			return m;
		}

		// the general case that includes child clusters, and they must be two.
		int split = split(left + 1, right -1, clusters);

		// by now, [0, index) is the left child; [index, length-1] is the right child
		MidPoint leftMidPoint = prepareBrackets(left+1, split-1, clusters, coordinates);
		MidPoint rightMidPoint = prepareBrackets(split, right-1, clusters, coordinates);
		double height = Math.max(leftMidPoint.height, rightMidPoint.height) + deltaH;
		double x1 = 0.5+(int)leftMidPoint.mid; // trick to create crisp line if width 1
		double x2 = 0.5+(int)rightMidPoint.mid;
		double y = 0.5+(int)height;
		// five coordinate values needed to draw a bracket
		coordinates.add(x1);
		coordinates.add(x2);
		coordinates.add(leftMidPoint.height);
		coordinates.add(y);
		coordinates.add(rightMidPoint.height);
	
		return new MidPoint(0.5*(leftMidPoint.mid+rightMidPoint.mid), height);
	}
	
	// assume [begin, end] contains two nodes, returns the starting position of the second one
	static private int split(int begin, int end, char[] clusters) {
		int split = begin;
		int count = 0;
		do {
			if(clusters[split]=='(') {
				count++;
			} else { // this must be ')'. 
				count--;
			}
			split++;
		} while (count>0 || split==end);
		return split;
	}

}