package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class FriendStatusChangeEvent implements Event{
	
	private String friendName;
	
	public FriendStatusChangeEvent(String friendName) {
		this.friendName = friendName;
	}
	
	public String getFriendName() {
		return this.friendName;
	}
	
	public interface FriendStatusChangeListener extends Listener {
		public void changeFriendStatus(FriendStatusChangeEvent evt);
	};

}
