package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class NodeAddEvent implements Event {

	private final String dataSetName;

	private final String dataType;
	
	public NodeAddEvent(final String dataSetName, final String dataType) {
		this.dataSetName 	= 	dataSetName;
		this.dataType		=	dataType; 
	}

	public String getDataSetName() {
		return dataSetName;
	}

	public String getDataType() {
		return dataType;
	}

	public interface NodeAddEventListener extends Listener {
		public void addNode(final NodeAddEvent event);
	}
	
}
