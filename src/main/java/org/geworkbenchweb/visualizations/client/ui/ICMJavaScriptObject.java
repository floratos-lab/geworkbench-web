package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;

// interaction color mosaic
public class ICMJavaScriptObject extends JavaScriptObject {
	
	protected ICMJavaScriptObject() {}

	public static native void createInstance(String containerId)/*-{

    $wnd.$interaction_color_mosaic.create(containerId);
   
	}-*/;

}
