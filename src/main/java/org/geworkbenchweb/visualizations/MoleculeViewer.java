package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VMoleculeViewer.class)
public class MoleculeViewer extends AbstractComponent {

	private static final long serialVersionUID = 4600493722698214718L;

	final private String pdbcontent;
	final private String representation;
	
	public MoleculeViewer(String pdbcontent) {
		this.pdbcontent = pdbcontent;
		this.representation = null;
	}
	
	public MoleculeViewer(String pdbcontent, String representation) {
		this.pdbcontent = pdbcontent;
		this.representation = representation;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

        target.addAttribute("pdbcontent", pdbcontent);
        if(representation!=null) // both the name and the value must be non-null
        	target.addAttribute("representation", representation);
	}

}
