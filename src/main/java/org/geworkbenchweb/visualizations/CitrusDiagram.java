package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VCitrusDiagram.class)
public class CitrusDiagram extends AbstractComponent {

	private static final long serialVersionUID = 2073659992756579843L;
	private String[] alteration = new String[0];
	private String[] samples = new String[0];
	private String[] presence = new String[0];
	private Integer[] preppi = new Integer[0];
	private Integer[] cindy = new Integer[0];
	private String[] pvalue = new String[0];
	private String[] nes = new String[0];
	
	private boolean zoom = false;
	private double xzoom = 1, yzoom = 1;

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addAttribute("alteration", alteration);
		target.addAttribute("samples", samples);
		target.addAttribute("presence", presence);
		target.addAttribute("preppi", preppi);
		target.addAttribute("cindy", cindy);
		target.addAttribute("pvalue", pvalue);
		target.addAttribute("nes", nes);
		
		target.addAttribute("zoom", zoom);
		target.addAttribute("xzoom", xzoom);
		target.addAttribute("yzoom", yzoom);
	}

	public void setCitrusData(String[] alteration, String[] samples, String[] presence,
			Integer[] preppi, Integer[] cindy,
			String[] pvalue, String[] nes) {
		this.alteration = alteration;
		this.samples = samples;
		this.presence = presence;
		this.preppi = preppi;
		this.cindy = cindy;
		this.pvalue = pvalue;
		this.nes = nes;
		this.zoom = false;

		requestRepaint();
	}

	public void zoomX(double x) {
		this.zoom = true;
		this.xzoom = x;
		requestRepaint();
	}
}
