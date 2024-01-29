package org.geworkbenchweb.visualizations;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

@JavaScript({ "https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js", "ChemDoodleWeb.js",
		"molecule_viewer.js" })
public class MoleculeViewer extends AbstractJavaScriptComponent {

	@Override
	public MoleculeViewerState getState() {
		return (MoleculeViewerState) super.getState();
	}

	public MoleculeViewer(String pdbcontent) {
		getState().pdb_content = pdbcontent;
	}
}
