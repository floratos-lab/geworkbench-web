package org.geworkbenchweb.genspace.ui;


import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.events.LogCompleteEvent;
import org.geworkbenchweb.events.ChatStatusChangeEvent.ChatStatusChangeEventListener;
import org.geworkbenchweb.events.LogCompleteEvent.LogCompleteEventListener;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.events.FriendStatusChangeEvent.FriendStatusChangeListener;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.vaadin.artur.icepush.ICEPush;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.ui.Window;

public class GenSpaceWindow extends Window{
	private static final long serialVersionUID = -4091993515000311665L;
	
	private static Blackboard genSpaceBlackboard = new Blackboard();
	static {
		genSpaceBlackboard.register(LogCompleteEventListener.class, LogCompleteEvent.class);
		genSpaceBlackboard.register(ChatStatusChangeEventListener.class, ChatStatusChangeEvent.class);
		genSpaceBlackboard.register(FriendStatusChangeListener.class, FriendStatusChangeEvent.class);
	}

	private GenspaceLayout layout;

	private GenspaceLogger logger;
	
	private ICEPush pusher;
	
	public GenSpaceWindow(GenspaceLogger genSpaceLogger)
	{
		setCaption("genSpace");
		this.logger = genSpaceLogger;
		this.pusher = new ICEPush();
		this.layout = new GenspaceLayout(this.logger, this.pusher);
		this.addComponent(this.pusher);
		this.setContent(layout);		
	}
	
	
	public GenspaceLayout getLayout() {
		return layout;
	}

	public GenspaceLogger getLogger() {
		return logger;
	}
	
	public ICEPush getPusher() {
		return this.pusher;
	}
	
	public synchronized static Blackboard getGenSpaceBlackboard() {
		return genSpaceBlackboard;
	}	
	
	static public void sPush(com.github.wolfie.blackboard.Listener listener, ICEPush pusher) {
		if (pusher.getApplication() == null) {	
			GenSpaceWindow.getGenSpaceBlackboard().removeListener(listener);
		}
		else {
			pusher.push();
		}
	}
	
}
