package org.geworkbenchweb.plugins.tabularview;

import java.io.Serializable;
import org.geworkbenchweb.pojos.Context;

public class FilterInfo implements Serializable
{
	 
	private static final long serialVersionUID = 2987602225797434168L;
	Context context;
	String[] selectedSet;
	FilterInfo(Context context, String[] selectedSet)
	{
		this.context = context;
		this.selectedSet = selectedSet;
	}
	
	public Context getContext()
	{
		return context;
	}
	
	public String[] getSelectedSet()
	{
		return selectedSet;
	}
	
}

