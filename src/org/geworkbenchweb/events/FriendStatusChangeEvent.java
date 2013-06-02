package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class FriendStatusChangeEvent implements Event{

	private int friendID;
	
	private int myID;
	
	public FriendStatusChangeEvent(int myID, int friendID) {
		this.myID = myID;
		this.friendID = friendID;
	}
	
	public int getMyID() {
		return this.myID;
	}
	
	public int getFriendID() {
		return this.friendID;
	}
	
	public interface FriendStatusChangeListener extends Listener {
		public void changeFriendStatus(FriendStatusChangeEvent evt);
	};

}
