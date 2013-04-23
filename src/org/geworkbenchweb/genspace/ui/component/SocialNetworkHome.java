package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;
import org.geworkbench.components.genspace.server.stubs.Network;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.genspace.chat.ChatReceiver;
import org.geworkbenchweb.plugins.Analysis;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Roster;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class SocialNetworkHome extends AbstractGenspaceTab implements GenSpaceTab{
	
	private AbsoluteLayout mainLayout;
	
	private VerticalLayout sbcLayout;
	
	private HorizontalLayout searchLayout;
	
	private HorizontalLayout bcLayout;
	
	private VerticalLayout buttonLayout;
	
	private VerticalLayout contentLayout;
	
	private String GenSpace = "GenSpace";
	
	private String Search = "Search";
	
	private String Go = "Go";
	
	private String title = "My Profile";
	
	private String myNet = "My Networks";
	
	private String myFriends = "My Friends";
	
	private String chat = "Chat";
	
	private String settings = "Settings";
	
	private String vRequests = "View Requests";
	
	private String pRequests = "Pending Requests";
	
	private String findNoUserMsg = "Error: Could not find xxx's profile";
	
	private String back = "Back";
	
	private int pPanelIdx;
	
	private List<User> friendList;
	
	private List<UserNetwork> uNetworkList;
	
	
	private ComboBox search;
	
	private SocialPanel current;
	
	private SocialPanel proPanel;
	
	private SocialPanel netPanel;
	
	private SocialPanel friendPanel;
	
	private SocialPanel privacyPanel;
	
	private SocialPanel viewPanel;
	
	private ChatReceiver chatHandler;

	private Stack<SocialPanel> last;
	
	private SocialNetworkHome instance;
	
	private Refresher refresher;
	
	{
		init();
	}
	
	/*public SocialNetworkHome(){
		this.last = new Stack<SocialPanel>();
		makeLayout();
		setCompositionRoot(this.mainLayout);
	}*/
	
	public SocialNetworkHome(GenSpaceLogin login)
	{
		super(login);
	}
	
	private void init() {
		instance = this;
		this.last = new Stack<SocialPanel>();
		makeLayout();
		setCompositionRoot(this.mainLayout);
	}
	
	public SocialNetworkHome getInstance() {
		return instance;
	}
	
	private void createDefaultPanel() {
		ProfilePanel pPanel = new ProfilePanel(title, login);
		contentLayout.removeAllComponents();
		contentLayout.addComponent(pPanel);
		last.push(pPanel);
	}
	
	private void makeLayout() {
		mainLayout = new AbsoluteLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		
		setWidth("100.0%");
		setHeight("100.0%");
		
		sbcLayout = new VerticalLayout();
		
		this.makeSearchLayout();
		this.makeContentLayout();
		this.makeButtonLayout();
		
		bcLayout = new HorizontalLayout();
		bcLayout.addComponent(buttonLayout);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("40px");
		bcLayout.addComponent(emptyLabel);
		bcLayout.addComponent(contentLayout);
		
		sbcLayout.addComponent(searchLayout);
		emptyLabel = new Label();
		sbcLayout.addComponent(emptyLabel);
		sbcLayout.addComponent(bcLayout);
		
		mainLayout.addComponent(sbcLayout);
		
		/*refresher = new Refresher();
		refresher.setRefreshInterval(100);*/
		//refresher.setRefreshInterval(5 * 1000);
		//mainLayout.addComponent(refresher);
	}
	
	private void makeSearchLayout() {
		searchLayout = new HorizontalLayout();
		Label genSpaceLabel = new Label("<b>" + this.GenSpace + "</b>", Label.CONTENT_XHTML);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("400px");
		search = new ComboBox(this.Search);
		search.setTextInputAllowed(true);
		search.setNewItemsAllowed(true);
		search.setInputPrompt("Please select or input username for search");

		Button go = new Button(this.Go);
		go.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				Object comboObject = search.getValue();
				User f;
				if (comboObject instanceof User) {
					f = (User) comboObject;
				} else {
					String comboValue = comboObject.toString();
					//f = GenSpaceServerFactory.getUserOps().getProfile(comboValue);
					f = login.getGenSpaceServerFactory().getUserOps().getProfile(comboValue);
					
					if (f == null) {
						String errorMsg = findNoUserMsg.replace("xxx", comboValue);
						getApplication().getMainWindow().showNotification(errorMsg);
						return ;
					}
				}
				
				UserSearchWindow usw = new UserSearchWindow(f, login, SocialNetworkHome.this);
				getApplication().getMainWindow().addWindow(usw);
				System.out.println("Got user: " + f.getUsername());

				String comboValue = search.getValue().toString();
				System.out.println("Test go button: " + comboValue);
			}
		});
		
		searchLayout.addComponent(genSpaceLabel);
		searchLayout.addComponent(emptyLabel);
		searchLayout.addComponent(search);
		searchLayout.addComponent(go);
	}
	
	private void makeButtonLayout() {
		this.buttonLayout = new VerticalLayout();
		Button profile = new Button(this.title);
		profile.addListener(new Button.ClickListener(){
			
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				//createDefaultPanel();
				/*ProfilePanel pPanel = new ProfilePanel(title);
				contentLayout.removeAllComponents();
				contentLayout.addComponent(pPanel);*/
				
				/*ProfilePanel pPanel = new ProfilePanel(title, login);
				setContent(pPanel);*/
				proPanel.updatePanel();
				setContent(proPanel);
			}
		});
		profile.setWidth("100px");
		
		Button network = new Button(this.myNet);
		network.addListener(new Button.ClickListener(){

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				/*NetworkPanel nPanel = new NetworkPanel(myNet, login);
				setContent(nPanel);*/
				netPanel.updatePanel();
				setContent(netPanel);
			}
		});
		network.setWidth("100px");
		
		Button friend = new Button(this.myFriends);
		friend.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					/*List<User> friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
					FriendPanel fPanel = new FriendPanel(myFriends, friendList);
					setContent(fPanel);*/
					friendPanel.updatePanel();
					setContent(friendPanel);
				}
		});
		friend.setWidth("100px");
		
		Button chat = new Button(this.chat);
		chat.setWidth("100px");
		chat.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent e) {
				/*Iterator<Window> windowIT = getApplication().getMainWindow().getChildWindows().iterator();
				RosterFrame buddy = null;
				Window temp;
				while(windowIT.hasNext()) {
					temp = windowIT.next();
					if(temp.getCaption().equals("Buddies")) {
						buddy = (RosterFrame)temp;
					}
				}
				
				if (buddy != null) {
					buddy.setVisible(true);
					buddy.focus();
				} else {
					System.out.println("Buddy window is gone");
					System.out.println("Test username: " + login.getGenSpaceServerFactory().getUsername());
					chatHandler = login.getChatHandler();
					Roster r = chatHandler.getConnection().getRoster();
					r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

					chatHandler.rf.setRoster(r);
					chatHandler.rf.setVisible(true);
					chatHandler.rf.focus();

					getApplication().getMainWindow().addWindow(chatHandler.rf);
				}*/
				System.out.println("In the chat button");
				searchRosterFrame();
			}
		});
		
		Button bSettings = new Button(this.settings);
		bSettings.setWidth("100px");
		bSettings.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					/*List<User> friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
					List<UserNetwork> uNetworkList = login.getGenSpaceServerFactory().getUserOps().getMyNetworks();
					PrivacyPanel pPanel = new PrivacyPanel(settings, friendList, uNetworkList, login);
					setContent(pPanel);*/
					privacyPanel.updatePanel();
					setContent(privacyPanel);
				}			
		});
		
		Button vRequest = new Button(this.vRequests);
		vRequest.setWidth("100px");
		vRequest.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				/*RequestPanel rPanel = new RequestPanel(pRequests, login);
				setContent(rPanel);*/
				viewPanel.updatePanel();
				setContent(viewPanel);
			}
		});
		
		Button back = new Button(this.back);
		back.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				
				if(last.isEmpty())
					return ;
				
				SocialPanel sp = last.pop();
				System.out.println(sp.getPanelTitle());
				
				setRealContent(sp);
			}
		});
		back.setWidth("100px");
		
		buttonLayout.addComponent(profile);
		buttonLayout.addComponent(network);
		buttonLayout.addComponent(friend);
		buttonLayout.addComponent(chat);
		buttonLayout.addComponent(bSettings);
		buttonLayout.addComponent(vRequest);
		buttonLayout.addComponent(back);
	}
	
	private void makeContentLayout() {
		this.contentLayout = new VerticalLayout();
		proPanel = new ProfilePanel(title, login);
		this.setRealContent(proPanel);

		this.pPanelIdx = contentLayout.getComponentIndex(proPanel);	
	}
	
	private void setContent(SocialPanel sp) {
		if(sp != null) {
			last.push(current);
			this.setRealContent(sp);
		}
	}
	
	private void setRealContent(SocialPanel sp) {
		current = sp;
		if(sp.getPanelTitle().equals(title)) {
			ProfilePanel pp = (ProfilePanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(pp);
		} else if(sp.getPanelTitle().equals(myNet)) {
			NetworkPanel np = (NetworkPanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(np);
		} else if(sp.getPanelTitle().equals(myFriends)) {
			FriendPanel fp = (FriendPanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(fp);
		} else if(sp.getPanelTitle().equals(settings)) {
			PrivacyPanel pr = (PrivacyPanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(pr);
		} else if(sp.getPanelTitle().equals(pRequests)) {
			RequestPanel rp = (RequestPanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(rp);
		}	
	}
	
	public boolean pendingFriendRequestTo(User u) {
		if(u.isFriendsWith())
			return false;
		for(User tmpU : friendList)
		{
			if(tmpU.getUsername().equals(u.getUsername()))
				return true;
		}
		return false;
	}
	
	private void searchRosterFrame() {
		if (this.chatHandler.rf == null) {
			this.chatHandler = this.login.getChatHandler();
			this.chatHandler.updateRoster();
			this.chatHandler.createRosterFrame();
			System.out.println("Create a new Roster frame");
			getApplication().getMainWindow().addWindow(chatHandler.rf);
		} else if (!this.chatHandler.rf.isVisible()) {
			this.chatHandler.rf.setVisible(true);
			this.chatHandler.rf.focus();
			System.out.println("Roster frame from invisible to visible");
		} else if (!getApplication().getMainWindow().getChildWindows().contains(chatHandler.rf)){      
			System.out.println("RosterFrame visible: " + this.chatHandler.rf.isVisible());
			System.out.println("RosterFrame closable: " + this.chatHandler.rf.isClosable());
			getApplication().getMainWindow().addWindow(chatHandler.rf);
		}
	}

	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub
		ProfilePanel tmp = (ProfilePanel)contentLayout.getComponent(this.pPanelIdx);
		tmp.createProfileForm();
	}

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		System.out.println("Log in: " + login.getGenSpaceServerFactory().getUsername());
		
		/*this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		this.friendPanel = new FriendPanel(this.myFriends, this.friendList);
		this.netPanel = new NetworkPanel(this.myNet, this.login);
		this.privacyPanel = new PrivacyPanel(this.settings, this.friendList, this.uNetworkList, this.login);
		this.viewPanel = new RequestPanel(this.pRequests, this.login);
		
		this.loadSearchItems();*/
		this.initForm();
		this.chatHandler = this.login.getChatHandler();
	}

	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub
		System.out.println("Log out: " + login.getGenSpaceServerFactory().getUsername());
		this.last.clear();
		this.clearSettings();
	}
	
	private void clearSettings() {
		this.friendList = null;
		this.uNetworkList = null;
		this.friendPanel = null;
		this.netPanel = null;
		this.privacyPanel = null;
		this.viewPanel = null;
		this.chatHandler.getConnection().disconnect();
		this.chatHandler.rf.cleanSettings();
	}
	
	public void loadSearchItems() {
		search.removeAllItems();
		
		if (!friendList.isEmpty() && friendList != null) {
			Iterator<User> friendIT = friendList.iterator();
			User f;
			while (friendIT.hasNext()) {
				f = friendIT.next();
				this.search.addItem(f);
				this.search.setItemCaption(f, f.getUsername());
			}
		}
	}
	
	public void initForm() {
		System.out.println("Is loggedin: " + login.getGenSpaceServerFactory().isLoggedIn());
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
			this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
			//this.evtList = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEvents("2012-06-01");
			this.friendPanel = new FriendPanel(this.myFriends, this.login);
			this.netPanel = new NetworkPanel(this.myNet, this.login);
			this.privacyPanel = new PrivacyPanel(this.settings, this.login);
			this.viewPanel = new RequestPanel(this.pRequests, this.login);
			this.loadSearchItems();
			//this.makeGentifyLayout();
		}
	}
	
	public void updateForm() {
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		//this.evtList = login.getGenSpaceServerFactory().getFriendOps().getMyFriendsEvents("2012-06-01");
		
		this.friendPanel.updatePanel();
		this.netPanel.updatePanel();
		this.privacyPanel.updatePanel();
		this.viewPanel.updatePanel();
		this.chatHandler.rf.refresh();
		this.loadSearchItems();
		//this.makeGentifyLayout();
	}

}