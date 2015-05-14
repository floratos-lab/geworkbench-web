package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VMoleculeViewer.class)
public class MoleculeViewer extends AbstractComponent {

	private static final long serialVersionUID = 4600493722698214718L;

	final private String filepath;
	
	public MoleculeViewer(String filepath) {
		this.filepath = filepath;
	}
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

        target.addAttribute("filepath", filepath);
	}

}
