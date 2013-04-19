package org.geworkbenchweb.genspace.chat;

import java.util.HashMap;
import java.util.Iterator;

import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;
import org.geworkbenchweb.genspace.ui.chat.ChatWindow;
import org.geworkbenchweb.genspace.ui.chat.MessageTypes;
import org.geworkbenchweb.genspace.ui.chat.RosterFrame;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.vaadin.artur.icepush.ICEPush;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class ChatReceiver implements MessageListener, ChatManagerListener, Window.CloseListener {
	
	public ChatManager manager;
	
	public XMPPConnection connection;
	
	public RosterFrame rf;
	
	public HashMap<String, ChatWindow> chats = new HashMap<String, ChatWindow>();
	
	private GenSpaceLogin login;
	
	private Roster r;
	
	public ChatReceiver(GenSpaceLogin login){
		this.login = login;
	}
	
	public void login(String u, String p) {
		ConnectionConfiguration config = new ConnectionConfiguration(RuntimeEnvironmentSettings.PROD_HOST, 5222, "genspace");
		connection = new XMPPConnection(config);
		if(tryLogin(u, p)) {
			System.out.println("Connection succeeds");
			Presence pr = new Presence(Presence.Type.available);
			pr.setStatus("On genspace...");
			connection.sendPacket(pr);
			
			manager = connection.getChatManager();
			/*this.r = connection.getRoster();
			this.r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);*/
			this.updateRoster();
			this.createRosterFrame();
			/*rf = new RosterFrame(login, this);
			rf.setRoster(r);
			rf.setVisible(true);
			final Refresher refresher = new Refresher();
			refresher.setRefreshInterval(50);
			refresher.addListener(this);
			this.rf.addComponent(refresher);*/

			manager.addChatListener(this);
		} else
			System.out.println("Connection fails");

	}
	
	public void updateRoster() {
		this.r = this.connection.getRoster();
		this.r.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
	}
	
	public void createRosterFrame() {
		this.rf = new RosterFrame(login, this);
		this.rf.setRoster(r);
		this.rf.setVisible(true);
		this.rf.setPositionX(10);
		this.rf.setPositionY(10);
		
		//Mike 0408
		/*final Refresher refresher = new Refresher();
		refresher.setRefreshInterval(1000);
		refresher.addListener(this);
		this.rf.addComponent(refresher);*/
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
		System.out.println("In ChatReceiver.chatCreated");
		System.out.println("New chat property: " + c.toString());
		if(createdLocal) {
			System.out.println("Within createdLocal test participant: " + c.getParticipant());
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.addListener(this);
			
			chats.put(c.getParticipant(), cw);
			rf.getApplication().getMainWindow().addWindow(cw);
		}
		c.addMessageListener(this);
		
		/*Iterator<MessageListener> lIT = c.getListeners().iterator();
		while (lIT.hasNext()) {
			System.out.println("Print listener: " + lIT.next().toString());
		}*/
		
		System.out.println("A new chat is created in ChatReceiver.chatCreated");
		
		this.login.getPusher().push();
		
	}

	@Override
	public void processMessage(Chat c, Message m) {
		// TODO Auto-generated method stub
		System.out.println("In ChatReceiver.processMessage");
		System.out.println("Get message prpoerty: " + m.getProperty("specialType"));
		System.out.println("Get message body: " + m.getBody());
		System.out.println("Check chat property: " + c.toString());

		if ((m.getProperty("specialType") == null || m.getProperty("specialType").equals(MessageTypes.CHAT)) && (m.getBody() == null || m.getBody().equals("")))
			return;
		
		Iterator<String> chatIT = chats.keySet().iterator();
		while (chatIT.hasNext()) {
			System.out.println("In processMessage: " + chatIT.next());
		}
		
		if (chats.containsKey(c.getParticipant())) {
			System.out.println("In processMessage get window: " + chats.get(c.getParticipant()).getCaption());
			System.out.println("In processMessage no new window check main: " + rf.getApplication().getMainWindow());
			chats.get(c.getParticipant()).processMessage(m);
		}
		else {
			final ChatWindow cw = new ChatWindow(login);
			cw.setChat(c);
			cw.setVisible(true);
			cw.addListener(this);
			
			chats.put(c.getParticipant(), cw);
			rf.getApplication().getMainWindow().addWindow(cw);
			
			cw.processMessage(m);
			
			System.out.println("In processMessage get new window: " + chats.get(c.getParticipant()).getCaption());
			System.out.println("In processMessage new window check main: " + rf.getApplication().getMainWindow());
		}
		System.out.println("Message is dispatched in ChatReceiver.processMessage");
		
		this.login.getPusher().push();
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

	public void refresh(Refresher source) {
		// TODO Auto-generated method stub
		this.rf.refresh();
	}
}
