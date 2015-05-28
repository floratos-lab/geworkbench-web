package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VMoleculeViewer.class)
public class MoleculeViewer extends AbstractComponent {

	private static final long serialVersionUID = 4600493722698214718L;

	final private String pdbcontent;
	private String representation = null;
	private boolean atoms = true;
	private boolean bonds = true;
	private boolean ribbon = true;
	private boolean backbone = false;
	private boolean pipe = false;
	
	public MoleculeViewer(String pdbcontent) {
		this.pdbcontent = pdbcontent;
	}
	

	public void set3DRepresentation(String representation) {
		this.representation = representation;
		requestRepaint();
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

        if(representation==null) representation = "van der Waals Spheres";

        target.addAttribute("pdbcontent", pdbcontent);
       	target.addAttribute("representation", representation);

        target.addAttribute("displayAtoms", atoms);
        target.addAttribute("displayBonds", bonds);
        target.addAttribute("displayRibbon", ribbon);
        target.addAttribute("displayBackbone", backbone);
        target.addAttribute("displayPipe", pipe);
	}

	public void setDisplayAtoms(boolean checked) {
		this.atoms = checked;
		requestRepaint();
	}

	public void setDisplayBonds(boolean checked) {
		this.bonds = checked;
		requestRepaint();
	}
	
	public void setDisplayRibbon(boolean checked) {
		this.ribbon = checked;
		requestRepaint();
	}

	public void setDisplayBackbone(boolean checked) {
		this.backbone = checked;
		requestRepaint();
	}


	public void setDisplayPipe(boolean checked) {
		this.pipe = checked;
		requestRepaint();
	}
}
