package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;

public class PrivacyPanel extends SocialPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GenSpaceLogin_1 login;
	
	private String panelTitle;
	
	private String selectString = "Select who may see your activities";
	
	private String member = "Members of Networks";
	
	private String friends = "Friends";
	
	private String save  = "Save";
	
	private VerticalLayout vLayout;
	
	private VerticalLayout memberFriendLayout;
	
	private Panel privacyPanel;
	
	private Label selectLabel;
	
	private List<User> friendList;
	
	private List<UserNetwork> networkList;
	
	private Button saveButton;
		
	private TwinColSelect networkSelect;
	
	private TwinColSelect friendSelect;
	
	private BeanItemContainer<UserWrapper> userContainer;
	
	private BeanItemContainer<UserNetworkWrapper> networkContainer;
	
	private BorderLayout bLayout;
	
	private ICEPush pusher = new ICEPush();
	
	private HashMap<Object, Object> idMap = new HashMap<Object, Object>();
	
	public PrivacyPanel(String panelTitle, GenSpaceLogin_1 login) {
		this.login = login;
		
		bLayout = new BorderLayout();
		setCompositionRoot(bLayout);
		this.panelTitle = panelTitle;
		//System.out.println("1new parivacy panel!!");

		this.updatePanel();
		//System.out.println("new parivacy panel!!");

	}
	
	public String getPanelTitle() {
		return this.panelTitle;
	}
	
	public void updatePanel() {
		this.friendList = this.login.getGenSpaceServerFactory().getFriendOps().getFriends();
		//this.friendList = this.login.getGenSpaceServerFactory().getFriendOps().getFriendsOnMe();
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
		
		this.memberFriendLayout = new VerticalLayout();
		vLayout.addComponent(memberFriendLayout);
		
		this.createNetworkListSelect();
		this.createFriendListSelect();
		
		memberFriendLayout.addComponent(this.networkSelect);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("40px");
		memberFriendLayout.addComponent(emptyLabel);
		memberFriendLayout.addComponent(this.friendSelect);
		
		this.createVisibleButton();
		emptyLabel = new Label();
		emptyLabel.setHeight("20px");
		vLayout.addComponent(emptyLabel);
		vLayout.addComponent(this.saveButton);
		vLayout.addComponent(pusher);
	}
	
	private void createFriendListSelect() {
		
		userContainer = new BeanItemContainer<UserWrapper>(UserWrapper.class);
		Iterator<User> friendIT = this.friendList.iterator();
		List<BeanItem<UserWrapper>> invisibleIDs = new ArrayList<BeanItem<UserWrapper>>();
		User tempUser;
		UserWrapper tmpWrapper;
		BeanItem<UserWrapper> tempID;
	
		while(friendIT.hasNext()) {
			tempUser = friendIT.next(); // tempUser.isvisible();
			
			tmpWrapper = new UserWrapper(tempUser, login);
			tempID = userContainer.addItem(tmpWrapper);
			
			//System.out.println("Check friend visible: " + tempUser.isVisible());
			//System.out.println("Check friend mutual: ");
			
			if (!tempUser.isVisible()) {
				invisibleIDs.add(tempID);
				//System.out.println("^^ friend who cannot see: "+tempUser);
			}
			
		}
		
		friendSelect = new TwinColSelect(friends, userContainer);
		HashSet<UserWrapper> invSet = new HashSet<UserWrapper>();
		for(int i=0; i<invisibleIDs.size(); i++){
			//System.out.println("***"+invisibleIDs.get(i));
			invSet.add(invisibleIDs.get(i).getBean());
		}
		friendSelect.setValue(invSet);
		friendSelect.setLeftColumnCaption("Friend who can see");
		
		friendSelect.setRightColumnCaption("Friend who cannot see");
		//friendSelect = new TwinColSelect(friends, userContainer);
		//friendSelect.setRows(userContainer.size());
		friendSelect.setMultiSelect(true);
		friendSelect.setWidth("400px");
		friendSelect.setItemCaptionMode(ListSelect.ITEM_CAPTION_MODE_PROPERTY);
		friendSelect.setItemCaptionPropertyId("username");

	}
	
	private void createNetworkListSelect() {		
		networkContainer = new BeanItemContainer<UserNetworkWrapper>(UserNetworkWrapper.class);
		Iterator<UserNetwork> networkIT = this.networkList.iterator();
		List<BeanItem<UserNetworkWrapper>> invisibleIDs = new ArrayList<BeanItem<UserNetworkWrapper>>();
		UserNetwork tempNet;
		UserNetworkWrapper tempWrap;
		BeanItem<UserNetworkWrapper> tempID;
		while(networkIT.hasNext()) {
			tempNet = networkIT.next();
			tempWrap = new UserNetworkWrapper(tempNet);		
			tempID = networkContainer.addItem(tempWrap);
			
			if(!tempNet.isVisible()) {
				invisibleIDs.add(tempID);
				//System.out.println("^^ network who cannot see: " + tempWrap.getName());
			}
		}
		
		networkSelect = new TwinColSelect(member, networkContainer);
		HashSet<UserNetworkWrapper> invSet = new HashSet<UserNetworkWrapper>();
		for(int i=0; i<invisibleIDs.size(); i++){
			//System.out.println("^^^"+invisibleIDs.get(i));
			invSet.add(invisibleIDs.get(i).getBean());
			//networkSelect.select(invisibleIDs.get(i));
		}
		networkSelect.setValue(invSet);
		networkSelect.setLeftColumnCaption("Network that can see");
		networkSelect.setRightColumnCaption("Network that cannot see");
		networkSelect.setMultiSelect(true);
		networkSelect.setWidth("400px");
		networkSelect.setItemCaptionMode(ListSelect.ITEM_CAPTION_MODE_PROPERTY);
		networkSelect.setItemCaptionPropertyId("name");
		
		//setInitialNetworkSelectValue(visibleIDs);
	}
	
	private void setInitialNetworkSelectValue(List<Object> visibleIDs) {
		for(Object id: visibleIDs) {
			//System.out.println("^^"+networkContainer.getItem(id).getBean().getId());
			//System.out.println("**"+networkContainer.getItem(id).getBean().getName());
			networkSelect.setValue(id);
		}
	}
	
	private void createVisibleButton(){
		this.saveButton = new Button(save);
		this.saveButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				//resetVisibility();
				Iterator<UserNetworkWrapper> nSelect = networkContainer.getItemIds().iterator();
				UserNetworkWrapper nSelected;
				while(nSelect.hasNext()) {
					nSelected = nSelect.next();
					if(networkContainer.getItem(nSelected)==null){
						//System.out.println("empty: "+nSelected);
					}else if(networkSelect.isSelected(nSelected)) {
						//System.out.println("Network: false " + networkContainer.getItem(nSelected).getBean().getName());
						login.getGenSpaceServerFactory().getNetworkOps().updateNetworkVisibility(networkContainer.getItem(nSelected).getBean().getId(), false);
					} else {
						//System.out.println("Network: true  " + networkContainer.getItem(nSelected).getBean().getName());
						login.getGenSpaceServerFactory().getNetworkOps().updateNetworkVisibility(networkContainer.getItem(nSelected).getBean().getId(), true);
					}
				}
				createNetworkListSelect();
				Iterator fSelect = friendSelect.getItemIds().iterator();
				
				Object fSelected;
				while(fSelect.hasNext()) {
					fSelected = fSelect.next();
					if(friendSelect.isSelected(fSelected)) {
						//System.out.println("friend: false " + userContainer.getItem(fSelected).getBean().getUsername());
						login.getGenSpaceServerFactory().getFriendOps().updateFriendVisibility(userContainer.getItem(fSelected).getBean().getId(), false);
						//System.out.println(userContainer.getItem(fSelected).getBean().isVisible());
						friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
						createFriendListSelect();
						//System.out.println(userContainer.getItem(fSelected).getBean().isVisible());

					}else{
						//System.out.println("frined: true "+ userContainer.getItem(fSelected).getBean().getUsername());
						login.getGenSpaceServerFactory().getFriendOps().updateFriendVisibility(userContainer.getItem(fSelected).getBean().getId(), true);
						//System.out.println(userContainer.getItem(fSelected).getBean().isVisible());

						friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
						createFriendListSelect();
						//System.out.println(userContainer.getItem(fSelected).getBean().isVisible());

					}
				}
				//createFriendListSelect();
				pusher.push();
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
	
	public void attachPusher() {
		
	}

}
