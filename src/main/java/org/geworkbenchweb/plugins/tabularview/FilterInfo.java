package org.geworkbenchweb.plugins.tabularview;

import java.io.Serializable;

import org.geworkbenchweb.pojos.Context;

public class FilterInfo implements Serializable
{
	private static final long serialVersionUID = 2987602225797434168L;

	final private Context context;
	/* selectedSet stores ID's of the selected sets. 
	 * The first element could be 0, in which case all the other elements are ignored and it means 'all selected'.
	 * Although the special case's handling is no better than explicitly using the string 'all selected', 
	 * this approach is better to make sure all the other real ID's are handled properly and efficiently as Long. */
	final private Long[] selectedSet;
	
	FilterInfo(Context context, Long[] selectedSet)
	{
		this.context = context;
		this.selectedSet = selectedSet;
	}
	
	public Context getContext()
	{
		return context;
	}
	
	public Long[] getSelectedSet()
	{
		return selectedSet;
	}
	
}

