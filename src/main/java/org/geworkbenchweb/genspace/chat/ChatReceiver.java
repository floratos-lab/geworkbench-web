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
import org.jivesoftware.smack.RosterEntry;
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
	
	private Roster r;
	private ICEPush pusher = new ICEPush();
	private String u;
	private String p;	
	
	public static Window createChatMain(final ChatReceiver chatHandler) {
		Window ret = new Window();
		ret.setCaption("GMessage");
		ret.setHeight("380px");
		ret.setWidth("310px");
		ret.setResizable(false);
		ret.setScrollable(false);

		VerticalLayout chatLayout = new VerticalLayout();
		ret.addComponent(chatLayout);

		if (chatHandler.rf != null) {
			GenSpaceWindow.getGenSpaceBlackboard().removeListener(chatHandler.rf);
		}

		chatHandler.updateRoster();
		chatHandler.createRosterFrame();
		chatHandler.rf.addStyleName("feature-info");
		chatLayout.addComponent(chatHandler.rf);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(chatHandler.rf);
		
		return ret;
	}
	
	public static String genKey(String participant) {
		return participant.replace("@genspace", "").replaceAll("([0-9a-zA-Z.]*)(/)([0-9a-zA-Z.]*)", "$1");
	}
		
	public ChatReceiver(GenSpaceLogin_1 genSpaceLogin_1){
		this.login = genSpaceLogin_1;
	}
	
	public boolean login(String u, String p) {
		this.u = u;
		this.p = p;
		ConnectionConfiguration config = new ConnectionConfiguration(RuntimeEnvironmentSettings.PROD_HOST, 5222, "genspace");
		config.setReconnectionAllowed(true);
		
		connection = new XMPPConnection(config);
		if(tryLogin(u, p)) {
			manager = connection.getChatManager();
			this.updateRoster();
			
			manager.addChatListener(this);
			return true;
		} else{
			return false;
		}
	}
	
	public boolean reLogin() {
		return login(this.u, this.p);
	}
	
	public void logout(String user, boolean disconnect) {
		Presence pr = new Presence(Presence.Type.unavailable);
		pr.setStatus("Unavailable");
		
		if (this.r != null && this.rf != null) {
			this.r.removeRosterListener(this.rf);
			GenSpaceWindow.getGenSpaceBlackboard().removeListener(this.rf);
		}
		
		if (this.getConnection().isConnected()) {
			this.getConnection().sendPacket(pr);
			GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
		}
		
		XMPPConnection connection = this.getConnection();
		if (disconnect && connection != null) {
			try {
				connection.disconnect();
			} catch (Exception ex) {
				//Means the connection has been removed
			}
		}
	}
	
	public void updateRoster() {
		this.r = this.connection.getRoster();
		
		if (this.r != null) {
			this.r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		}
	}
	
	public void createRosterFrame() {
		this.rf = new RosterFrame(login, this);
		this.rf.setRoster(r);
		this.rf.setVisible(true);
	}
	
	public boolean tryLogin(String u, String p) {
		try{
			connection.connect();
			connection.login(u, p);
		} catch (XMPPException e) {
			GenSpaceServerFactory.handleException(e);
			return false;
		} catch (Exception e) {
			GenSpaceServerFactory.handleException(e);
			return false;
		}
		return true;
	}
	
	private boolean verifyRoster(String key) {
		if (this.r == null || this.r.getEntries() == null)
			return false;
		for (RosterEntry re: this.r.getEntries()) {
			if (genKey(re.getUser()).equals(key))
				return true;
		}
		return false;
	}
	
	public void clearWindow(String key) {
		ChatWindow removed = this.chats.get(key);
		this.login.getUMainToolBar().getApplication().getMainWindow().removeWindow(this.chats.get(key));
		removed.removeListener(this);
		this.chats.remove(key);
	}

	@Override
	public void chatCreated(Chat c, boolean createdLocal) {
		// TODO Auto-generated method stub
		String key = genKey(c.getParticipant());
		Window mainWindow = login.getUMainToolBar().getApplication().getMainWindow();
		if (chats.containsKey(key)) {
			
			//In case one user remove a friend when chatting
			if (!verifyRoster(key)) {
				this.clearWindow(key);
				return ;
			}
			
			ChatWindow cw = chats.get(key);
			if (cw.getParent() == null) {
				cw.setVisible(true);
				mainWindow.addWindow(cw);
			}
		} else if(createdLocal) {
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.setUserkey(key);
			cw.setChatReceiver(this);
			cw.addListener(this);
			cw.addComponent(pusher);
			chats.put(key, cw);
			rf.getApplication().getMainWindow().addWindow(cw);
			pusher.push();
		}
		c.addMessageListener(this);
	}

	@Override
	public void processMessage(Chat c, Message m) {
		// TODO Auto-generated method stub
		Window mainWindow = login.getUMainToolBar().getApplication().getMainWindow();
		String key = genKey(c.getParticipant());
		if ((m.getProperty("specialType") == null || m.getProperty("specialType").equals(MessageTypes.CHAT)) && (m.getBody() == null || m.getBody().equals(""))){
			return;
		}
		if (chats.containsKey(key)) {
			ChatWindow cw = chats.get(key);
			
			if (cw.getParent() == null) {
				cw.setVisible(true);
				mainWindow.addWindow(cw);
			}
			
			chats.get(key).processMessage(m);
		} else {
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.setUserkey(key);
			cw.setChatReceiver(this);
			cw.addListener(this);
			cw.addComponent(pusher);
			chats.put(key, cw);
			
			if (rf != null && rf.getApplication() != null) {
				rf.getApplication().getMainWindow().addWindow(cw);
			} else {
				//If need chatMain, add it here
				mainWindow.addWindow(cw);
			}
			cw.processMessage(m);
		}
		pusher.push();
	}
	
	public ChatManager getManager() {
		return this.manager;
	}
	
	public XMPPConnection getConnection() {
		return this.connection;
	}
	
	public RosterFrame getRosterFrame() {
		return this.rf;
	}
	
	@Override
	public void windowClose(CloseEvent event) {
		ChatWindow tmpCw = (ChatWindow)event.getWindow();
		String user = genKey(tmpCw.getChat().getParticipant());
		//chats.remove(user);
	}
	
	public Roster getRoster() {
		return this.r;
	}
}
