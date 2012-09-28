package org.geworkbenchweb.events;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class NodeAddEvent implements Event {

	private final AbstractPojo data;
	
	public NodeAddEvent(AbstractPojo data) {
		this.data 	= 	data;
	}
	
	public AbstractPojo getData() {
		return data;
	}
	
	public interface NodeAddEventListener extends Listener {
		public void addNode(final NodeAddEvent event);
	}
	
}
