package org.geworkbenchweb.visualizations;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class MoleculeViewerState extends JavaScriptComponentState {
    public String pdb_content;
    public String representation = "van der Waals Spheres";
    public boolean atoms = false;
    public boolean bonds = false;
    public boolean labels = false;
    public boolean ribbon = true;
    public boolean backbone = false;
    public boolean pipe = false;
    public boolean cartoonize = true;
    public boolean colorByChain = false;
    public boolean colorByResidue = false;
    public String colorType = "amino";
}
