package org.geworkbenchweb.genspace.ui.component;

import org.geworkbenchweb.utils.LayoutUtil;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NetConfirmWindow extends Window {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NetConfirmWindow(String networkName) {
		setModal(true);
		
		setWidth("30%");
		
		center();
		
		setCaption("Network Confirmation");
		
		VerticalLayout layout = LayoutUtil.addComponent((new Label("Your request for " + networkName + " has been received. It will not appear in your network list until your request is approved.")));
		setContent(layout);
		
		Button close = new Button("OK");
		close.addClickListener(new Button.ClickListener(){
			
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(NetConfirmWindow.this);
			}
		});
		layout.addComponent(close);
	}
}
