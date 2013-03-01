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

	private final Canvas downArrowCanvas, upArrowCanvas;
	
	public VDendrogram() {   
		
		initWidget(panel);
		panel.add(canvas);
		panel.add(arrayDendrogramCanvas);
		panel.add(markerDendrogramCanvas);
		panel.add(arrayLabelCanvas);
		panel.add(markerLabelCanvas);

		downArrowCanvas = createRetrievingButton(true);
		upArrowCanvas = createRetrievingButton(false);
		panel.add(downArrowCanvas);
		panel.add(upArrowCanvas); 
	}
	
	private int firstMarker = 0;
	// the following variables are necessary to be member variables only to handle the last 'page'
	private int markerNumber = 0; 
	private int paintableMarkers = 0;
	
    private class ArrowHandler implements ClickHandler {

    	final private boolean down;
    	
    	ArrowHandler(boolean down) {
    		this.down = down;
    	}
    	
		@Override
		public void onClick(ClickEvent event) {
			if(firstMarker+paintableMarkers>markerNumber) { // last 'page'
				return;
			}
			if(down) {
				firstMarker += 100;
			} else {
				firstMarker -= 100;
			}
			client.updateVariable(paintableId, "paintableMarkers", paintableMarkers, false);
			client.updateVariable(paintableId, "firstMarker", firstMarker, true);
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
		markerNumber = uidl.getIntAttribute("markerNumber");
		String arrayCluster = uidl.getStringAttribute("arrayCluster");
		String markerCluster = uidl.getStringAttribute("markerCluster");
		int[] colors = uidl.getIntArrayAttribute("colors"); // this could be partial data if firstMarker>0
		String[] arrayLabels = uidl.getStringArrayAttribute("arrayLabels");
		String[] markerLabels = uidl.getStringArrayAttribute("markerLabels");
		
		int cellWidth = uidl.getIntAttribute("cellWidth");
		int cellHeight = uidl.getIntAttribute("cellHeight");
		
		firstMarker = uidl.getIntVariable("firstMarker");
		
		// [1] array labels on the bottom
		int heatmapWidth = cellWidth*arrayNumber;
		sizeCanvas(arrayLabelCanvas, heatmapWidth, 150); // default height
		Context2d context3 = arrayLabelCanvas.getContext2d();
		context3.rotate(0.5*Math.PI);
		context3.translate(0, -heatmapWidth);
		if(cellWidth<10) {
			context3.setFont((cellWidth-1)+"px sans-serif");
		}
		int arrayLabelHeight = drawLabels(arrayLabelCanvas, arrayLabels, cellWidth, 0);
        
		// [2] canvas for microarray dendrogram
		index = 0; // this must be reset to start reading the cluster string
		List<Double> bracketCoordinates = new ArrayList<Double>();
		final char[] clusters = arrayCluster.toCharArray();
		MidPoint midPoint = prepareBrackets(0, clusters.length-1, clusters, bracketCoordinates, cellWidth);

		int arrayClusterHeight = (int)midPoint.height + deltaH; // add some extra space on top
		sizeCanvas(arrayDendrogramCanvas, heatmapWidth, arrayClusterHeight);

		Context2d arrayDendrogramContext = arrayDendrogramCanvas.getContext2d();
		arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down
		drawBrackets(arrayDendrogramContext, bracketCoordinates);

		// [3] heatmap
		paintableMarkers = Math.min(markerNumber, (MAX_HEIGHT-arrayLabelHeight-arrayClusterHeight)/cellHeight);
        int heatmapHeight = paintableMarkers*cellHeight;
        updateArrowCanvas(heatmapHeight, arrayClusterHeight);
        drawHeatmap(canvas, colors, paintableMarkers, arrayNumber, cellHeight, cellWidth);

		// [4] canvas for marker dendrogram
		index = 0; // this must be reset to start reading the cluster string
		List<Double> bracketCoordinates2 = new ArrayList<Double>();
		final char[] clusters2 = markerCluster.toCharArray();
		MidPoint midPoint2 = prepareBrackets(0, clusters2.length-1, clusters2, bracketCoordinates2, cellHeight); 

		int markerClusterHeight = (int)midPoint2.height + deltaH; // add some extra space to the left
		sizeCanvas(markerDendrogramCanvas, markerClusterHeight, heatmapHeight);

		// rotate and move it to the left hand side area
		Context2d markerDendrogramContext = markerDendrogramCanvas.getContext2d();
		markerDendrogramContext.rotate(0.5*Math.PI);
		markerDendrogramContext.translate(0, -markerClusterHeight);
		drawBrackets(markerDendrogramContext, bracketCoordinates2);

		// [5] markers labels on the right
		sizeCanvas(markerLabelCanvas, 300, heatmapHeight); // default width
		Context2d context4 = markerLabelCanvas.getContext2d();
		if(cellHeight<10) {
			context4.setFont((cellHeight-1)+"px sans-serif");
		}
		int markerLabelWidth = drawLabels(markerLabelCanvas, markerLabels, cellHeight, firstMarker);
		
		// place things in place
		positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight);
		positionCanvas(markerDendrogramCanvas, arrayClusterHeight, 0);
		positionCanvas(canvas, arrayClusterHeight, markerClusterHeight);
		positionCanvas(arrayLabelCanvas, arrayClusterHeight+heatmapHeight, markerClusterHeight);
		positionCanvas(markerLabelCanvas, arrayClusterHeight, markerClusterHeight+heatmapWidth);
		
		// calculate the proper panel size
		int width0 = heatmapWidth + markerClusterHeight +  markerLabelWidth;
		int height0 = heatmapHeight + arrayClusterHeight +  arrayLabelHeight;
		panel.setWidth(Math.min(width0, MAX_WIDTH) + "px");
		panel.setHeight(Math.min(height0, MAX_HEIGHT) + "px");
	}
	
	private void updateArrowCanvas(final int heatmapHeight,
			final int arrayClusterHeight) {
		if (firstMarker + paintableMarkers < markerNumber) {
			Context2d context = downArrowCanvas.getContext2d();
			context.setFillStyle(CssColor.make(225, 255, 225)); // light green
			context.fillRect(150, 2, 340, 16);
			context.setFillStyle(CssColor.make(0, 0, 0));
			context.fillText("Displayed to row #"
					+ (firstMarker + paintableMarkers)
					+ ". Click to scroll down.", 150, 15);
			downArrowCanvas.setVisible(true);
			positionCanvas(downArrowCanvas, heatmapHeight + arrayClusterHeight - 22, 100);
		} else {
			downArrowCanvas.setVisible(false);
		}
		
		if (firstMarker > 0) {
			Context2d context = upArrowCanvas.getContext2d();
			context.setFillStyle(CssColor.make(225, 255, 225)); // light green
			context.fillRect(150, 2, 340, 16);
			context.setFillStyle(CssColor.make(0, 0, 0));
			context.fillText("Displayed to row #" + firstMarker
					+ ". Click to scroll up.", 150, 15);
			upArrowCanvas.setVisible(true);
			positionCanvas(upArrowCanvas, arrayClusterHeight + 2, 100);
		} else {
			upArrowCanvas.setVisible(false);
		}
	}
	
	static private void sizeCanvas(Canvas canvas, int width, int height) {
		canvas.setPixelSize(width, height); // when necessary?
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
	}
	
	static private void positionCanvas(Canvas canvas, int top, int left) {
		CanvasElement element = canvas.getCanvasElement();
		Style style = element.getStyle();
		style.setPosition(Position.ABSOLUTE);
		style.setTop(top, Unit.PX);
		style.setLeft(left, Unit.PX);
	}
	
	// colors[] must be of the size of row*column, where paintable<row. (The logic is still ok if paintable==row)
	private static void drawHeatmap(final Canvas heatmapCanvas, int[] colors, int paintable, int column, int cellHeight, int cellWidth) {
		int height = cellHeight*paintable;
		int width = cellWidth*column;
		
		sizeCanvas(heatmapCanvas, width, height);
        
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

				context.setFillStyle(CssColor.make("rgb(" + r + ", " + g + ","
						+ b + ")"));
				context.fillRect(x, y, cellWidth, cellHeight);
			}
		}
	}
	
	private static int drawLabels(final Canvas canvas, String[] labels, int interval, int first) {
		Context2d context = canvas.getContext2d();
		String longestArrayName = "";
		int y = interval;
		for(int i=first; i<labels.length; i++) {
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

	private Canvas createRetrievingButton(boolean downDirection) {
		Canvas canvas = Canvas.createIfSupported();
		if(canvas==null) return null; // canvas not supported
		 
		sizeCanvas(canvas, 500, 20);
		Context2d context = canvas.getContext2d();
		context.setFillStyle(CssColor.make(225, 255, 225)); // light green
		context.fillRect(0, 0, 500, 20);
		context.strokeRect(0, 0, 500, 20);
		context.beginPath();
		if(downDirection) { // down triangle
			context.moveTo(100, 2);
			context.lineTo(110, 18);
			context.lineTo(120, 2);
		} else { // up triangle
			context.moveTo(100, 18);
			context.lineTo(110, 2);
			context.lineTo(120, 18);
		}
		context.setFillStyle(CssColor.make(0, 100, 50)); // dark green
		context.fill();
		canvas.setVisible(false);
		canvas.addClickHandler(new ArrowHandler(downDirection));
		Style s = canvas.getCanvasElement().getStyle();
		s.setPosition(Position.ABSOLUTE);
		s.setLeft(100,  Unit.PX);
		s.setZIndex(1);
		
		return canvas;
	}
}
