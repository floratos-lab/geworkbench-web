package org.geworkbenchweb.genspace.chat;

import java.util.HashMap;

import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;
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
	
	public ChatReceiver(GenSpaceLogin_1 genSpaceLogin_1){
		this.login = genSpaceLogin_1;
	}
	
	public void login(String u, String p) {
		ConnectionConfiguration config = new ConnectionConfiguration(RuntimeEnvironmentSettings.PROD_HOST, 5222, "genspace");
		connection = new XMPPConnection(config);
		if(tryLogin(u, p)) {
			//System.out.println("Connection succeeds");
			Presence pr = new Presence(Presence.Type.available);
			pr.setStatus("On genspace...");
			connection.sendPacket(pr);
			
			manager = connection.getChatManager();
			this.updateRoster();
			// this.createRosterFrame();
			
			manager.addChatListener(this);
			//System.out.println("OOOOOOO"+this.rf);
		} else{
			//System.out.println("Connection fails");
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
			//System.out.println("DEBUG participant: " + c.getParticipant());
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
		//System.out.println("Get message body: " + m.getBody());

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
		// System.out.println("Message is dispatched in ChatReceiver.processMessage");
		//this.login.getPusher().push();
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
	
	public void windowClose(CloseEvent event) {
		ChatWindow tmpCw = (ChatWindow)event.getWindow();
		String user = tmpCw.getChat().getParticipant();
		
		chats.remove(user);
	}
}
