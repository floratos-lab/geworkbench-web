package org.geworkbenchweb.genspace.chat;

import java.util.HashMap;

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
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.vaadin.ui.UI;
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
			this.updateRoster();
			this.createRosterFrame();

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
	public void chatCreated(final Chat c, final boolean createdLocal) {
		// TODO Auto-generated method stub
		if (chats.containsKey(c.getParticipant())) {
			return ;
		}
		
		UI.getCurrent().access(new Runnable(){
			@Override
			public void run(){
				if(createdLocal) {
					System.out.println("DEBUG participant: " + c.getParticipant());
					final ChatWindow cw = new ChatWindow(login);
					cw.setChat(c);
					cw.setVisible(true);
					cw.addListener(ChatReceiver.this);
					
					chats.put(c.getParticipant(), cw);
					UI.getCurrent().addWindow(cw);
				}
				c.addMessageListener(ChatReceiver.this);
			}
		});
		
	}

	@Override
	public void processMessage(final Chat c, final Message m) {
		// TODO Auto-generated method stub
		/*System.out.println("Get message prpoerty: " + m.getProperty("specialType"));
		System.out.println("Get message body: " + m.getBody());*/

		if ((m.getProperty("specialType") == null || m.getProperty("specialType").equals(MessageTypes.CHAT)) && (m.getBody() == null || m.getBody().equals("")))
			return;

		UI.getCurrent().access(new Runnable(){
			@Override
			public void run(){
				if (chats.containsKey(c.getParticipant())) {
					chats.get(c.getParticipant()).processMessage(m);
				}
				else {
					final ChatWindow cw = new ChatWindow(login);
					cw.setChat(c);
					cw.setVisible(true);
					cw.addListener(ChatReceiver.this);
					
					chats.put(c.getParticipant(), cw);
					UI.getCurrent().addWindow(cw);
					
					cw.processMessage(m);
				}
				// System.out.println("Message is dispatched in ChatReceiver.processMessage");
				
			}
		});
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
