package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.Network;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class PrivacyPanel extends SocialPanel{
	
	private GenSpaceLogin login;
	
	private String panelTitle;
	
	private String selectString = "Select who may see your activities";
	
	private String member = "Members of Networks";
	
	private String friends = "Friends";
	
	private String save  = "Save";
	
	private VerticalLayout vLayout;
	
	private HorizontalLayout memberFriendLayout;
	
	private Panel privacyPanel;
	
	private Label selectLabel;
	
	private List<User> friendList;
	
	private List<UserNetwork> networkList;
	
	private Button saveButton;
	
	private ListSelect networkSelect;
	
	private ListSelect friendSelect;
	
	private BeanItemContainer<User> userContainer;
	
	private BeanItemContainer<UserNetworkWrapper> networkContainer;
	
	private BorderLayout bLayout;
	
	public PrivacyPanel(String panelTitle, GenSpaceLogin login) {
		this.login = login;
		
		bLayout = new BorderLayout();
		setCompositionRoot(bLayout);
		this.panelTitle = panelTitle;
		/*this.friendList = this.login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.networkList = this.login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		
		this.privacyPanel = new Panel(this.panelTitle);
		this.privacyPanel.setWidth("800px");
		this.createMainLayout();
		this.privacyPanel.addComponent(this.vLayout);
		this.bLayout.addComponent(privacyPanel, BorderLayout.Constraint.CENTER);*/
		this.updatePanel();
	}
	
	public String getPanelTitle() {
		return this.panelTitle;
	}
	
	public void updatePanel() {
		this.friendList = this.login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.networkList = this.login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		
		if (this.bLayout.getComponentCount() > 0) {
			bLayout.removeAllComponents();
		}
		
		this.privacyPanel = new Panel(this.panelTitle);
		this.privacyPanel.setWidth("600px");
		this.createMainLayout();
		this.privacyPanel.addComponent(this.vLayout);
		this.bLayout.addComponent(privacyPanel, BorderLayout.Constraint.CENTER);
	}
	
	private void createMainLayout() {
		this.vLayout = new VerticalLayout();
		this.privacyPanel.addComponent(vLayout);
		
		this.selectLabel = new Label(selectString);
		vLayout.addComponent(selectLabel);
		
		this.memberFriendLayout = new HorizontalLayout();
		vLayout.addComponent(memberFriendLayout);
		
		this.createNetworkListSelect();
		this.createFriendListSelect();
		
		memberFriendLayout.addComponent(this.networkSelect);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("40px");
		memberFriendLayout.addComponent(emptyLabel);
		memberFriendLayout.addComponent(this.friendSelect);
		
		this.createSaveButton();
		emptyLabel = new Label();
		emptyLabel.setHeight("20px");
		vLayout.addComponent(emptyLabel);
		vLayout.addComponent(this.saveButton);
	}
	
	private void createFriendListSelect() {

		userContainer = new BeanItemContainer<User>(User.class);
		
		Iterator<User> friendIT = this.friendList.iterator();
		User tempUser;
		while(friendIT.hasNext()) {
			tempUser = friendIT.next();
			userContainer.addItem(tempUser);
		}
		
		friendSelect = new ListSelect(friends, userContainer);
		friendSelect.setRows(10);
		friendSelect.setMultiSelect(true);
		friendSelect.setWidth("200px");
		friendSelect.setItemCaptionMode(ListSelect.ITEM_CAPTION_MODE_PROPERTY);
		friendSelect.setItemCaptionPropertyId("username");
	}
	
	private void createNetworkListSelect() {		
		networkContainer = new BeanItemContainer<UserNetworkWrapper>(UserNetworkWrapper.class);
		Iterator<UserNetwork> networkIT = this.networkList.iterator();
		List<Object> visibleIDs = new ArrayList<Object>();
		UserNetwork tempNet;
		UserNetworkWrapper tempWrap;
		Object tempID;
		while(networkIT.hasNext()) {
			tempNet = networkIT.next();
			tempWrap = new UserNetworkWrapper(tempNet);		
			tempID = networkContainer.addItem(tempWrap);
			
			if(tempNet.isVisible()) {
				visibleIDs.add(tempID);
				//System.out.println("Test net visibility in privacy panel: " + tempWrap.getName());
			}
		}
		
		networkSelect = new ListSelect(member, networkContainer);
		networkSelect.setRows(10);
		networkSelect.setMultiSelect(true);
		networkSelect.setWidth("200px");
		networkSelect.setItemCaptionMode(ListSelect.ITEM_CAPTION_MODE_PROPERTY);
		networkSelect.setItemCaptionPropertyId("name");
		
		//setInitialNetworkSelectValue(visibleIDs);
	}
	
	private void setInitialNetworkSelectValue(List<Object> visibleIDs) {
		for(Object id: visibleIDs) {
			System.out.println(networkContainer.getItem(id).getBean().getId());
			System.out.println(networkContainer.getItem(id).getBean().getName());
			networkSelect.setValue(id);
		}
	}
	
	private void createSaveButton(){
		this.saveButton = new Button(this.save);
		this.saveButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				//resetVisibility();
				Iterator nSelect = networkSelect.getItemIds().iterator();
				Object nSelected;
				while(nSelect.hasNext()) {
					nSelected = nSelect.next();
					if(networkSelect.isSelected(nSelected)) {
						System.out.println("Select: " + networkContainer.getItem(nSelected).getBean().getId());
						System.out.println("Select: " + networkContainer.getItem(nSelected).getBean().getName());
						login.getGenSpaceServerFactory().getNetworkOps().updateNetworkVisibility(networkContainer.getItem(nSelected).getBean().getId(), true);
					} else {
						System.out.println("Not Select: " + networkContainer.getItem(nSelected).getBean().getId());
						System.out.println("Not Select: " + networkContainer.getItem(nSelected).getBean().getName());
						login.getGenSpaceServerFactory().getNetworkOps().updateNetworkVisibility(networkContainer.getItem(nSelected).getBean().getId(), false);
					}
				}
				
				Iterator fSelect = friendSelect.getItemIds().iterator();
				Object fSelected;
				while(fSelect.hasNext()) {
					fSelected = fSelect.next();
					if(friendSelect.isSelected(fSelected)) {
						System.out.println("Select: " + userContainer.getItem(fSelected).getBean().getId());
						System.out.println("Select: " + userContainer.getItem(fSelected).getBean().getUsername());
						//System.out.println(GenSpaceServerFactory.getFriendOps().getFriends().get(0).getUsername());
						//GenSpaceServerFactory.getFriendOps().updateFriendVisibility(userContainer.getItem(fSelected).getBean().getId(), true);
					} else {
						System.out.println("Not Select: " + userContainer.getItem(fSelected).getBean().getId());
						System.out.println("Not Select: " + userContainer.getItem(fSelected).getBean().getUsername());
						//GenSpaceServerFactory.getFriendOps().updateFriendVisibility(userContainer.getItem(fSelected).getBean().getId(), false);
					}
				}
			}
			
			private void resetVisibility() {
				Iterator<UserNetwork> networkIT = networkList.iterator();
				UserNetwork tempNet;
				while(networkIT.hasNext()) {
					tempNet = networkIT.next();
					tempNet.setVisible(false);
				}
				
				Iterator<User> userIT = friendList.iterator();
				User user;
				while(userIT.hasNext()) {
					user = userIT.next();
					user.setVisible(false);
				}
			}
		});
	}
	
	private String userName(User user) {
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		String userName = user.getUsername();
		String finalName;
		
		if (firstName.isEmpty() && lastName.isEmpty()) {
			finalName = userName;
		} else {
			finalName = firstName + " " + lastName + " " + "(" +userName + ")";
		}
		return finalName;
	}

}
