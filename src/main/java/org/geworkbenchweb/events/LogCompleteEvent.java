package org.geworkbenchweb.events;

import org.geworkbench.components.genspace.server.stubs.User;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class LogCompleteEvent implements Event{
	
	private int myID;
	
	public LogCompleteEvent(int myID) {
		this.myID = myID;
	}
	
	public int getID() {
		return this.myID;
	}
	
	public interface LogCompleteEventListener extends Listener {
		public void completeLog(LogCompleteEvent evt);
	};
}
