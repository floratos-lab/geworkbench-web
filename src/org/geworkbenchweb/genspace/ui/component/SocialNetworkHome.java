package org.geworkbenchweb.genspace.ui.component;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.events.FriendStatusChangeEvent.FriendStatusChangeListener;
import org.geworkbenchweb.genspace.chat.ChatReceiver;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SocialNetworkHome extends AbstractGenspaceTab implements GenSpaceTab, FriendStatusChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	
	private Label infoLabel = new Label(
			"Please login to genSpace to access this area.");
	
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
		//mainLayout.setHeight("100%");
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
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}
				
				Object comboObject = search.getValue();
				User f;
				if (comboObject instanceof User) {
					f = (User) comboObject;
				} else if (comboObject != null){
					String comboValue = comboObject.toString();
					f = login.getGenSpaceServerFactory().getUserOps().getProfile(comboValue);
					
					if (f == null) {
						String errorMsg = findNoUserMsg.replace("xxx", comboValue);
						getApplication().getMainWindow().showNotification(errorMsg);
						return ;
					}
				} else {
					return ;
				}
				UserSearchWindow usw = new UserSearchWindow(f, login, SocialNetworkHome.this);
				getApplication().getMainWindow().addWindow(usw);
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
				proPanel.updatePanel();
				setContent(proPanel);
			}
		});
		profile.setWidth("100px");
		
		Button network = new Button(this.myNet);
		network.addListener(new Button.ClickListener(){

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (!login.getGenSpaceServerFactory().isLoggedIn())
					return ;
				
				netPanel.updatePanel();
				setContent(netPanel);
			}
		});
		network.setWidth("100px");
		
		Button friend = new Button(this.myFriends);
		friend.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					if (!login.getGenSpaceServerFactory().isLoggedIn()) {
						return ;
					}
					
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
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}
				
				searchRosterFrame();
				searchAFWindow();
			}
		});
		
		Button bSettings = new Button(this.settings);
		bSettings.setWidth("100px");
		bSettings.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					if (!login.getGenSpaceServerFactory().isLoggedIn()) {
						return ;
					}
					
					privacyPanel.updatePanel();
					setContent(privacyPanel);
				}			
		});
		
		Button vRequest = new Button(this.vRequests);
		vRequest.setWidth("100px");
		vRequest.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}
				
				viewPanel.updatePanel();
				setContent(viewPanel);
			}
		});
		
		Button back = new Button(this.back);
		back.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}
				
				if(last.isEmpty())
					return ;
				
				SocialPanel sp = last.pop();
				//System.out.println(sp.getPanelTitle());
				
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
			System.out.println("Roster frame from invisible to visible");
		} else if (!getApplication().getMainWindow().getChildWindows().contains(chatHandler.rf)){      
			System.out.println("RosterFrame visible: " + this.chatHandler.rf.isVisible());
			getApplication().getMainWindow().addWindow(chatHandler.rf);
		}
		this.chatHandler.rf.focus();
	}
	
	private void searchAFWindow() {
		ActivityFeedWindow tmp = this.login.getAFWindow();
		if (tmp == null) {
			this.login.createAFWindow();
			tmp = this.login.getAFWindow();
			System.out.println("Create a new AFWindow");
		} else if (!tmp.isVisible()) {
			tmp.setVisible(true);
			System.out.println("AFWindow from invisible to visible");
		} else if (!getApplication().getMainWindow().getChildWindows().contains(tmp)){      
			System.out.println("AFWindow visible: " + this.chatHandler.rf.isVisible());
			getApplication().getMainWindow().addWindow(tmp);
		}
		tmp.focus();
	}

	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub
		//ProfilePanel tmp = (ProfilePanel)contentLayout.getComponent(this.pPanelIdx);
		//tmp.createProfileForm();
		ProfilePanel tmp = (ProfilePanel)this.proPanel;
		tmp.createProfileForm();
	}

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		//System.out.println("Log in: " + login.getGenSpaceServerFactory().getUsername());
		this.initForm();
		this.chatHandler = this.login.getChatHandler();
	}

	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub
		//System.out.println("Log out: " + login.getGenSpaceServerFactory().getUsername());
		this.last.clear();
		this.clearSettings();
	}
	
	private void clearSettings() {
		/*this.friendList = null;
		this.uNetworkList = null;
		this.proPanel = null;
		this.friendPanel = null;
		this.netPanel = null;
		this.privacyPanel = null;
		this.viewPanel = null;*/
		this.chatHandler.getConnection().disconnect();
		//this.chatHandler.rf.cleanSettings();
		getApplication().getMainWindow().removeWindow(this.chatHandler.rf);
		this.search.removeAllItems();
		
		this.contentLayout.removeAllComponents();
		this.contentLayout.addComponent(this.proPanel);
		
		//Remove all chat window here
		Iterator<String> chatIT = this.chatHandler.chats.keySet().iterator();
		while(chatIT.hasNext()) {
			getApplication().getMainWindow().removeWindow(this.chatHandler.chats.get(chatIT.next()));
		}
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
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
			this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
			this.friendPanel = new FriendPanel(this.myFriends, this.login);
			this.netPanel = new NetworkPanel(this.myNet, this.login);
			this.privacyPanel = new PrivacyPanel(this.settings, this.login);
			this.viewPanel = new RequestPanel(this.pRequests, this.login);
			this.loadSearchItems();
		}
	}
	
	public void updateForm() {
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		this.proPanel.updatePanel();
		this.friendPanel.updatePanel();
		this.netPanel.updatePanel();
		this.privacyPanel.updatePanel();
		this.viewPanel.updatePanel();
		this.chatHandler.rf.refresh();
		this.loadSearchItems();
	}
	
	@Override
	public void changeFriendStatus(FriendStatusChangeEvent evt) {
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			int myID = login.getGenSpaceServerFactory().getUser().getId();
			
			if (myID == evt.getMyID() || myID == evt.getFriendID()) {
				updateForm();
			}
		}
	}
}
