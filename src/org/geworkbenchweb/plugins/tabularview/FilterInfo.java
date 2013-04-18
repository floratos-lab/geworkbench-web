package org.geworkbenchweb.plugins.tabularview;

import java.io.Serializable;

public class FilterInfo implements Serializable
{
	 
	private static final long serialVersionUID = 2987602225797434168L;
	long contextId;
	String[] selectedSet;
	FilterInfo(long contextId, String[] selectedSet)
	{
		this.contextId = contextId;
		this.selectedSet = selectedSet;
	}
	
	public long getContextId()
	{
		return contextId;
	}
	
	public String[] getSelectedSet()
	{
		return selectedSet;
	}
	
}

