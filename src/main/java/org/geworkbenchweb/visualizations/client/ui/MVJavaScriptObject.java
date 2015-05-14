package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;

public class MVJavaScriptObject extends JavaScriptObject {
	
	protected MVJavaScriptObject() {}

	public static native void createInstance(String containerId, String filepath)/*-{

    $wnd.$molecule_viewer.create(containerId, filepath);
   
}-*/;

}
