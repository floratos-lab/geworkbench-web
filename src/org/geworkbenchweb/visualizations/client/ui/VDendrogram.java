package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
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
public final class VDendrogram extends Composite implements Paintable {

	/* two variables to communicate back to the server side*/
	/** The client side widget identifier */
	private String paintableId;

	/** Reference to the server connection object. */
	private ApplicationConnection client;

	/* GUI elements */
	private final AbsolutePanel panel = new AbsolutePanel();
	
	private final Canvas canvas = Canvas.createIfSupported();
	private final Canvas arrayDendrogramCanvas = Canvas.createIfSupported();
	private final Canvas markerDendrogramCanvas = Canvas.createIfSupported();
	private final Canvas arrayLabelCanvas = Canvas.createIfSupported();
	private final Canvas markerLabelCanvas = Canvas.createIfSupported();

	private final Canvas downArrowCanvas, upArrowCanvas;
	
	/* the status variables that are needed even without communication with server side */
	private int firstMarker = 0;
	// the following variables are necessary to be member variables only to handle the last 'page'
	private int markerNumber = 0; 
	private int paintableMarkers = 0;
	
    private ClusterNode microarrayDendrogramRoot;
    private ClusterNode markerDendrogramRoot;
    
    private int arrayClusterHeight = 0;
    private int markerClusterHeight = 0;
    
	/* variables from server side. they are also part of status */
	private int arrayNumber;
	private int cellWidth;
	private int cellHeight;
	private String[] arrayLabels;
	private String[] markerLabels;
	private int[] colors;
	
    /* constants */
    final static private int MAX_WIDTH = 3000;
    final static private int MAX_HEIGHT = 2000;
    final static private CssColor SELECTED_COLOR = CssColor.make(225, 255, 225);
    final static private CssColor UNSELECTED_COLOR = CssColor.make(255, 255, 255);
	final static private int deltaH = 5; // the increment of the dendrogram height

    private ClusterHandler microarrayClusterHandler = new ClusterHandler() {

		@Override
		void getOriginal(int x, int y) {
			this.x = x;
			this.y = arrayClusterHeight-y; // flip y
		}
    };
    
    private ClusterHandler markerClusterHandler = new ClusterHandler() {

		@Override
		void getOriginal(int x, int y) {
			this.x = y;
			this.y = markerClusterHeight - x;
		}
    	
    };

    /* the only constructor */
	public VDendrogram() {   
		
		initWidget(panel);
		panel.add(canvas);
		panel.add(arrayDendrogramCanvas);
		panel.add(markerDendrogramCanvas);
		panel.add(arrayLabelCanvas);
		panel.add(markerLabelCanvas);

		downArrowCanvas = createRetrievingButton(true, new PagingHandler(true));
		upArrowCanvas = createRetrievingButton(false, new PagingHandler(false));
		panel.add(downArrowCanvas);
		panel.add(upArrowCanvas);
		
		arrayDendrogramCanvas.addClickHandler(microarrayClusterHandler);
		markerDendrogramCanvas.addClickHandler(markerClusterHandler);
	}
    
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
		arrayNumber = uidl.getIntAttribute("arrayNumber");
		markerNumber = uidl.getIntAttribute("markerNumber");
		String arrayCluster = uidl.getStringAttribute("arrayCluster");
		String markerCluster = uidl.getStringAttribute("markerCluster");
		colors = uidl.getIntArrayAttribute("colors"); // this could be partial data if firstMarker>0
		arrayLabels = uidl.getStringArrayAttribute("arrayLabels");
		markerLabels = uidl.getStringArrayAttribute("markerLabels");
		
		cellWidth = uidl.getIntAttribute("cellWidth");
		cellHeight = uidl.getIntAttribute("cellHeight");
		
		firstMarker = uidl.getIntVariable("firstMarker");

		ClusterParser parser = new ClusterParser();
		microarrayDendrogramRoot = parser.parse(arrayCluster, cellWidth);
		microarrayClusterHandler.setDendrogramRoot(microarrayDendrogramRoot);

		markerDendrogramRoot = parser.parse(markerCluster, cellHeight);
		markerClusterHandler.setDendrogramRoot(markerDendrogramRoot);

		paint();
	}
	
	/* separate from updateFromUIDL because it is not always necessary to get back to server side */
	private void paint() {
		
		// [1] array labels on the bottom
		int heatmapWidth = cellWidth*arrayNumber;
		sizeCanvas(arrayLabelCanvas, heatmapWidth, 150); // default height
		Context2d context3 = arrayLabelCanvas.getContext2d();
		context3.rotate(-0.5*Math.PI);
		context3.translate(-10, 0); // because of using right align
		context3.setTextAlign(TextAlign.RIGHT);
		if(cellWidth<10) {
			context3.setFont((cellWidth-1)+"px sans-serif");
		}
		int arrayLabelHeight = drawLabels(arrayLabelCanvas, arrayLabels, cellWidth, 0);
        
		// [2] canvas for microarray dendrogram
		arrayClusterHeight = (int)microarrayDendrogramRoot.y + deltaH; // add some extra space on top
		sizeCanvas(arrayDendrogramCanvas, heatmapWidth, arrayClusterHeight);

		Context2d arrayDendrogramContext = arrayDendrogramCanvas.getContext2d();
		arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down
		drawBrackets(arrayDendrogramContext, microarrayDendrogramRoot);

		// [3] heatmap
		paintableMarkers = Math.min(markerNumber, (MAX_HEIGHT-arrayLabelHeight-arrayClusterHeight)/cellHeight);
        int heatmapHeight = paintableMarkers*cellHeight;
        updateArrowCanvas(heatmapHeight, arrayClusterHeight);
        drawHeatmap(canvas, colors, paintableMarkers, arrayNumber, cellHeight, cellWidth);

		// [4] canvas for marker dendrogram
		markerClusterHeight = (int)markerDendrogramRoot.y + deltaH; // add some extra space to the left
		sizeCanvas(markerDendrogramCanvas, markerClusterHeight, heatmapHeight);

		// rotate and move it to the left hand side area
		Context2d markerDendrogramContext = markerDendrogramCanvas.getContext2d();
		markerDendrogramContext.rotate(0.5*Math.PI);
		markerDendrogramContext.translate(0, -markerClusterHeight);
		drawBrackets(markerDendrogramContext, markerDendrogramRoot);

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
	
	/* the following methods are static, namely independent of the GUI status */
	/* find the lowest level node that matches the position */
	static private ClusterNode findNode(int x, int y, ClusterNode node) {
		// note that left and right children will not match at the the same time
		if(node.left!=null) {
			ClusterNode match = findNode(x, y, node.left);
			if(match!=null) {
				return match;
			}
		}
		
		if(node.right!=null) {
			ClusterNode match = findNode(x, y, node.right);
			if(match!=null) {
				return match;
			}
		}
		
		if(x>node.x1 && x<node.x2 & y<node.y){
			return node;
		}

		return null;
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
	static private void drawHeatmap(final Canvas heatmapCanvas, int[] colors, int paintable, int column, int cellHeight, int cellWidth) {
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
	
	static private int drawLabels(final Canvas canvas, String[] labels, int interval, int first) {
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

	static private void drawBrackets(final Context2d context, ClusterNode node) {
		/* note the interesting and dangerous part of this: because this is compiled into javascript so you will not see null pointer exception in java*/
		if(node.left==null || node.right==null) { // do nothing for the leave node
			return;
		}
		
		context.beginPath();
		context.moveTo(node.x1, 0);
		context.lineTo(node.x1, node.y);
		context.lineTo(node.x2, node.y);
		context.lineTo(node.x2, 0);
		context.stroke();
		
		if(node.selected)
			context.setFillStyle(SELECTED_COLOR);
		else
			context.setFillStyle(UNSELECTED_COLOR);
		context.fillRect(node.x1+0.5, 0, node.x2-node.x1-1, node.y);
		
		drawBrackets(context, node.left);
		drawBrackets(context, node.right);
	}

	static private Canvas createRetrievingButton(boolean downDirection, PagingHandler pagingHandler) {
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
		canvas.addClickHandler(pagingHandler);
		Style s = canvas.getCanvasElement().getStyle();
		s.setPosition(Position.ABSOLUTE);
		s.setLeft(100,  Unit.PX);
		s.setZIndex(1);
		
		return canvas;
	}
	
	/* private inner classes: mouse click handlers */
	/* handle for scrolling to more 'pages' of large heat map */
    private class PagingHandler implements ClickHandler {

    	final private boolean down;
    	
    	PagingHandler(boolean down) {
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
    
    private abstract class ClusterHandler implements ClickHandler {

    	private ClusterNode root;
    	
    	void setDendrogramRoot(final ClusterNode root) {
    		this.root = root;
    	}
    	
    	transient int x, y;
    	/* get the coordinates in the dendrogram BEFORE transformation for display */
    	abstract void getOriginal(int x, int y);
    	
		@Override
		public void onClick(ClickEvent event) {
			getOriginal(event.getX(), event.getY());
			ClusterNode lowestMatch = findNode(x, y, root);
			if(lowestMatch.selected) {
				ClusterNode highestSelect = root.getSelected();
				// TODO replace this with real functionality
				Window.alert("Selected: "+highestSelect.x1+" "+highestSelect.x2+" "+highestSelect.y);
				root.select( false ); // clear the selection
			} else {
				root.select( false ); // clear the selection
				lowestMatch.select( true ); // highlight a new selection
			}
			paint();
		}
    	
    };

}
