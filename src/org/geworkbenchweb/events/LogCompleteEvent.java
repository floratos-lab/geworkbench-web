package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class LogCompleteEvent implements Event{
	public LogCompleteEvent() {
		
	}
	
	public interface LogCompleteEventListener extends Listener {
		public void completeLog(LogCompleteEvent evt);
	};
}
