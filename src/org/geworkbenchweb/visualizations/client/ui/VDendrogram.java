package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
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
		int[] colors = uidl.getIntArrayAttribute("colors");

		int canvasWidth = cellWidth*arrayNumber; // TODO this should be decided by the available space from container, not the entire heatmap
//		canvas.setWidth(canvasWidth + "px");
        canvas.setCoordinateSpaceWidth(canvasWidth);
         
        final int CANVAS_HEIGHT_LIMIT = 8000; // canvas has browser specific size limit
        int canvasHeight = Math.min(cellHeight*markerNumber, CANVAS_HEIGHT_LIMIT);
//		canvas.setHeight(canvasHeight + "px");     
        canvas.setCoordinateSpaceHeight(canvasHeight);
		
		Context2d context = canvas.getContext2d();

		int index = 0;
		
		for (int y = 0; y < canvasHeight; y+=cellHeight) {

			for (int x = 0; x < cellWidth*arrayNumber; x+=cellWidth) {

				int color = colors[index++];
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
		int arrayClusterHeight = 400; // TODO strategy to be decided
		arrayDendrogramCanvas.setCoordinateSpaceHeight(arrayClusterHeight); //(int)midPoint.height);
		arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down
//		arrayDendrogramContext.setStrokeStyle(CssColor.make(0,128,0));

		final char[] clusters = arrayCluster.toCharArray();
		String v = verifyClusterString(0, clusters.length-1, clusters, 0);
		if(v!=null) {
			arrayDendrogramContext.setFont("20px");
			arrayDendrogramContext.fillText("ill-formed cluster string:"+v, 10, 30);
		}
		arrayDendrogramContext.beginPath();
		index = 0; // this must be reset to start reading the cluster string
		MidPoint midPoint = drawBracket(0, clusters.length-1, clusters, arrayDendrogramContext); // ignore the top level return value?
//		arrayDendrogramCanvas.setCoordinateSpaceHeight(arrayClusterHeight); //(int)midPoint.height);
//		arrayDendrogramContext.transform(1, 0, 0, -1, 0, midPoint.height); // flip upside down
		arrayDendrogramContext.stroke();

		// <div><canvas id=array_dendrogram></canvas><canvas id=array_heatmap></canvas><canvas id=array_labels></canvas></div>
		this.getElement().appendChild(arrayDendrogramCanvas.getCanvasElement());
		this.getElement().appendChild(canvas.getCanvasElement());
		
		Canvas canvas3 = Canvas.createIfSupported();
		Context2d context3 = canvas3.getContext2d();
		context3.setFont("20px");
		context3.fillText("... microarray labels go here ...", 10, 30); // TODO
		this.getElement().appendChild(canvas3.getCanvasElement());
	}
	
	transient private int index;
	
	private static class MidPoint {
		final double mid;
		final double height;
		MidPoint(double mid, double height) { this.mid = mid; this.height = height;}
	}
	
	private static double deltaH = 5; // the increment of the dendrogram height
	
	// FIXME temporary
	static double x0 = 0;
	static double deltaX = 10;
	
	/* the first and last characters of String cluster must be "(" and ")", respectively. */
	/* just to be efficient, we assume it is always well formed. if necessary we can use separate step to verify this */ 
	private MidPoint drawBracket(int leftMid, int rightMid, final char[] clusters, final Context2d context) {
		if(rightMid-leftMid==1) { // it never should be shorter
			MidPoint m = new MidPoint(x0+(index+0.5)*deltaX, 0);
			index++;
			return m;
		}
		
		// the general case that includes child clusters, and they must be two.
		int split = split(leftMid + 1, rightMid -1, clusters);

		// by now, [0, index) is the left child; [index, length-1] is the right child
		MidPoint left = drawBracket(leftMid+1, split-1, clusters, context);
		MidPoint right = drawBracket(split, rightMid-1, clusters, context);
		double height = Math.max(left.height, right.height) + deltaH;
		double x1 = 0.5+(int)left.mid; // trick to create crisp line if width 1
		double x2 = 0.5+(int)right.mid;
		double y = 0.5+(int)height;
		context.moveTo(x1, left.height);
		context.lineTo(x1, y);
		context.lineTo(x2, y);
		context.lineTo(x2, right.height);
	
		return new MidPoint(0.5*(left.mid+right.mid), height);
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
	
	/* This is mainly useful for debug purpose. return null if the string is well-formed 'cluster string'; otherwise a error message. */
	static private String verifyClusterString(int begin, int end, char[] clusters, int c) {
		if(clusters[begin]!='(') return "first letter in ("+begin+","+end+") is "+clusters[begin];
		if(clusters[end]!=')') return "last letter  in ("+begin+","+end+") is "+clusters[end];
		if(begin+1==end) return null;
		int split = split(begin+1, end-1, clusters);
		String s = verifyClusterString(begin+1, split-1, clusters, c+1);
		if(s!=null) return c+" "+s;
		s = verifyClusterString(split, end-1, clusters, c+1);
		if(s!=null) return c+" "+s;
		else return null;
	}

}