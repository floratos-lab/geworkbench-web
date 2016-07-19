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
	
	private final Canvas heatmapCanvas = Canvas.createIfSupported();
	private final Canvas arrayDendrogramCanvas = Canvas.createIfSupported();
	private final Canvas markerDendrogramCanvas = Canvas.createIfSupported();
	private final Canvas arrayLabelCanvas = Canvas.createIfSupported();
	private final Canvas markerLabelCanvas = Canvas.createIfSupported();

	private final Canvas downArrowCanvas, upArrowCanvas;
	
	/* the status variables that are needed even without communication with server side */
	private int firstMarker = 0;
	// the following variables are necessary to be member variables only to handle the last 'page'
	//private int markerNumber = 0; 
	private int paintableMarkers = 0; // the number of row we may want to draw. that should cover [yIndex1, yIndex2]
	
    private ClusterNode microarrayDendrogramSelected;
    private ClusterNode markerDendrogramSelected;
    
    private int arrayClusterHeight = 0;
    private int markerClusterHeight = 0;
    
	/* variables from server side. they are also part of status */
	private int arrayNumber;
	private int cellWidth;
    private int cellHeight;
	private String[] arrayLabels;
	private String[] markerLabels;
	private int[] colors; // the actual color data in the form of a one-dimensional array	 
	
    /* constants */
    final static private int MAX_WIDTH = 15000;
    final static private int MAX_HEIGHT = 10000;
    final static private CssColor SELECTED_COLOR = CssColor.make(225, 255, 225);
    final static private CssColor UNSELECTED_COLOR = CssColor.make(255, 255, 255);
	final static private int deltaH = 5; // the increment of the dendrogram height

	/* support part selection of dendrogram*/
	private int xIndex1;
	private int xIndex2;
	private int yIndex1;
	private int yIndex2;
	
	//20.07.2016
	private String arrayPos; //Can be Bottom or Top also
	private String markerPos; //Can be Right or Left also
	///
	
    private ClusterHandler microarrayClusterHandler = new ClusterHandler() {

		@Override
		void getOriginal(int x, int y) {
			this.x = x + xIndex1*cellWidth;
			this.y = arrayClusterHeight-y; // flip y
		}

		@Override
		void setDendrogramSelected(ClusterNode selected) {
			microarrayDendrogramSelected = selected;
			xIndex1 = microarrayDendrogramSelected.index1;
			xIndex2 = microarrayDendrogramSelected.index2;
		}

		@Override
		void updateServerSideSelection() {
			client.updateVariable(paintableId, "selectedArrayClusters", microarrayDendrogramSelected.toString(), false);
			client.updateVariable(paintableId, "arrayIndex1", xIndex1, false);
			client.updateVariable(paintableId, "arrayIndex2", xIndex2, true);
		}
    };
    
    private ClusterHandler markerClusterHandler = new ClusterHandler() {

		@Override
		void getOriginal(int x, int y) {
			this.x = y + yIndex1*cellHeight;
			this.y = markerClusterHeight - x;
		}

		@Override
		void setDendrogramSelected(ClusterNode selected) {
			markerDendrogramSelected = selected;
			yIndex1 = markerDendrogramSelected.index1;
			yIndex2 = markerDendrogramSelected.index2;
		}

		@Override
		void updateServerSideSelection() {
			client.updateVariable(paintableId, "selectedMarkerClusters", markerDendrogramSelected.toString(), false);
			client.updateVariable(paintableId, "markerIndex1", yIndex1, false);
			client.updateVariable(paintableId, "markerIndex2", yIndex2, true);
		
		}
    };

    /* the only constructor */
	public VDendrogram() {   
		
		initWidget(panel);
		panel.add(heatmapCanvas);
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

		if (heatmapCanvas == null) {
            // "Sorry, your browser doesn't support the HTML5 Canvas element";
            return;
		}

		// the difference between Attribute and Variable: in general, if not to be changed here (in client), use attribute
		// see https://vaadin.com/forum/-/message_boards/view_message/192733
		arrayNumber = uidl.getIntAttribute("arrayNumber");
		int markerNumber = uidl.getIntAttribute("markerNumber"); // TODO how much use of this on server side?
		String arrayCluster = uidl.getStringAttribute("arrayCluster");
		String markerCluster = uidl.getStringAttribute("markerCluster");
		colors = uidl.getIntArrayAttribute("colors"); // this could be partial data if firstMarker>0
		arrayLabels = uidl.getStringArrayAttribute("arrayLabels");
		markerLabels = uidl.getStringArrayAttribute("markerLabels");
		
		cellWidth = uidl.getIntAttribute("cellWidth");
		cellHeight = uidl.getIntAttribute("cellHeight");
		int exportImageCellWidth = uidl.getIntAttribute("exportImageCellWidth");
		int exportImageCellHeight = uidl.getIntAttribute("exportImageCellHeight");
	 
		firstMarker = uidl.getIntVariable("firstMarker");

		xIndex1 = uidl.getIntVariable("arrayIndex1");
		xIndex2 = uidl.getIntVariable("arrayIndex2");
		yIndex1 = uidl.getIntVariable("markerIndex1");
		yIndex2 = uidl.getIntVariable("markerIndex2");	 
		
		//20.07.2016
		arrayPos = uidl.getStringVariable("arrayPos");
		markerPos = uidl.getStringVariable("markerPos");
		///

		if(uidl.getBooleanVariable("exportImage")) {
			// this does not show in eclipse workspace browser somehow		 
			Canvas offline = createOfflineImage(exportImageCellWidth, exportImageCellHeight, markerNumber, arrayCluster, markerCluster);
			// use the fixed window name so the window is reused for this functionality
			String dataUrl = offline.toDataUrl("image/png");		 
			
			if (isIEBrowser())
			    openCanvasImage(dataUrl);
			else
		        Window.open(dataUrl, "dendrogram_snapshot", "");
		     
		    
			//	client.updateVariable(paintableId, "imageUrl", heatmapCanvas.toDataUrl("image/png"), true);
		}
		else
			createOnlineImage(markerNumber, arrayCluster, markerCluster);
		
	}
	
	
	public native void alert(String s)/*-{
	    alert(s);

    }-*/;
	
	
	public native void openCanvasImage(String dataUrl)/*-{
  
         var canvasDataUrl = dataUrl;      
         var win=window.open();    
         win.document.write("<div align=\"center\" ><img src='" + canvasDataUrl + "' /></div>");     
         win.document.title = "dendrogram snapshot";
    
    }-*/;
	
	
	/**
	* Returns true if the current browser is IE (Internet Explorer).
	*/
	public static boolean isIEBrowser() {
	    return getBrowserName().toLowerCase().contains("msie");
	}

	
	/**
	* Gets the name of the used browser.
	*/
	public static native String getBrowserName() /*-{
	    return navigator.userAgent.toLowerCase();
	}-*/;
	
    private void createOnlineImage(int markerNumber, String arrayCluster, String markerCluster) {
    	
    	ClusterParser parser = new ClusterParser();
		ClusterNode microarrayDendrogramRoot = parser.parse(arrayCluster, cellWidth, xIndex1);
		microarrayClusterHandler.setDendrogramRoot(microarrayDendrogramRoot);
		if(arrayCluster.length()==0) { // no cluster
			xIndex1 = 0;
			xIndex2 = arrayNumber-1;
		}

		ClusterNode markerDendrogramRoot = parser.parse(markerCluster, cellHeight, yIndex1);
		markerClusterHandler.setDendrogramRoot(markerDendrogramRoot);
		if(markerCluster.length()==0) { // no cluster
			yIndex1 = 0;
			yIndex2 = markerNumber - 1;
		}
	 
		paintOnLine(cellWidth, cellHeight, heatmapCanvas,
				  arrayDendrogramCanvas, markerDendrogramCanvas, arrayLabelCanvas, markerLabelCanvas, microarrayDendrogramSelected, markerDendrogramSelected);
	}
    
	
	private Canvas createOfflineImage(int offlineCellWidth, int offlineCellHeight, int markerNumber, String arrayCluster, String markerCluster) {
		
		Canvas heatmapCanvas = Canvas.createIfSupported();
		Canvas arrayDendrogramCanvas = Canvas.createIfSupported();
		Canvas markerDendrogramCanvas = Canvas.createIfSupported();
		Canvas arrayLabelCanvas = Canvas.createIfSupported();
		Canvas markerLabelCanvas = Canvas.createIfSupported();			
		 
		ClusterParser parser = new ClusterParser();
		ClusterNode microarrayDendrogramRoot = parser.parse(arrayCluster, offlineCellWidth, xIndex1);
		 
		if(arrayCluster.length()==0) { // no cluster
			xIndex1 = 0;
			xIndex2 = arrayNumber-1;
		}

		ClusterNode markerDendrogramRoot = parser.parse(markerCluster, offlineCellHeight, yIndex1);
	 
		if(markerCluster.length()==0) { // no cluster
			yIndex1 = 0;
			yIndex2 = markerNumber - 1;
		}
		
		//Receive ayyarLabelHeight and markerLabelWidth values from painOffLine
		int[] arrayHeightMarkerWidth =paintOffLine(offlineCellWidth, offlineCellHeight, heatmapCanvas,
				   arrayDendrogramCanvas, markerDendrogramCanvas, arrayLabelCanvas, markerLabelCanvas, microarrayDendrogramRoot, markerDendrogramRoot);
		
		int heatmapWidth = offlineCellWidth*(xIndex2 - xIndex1 +1);
		int markerNumberToPaint = yIndex2 - yIndex1 +1;
        int heatmapHeight = markerNumberToPaint*offlineCellHeight;
		int width = markerClusterHeight + heatmapWidth + 300;
		int height = arrayClusterHeight+heatmapHeight + 150;
		
		Canvas offline = Canvas.createIfSupported();
     	//offline.setPixelSize(width, height); // when necessary?
		offline.setCoordinateSpaceWidth(width);
		offline.setCoordinateSpaceHeight(height);
		Context2d context = offline.getContext2d();
		
		
		//20.07.2016
		// place things in place based  array label position 
		int arrayLabelHeight=arrayHeightMarkerWidth[0];
		int markerLabelWidth=arrayHeightMarkerWidth[1];
		if((arrayPos.equals("Bottom")) && (markerPos.equals("Right"))) {
			context.drawImage(arrayDendrogramCanvas.getCanvasElement(), markerClusterHeight, 0);
			context.drawImage(markerDendrogramCanvas.getCanvasElement(), 0, arrayClusterHeight);
			context.drawImage(heatmapCanvas.getCanvasElement(), markerClusterHeight, arrayClusterHeight);
			context.drawImage(arrayLabelCanvas.getCanvasElement(), markerClusterHeight, arrayClusterHeight+heatmapHeight);
			context.drawImage(markerLabelCanvas.getCanvasElement(), markerClusterHeight+heatmapWidth, arrayClusterHeight);
		}
		else if((arrayPos.equals("Bottom")) && (markerPos.equals("Left"))) {
			context.drawImage(arrayDendrogramCanvas.getCanvasElement(),markerClusterHeight+markerLabelWidth,0);
			context.drawImage(markerDendrogramCanvas.getCanvasElement(),0,arrayClusterHeight);
			context.drawImage(markerLabelCanvas.getCanvasElement(),markerClusterHeight,arrayClusterHeight);
			context.drawImage(heatmapCanvas.getCanvasElement(),markerClusterHeight+markerLabelWidth,arrayClusterHeight);
			context.drawImage(arrayLabelCanvas.getCanvasElement(), markerClusterHeight+markerLabelWidth,arrayClusterHeight+heatmapHeight);
		}
		else if((arrayPos.equals("Top")) && (markerPos.equals("Right"))) {
			context.drawImage(arrayDendrogramCanvas.getCanvasElement(), markerClusterHeight, 0);
			context.drawImage(arrayLabelCanvas.getCanvasElement(), markerClusterHeight, arrayClusterHeight);
			context.drawImage(markerDendrogramCanvas.getCanvasElement(), 0, arrayClusterHeight+arrayLabelHeight);
			context.drawImage(heatmapCanvas.getCanvasElement(), markerClusterHeight, arrayClusterHeight+arrayLabelHeight);
			context.drawImage(markerLabelCanvas.getCanvasElement(), markerClusterHeight+heatmapWidth, arrayClusterHeight+arrayLabelHeight);
		}
		else{ // arrayPos="Top" and markerPos="Left"
			context.drawImage(arrayDendrogramCanvas.getCanvasElement(), markerClusterHeight+markerLabelWidth, 0);
			context.drawImage(arrayLabelCanvas.getCanvasElement(), markerClusterHeight+markerLabelWidth, arrayClusterHeight);
			context.drawImage(markerDendrogramCanvas.getCanvasElement(), 0, arrayClusterHeight+arrayLabelHeight);		
			context.drawImage(markerLabelCanvas.getCanvasElement(), markerClusterHeight, arrayClusterHeight+arrayLabelHeight);
			context.drawImage(heatmapCanvas.getCanvasElement(), markerClusterHeight+markerLabelWidth, arrayClusterHeight+arrayLabelHeight);
		}
		///
		return offline;
	}
		 
	
	private void paintOnLine(int cellWidth, int cellHeight, Canvas heatmapCanvas,
	Canvas arrayDendrogramCanvas, Canvas markerDendrogramCanvas, Canvas arrayLabelCanvas, Canvas markerLabelCanvas,
	ClusterNode microarrayDendrogramSelected, ClusterNode markerDendrogramSelected) {
		
			
		int arrayNumberToPaint = xIndex2 - xIndex1 +1;
		int markerNumberToPaint = yIndex2 - yIndex1 +1;
		
		// [1] array labels on the bottom
		int heatmapWidth = cellWidth*arrayNumberToPaint;
		sizeCanvas(arrayLabelCanvas, heatmapWidth, 150); // default height
		Context2d context3 = arrayLabelCanvas.getContext2d();
		context3.rotate(-0.5*Math.PI);
		context3.translate(-10, 0); // because of using right align
		context3.setTextAlign(TextAlign.RIGHT);
		if(cellWidth<10) {
			context3.setFont((cellWidth-1)+"px sans-serif");
		}
		int arrayLabelHeight = drawLabels(arrayLabelCanvas, arrayLabels, cellWidth, xIndex1); // TODO I need to add the ending index as well
        
		// [2] canvas for microarray dendrogram
		arrayClusterHeight = (int)microarrayDendrogramSelected.y + deltaH; // add some extra space on top
		sizeCanvas(arrayDendrogramCanvas, heatmapWidth, arrayClusterHeight);

		Context2d arrayDendrogramContext = arrayDendrogramCanvas.getContext2d();
		arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down
		arrayDendrogramContext.translate(-xIndex1*cellWidth, 0); // this is to handle the case when we draw part of the drendrogram 
		drawBrackets(arrayDendrogramContext, microarrayDendrogramSelected);

		// [3] heatmap
		paintableMarkers = Math.min(markerNumberToPaint, (MAX_HEIGHT-arrayLabelHeight-arrayClusterHeight)/cellHeight);
        int heatmapHeight = paintableMarkers*cellHeight ;
        updateArrowCanvas(heatmapHeight, arrayClusterHeight);
        drawHeatmap(heatmapCanvas, cellWidth, cellHeight, paintableMarkers);

		// [4] canvas for marker dendrogram
        markerClusterHeight = (int)markerDendrogramSelected.y + deltaH; // add some extra space to the left
		sizeCanvas(markerDendrogramCanvas, markerClusterHeight, heatmapHeight);

		// rotate and move it to the left hand side area
		Context2d markerDendrogramContext = markerDendrogramCanvas.getContext2d();
		markerDendrogramContext.rotate(0.5*Math.PI);
		markerDendrogramContext.translate(0, -markerClusterHeight);
		markerDendrogramContext.translate(-yIndex1*cellHeight, 0); // this is to handle the case when we draw part of the drendrogram
		drawBrackets(markerDendrogramContext, markerDendrogramSelected);

		// [5] markers labels on the right
		sizeCanvas(markerLabelCanvas, 300, heatmapHeight); // default width
		Context2d context4 = markerLabelCanvas.getContext2d();
		if(cellHeight<10) {
			context4.setFont((cellHeight-1)+"px sans-serif");
		}
		
		int markerLabelWidth=0;
		
		if (firstMarker > 0)
			markerLabelWidth  = drawLabels(markerLabelCanvas, markerLabels, cellHeight, firstMarker);
		else
			markerLabelWidth  = drawLabels(markerLabelCanvas, markerLabels, cellHeight, yIndex1);
		
		//20.07.2016
		// place things in place
		if((arrayPos.equals("Bottom")) && (markerPos.equals("Right"))) {
			positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight);
			positionCanvas(markerDendrogramCanvas, arrayClusterHeight, 0);
			positionCanvas(heatmapCanvas, arrayClusterHeight, markerClusterHeight);
			positionCanvas(arrayLabelCanvas, arrayClusterHeight+heatmapHeight, markerClusterHeight);
			positionCanvas(markerLabelCanvas, arrayClusterHeight, markerClusterHeight+heatmapWidth);
		}
		else if((arrayPos.equals("Bottom")) && (markerPos.equals("Left"))) {
			positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight+markerLabelWidth);
			positionCanvas(markerDendrogramCanvas, arrayClusterHeight, 0);
			positionCanvas(markerLabelCanvas, arrayClusterHeight,markerClusterHeight);
			positionCanvas(heatmapCanvas, arrayClusterHeight, markerClusterHeight+markerLabelWidth);
			positionCanvas(arrayLabelCanvas, arrayClusterHeight+heatmapHeight, markerClusterHeight+markerLabelWidth);
		}
		else if((arrayPos.equals("Top")) && (markerPos.equals("Right"))) {
			positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight);
			positionCanvas(arrayLabelCanvas, arrayClusterHeight, markerClusterHeight);
			positionCanvas(markerDendrogramCanvas, arrayClusterHeight+arrayLabelHeight, 0);
			positionCanvas(heatmapCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight);
			positionCanvas(markerLabelCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight+heatmapWidth);
		}
		else{ // arrayPos="Top" and markerPos="Left"
			positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight+markerLabelWidth);
			positionCanvas(arrayLabelCanvas, arrayClusterHeight, markerClusterHeight+markerLabelWidth);
			positionCanvas(markerDendrogramCanvas, arrayClusterHeight+arrayLabelHeight, 0);		
			positionCanvas(markerLabelCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight);
			positionCanvas(heatmapCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight+markerLabelWidth);
		}
		///
		
		//calculate the proper panel size
		int width0 = heatmapWidth + markerClusterHeight +  markerLabelWidth;
		int height0 = heatmapHeight + arrayClusterHeight +  arrayLabelHeight;
		panel.setWidth(Math.min(width0, MAX_WIDTH) + "px");
		panel.setHeight(Math.min(height0, MAX_HEIGHT) + "px"); 
	}
	
	//Changed return type from void to an array of two values representing arrayLabelHeight and markerLabelWidth
	private int[] paintOffLine(int cellWidth, int cellHeight, Canvas heatmapCanvas,
			Canvas arrayDendrogramCanvas, Canvas markerDendrogramCanvas, Canvas arrayLabelCanvas, Canvas markerLabelCanvas,
			ClusterNode microarrayDendrogramSelected, ClusterNode markerDendrogramSelected) {
			//20.07.2016	
			int[] retVals=new int[2];
			///
				int arrayNumberToPaint = xIndex2 - xIndex1 +1;
				int markerNumberToPaint = yIndex2 - yIndex1 +1;
				
				// [1] array labels on the bottom
				int heatmapWidth = cellWidth*arrayNumberToPaint;
				sizeCanvas(arrayLabelCanvas, heatmapWidth, 150); // default height
				Context2d context3 = arrayLabelCanvas.getContext2d();
				context3.rotate(-0.5*Math.PI);
				context3.translate(-10, 0); // because of using right align
				context3.setTextAlign(TextAlign.RIGHT);
				if(cellWidth<10) {
					context3.setFont((cellWidth-1)+"px sans-serif");
				}
				int arrayLabelHeight = drawLabels(arrayLabelCanvas, arrayLabels, cellWidth, xIndex1); // TODO I need to add the ending index as well
				//20.07.2016
				retVals[0]=arrayLabelHeight;
				///
		        
				// [2] canvas for microarray dendrogram
				arrayClusterHeight = (int)microarrayDendrogramSelected.y + deltaH; // add some extra space on top
				sizeCanvas(arrayDendrogramCanvas, heatmapWidth, arrayClusterHeight);

				Context2d arrayDendrogramContext = arrayDendrogramCanvas.getContext2d();
				arrayDendrogramContext.transform(1, 0, 0, -1, 0, arrayClusterHeight); // flip upside down
				arrayDendrogramContext.translate(-xIndex1*cellWidth, 0); // this is to handle the case when we draw part of the drendrogram 
				drawBrackets(arrayDendrogramContext, microarrayDendrogramSelected);

				// [3] heatmap				 
		        int heatmapHeight = markerNumberToPaint*cellHeight ;
		        //updateArrowCanvas(heatmapHeight, arrayClusterHeight);
		        drawHeatmap(heatmapCanvas, cellWidth, cellHeight, markerNumberToPaint);

				// [4] canvas for marker dendrogram
		        markerClusterHeight = (int)markerDendrogramSelected.y + deltaH; // add some extra space to the left
				sizeCanvas(markerDendrogramCanvas, markerClusterHeight, heatmapHeight);

				// rotate and move it to the left hand side area
				Context2d markerDendrogramContext = markerDendrogramCanvas.getContext2d();
				markerDendrogramContext.rotate(0.5*Math.PI);
				markerDendrogramContext.translate(0, -markerClusterHeight);
				markerDendrogramContext.translate(-yIndex1*cellHeight, 0); // this is to handle the case when we draw part of the drendrogram
				drawBrackets(markerDendrogramContext, markerDendrogramSelected);

				// [5] markers labels on the right
				sizeCanvas(markerLabelCanvas, 300, heatmapHeight); // default width
				Context2d context4 = markerLabelCanvas.getContext2d();
				if(cellHeight<10) {
					context4.setFont((cellHeight-1)+"px sans-serif");
				}
				
				int markerLabelWidth=0;					 
			    markerLabelWidth  = drawLabels(markerLabelCanvas, markerLabels, cellHeight, yIndex1);
			    retVals[1]=markerLabelWidth;
				//20.07.2016
				// place things in place
				if((arrayPos.equals("Bottom")) && (markerPos.equals("Right"))) {
					positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight);
					positionCanvas(markerDendrogramCanvas, arrayClusterHeight, 0);
					positionCanvas(heatmapCanvas, arrayClusterHeight, markerClusterHeight);
					positionCanvas(arrayLabelCanvas, arrayClusterHeight+heatmapHeight, markerClusterHeight);
					positionCanvas(markerLabelCanvas, arrayClusterHeight, markerClusterHeight+heatmapWidth);
				}
				else if((arrayPos.equals("Bottom")) && (markerPos.equals("Left"))) {
					positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight+markerLabelWidth);
					positionCanvas(markerDendrogramCanvas, arrayClusterHeight, 0);
					positionCanvas(markerLabelCanvas, arrayClusterHeight,markerClusterHeight);
					positionCanvas(heatmapCanvas, arrayClusterHeight, markerClusterHeight+markerLabelWidth);
					positionCanvas(arrayLabelCanvas, arrayClusterHeight+heatmapHeight, markerClusterHeight+markerLabelWidth);
				}
				else if((arrayPos.equals("Top")) && (markerPos.equals("Right"))) {
					positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight);
					positionCanvas(arrayLabelCanvas, arrayClusterHeight, markerClusterHeight);
					positionCanvas(markerDendrogramCanvas, arrayClusterHeight+arrayLabelHeight, 0);
					positionCanvas(heatmapCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight);
					positionCanvas(markerLabelCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight+heatmapWidth);
				}
				else{ // arrayPos="Top" and markerPos="Left"
					positionCanvas(arrayDendrogramCanvas, 0, markerClusterHeight+markerLabelWidth);
					positionCanvas(arrayLabelCanvas, arrayClusterHeight, markerClusterHeight+markerLabelWidth);
					positionCanvas(markerDendrogramCanvas, arrayClusterHeight+arrayLabelHeight, 0);		
					positionCanvas(markerLabelCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight);
					positionCanvas(heatmapCanvas, arrayClusterHeight+arrayLabelHeight, markerClusterHeight+markerLabelWidth);
				}
				///
				
				//calculate the proper panel size
				int width0 = heatmapWidth + markerClusterHeight +  markerLabelWidth;
				int height0 = heatmapHeight + arrayClusterHeight +  arrayLabelHeight;
				panel.setWidth(Math.min(width0, MAX_WIDTH) + "px");
				panel.setHeight(Math.min(height0, MAX_HEIGHT) + "px"); 
				
				return retVals;
			}
	
	private void updateArrowCanvas(final int heatmapHeight,
			final int arrayClusterHeight) {
		int markerNumberToPaint = yIndex2 - yIndex1 +1;
		if (firstMarker + paintableMarkers < markerNumberToPaint) {
			Context2d context = downArrowCanvas.getContext2d();
			context.setFillStyle(CssColor.make(225, 255, 225)); // light green
			context.fillRect(150, 2, 340, 16);
			context.setFillStyle(CssColor.make(0, 0, 0));
			context.fillText("Displayed to row #"
					+ (firstMarker + paintableMarkers)
					+ ". Click to scroll down.", 150, 15);
			downArrowCanvas.setVisible(true);
			positionCanvas(downArrowCanvas, heatmapHeight + arrayClusterHeight - 22, 100 + markerClusterHeight);
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
			positionCanvas(upArrowCanvas, arrayClusterHeight + 2, 100 + markerClusterHeight);
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
	/* this version draw a portion of heatmap although all the data is available */
	// note the careful way to skip the portion not to paint. it is not the most obvious code, but necessary to stay efficient.
	private void drawHeatmap(Canvas heatmapCanvas, int cellWidth, int cellHeight, int paintableMarkers) {
		int height = cellHeight*paintableMarkers;
		int width = cellWidth*(xIndex2-xIndex1+1);
		
		sizeCanvas(heatmapCanvas, width, height);
        
		Context2d context = heatmapCanvas.getContext2d();

		int skipOnRight = arrayNumber -1 - xIndex2;
		int valueIndex = yIndex1*arrayNumber; // skip the unpainted rows
		
		for (int y = 0; y < height; y+=cellHeight) {
			valueIndex += xIndex1; // skip the unpainted column on the left side
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
			valueIndex += skipOnRight; // skip the unpainted column on the right side
		}
	}
	
	private int drawLabels(final Canvas canvas, String[] labels, int interval, int first) {
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

	  private void drawBrackets(final Context2d context, ClusterNode node) {
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
			int markerNumberToPaint = yIndex2 - yIndex1 +1;
			if(firstMarker+paintableMarkers>markerNumberToPaint) { // last 'page'
				return;
			}
			if(down) {
				firstMarker += 100;
			} else {
				firstMarker -= 100;
			}
			if(firstMarker<0)firstMarker = 0;
			client.updateVariable(paintableId, "paintableMarkers", paintableMarkers, false);
			client.updateVariable(paintableId, "firstMarker", firstMarker, true);
		}
    	
    };
    
    private abstract class ClusterHandler implements ClickHandler {

    	private ClusterNode root;
    	
    	void setDendrogramRoot(final ClusterNode root) {
    		this.root = root;
    		setDendrogramSelected(root);
    	}

    	abstract void setDendrogramSelected(ClusterNode selected);
    	
    	transient int x, y;
    	/* get the coordinates in the dendrogram BEFORE transformation for display */
    	abstract void getOriginal(int x, int y);
    	
		@Override
		public void onClick(ClickEvent event) {
			getOriginal(event.getX(), event.getY());
			ClusterNode lowestMatch = findNode(x, y, root);
			if(lowestMatch.selected) {
				setDendrogramSelected( root.getSelected() );
				root.select( false ); // clear the selection
			} else {
				root.select( false ); // clear the selection
				lowestMatch.select( true ); // highlight a new selection
			}
			paintOnLine(cellWidth, cellHeight, heatmapCanvas,
					  arrayDendrogramCanvas, markerDendrogramCanvas, arrayLabelCanvas, markerLabelCanvas, microarrayDendrogramSelected, markerDendrogramSelected);
			updateServerSideSelection();
		}

		abstract void updateServerSideSelection();
    	
    };

}
