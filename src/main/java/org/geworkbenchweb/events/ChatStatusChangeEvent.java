package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class ChatStatusChangeEvent implements Event{
	
	private String username;
	
	public ChatStatusChangeEvent (String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public interface ChatStatusChangeEventListener extends Listener{
		public void changeStatus(ChatStatusChangeEvent evt);
	};
}
