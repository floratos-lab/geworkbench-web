package org.geworkbenchweb.plugins.anova.results;

import java.io.Serializable;

 
public class AnovaTablePreferences implements Serializable {
	
	 
	private static final long serialVersionUID = 5577968418654145211L;
	// preferences
		private boolean selectMarker;
		private boolean selectGeneSymbol;
		private boolean selectFStat;
		private boolean selectPval;
		private boolean selectMean;
		private boolean selectStd = true;
		private int thresholdControl;
	    private float thresholdValue;
	 
	
	public AnovaTablePreferences()
	{
		selectMarker = true;
		selectGeneSymbol = true;
		selectFStat = true;
		selectPval = true;
		selectMean = true;
		selectStd = true;
		thresholdControl = 0;
	    thresholdValue = 0;
	    
	} 
	 
	public AnovaTablePreferences(boolean selectMarker, boolean selectGeneSymbol, boolean selectFStat,
			boolean selectPval, boolean selectMean, boolean selectStd, int thresholdControl, float thresholdValue)	{
		this.selectMarker  = selectMarker;
		this.selectGeneSymbol = selectGeneSymbol;
		this.selectFStat = selectFStat;
		this.selectPval = selectPval;
		this.selectMean = selectMean;
		this.selectStd = selectStd;
		this.thresholdControl = thresholdControl;
		this.thresholdValue = thresholdValue;
		 
	}
	
	
	public boolean selectMarker( )
	{
		return this.selectMarker;
	}
	public void selectMarker(boolean selectMarker)
	{
		this.selectMarker = selectMarker;
	}
	
	public boolean selectGeneSymbol( )
	{
		return this.selectGeneSymbol;
	}
	public void selectGeneSymbol(boolean selectGeneSymbol)
	{
		this.selectGeneSymbol = selectGeneSymbol;
	}
	public boolean selectFStat( )
	{
		return this.selectFStat;
	}
	public void selectFStat(boolean selectFStat )
	{
		this.selectFStat = selectFStat;
	}
	public boolean selectPVal( )
	{
		return this.selectPval;
	}
	public void selectPVal( boolean selectPval)
	{
		this.selectPval = selectPval;
	}
	public boolean selectMean( )
	{
		return this.selectMean;
	}
	public void selectMean(boolean selectMean )
	{
		this.selectMean = selectMean;
	}
	public boolean selectStd( )
	{
		return this.selectStd;
	}
	public void selectStd(boolean selectStd)
	{
		this.selectStd = selectStd;
	}
    
	public int getThresholdControl( )
	{
		return this.thresholdControl;
	}
	public void setThresholdControl(int thresholdControl)
	{
		this.thresholdControl = thresholdControl;
	}
    
	
	public float getThresholdValue( )
	{
		return this.thresholdValue;
	}
	public void setThresholdValue(float thresholdValue)
	{
		this.thresholdValue = thresholdValue;
	}
    
	 
    public void reset()
    {
        selectMarker = true;
		selectGeneSymbol = true;
		selectFStat = true;
		selectPval = true;
		selectMean = true;
		selectStd = true;
		thresholdControl = 0;
		thresholdValue = 0;
		 
    }
 
    
}
