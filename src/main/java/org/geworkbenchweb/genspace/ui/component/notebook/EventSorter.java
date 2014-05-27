package org.geworkbenchweb.genspace.ui.component.notebook;

public class EventSorter {
	
	private static DateSorter ds = new DateSorter();
	
	private static ToolSorter ts = new ToolSorter();
	
	private static UserSorter us = new UserSorter();
	
	public static DateSorter getDateSorter() {
		return ds;
	}
	
	public static ToolSorter getToolSorter() {
		return ts;
	}
	
	public static UserSorter getUserSorter() {
		return us;
	}

}
