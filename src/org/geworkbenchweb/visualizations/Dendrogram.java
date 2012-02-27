package org.geworkbenchweb.visualizations;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VDendrogram widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VDendrogram.class)
public class Dendrogram extends AbstractComponent {
	
	private static final long serialVersionUID = 1L;
	private String[] colors;
	private int numArrays;
	private String[] markerLabels;
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addVariable(this, "color", getColors());
		target.addVariable(this, "arrayNumber", getArrayNumber());
		target.addVariable(this, "markerLabels", getMarkerLabels());
	}

	
	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		//TODO
	}
	
	public void setColors(String[] colors) {
        this.colors = colors;
        requestRepaint();
	}

	public String[] getColors() {
        
		return colors;
	
	}
	
	public void setArrayNumber(int numArrays) {
        this.numArrays = numArrays;
        requestRepaint();
	}

	public int getArrayNumber() {
		return numArrays;
	}
	
	public void setMarkerLabels(String[] markerLabels) {
        this.markerLabels = markerLabels;
        requestRepaint();
	}

	public String[] getMarkerLabels() {
		return markerLabels;
	}
	

}
