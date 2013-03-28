package org.geworkbenchweb.visualizations;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the dendrogram widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VDendrogram.class)
public class Dendrogram extends AbstractComponent {

	private static final long serialVersionUID = -6825142416797042091L;
	
	final private int[] colors;

	final private int arrayNumber;

	final private int markerNumber;

	final private String arrayCluster, markerCluster;
	final private String[] arrayLabels, markerLabels;
	
	private int cellWidth = 10, cellHeight = 5;
	
	public Dendrogram(int arrayNumber, int markerNumber, String arrayCluster, String markerCluster, String[] arrayLabels, String[] markerLabels, int[] colors) {
		this.arrayNumber = arrayNumber;
		this.markerNumber = markerNumber;
		this.arrayCluster = arrayCluster;
		this.markerCluster = markerCluster;
		this.arrayLabels = arrayLabels;
		this.markerLabels = markerLabels;
		 
		/* element value range [-255, 255] */
		this.colors = colors;
		
		// this is the upper limit because on the client side the space is smaller by excluding microarray dendrogram and microarray labels
		paintableMarkers =  Math.min(markerNumber, MAX_HEIGHT/cellHeight);
	}

    final private int MAX_HEIGHT = 2000;

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		
		/* get a subset of color values */
        if(firstMarker+paintableMarkers>markerNumber) { // handle the last 'page'
        	firstMarker = markerNumber - paintableMarkers; 
        }

		Integer[] colorSubset = new Integer[paintableMarkers*arrayNumber];
		int i = firstMarker*arrayNumber; // numbers of color values to skip
		int iSubset = 0;
		for(int y=0; y<paintableMarkers; y++) {
			for(int x=0; x<arrayNumber; x++) {
				colorSubset[iSubset++] = colors[i++];
			}
		}
		
		// Paint any component specific content by setting attributes
		// These attributes can be read in updateFromUIDL in the widget.
		target.addAttribute("arrayNumber", arrayNumber);
		target.addAttribute("markerNumber", markerNumber);
		target.addAttribute("arrayCluster", arrayCluster);
		target.addAttribute("markerCluster", markerCluster); 
		target.addAttribute("colors", colorSubset);
		target.addAttribute("arrayLabels", arrayLabels);
		target.addAttribute("markerLabels", markerLabels);
		
		target.addAttribute("cellWidth", cellWidth);
		target.addAttribute("cellHeight", cellHeight);
		
		target.addVariable(this, "firstMarker", firstMarker);
	}

	private int firstMarker = 0;
	private int paintableMarkers;
	
	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		// get the variable for server side
		if (variables.containsKey("firstMarker")) {
			firstMarker = (Integer) variables.get("firstMarker");
			paintableMarkers = (Integer) variables.get("paintableMarkers");
			requestRepaint();
		}
	}

	public void zoomIn() {
		cellWidth *= 2;
		cellHeight *= 2;
		requestRepaint();
	}

	public void zoomOut() {
		if(cellHeight<=1 || cellWidth<=1) return;
		
		cellWidth /= 2;
		cellHeight /= 2;
		paintableMarkers =  Math.min(markerNumber, MAX_HEIGHT/cellHeight);
		requestRepaint();
	}
	
	public void reset() {
		cellWidth = 10;
		cellHeight = 5;
		paintableMarkers =  Math.min(markerNumber, MAX_HEIGHT/cellHeight);
		// TODO reset the selection as well
		requestRepaint();
	}
	
}
