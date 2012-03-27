package org.geworkbenchweb.visualizations;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VCytoscape widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VCytoscape.class)
public class Cytoscape extends AbstractComponent {

	private String message = "Click here.";
	private int clicks = 0;

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		
	}

}
