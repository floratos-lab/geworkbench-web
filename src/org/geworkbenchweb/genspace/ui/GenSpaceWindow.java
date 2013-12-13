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

	private GenSpaceComponent component;

	private GenspaceLogger logger;
	
	private ICEPush pusher;
	
	public GenSpaceWindow(GenspaceLogger genSpaceLogger)
	{
		setCaption("genSpace");
		this.logger = genSpaceLogger;
		this.pusher = new ICEPush();
		this.component = new GenSpaceComponent(this.logger, this.pusher);
		this.addComponent(this.pusher);
		this.setContent(component);		
	}
	
	public GenSpaceComponent getComponent() {
		return component;
	}

	public GenspaceLogger getLogger() {
		return logger;
	}
	
	public ICEPush getPusher() {
		return this.pusher;
	}
	
	public static Blackboard getGenSpaceBlackboard() {
		return genSpaceBlackboard;
	}
}