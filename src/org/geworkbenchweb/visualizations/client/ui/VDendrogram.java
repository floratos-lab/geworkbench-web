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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side code for Dendrogram widget.
 */
public class VDendrogram extends Composite implements Paintable {

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private AbsolutePanel panel = new AbsolutePanel();
	
	private Canvas canvas = Canvas.createIfSupported();
	private Canvas arrayDendrogramCanvas = Canvas.createIfSupported();
	private Canvas markerDendrogramCanvas = Canvas.createIfSupported();
	private Canvas arrayLabelCanvas = Canvas.createIfSupported();
	private Canvas markerLabelCanvas = Canvas.createIfSupported();

	private final Canvas arrowCanvas;
	
	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VDendrogram() {   
		
		initWidget(panel);
		panel.add(canvas);
		panel.add(arrayDendrogramCanvas);
		panel.add(markerDendrogramCanvas);
		panel.add(arrayLabelCanvas);
		panel.add(markerLabelCanvas);

		arrowCanvas = createRetrievingButton(handler);
		panel.add(arrowCanvas);
		
		/** Set the CSS class name to allow styling. */
//		setStyleName(CLASSNAME);
	}
	
	private int firstMarker = 0;
	
    private ClickHandler handler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			firstMarker += 100;
			client.updateVariable(paintableId, "firstMarker", firstMarker, true);
			Window.alert("not implemented. new firstMarker is "+firstMarker);
		}
    	
    };
    
    final private int MAX_WIDTH = 3000;
    final private int MAX_HEIGHT = 2000;

    /**
     * Called whenever an update is received from the server 
     */
	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		this.client = client;
		paintableId = uidl.getId();

		if (canvas == null) {
            // "Sorry, your browser doesn't support the HTML5 Canvas element";
            return;
		}

		// the difference between Attribute and Variable: in general, if not to be changed here (in client), use attribute
		// see https://vaadin.com/forum/-/message_boards/view_message/192733
		int arrayNumber = uidl.getIntAttribute("arrayNumber");
		int markerNumber = uidl.getIntAttribute("markerNumber");
		String arrayCluster = uidl.getStringAttribute("arrayCluster");
		String markerCluster = uidl.getStringAttribute("markerCluster");
		int[] colors = uidl.getIntArrayAttribute("colors"); // truncate the data sent if it is not needed due to the maximum display size
		String[] arrayLabels = uidl.getStringArrayAttribute("arrayLabels");
		String[] markerLabels = uidl.getStringArrayAttribute("markerLabels");
		
		int cellWidth = uidl.getIntAttribute("cellWidth");
		int cellHeight = uidl.getIntAttribute("cellHeight");
		

		// [1] array labels on the bottom
		int canvasWidth = cellWidth*arrayNumber;
		arrayLabelCanvas.setCoordinateSpaceWidth(canvasWidth);
		//canvas3.setCoordinateSpaceHeight(height);
		Context2d context3 = arrayLabelCanvas.getContext2d();
		context3.rotate(0.5*Math.PI);
		context3.translate(0, -canvasWidth);
		if(cellWidth<10) {
			context3.setFont((cellWidth-1)+"px sans-serif");
		}
		int arrayLabelHeight = drawLabels(arrayLabelCanvas, arrayLabels, cellWidth);
        
		// [2] canvas for microarray dendrogram
		arrayDendrogramCanvas.setCoordinateSpaceWidth(canvasWidth);
		Context2d arrayDendrogramContext = arrayDendrogramCanvas.getContext2d();
		
		index = 0; // this must be reset to start reading the cluster string
		List<Double> bracketCoordinates = new ArrayList<Double>();
		final char[] clusters = arrayCluster.toCharArray();
		MidPoint midPoint = prepareBrackets(0, clusters.length-1, clusters, bracketCoordinates, cellWidth);

		int arrayClusterHeight = (int)midPoint.height + deltaH; // add some extra space on top
		arrayDendrogramCanvas.setCoordinateSpaceHeight(arrayClusterHeight);
		arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down

		drawBrackets(arrayDendrogramContext, bracketCoordinates);

		// [3] heatmap
        int countPaintableMarkers = Math.min(markerNumber, (MAX_HEIGHT-arrayLabelHeight-arrayClusterHeight)/cellHeight);
		if(countPaintableMarkers<markerNumber) {
			arrowCanvas.setVisible(true);
			Style s = arrowCanvas.getCanvasElement().getStyle();
			s.setTop(countPaintableMarkers*cellHeight+arrayClusterHeight-22, Unit.PX);
		}
        drawHeatmap(canvas, colors, countPaintableMarkers, arrayNumber, cellHeight, cellWidth);
        int canvasHeight = countPaintableMarkers*cellHeight;

		// [4] canvas for marker dendrogram
		int markerClusterHeight = 0;
		markerDendrogramCanvas.setCoordinateSpaceHeight(canvasHeight); // note this is heatmap canvas height
		Context2d markerDendrogramContext = markerDendrogramCanvas.getContext2d();
		
		index = 0; // this must be reset to start reading the cluster string
		List<Double> bracketCoordinates2 = new ArrayList<Double>();
		final char[] clusters2 = markerCluster.toCharArray();
		MidPoint midPoint2 = prepareBrackets(0, clusters2.length-1, clusters2, bracketCoordinates2, cellHeight); 

		markerClusterHeight = (int)midPoint2.height + deltaH; // add some extra space to the left
		markerDendrogramCanvas.setCoordinateSpaceWidth(markerClusterHeight);
		// rotate and move it to the left hand side area
		markerDendrogramContext.rotate(0.5*Math.PI);
		markerDendrogramContext.translate(0, -markerClusterHeight);

		drawBrackets(markerDendrogramContext, bracketCoordinates2);

		// [5] markers labels on the right
		markerLabelCanvas.setCoordinateSpaceHeight(canvasHeight);
		Context2d context4 = markerLabelCanvas.getContext2d();
		if(cellHeight<10) {
			context4.setFont((cellHeight-1)+"px sans-serif");
		}
		int markerLabelWidth = drawLabels(markerLabelCanvas, markerLabels, cellHeight);
		
		// place things in place
		CanvasElement arrayDendrogram = arrayDendrogramCanvas.getCanvasElement();
		Style style = arrayDendrogram.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(0, Unit.PX);
		style.setLeft(markerClusterHeight, Unit.PX);
		
		CanvasElement markerDendrogram = markerDendrogramCanvas.getCanvasElement();
		style = markerDendrogram.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(arrayClusterHeight, Unit.PX);
		style.setLeft(0, Unit.PX);

		CanvasElement heatmap = canvas.getCanvasElement();
		style = heatmap.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(arrayClusterHeight, Unit.PX);
		style.setLeft(markerClusterHeight, Unit.PX);
		
		CanvasElement element3 = arrayLabelCanvas.getCanvasElement();
		style = element3.getStyle();
		style.setTop(arrayClusterHeight+canvasHeight, Unit.PX);
		style.setLeft(markerClusterHeight, Unit.PX);

		CanvasElement element4 = markerLabelCanvas.getCanvasElement();
		style = element4.getStyle();
		style.setTop(arrayClusterHeight, Unit.PX);
		style.setLeft(markerClusterHeight+canvasWidth, Unit.PX);
		
		// calculate the proper panel size
		int width0 = arrayNumber*cellWidth + markerClusterHeight +  markerLabelWidth;
		int height0 = markerNumber*cellHeight + arrayClusterHeight +  arrayLabelHeight;
		panel.setWidth(Math.min(width0, MAX_WIDTH) + "px");
		panel.setHeight(Math.min(height0, MAX_HEIGHT) + "px");

	}
	
	// colors[] must be of the size of row*column
	private static void drawHeatmap(final Canvas heatmapCanvas, int[] colors, int row, int column, int cellHeight, int cellWidth) {
		int height = cellHeight*row;
		int width = cellWidth*column;
		
//		heatmapCanvas.setWidth(width + "px");
//		heatmapCanvas.setHeight(height + "px"); // necessary?
		heatmapCanvas.setCoordinateSpaceWidth(width);
		heatmapCanvas.setCoordinateSpaceHeight(height);
        
		Context2d context = heatmapCanvas.getContext2d();

		int valueIndex = 0;
		
		for (int y = 0; y < height; y+=cellHeight) {

			for (int x = 0; x < width; x+=cellWidth) {

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
	}
	
	private static int drawLabels(final Canvas canvas, String[] labels, int interval) {
		Context2d context = canvas.getContext2d();
		String longestArrayName = "";
		int y = interval;
		for(int i=0; i<labels.length; i++) {
			context.fillText(labels[i], 5, y);
			y += interval;
			if(labels[i].length()>longestArrayName.length()) {
				longestArrayName = labels[i]; 
			}
		}
		int height = (int)(context.measureText(longestArrayName).getWidth() + 10);
		CanvasElement element3 = canvas.getCanvasElement();
		Style style = element3.getStyle();
		style.setPosition(Position.ABSOLUTE);
		return height;
	}
	
	private static void drawBrackets(final Context2d context, List<Double> bracketCoordinates) {
		context.beginPath();
		for(int i=0; i<bracketCoordinates.size(); i+=5) {
			double x1 = bracketCoordinates.get(i);
			double x2 = bracketCoordinates.get(i+1);
			double y1 = bracketCoordinates.get(i+2);
			double y = bracketCoordinates.get(i+3);
			double y2 = bracketCoordinates.get(i+4);
			context.moveTo(x1, y1);
			context.lineTo(x1, y);
			context.lineTo(x2, y);
			context.lineTo(x2, y2);
		}
		context.stroke();
	}
	
	transient static private int index;
	
	private static class MidPoint {
		final double mid;
		final double height;
		MidPoint(double mid, double height) { this.mid = mid; this.height = height;}
	}
	
	private static int deltaH = 5; // the increment of the dendrogram height
	
	/**
	 * Prepare the collection of three points coordinates to draw the brackets,
	 * and calculate the height so the container know the size of the resulted dendrogram.
	 * 
	 * precondition: clusters[left]=='(', clusters[right]=')'
	 */
	static private MidPoint prepareBrackets(int left, int right, final char[] clusters, final List<Double> coordinates, 
			int deltaX) { // side-way width
		if(right-left<=1) { // 1: leaf node; -1: empty cluster
			MidPoint m = new MidPoint((index+0.5)*deltaX, 0);
			index++;
			return m;
		}

		// the general case that includes child clusters, and they must be two.
		int split = split(left + 1, right -1, clusters);

		// by now, [0, index) is the left child; [index, length-1] is the right child
		MidPoint leftMidPoint = prepareBrackets(left+1, split-1, clusters, coordinates, deltaX);
		MidPoint rightMidPoint = prepareBrackets(split, right-1, clusters, coordinates, deltaX);
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

	static private Canvas createRetrievingButton(final ClickHandler handler) {
		Canvas canvas = Canvas.createIfSupported();
		if(canvas==null) return null; // canvas not supported
		 
		canvas.setWidth("500px");
		canvas.setHeight("20px");
		canvas.setCoordinateSpaceWidth(500);
		canvas.setCoordinateSpaceHeight(20);
		Context2d context = canvas.getContext2d();
		context.setFillStyle(CssColor.make(225, 255, 225)); // light green
		context.fillRect(0, 0, 500, 20);
		context.strokeRect(0, 0, 500, 20);
		context.beginPath();
		context.moveTo(100, 2);
		context.lineTo(110, 18);
		context.lineTo(120, 2);
		context.setFillStyle(CssColor.make(0, 100, 50)); // dark green
		context.fill();
		context.fillText("Click to scroll down 100 more rows.", 150, 15);
		canvas.setVisible(false);
		canvas.addClickHandler(handler);
		Style s = canvas.getCanvasElement().getStyle();
		s.setPosition(Position.ABSOLUTE);
		s.setLeft(100,  Unit.PX);
		s.setZIndex(1);
		
		return canvas;
	}
}
