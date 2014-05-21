package org.geworkbenchweb.genspace.ui.component;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.geworkbench.components.genspace.server.stubs.Network;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.genspace.chat.ChatReceiver;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.ui.chat.RosterFrame;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class NetworkPanel extends SocialPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GenSpaceLogin_1 login;
	
	private String title;
	
	private Panel networkPanel;
	
	private HorizontalLayout mainLayout;
	
	private VerticalLayout nwPresentationLayout;
	
	private VerticalLayout selectionLayout;
	
	private String selectTitle = "Join/Leave a network";
	
	private String go = "Go";
	
	private String leaveSNet = "Leave selected network";
	
	private String createTitle = "Create a network";
	
	private String networkLabelName = "Moderated by ";
	
	private Select networkSelect = null;
	
	private Network selectedNet = null;
	
	private TextField createNet;
	
	private String createNetString;
	
	private BeanItemContainer<UserNetwork> usrNetContainer = new BeanItemContainer<UserNetwork>(UserNetwork.class);
	
	private BeanItemContainer<Network> container = new BeanItemContainer<Network>(Network.class);
		
	private List<UserNetwork> cachedMyNetWorks;
	
	private List<Network> cachedAllNetWorks;
	
	private BorderLayout blLayout;
	
	private ICEPush pusher = new ICEPush();
	
	private RosterFrame rf;
	
	private ChatReceiver cr;
	
	private SocialNetworkHome parent;
	
	
	public NetworkPanel(String panelTitle, GenSpaceLogin_1 login) {
		this.login = login;
		
		this.blLayout = new BorderLayout();
		this.setCompositionRoot(blLayout);
		this.title = panelTitle;
		this.updatePanel();
	}
	
	public NetworkPanel(String panelTitle, GenSpaceLogin_1 login, SocialNetworkHome parent) {
		this(panelTitle, login);
		this.parent = parent;
	}
	
	public void setRf(RosterFrame rf){
		this.rf = rf;
	}
	
	public void setCr(ChatReceiver cr){
		this.cr = cr;
	}
	
	public String getPanelTitle() {
		return this.title;
	}
	
	public void updatePanel() {
		if (blLayout.getComponentCount() > 0) {
			blLayout.removeAllComponents();
		}
		
		this.networkPanel = new Panel(this.title);
		this.networkPanel.setHeight("1000px");
		this.cachedMyNetWorks = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		this.cachedAllNetWorks = login.getGenSpaceServerFactory().getNetworkOps().getAllNetworks();
		this.createMainLayout();
		this.networkPanel.addComponent(mainLayout);
		this.blLayout.addComponent(networkPanel, BorderLayout.Constraint.CENTER);
	}
	
	private void createMainLayout() {		
		mainLayout = new HorizontalLayout();
		mainLayout.addComponent(pusher);		
		nwPresentationLayout = new VerticalLayout();
				
		if(this.usrNetContainer.size() > 0)
			this.usrNetContainer.removeAllItems();
		
		if(this.container.size() > 0)
			this.container.removeAllItems();
		
		Iterator<UserNetwork> unIT = this.cachedMyNetWorks.iterator();
		UserNetwork tmpNet;
		while(unIT.hasNext()) {
			tmpNet = unIT.next();
			this.usrNetContainer.addBean(tmpNet);
			addNetwork(tmpNet, new UserWrapper(tmpNet.getNetwork().getOwner(), this.login).getFullName());
		}
		
		Iterator<Network> nIT = this.cachedAllNetWorks.iterator();
		while(nIT.hasNext()) {
			this.container.addBean(nIT.next());
		}
		
		mainLayout.addComponent(nwPresentationLayout);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("40px");
		mainLayout.addComponent(emptyLabel);
		
		selectionLayout = new VerticalLayout();
		this.makeActionItems();
		mainLayout.addComponent(selectionLayout);
	}
	
	private void addNetwork(final UserNetwork uNet, String usrName) {
		String netName = uNet.getNetwork().getName();
		if (netName == null || netName.isEmpty() || usrName == null || usrName.isEmpty()) {
			return ;
		}
		Panel newPanel = new Panel(netName);
		newPanel.setWidth("200px");
		Label moderationInfo = new Label(this.networkLabelName + usrName);
		newPanel.addComponent(moderationInfo);
		newPanel.addListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			String nupTitle = "Users in network ";
			
			@Override
			public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
				// TODO Auto-generated method stub
				if (event.isDoubleClick()) {
					nupTitle = nupTitle + event.getComponent().getCaption();
					NetUserPanel nup = new NetUserPanel(nupTitle, login, uNet.getNetwork());
					nwPresentationLayout.removeAllComponents();
					nwPresentationLayout.addComponent(nup);
					NetworkPanel.this.parent.backTo = SocialNetworkHome.BACK_TO_MYNET;
				}
			}
			
		});
		
		this.nwPresentationLayout.addComponent(newPanel);
		this.cachedMyNetWorks = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
	}
	
	private void elimNetwork(String networkName) {
		if (networkName == null) {
			return ;
		}
		
		Iterator<Component> networkPanelIT = this.nwPresentationLayout.getComponentIterator();
		Component tempPanel = null;
		while(networkPanelIT.hasNext()) {
			tempPanel = networkPanelIT.next();
			if (tempPanel.getCaption().equals(networkName)) {
				this.nwPresentationLayout.removeComponent(tempPanel);
				this.cachedMyNetWorks = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
				break;
			}
		}
	}
	
	private void makeActionItems() {
		if(networkSelect != null)
			networkSelect = null;
		
		networkSelect = new Select(this.selectTitle, this.container);
		networkSelect.setImmediate(true);
		networkSelect.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
		networkSelect.setItemCaptionPropertyId("name");
		networkSelect.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent event){
				if(event.getProperty().getValue()==null){
					return;
				}
				selectedNet = (Network)event.getProperty().getValue();
				//System.out.println("ValueChange: " + selectedNet.getName() + " " + selectedNet);
			}
		});
		
		Button goButton = new Button(this.go);
		goButton.setWidth("150px");
		goButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event){
					if (selectedNet != null && selectedNet.getName() != null && !selectedNet.getName().isEmpty()){
						if (isCachedMyNetWorks() == null) {						
							NetConfirmWindow ncw = new NetConfirmWindow(selectedNet.getName());
							getApplication().getMainWindow().addWindow(ncw);
							
							//Should wait for administrator's approval?
							login.getGenSpaceServerFactory().getNetworkOps().joinNetwork(selectedNet.getName());
							//addNetwork(selectedNet.getName(), new UserWrapper(selectedNet.getOwner(), login).getFullName());
							updatePanel();
//							if (rf == null) {
//								rf = NetworkPanel.this.parent.getRf();
//							}
//							rf.refresh();
							

							//createMainLayout();
						}
					}else{
						
					}
					pusher.push();
			}
		});
		
		Button leaveButton = new Button(this.leaveSNet);
		leaveButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
						
			public void buttonClick(ClickEvent event){
				//System.out.println("DEBUG : leave " + selectedNet.getName());
				if (selectedNet != null && selectedNet.getName() != null && !selectedNet.getName().isEmpty()) {
					//System.out.println("selected network captured by the leave button: " + selectedNet.getName());
					UserNetwork un = isCachedMyNetWorks();
					if(un != null) {
						//System.out.println("elim cachedMyNetWorks: " + un.getNetwork().getName() + " " + un.getId());
						
						login.getGenSpaceServerFactory().getNetworkOps().leaveNetwork(un.getId());
						//elimNetwork(selectedNet.getName());
						updatePanel();
					
						//System.out.println(rf.getCaption());
//						if (rf == null) {
//							rf = NetworkPanel.this.parent.getRf();
//						}
//						rf.refresh();
						
						FriendStatusChangeEvent e = new FriendStatusChangeEvent(FriendStatusChangeEvent.NETWORK_EVENT, 
																			FriendStatusChangeEvent.NETWORK_EVENT);
						e.setOptType(FriendStatusChangeEvent.RM_FRIEND);						
						GenSpaceWindow.getGenSpaceBlackboard().fire(e);
						
						//cr.updateRoster();
						//cr.createRosterFrame();
					}
				}else{
					
				}
				pusher.push();
			}
		});
		leaveButton.setWidth("150px");
		this.selectionLayout.setSpacing(true);
		this.selectionLayout.addComponent(networkSelect);
		this.selectionLayout.addComponent(goButton);
		this.selectionLayout.addComponent(leaveButton);
		
		Label emptyLabel = new Label();
		emptyLabel.setHeight("20px");
		this.selectionLayout.addComponent(emptyLabel);
		
		this.createNet = new TextField(this.createNetString);
		createNet.setWidth("158px");
		Button createButton = new Button(this.createTitle);
		createButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				String textValue = createNet.getValue().toString();
				if (textValue != null && !textValue.isEmpty()) {
					login.getGenSpaceServerFactory().getNetworkOps().createNetwork(textValue);
					getApplication().getMainWindow().showNotification("This network has been created");
					updatePanel();
				}
			}
		});
		this.selectionLayout.addComponent(createNet);
		this.selectionLayout.addComponent(createButton);
	}
	
	private void addSelectionItem(Network select) {
		networkSelect.addItem(select);
		networkSelect.setItemCaption(select, select.getName());
	}
	
	public void attachPusher() {
		this.addComponent(this.pusher);
	}
	
	private UserNetwork isCachedMyNetWorks() {
		if(this.selectedNet == null)
			return null;
		
		for(UserNetwork un: cachedMyNetWorks) {
			if(un.getNetwork().getName().equals(selectedNet.getName()))
				return un;
		}
		return null;
	}

}
