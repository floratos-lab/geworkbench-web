package org.geworkbenchweb.visualizations;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the ... widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VDendrogram.class)
public class Dendrogram extends AbstractComponent {

	private static final long serialVersionUID = -6825142416797042091L;
	
	private Integer[] colors;

	private int arrayNumber;

	private int markerNumber;

	private String arrayCluster;
	
	public Dendrogram(int arrayNumber, int markerNumber, String arrayCluster) {
		this.arrayNumber = arrayNumber;
		this.markerNumber = markerNumber;
		this.arrayCluster = arrayCluster;
	}


	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		
		// Paint any component specific content by setting attributes
		// These attributes can be read in updateFromUIDL in the widget.
		target.addAttribute("arrayNumber", arrayNumber);
		target.addAttribute("markerNumber", markerNumber);
		target.addAttribute("arrayCluster", arrayCluster);
		target.addAttribute("colors", colors);
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


	public void setGeneHeight(int geneHeight) {
		// TODO Auto-generated method stub
		
	}


	public void setGeneWidth(int geneWidth) {
		// TODO Auto-generated method stub
		
	}


	public String[] getMarkerLabels() {
		// TODO Auto-generated method stub
		// null WILL crash the caller
		return null;
	}


	public String[] getArrayLabels() {
		// TODO Auto-generated method stub
		return null;
	}


	public void setSVGFlag(String string) {
		// TODO Auto-generated method stub
		
	}


	public void setMarkerCluster(String mString) {
		// TODO Auto-generated method stub
		
	}


	public void setArrayCluster(String aString) {
		// TODO Auto-generated method stub
		
	}


	public void setColors(int[] colors) { /* element value range [-255, 255] */
		this.colors = new Integer[colors.length];
		for(int i=0; i<colors.length; i++) {
			this.colors[i] = colors[i];
		}
	}


	public void setMarkerLabels(String[] markerNames) {
		// TODO Auto-generated method stub
		
	}


	public void setArrayLabels(String[] arrayNames) {
		// TODO Auto-generated method stub
		
	}

}
