package org.geworkbenchweb.genspace.ui.component;

import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.utils.LayoutUtil;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

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
			
	private GenSpaceLogin login;
	
	public FriendPanel(String panelTitle, GenSpaceLogin login) {
		this.panelTitle = panelTitle;
		this.login = login;
		
		this.blLayout = new BorderLayout();
		this.setCompositionRoot(blLayout);
		this.friendPanel = new Panel(this.panelTitle);
		this.friendPanel.setWidth("500px");
		//this.friendPanel.setHeight("1000px");
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		
		this.createMainLayout();
		this.friendPanel.setContent(mainLayout);
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
		
		if (this.friendPanel.iterator().hasNext()) {
			this.friendPanel.setContent(null);
		}
		
		this.createMainLayout();
		this.friendPanel.setContent(mainLayout);
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
			tempPanel.setWidth("200px");
			tempPanel.addClickListener(new ClickListener(){

				private static final long serialVersionUID = 1L;
				
				private String userPanelTitle;

				public void click(ClickEvent event) {
					if (event.isDoubleClick()) {
						userPanelTitle = event.getComponent().getCaption() + "'s genSpace profile";
						
						UserPanel userPanel = new UserPanel(userPanelTitle, forListener);
						mainLayout.removeAllComponents();
						mainLayout.addComponent(userPanel);
					}
				}				
			});
			tempAffLabel = new Label(tempAffiliation);
			tempPanel.setContent(LayoutUtil.addComponent(tempAffLabel));
			mainLayout.addComponent(tempPanel);
		}
	}

}
