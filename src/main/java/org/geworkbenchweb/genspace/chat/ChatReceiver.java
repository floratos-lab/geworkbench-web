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
	
	
	public ChatReceiver(GenSpaceLogin_1 genSpaceLogin_1){
		this.login = genSpaceLogin_1;
	}
	
	public boolean login(String u, String p) {
		this.u = u;
		this.p = p;
		ConnectionConfiguration config = new ConnectionConfiguration(RuntimeEnvironmentSettings.PROD_HOST, 5222, "genspace");

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
	
	public void logout(String user) {
		
		if (this.getConnection().isConnected()) {
			Presence pr = new Presence(Presence.Type.unavailable);
			this.getConnection().sendPacket(pr);
			GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(user));
			this.getConnection().disconnect();
		}
	}
	
	public void updateRoster() {
		this.r = this.connection.getRoster();
		this.r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
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
		}
		return true;
	}

	@Override
	public void chatCreated(Chat c, boolean createdLocal) {
		// TODO Auto-generated method stub
		if (chats.containsKey(c.getParticipant())) {
			// System.out.println("contained participant!");
			// return ;
		}
		
		else if(createdLocal) {
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.addListener(this);
			cw.addComponent(pusher);
			chats.put(c.getParticipant(), cw);
			rf.getApplication().getMainWindow().addWindow(cw);
			pusher.push();
		}
		c.addMessageListener(this);
	}

	@Override
	public void processMessage(Chat c, Message m) {

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
			rf.getApplication().getMainWindow().addWindow(cw);
			cw.processMessage(m);
		}
		pusher.push();
	}
	
	public ChatManager getManager() {
		return this.manager;
	}
	
	public XMPPConnection getConnection() {
		if (!this.connection.isConnected()) {
			int tryTimes = 0;
			while (tryTimes != 5) {
				if (tryLogin(u, p))
					break;
			}
		}
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
	
}
