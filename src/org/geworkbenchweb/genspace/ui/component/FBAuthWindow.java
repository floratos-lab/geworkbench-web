package org.geworkbenchweb.genspace.ui.component;

import org.geworkbenchweb.genspace.FBManager;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FBAuthWindow extends Window{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GenSpaceLogin login;
	
	private ThemeResource gefb = new ThemeResource("img/gefb.png");
	
	private Label fbUserNotebook;
	
	public FBAuthWindow(GenSpaceLogin login, Label fbUser) {
		this.login = login;
		this.setWidth("350px");
		this.setHeight("250px");
		this.setCaption("Facebook Authentication");
		this.fbUserNotebook = fbUser;
		this.makeLayout();
	}
	
	private void makeLayout() {
		VerticalLayout vLayout = new VerticalLayout();
		this.setContent(vLayout);
		Label authProc = new Label("<b>Facebook Authentication Guide</b>", ContentMode.HTML);
		Label first = new Label("1. Login your Facebook");
		Label second = new Label("2. Retrieve your Facebook token at \n");
		Link authLink = new Link("Facebook", new ExternalResource("https://developers.facebook.com/tools/explorer"));
		authLink.setTargetName("_blank");
		Label third = new Label("3. Submit your Facebook token for GenSpace");
		vLayout.addComponent(authProc);
		vLayout.addComponent(first);
		vLayout.addComponent(second);
		vLayout.addComponent(authLink);
		vLayout.addComponent(third);
		
		HorizontalLayout fbLayout = new HorizontalLayout();
		vLayout.addComponent(fbLayout);
		
		Label tokenLabel = new Label("Facebook Token");
		final TextField tokenField = new TextField();
		Label emptyLabel = new Label();
		emptyLabel.setWidth("10px");
		fbLayout.addComponent(tokenLabel);
		fbLayout.addComponent(emptyLabel);
		fbLayout.addComponent(tokenField);
		
		fbLayout.setComponentAlignment(tokenLabel, Alignment.BOTTOM_CENTER);
		fbLayout.setComponentAlignment(tokenField, Alignment.BOTTOM_CENTER);
		
		emptyLabel = new Label();
		emptyLabel.setHeight("10px");
		vLayout.addComponent(emptyLabel);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		vLayout.addComponent(buttonLayout);
		Button fbSubmit = new Button("Submit");
		fbSubmit.addClickListener(new Button.ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent evt) {
				System.out.println("Test tokens: " + tokenField.getValue().toString());
				String token = tokenField.getValue().toString();
				
				final FBManager fbManager = new FBManager(token, login);
				if (fbManager.connect()) {
					UI.getCurrent().access(new Runnable(){
						@Override
						public void run(){
							login.setFBManager(fbManager);
							Notification.show("Facebook connection succeeds! Login as " + fbManager.getMe().getUsername());
							fbUserNotebook.setValue("<b>Facebook Account: " + fbManager.getMe().getUsername() + "</b>");
							UI.getCurrent().removeWindow(FBAuthWindow.this);
						}
					});
				} else {
					Notification.show("Facebook connection fails. Please input valid token");
				}
			}
		});
		buttonLayout.addComponent(fbSubmit);
		Embedded fbFig = new Embedded(null, this.gefb);
		fbFig.setHeight(fbSubmit.getHeight(), fbSubmit.getHeightUnits());
		buttonLayout.addComponent(fbFig);
		buttonLayout.setComponentAlignment(fbSubmit, Alignment.BOTTOM_CENTER);
		buttonLayout.setComponentAlignment(fbFig, Alignment.BOTTOM_CENTER);
	}

}
