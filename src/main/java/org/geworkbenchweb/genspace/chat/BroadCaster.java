package org.geworkbenchweb.genspace.chat;

import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

public class BroadCaster {
	
	public static void broadcastPresence(GenSpaceLogin_1 login) {
		ChatReceiver chatHandler = login.getChatHandler();
		if (login.getChatHandler().rf != null) {
			XMPPConnection c = chatHandler.getConnection();
			Presence pr = null;
			if (chatHandler.rf.getPresence() == null) {
				//Means that the user has not touched the chat
				pr = new Presence(Presence.Type.available);
				pr.setStatus("On genspace...");
				pr.setMode(Presence.Mode.available);
			} else {
				pr = chatHandler.rf.getPresence();
			}
			
			if (c.isConnected()) {
				c.sendPacket(pr);
			} else {
				chatHandler.reLogin();
				c = chatHandler.getConnection();
				c.sendPacket(pr);
			}
		}
	}

}
