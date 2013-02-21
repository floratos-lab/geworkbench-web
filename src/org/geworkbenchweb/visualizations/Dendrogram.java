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
	
	private Integer[] colors;

	private int arrayNumber;

	private int markerNumber;

	private String arrayCluster, markerCluster;
	private String[] arrayLabels, markerLabels;
	
	private int cellWidth = 10, cellHeight = 5;
	
	public Dendrogram(int arrayNumber, int markerNumber, String arrayCluster, String markerCluster, String[] arrayLabels, String[] markerLabels, int[] colors) {
		this.arrayNumber = arrayNumber;
		this.markerNumber = markerNumber;
		this.arrayCluster = arrayCluster;
		this.markerCluster = markerCluster;
		this.arrayLabels = arrayLabels;
		this.markerLabels = markerLabels;
		 
		/* element value range [-255, 255] */
		this.colors = new Integer[colors.length];
		for(int i=0; i<colors.length; i++) {
			this.colors[i] = colors[i];
		}
	}


	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		
		// Paint any component specific content by setting attributes
		// These attributes can be read in updateFromUIDL in the widget.
		target.addAttribute("arrayNumber", arrayNumber);
		target.addAttribute("markerNumber", markerNumber);
		target.addAttribute("arrayCluster", arrayCluster);
		target.addAttribute("markerCluster", markerCluster); 
		target.addAttribute("colors", colors);
		target.addAttribute("arrayLabels", arrayLabels);
		target.addAttribute("markerLabels", markerLabels);
		
		target.addAttribute("cellWidth", cellWidth);
		target.addAttribute("cellHeight", cellHeight);
	}


	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		// get the variable for server side
//		if (variables.containsKey("marker")) {
//			markerLabels	= 	(String[]) variables.get("markerLabels");
//			markerCluster 	= 	(String) variables.get("marker");
//			colors  		= 	(String[]) variables.get("markerColor");
//			numMarkers 		= 	(Integer) variables.get("markerNumber");
//			requestRepaint();
//		}

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
		requestRepaint();
	}
	
	public void reset() {
		cellWidth = 10;
		cellHeight = 5;
		// TDOD reset the selection as well
		requestRepaint();
	}
	
}
