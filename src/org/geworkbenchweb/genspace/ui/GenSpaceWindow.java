package org.geworkbenchweb.genspace.ui;


import org.geworkbenchweb.genspace.GenspaceLogger;
import org.vaadin.artur.icepush.ICEPush;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.ui.Window;

public class GenSpaceWindow extends Window{
	private static final long serialVersionUID = -4091993515000311665L;
	
	private static Blackboard genSpaceBlackboard = new Blackboard();

	private GenSpaceComponent component;

	private GenspaceLogger logger;
	
	private ICEPush pusher;
	
	public GenSpaceWindow(GenspaceLogger genSpaceLogger)
	{
		setCaption("genSpace");
		//logger = new GenspaceLogger();
		this.logger = genSpaceLogger;
		this.pusher = new ICEPush();
		//this.pusher = pusher;
		this.component = new GenSpaceComponent(this.logger, this.pusher);
		this.addComponent(this.pusher);
		setContent(component);
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
