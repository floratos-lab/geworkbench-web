package org.geworkbenchweb.visualizations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VMoleculeViewer.class)
public class MoleculeViewer extends AbstractComponent {

	private static final long serialVersionUID = 4600493722698214718L;

	final private String pdbcontent;
	private String representation = null;
	private boolean atoms = false;
	private boolean bonds = false;
	private boolean labels = false;
	private boolean ribbon = true;
	private boolean backbone = false;
	private boolean pipe = false;
	private boolean cartoonize = true;
	private boolean colorByChain = false;
	private boolean colorByResidue = false;
	
	private String colorType = "amino";
	
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
        target.addAttribute("displayLabels", labels);
        target.addAttribute("displayRibbon", ribbon);
        target.addAttribute("displayBackbone", backbone);
        target.addAttribute("displayPipe", pipe);
        target.addAttribute("cartoonize", cartoonize);
        target.addAttribute("colorByChain", colorByChain);
        target.addAttribute("colorByResidue", colorByResidue);
        
        target.addAttribute("colorType", colorType);
	}

	public void setDisplayAtoms(boolean checked) {
		this.atoms = checked;
		requestRepaint();
	}

	public void setDisplayBonds(boolean checked) {
		this.bonds = checked;
		requestRepaint();
	}
	
	public void setDisplayLabels(boolean checked) {
		this.labels = checked;
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

	public void setCartoonize(boolean checked) {
		this.cartoonize = checked;
		requestRepaint();
	}

	public void setColorByChain(boolean checked) {
		this.colorByChain = checked;
		requestRepaint();
	}

	public void setColorByResidue(boolean checked) {
		this.colorByResidue = checked;
		requestRepaint();
	}

	public void setResidueColorType(String colorType) {
		this.colorType = colorType;
		requestRepaint();
	}
}
