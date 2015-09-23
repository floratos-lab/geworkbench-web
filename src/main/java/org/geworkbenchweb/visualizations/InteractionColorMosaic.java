package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VInteractionColorMosaic.class)
public class InteractionColorMosaic extends AbstractComponent {

	private static final long serialVersionUID = 4600493722698214718L;

	final private String test;
	private String param2 = null;
	
	public InteractionColorMosaic(String parameters) {
		if (parameters == null)
			this.test = "";
		else
			this.test = parameters;
	}
	
	// example of setting a property
	public void setParameter2(String param2) {
		this.param2 = param2;
		requestRepaint();
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

        if(param2==null) param2 = "TEST PARAMETER";

        target.addAttribute("param1", test);
       	target.addAttribute("param2", param2);
	}
}
