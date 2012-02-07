package org.geworkbenchweb.analysis.hierarchicalclustering.client.ui.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.CommonOptions;
import com.google.gwt.visualization.client.visualizations.Visualization;

public class BioHeatMap extends Visualization<BioHeatMap.Options> {

	/**
	 * Options for drawing the BioHeatMap.
	 * 
	 */
	public static class Options extends CommonOptions {

		public static Options create() {
			return JavaScriptObject.createObject().cast();
		}

		protected Options() {
		}

		public final native void setCellWidth(int cellWidth) /*-{
		      this.cellWidth = cellWidth;
		    }-*/;
		
		public final native void setCellHeight(int cellHeight) /*-{
	      this.cellHeight = cellHeight;
	    }-*/;
		
		public final native void setNumberOfColors(int numberOfColors)/*-{
	      this.numberOfColors = numberOfColors;
	    }-*/;
	}
	
	public BioHeatMap() {
		super();
	}

	public BioHeatMap(AbstractDataTable data, Options options) {
		super(data, options);
	}

	@Override
	protected native JavaScriptObject createJso(Element parent);
}
