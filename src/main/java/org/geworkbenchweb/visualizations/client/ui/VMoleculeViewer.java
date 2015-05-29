package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VMoleculeViewer extends Widget implements Paintable {

	private Element placeholder = DOM.createDiv();

	public VMoleculeViewer() {
		setElement(placeholder);
	}
	
	private String pdbcontent = null;

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}		
		placeholder.setId(uidl.getId());

		String newcontent = uidl.getStringAttribute("pdbcontent");
		String representation = uidl.getStringAttribute("representation");
		boolean displayAtoms = uidl.getBooleanAttribute("displayAtoms");
		boolean displayBonds = uidl.getBooleanAttribute("displayBonds");
		boolean displayLabels = uidl.getBooleanAttribute("displayLabels");
		boolean displayRibbon = uidl.getBooleanAttribute("displayRibbon");
		boolean displayBackbone = uidl.getBooleanAttribute("displayBackbone");
		boolean displayPipe = uidl.getBooleanAttribute("displayPipe");
		boolean cartoonize = uidl.getBooleanAttribute("cartoonize");
		boolean colorByChain = uidl.getBooleanAttribute("colorByChain");
		boolean colorByResidue = uidl.getBooleanAttribute("colorByResidue");
		
		String colorType = uidl.getStringAttribute("colorType");

		if(newcontent.equals(pdbcontent)) {
			MVJavaScriptObject.set3DRepresentation(representation);
			MVJavaScriptObject.setDisplayAtoms(displayAtoms);
			MVJavaScriptObject.setDisplayBonds(displayBonds);
			MVJavaScriptObject.setDisplayLabels(displayLabels);
			MVJavaScriptObject.setDisplayRibbon(displayRibbon);
			MVJavaScriptObject.setDisplayBackbone(displayBackbone);
			MVJavaScriptObject.setDisplayPipe(displayPipe);
			MVJavaScriptObject.setCartoonize(cartoonize);
			MVJavaScriptObject.setColorByChain(colorByChain);
			
			MVJavaScriptObject.setColorType(colorType);
			MVJavaScriptObject.setColorByResidue(colorByResidue);
			
			return;
		}
		
		pdbcontent = newcontent;
		MVJavaScriptObject.createInstance(placeholder.getId(), pdbcontent, representation);
	}

}
