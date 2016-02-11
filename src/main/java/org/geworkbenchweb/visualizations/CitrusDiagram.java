package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VCitrusDiagram.class)
public class CitrusDiagram extends AbstractComponent {

	private static final long serialVersionUID = 2073659992756579843L;
	final private String[] alteration;
	final private Float[] nes;

	public CitrusDiagram() {
		alteration = new String[] { "1100", "0011", "1001" };
		nes = new Float[10];
		for (int i = 0; i < nes.length; i++)
			nes[i] = (float) i;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addAttribute("alteration", alteration);
		target.addAttribute("nes", nes);
	}
}
