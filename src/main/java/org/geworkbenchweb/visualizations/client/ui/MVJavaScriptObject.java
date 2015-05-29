package org.geworkbenchweb.visualizations.client.ui;

import com.google.gwt.core.client.JavaScriptObject;

public class MVJavaScriptObject extends JavaScriptObject {
	
	protected MVJavaScriptObject() {}

	public static native void createInstance(String containerId, String pdbcontent)/*-{

    $wnd.$molecule_viewer.create(containerId, pdbcontent);
   
	}-*/;

	public static native void createInstance(String containerId, String pdbcontent, String representation)/*-{

    $wnd.$molecule_viewer.create(containerId, pdbcontent, representation);
   
	}-*/;
	
	public static native void set3DRepresentation(String representation)/*-{

    $wnd.$molecule_viewer.set3DRepresentation(representation);
   
	}-*/;

	public static native void setDisplayAtoms(boolean displayAtoms)/*-{

    $wnd.$molecule_viewer.setDisplayAtoms(displayAtoms);
   
	}-*/;

	public static native void setDisplayBonds(boolean displayBonds)/*-{

    $wnd.$molecule_viewer.setDisplayBonds(displayBonds);
   
	}-*/;

	public static native void setDisplayRibbon(boolean displayRibbon)/*-{

    $wnd.$molecule_viewer.setDisplayRibbon(displayRibbon);
   
	}-*/;

	public static native void setDisplayBackbone(boolean displayBackbone)/*-{

    $wnd.$molecule_viewer.setDisplayBackbone(displayBackbone);
   
	}-*/;

	public static native void setDisplayPipe(boolean displayPipe)/*-{

    $wnd.$molecule_viewer.setDisplayPipe(displayPipe);
   
	}-*/;

	public static native void setCartoonize(boolean cartoonize)/*-{

    $wnd.$molecule_viewer.setCartoonize(cartoonize);
   
	}-*/;

	public static native void setColorByChain(boolean colorByChain)/*-{

    $wnd.$molecule_viewer.setColorByChain(colorByChain);
   
	}-*/;

	public static native void setColorByResidue(boolean colorByResidue)/*-{

    $wnd.$molecule_viewer.setColorByResidue(colorByResidue);
   
	}-*/;
}
