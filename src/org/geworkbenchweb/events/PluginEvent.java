package org.geworkbenchweb.events;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class PluginEvent implements Event {

	private String pluginName;
	
	private Long dataId;
	
	public PluginEvent(String pluginName, Long dataId) {
		this.pluginName 	= 	pluginName;
		this.dataId			=	dataId;
	}
	
	public String getPluginName() {
		return pluginName;
	}
	
	public long getDataId() {
		return dataId;
		
	}
	public interface PluginEventListener extends Listener {
		public void pluginSet(final PluginEvent event);
	}
	
}