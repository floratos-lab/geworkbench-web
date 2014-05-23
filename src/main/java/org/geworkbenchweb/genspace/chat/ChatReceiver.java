package org.geworkbenchweb.genspace.chat;

import java.util.HashMap;

import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.ui.chat.ChatWindow;
import org.geworkbenchweb.genspace.ui.chat.MessageTypes;
import org.geworkbenchweb.genspace.ui.chat.RosterFrame;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class ChatReceiver implements MessageListener, ChatManagerListener, Window.CloseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChatManager manager;
	
	public XMPPConnection connection;
	
	public RosterFrame rf;
	
	public HashMap<String, ChatWindow> chats = new HashMap<String, ChatWindow>();
	
	private GenSpaceLogin_1 login;
	
	private Window chatMain;
	
	private Roster r;
	private ICEPush pusher = new ICEPush();
	private String u;
	private String p;	
	
	public ChatReceiver(GenSpaceLogin_1 genSpaceLogin_1){
		this.login = genSpaceLogin_1;
	}
	
	public boolean login(String u, String p) {
		this.u = u;
		this.p = p;
		ConnectionConfiguration config = new ConnectionConfiguration(RuntimeEnvironmentSettings.PROD_HOST, 5222, "genspace");
//		config.setReconnectionAllowed(false);
//		ConnectionConfiguration config = new ConnectionConfiguration(RuntimeEnvironmentSettings.PROD_HOST, 5269, "genspace");
		
		connection = new XMPPConnection(config);
		if(tryLogin(u, p)) {
			//System.out.println("Connection succeeds");
			
			manager = connection.getChatManager();
			this.updateRoster();
			// this.createRosterFrame();
			
			manager.addChatListener(this);
			//System.out.println("OOOOOOO"+this.rf);
			return true;
		} else{
			return false;
			//System.out.println("Connection fails");
		}
	}
	
	
	
	public boolean reLogin() {
		return login(this.u, this.p);
	}
	
	public void logout(String user, boolean disconnect) {
		
		if (this.getConnection().isConnected()) {
			Presence pr = new Presence(Presence.Type.unavailable);
			pr.setStatus("Unavailable");
			this.getConnection().sendPacket(pr);
			GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
			
		}
		
		if (disconnect) {
			this.getConnection().disconnect();
		}
	}
	
	public void updateRoster() {
		this.r = this.connection.getRoster();
		if (this.r == null) {
			if (this.connection.isConnected()) {
				this.connection.disconnect();
			}
			this.reLogin();
			this.r = this.connection.getRoster();
		}
		
		if (this.r != null)
			this.r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
	}
	
	public void createRosterFrame() {
		this.rf = new RosterFrame(login, this);
		this.rf.setRoster(r);
		this.rf.setVisible(true);
		//this.rf.setPositionX(10);
		//this.rf.setPositionY(10);
	}
	
	public boolean tryLogin(String u, String p) {
		try{
			connection.connect();
			connection.login(u, p);
		} catch (XMPPException e) {
			GenSpaceServerFactory.handleException(e);
			return false;
		}
		return true;
	}

	@Override
	public void chatCreated(Chat c, boolean createdLocal) {
		// TODO Auto-generated method stub
		if (chats.containsKey(c.getParticipant())) {
			//System.out.println("contained participant!");
			//return ;
		}
		
		else if(createdLocal) {
			System.out.println("DEBUG sender to receiver: " + this.login.getGenSpaceServerFactory().getUsername() + " " + c.getParticipant());
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.addListener(this);
			cw.addComponent(pusher);
			chats.put(c.getParticipant(), cw);
			//System.out.println("check chat map: "+ chats);
			rf.getApplication().getMainWindow().addWindow(cw);
			//this.login.getPusher().push();
			pusher.push();
		}
		c.addMessageListener(this);
		//pusher.push();
		//this.login.getPusher().push();
	}

	@Override
	public void processMessage(Chat c, Message m) {
		// TODO Auto-generated method stub
		//System.out.println("Get message prpoerty: " + m.getProperty("specialType"));
		System.out.println("Debug receiver: " + c.getParticipant());
		System.out.println("Get message body: " + m.getBody());

		if ((m.getProperty("specialType") == null || m.getProperty("specialType").equals(MessageTypes.CHAT)) && (m.getBody() == null || m.getBody().equals(""))){
			return;
		}
		if (chats.containsKey(c.getParticipant())) {
			chats.get(c.getParticipant()).processMessage(m);
		}
		else {
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.addListener(this);
			cw.addComponent(pusher);
			chats.put(c.getParticipant(), cw);
			
			if (rf.getApplication() != null) {
				rf.getApplication().getMainWindow().addWindow(cw);
			} else {
				System.out.println("The rosterframe is not has not been shown: " + this.login.getGenSpaceServerFactory().getUsername());
				this.createChatMain();
				System.out.println("Add chat main: " + this.chatMain);
				this.login.getUMainToolBar().getApplication().getMainWindow().addWindow(this.chatMain);
				System.out.println("Add cw: " + cw);
				this.login.getUMainToolBar().getApplication().getMainWindow().addWindow(cw);
			}
			cw.processMessage(m);
		}
		pusher.push();
		// System.out.println("Message is dispatched in ChatReceiver.processMessage");
		//this.login.getPusher().push();
	}
	
	public ChatManager getManager() {
		return this.manager;
	}
	
	public XMPPConnection getConnection() {
//		if (!this.connection.isConnected()) {
//			int tryTimes = 0;
//			while (tryTimes < 5) {
//				tryTimes++;
//				if (tryLogin(u, p))
//					break;
//			}
//		}
		return this.connection;
	}
	
	public RosterFrame getRosterFrame() {
		return this.rf;
	}
	
	public void windowClose(CloseEvent event) {
		ChatWindow tmpCw = (ChatWindow)event.getWindow();
		String user = tmpCw.getChat().getParticipant();
		
		chats.remove(user);
	}
	
	public Roster getRoster() {
		return this.r;
	}
	
	
	public void reShown(String user) {
		if (this.getConnection().isConnected()) {		
			Presence pr = new Presence(Presence.Type.available);
			this.getConnection().sendPacket(pr);
			GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
		}
	}
	
	public void createChatMain() {
		
		final Window mainWindow = this.login.getUMainToolBar().getApplication().getMainWindow();
		chatMain = new Window();
		chatMain.setCaption("GMessage");
		chatMain.setHeight("380px");
		chatMain.setWidth("310px");
		chatMain.setResizable(false);
		chatMain.setScrollable(false);
		chatMain.addListener(new Window.CloseListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				// TODO Auto-generated method stub
				for (ChatWindow cw: chats.values()) {
					if (cw.getParent() != null)
						mainWindow.removeWindow(cw);
				}
				//chatHandler.logout(username, true);
				//chatMain.setVisible(false);
				//mainWindow.removeWindow(chatMain);
			}
		});
		VerticalLayout chatLayout = new VerticalLayout();
		chatMain.addComponent(chatLayout);
		
		if (this.rf != null){
			GenSpaceWindow.getGenSpaceBlackboard().removeListener(this.rf);
			GenSpaceWindow.getGenSpaceBlackboard().removeListener(this.rf);
		}
		
		this.updateRoster();
		this.createRosterFrame();
		this.rf.addStyleName("feature-info");
		chatLayout.addComponent(this.rf);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(this.rf);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(this.rf);
		mainWindow.addWindow(chatMain);
		
		//String user = login.getGenSpaceServerFactory().getUsername();
		//GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
	}
	
}
