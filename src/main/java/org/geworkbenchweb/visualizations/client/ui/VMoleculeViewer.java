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
	
	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}		
		placeholder.setId(uidl.getId());

		String pdbcontent = uidl.getStringAttribute("pdbcontent");
		String representation = uidl.getStringAttribute("representation");
		MVJavaScriptObject.createInstance(placeholder.getId(), pdbcontent, representation);
	}

}
