package org.geworkbenchweb.visualizations.client.ui;

import org.geworkbenchweb.layout.ActiveLink;

import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("deprecation")
@Connect(ActiveLink.class)
public class ActiveLinkConnector extends LegacyConnector {
	private static final long serialVersionUID = -274188700809823803L;
	@Override
	public VActiveLink getWidget(){
		return (VActiveLink)super.getWidget();
	}
}
