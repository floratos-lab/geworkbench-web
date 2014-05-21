package org.geworkbenchweb.genspace.ui.component;

import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.User;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;

public class FriendPanel extends SocialPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String panelTitle;
	
	private List<User> friendList;
	
	private Panel friendPanel;
	
	private VerticalLayout mainLayout;
	
	private BorderLayout blLayout;
			
	private GenSpaceLogin_1 login;
	
	public FriendPanel(String panelTitle, GenSpaceLogin_1 login2) {
		this.panelTitle = panelTitle;
		this.login = login2;
		
		this.blLayout = new BorderLayout();
		this.setCompositionRoot(blLayout);
		this.friendPanel = new Panel(this.panelTitle);
		this.friendPanel.setWidth("430px");
		this.friendList = login2.getGenSpaceServerFactory().getFriendOps().getFriends();
		
		this.createMainLayout();
		this.friendPanel.addComponent(mainLayout);
		blLayout.addComponent(friendPanel, BorderLayout.Constraint.CENTER);
	}
	
	public String getPanelTitle() {
		return this.panelTitle;
	}
	
	public void updatePanel() {
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		if (this.blLayout.getComponentCount() > 0) {
			this.blLayout.removeAllComponents();
		}
		
		if (this.friendPanel.getComponentIterator().hasNext()) {
			this.friendPanel.removeAllComponents();
		}
		
		this.createMainLayout();
		this.friendPanel.addComponent(mainLayout);
		this.blLayout.addComponent(friendPanel, BorderLayout.Constraint.CENTER);
	}
	
	public void createMainLayout() {
		this.mainLayout = new VerticalLayout();
		Panel tempPanel;
		User tempUser;
		Label tempAffLabel;
		String tempPanelTitle;
		String tempAffiliation;
		Iterator<User> friendIT = this.friendList.iterator();
		while(friendIT.hasNext()) {
			tempUser = friendIT.next();
			final User forListener = tempUser;
			if (tempUser.getFirstName().isEmpty() || tempUser.getLastName().isEmpty()) {
				tempPanelTitle = tempUser.getUsername();
			} else {
				tempPanelTitle = tempUser.getFirstName() + " " + tempUser.getLastName() + " " +  "(" + tempUser.getUsername() +")";
			}
			tempAffiliation = tempUser.getLabAffiliation();
			tempPanel = new Panel(tempPanelTitle);
			tempPanel.addStyleName(Runo.PANEL_LIGHT);
			tempPanel.setWidth("200px");
			tempPanel.addListener(new ClickListener(){

				private static final long serialVersionUID = 1L;
				
				private String userPanelTitle;

				public void click(ClickEvent event) {
					if (event.isDoubleClick()) {
						userPanelTitle = event.getComponent().getCaption() + "'s genSpace profile";
						
						UserPanel userPanel = new UserPanel(userPanelTitle, forListener);
						userPanel.addStyleName(Runo.PANEL_LIGHT);
						mainLayout.removeAllComponents();
						mainLayout.addComponent(userPanel);
					}
				}				
			});
			tempAffLabel = new Label(tempAffiliation);
			tempPanel.addComponent(tempAffLabel);
			mainLayout.addComponent(tempPanel);
		}
	}
	
	public void attachPusher() {
		
	}

}
