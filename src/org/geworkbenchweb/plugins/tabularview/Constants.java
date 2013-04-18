package org.geworkbenchweb.plugins.tabularview;

public class Constants {
	 
	public static final String MARKER_HEADER = "Marker";
	public static final String GENE_SYMBOL_HEADER = "Gene Symbol";
	public static final String ANNOTATION_HEADER = "Annotation";
	
	public static final String MARKER_DISPLAY_CONTROL = "TabularViewUI.MarkerDisplayControl";
	public static final String ANNOTATION_DISPLAY_CONTROL = "TabularViewUI.AnnotationDisplayControl";
	public static final String NUMBER_PRECISION_CONTROL = "TabularViewUI.NumberPrecisionControl";
	public static final String MARKER_FILTER_CONTROL = "TabularViewUI.MarkerFilterControl";
	public static final String ARRAY_FILTER_CONTROL = "TabularViewUI.ArrayFilterControl";
    public static final int DEFAULT_PRECISION_NUM = 2;
	
	public static enum MarkerDisplayControl  {
		marker, gene_symbol, both
	}

	public static  enum AnnotationDisplayControl  {
		on, off
	}
	
}
