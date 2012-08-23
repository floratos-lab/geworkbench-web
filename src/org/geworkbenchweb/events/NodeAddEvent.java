package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class NodeAddEvent implements Event {

	private final String dataSetName;

	private final String dataType;
	
	private final Long dataSetId;
	
	public NodeAddEvent(final Long dataSetId, final String dataSetName, final String dataType) {
		this.dataSetName 	= 	dataSetName;
		this.dataType		=	dataType; 
		this.dataSetId		=	dataSetId;
	}
	
	public Long getDataSetId() {
		return dataSetId;
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
