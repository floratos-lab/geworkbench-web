package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VInteractionColorMosaic extends Widget implements Paintable {

	private Element placeholder = DOM.createDiv();

	public VInteractionColorMosaic() {
		setElement(placeholder);
	}
	
	private String param1 = null;

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		
		if (client.updateComponent(this, uidl, true)) {
			return;
		}		
		placeholder.setId(uidl.getId());

		String newcontent = uidl.getStringAttribute("param1");
		String param2 = uidl.getStringAttribute("param2");

		if(newcontent.equals(param1)) {
			// TODO ICMJavaScriptObject.setParameter2(param2);
			return;
		}
		
		param1 = newcontent;
		ICMJavaScriptObject.createInstance(placeholder.getId());
	}

}
