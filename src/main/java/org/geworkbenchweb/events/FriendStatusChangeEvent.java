package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class FriendStatusChangeEvent implements Event{

	private int friendID;
	private int myID;
	private String myName;
	private String friendName;
	private int optType;
	
	public static final int RM_FRIEND = 0;
	public static final int ADD_FRIEND = 1;
	public static final int STATUS_REFRESH = 2;
	public static final int NETWORK_EVENT = -1;
	
	public FriendStatusChangeEvent(int myID, int friendID) {
		this.myID = myID;
		this.friendID = friendID;
		this.optType = this.STATUS_REFRESH;
	}
	
	public int getMyID() {
		return this.myID;
	}
	
	public int getFriendID() {
		return this.friendID;
	}
	
	public void setOptType(int optType) {
		this.optType = optType;
	}
	
	public int getOptType() {
		return this.optType;
	}
	
	public interface FriendStatusChangeListener extends Listener {
		public void changeFriendStatus(FriendStatusChangeEvent evt);
	};

}
