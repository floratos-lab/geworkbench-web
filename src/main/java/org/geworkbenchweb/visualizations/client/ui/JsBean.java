package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;

public class JsBean extends JavaScriptObject {
	protected JsBean(){}
	
	public static final native JsBean createObject()/*-{
		return new $wnd.Object();
	}-*/;
	
	public static final native JsArrayMixed createArray()/*-{
		return new $wnd.Array();
	}-*/;
	
	public static final native JsBean createFunction()/*-{
		return new $wnd.Function();
	}-*/;
	
	public final native void set(String property, String value)/*-{
		this[property] = value;
	}-*/;
	
	public final native void set(String property, boolean value)/*-{
		this[property] = value;
	}-*/;
	
	public final native void set(String property, int value)/*-{
		this[property] = value;
	}-*/;
	
	public final native void set(String property, double value)/*-{
		this[property] = value;
	}-*/;
	
	public final native void set(String property, JavaScriptObject value)/*-{
		this[property] = value;
	}-*/;
	
	public final native String getString(String property)/*-{
		return this[property];
	}-*/;
	
	public final native boolean getBoolean(String property)/*-{
		return this[property];
	}-*/;
	
	public final native int getInt(String property)/*-{
		return this[property];
	}-*/;
	
	public final native double getDouble(String property)/*-{
		return this[property];
	}-*/;
	
	public final native <T extends JavaScriptObject> T getObject(String property)/*-{
		return this[property];
	}-*/;
	
	public final native JsBean getBean(String property)/*-{
		return this[property];
	}-*/;
}
