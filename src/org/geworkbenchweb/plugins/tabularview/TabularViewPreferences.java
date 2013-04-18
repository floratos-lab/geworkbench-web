package org.geworkbenchweb.plugins.tabularview;

 
public class TabularViewPreferences {
	
	
	private int markerDisplayControl;
	private int annotationDisplayControl;
	private int numberPrecisionControl;
	private FilterInfo markerFilter = null;
	private FilterInfo arrayFilter = null;
	
	
	public TabularViewPreferences()
	{
		markerDisplayControl = Constants.MarkerDisplayControl.both.ordinal();
		annotationDisplayControl = Constants.AnnotationDisplayControl.on.ordinal();
		numberPrecisionControl = Constants.DEFAULT_PRECISION_NUM;
	
	} 
	 
	public TabularViewPreferences(int markerDisplayControl,
	 int annotationDisplayControl,
	 int numberPrecisionControl,
	 FilterInfo markerFilter,
     FilterInfo arrayFilter)	{
		this.markerDisplayControl = markerDisplayControl;
		this.annotationDisplayControl = annotationDisplayControl;
		this.numberPrecisionControl = numberPrecisionControl;
		this.markerFilter = markerFilter;
		this.arrayFilter = arrayFilter;
	}
	
	
	public void setMarkerDisplayControl(int markerDisplayControl)
	{
		this.markerDisplayControl = markerDisplayControl;
	}
	
    public int getMarkerDisplayControl()
    {
    	return this.markerDisplayControl;    	
    	
    }
    
    public void setAnnotationDisplayControl(int annotationDisplayControl)
	{
		this.annotationDisplayControl = annotationDisplayControl;
	}
	
    public int getAnnotationDisplayControl()
    {
    	return this.annotationDisplayControl;    	
    	
    }
    
    public void setNumberPrecisionControl(int numberPrecisionControl)
   	{
   		this.numberPrecisionControl = numberPrecisionControl;
   	}
   	
    public int getNumberPrecisionControl()
    {
       	return this.numberPrecisionControl;    	
       	
    }
    
    
    public void setMarkerFilter(FilterInfo markerFilter)
   	{		 
   		this.markerFilter = markerFilter;
   	}
   	
    public FilterInfo getMarkerFilter()
    {
       	return this.markerFilter;        	
    }
    
    public void setArrayFilter(FilterInfo arrayFilter)
   	{
    	 
   		this.arrayFilter = arrayFilter;
   	}
   	
    public FilterInfo getArrayFilter()
    {
       	return this.arrayFilter;    	
       	
    }
    
    
    public void reset()
    {
    	markerDisplayControl = Constants.MarkerDisplayControl.both.ordinal();
		annotationDisplayControl = Constants.AnnotationDisplayControl.on.ordinal();
		numberPrecisionControl = Constants.DEFAULT_PRECISION_NUM;
	    markerFilter = null;
	    arrayFilter = null;
    }
 
    
}
