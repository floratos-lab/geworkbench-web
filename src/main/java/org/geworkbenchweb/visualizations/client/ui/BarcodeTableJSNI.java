package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
 
 
public class BarcodeTableJSNI extends JavaScriptObject{
	
	
	protected BarcodeTableJSNI(){}
	
	 
	public static native void createTable(String containerId, String columnNames, String regulators, String barcodenMap, int barHeight)/*-{
		 
		 
          $wnd.createBarcodeTable(containerId, columnNames, regulators, barcodenMap, barHeight);
         
	}-*/;
	
	 
	 
	
	 
}
