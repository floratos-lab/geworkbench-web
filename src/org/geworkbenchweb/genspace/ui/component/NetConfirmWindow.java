package org.geworkbenchweb.genspace.ui.component;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class NetConfirmWindow extends Window {
	
	public NetConfirmWindow(String networkName) {
		setModal(true);
		
		setWidth("30%");
		
		center();
		
		setCaption("Network Confirmation");
		
		addComponent(new Label("Your request for " + networkName + " has been received. It will not appear in your network list until your request is approved."));
		
		Button close = new Button("OK");
		close.addListener(new Button.ClickListener(){
			
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				getApplication().getMainWindow().removeWindow(NetConfirmWindow.this);
			}
		});
		addComponent(close);
	}
}
