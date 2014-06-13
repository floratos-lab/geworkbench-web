package org.geworkbenchweb.plugins.cnkb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellularNetworkPreference implements java.io.Serializable {
	
	private static final long serialVersionUID = -2047397744290887425L;
	
	private final String title;
	private final List<String> displaySelectedInteractionTypes = new ArrayList<String>();
	private Short selectedConfidenceType = 0;
	private final List<Short> confidenceTypeList = new ArrayList<Short>();
	private final Map<Short, Double> maxConfidenceValueMap = new HashMap<Short, Double>();

	public CellularNetworkPreference(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return this.title;
	}

	/* FIXME replace this with an add method so we don't have to expose this list directly */
	public List<String> getDisplaySelectedInteractionTypes() {
		return this.displaySelectedInteractionTypes;
	}

	public Double getMaxConfidenceValue(Short confidenceType) {		 
			return maxConfidenceValueMap.get(confidenceType);
	}
	
	/* FIXME replace this with an put method so we don't have to expose this map directly */
	public Map<Short, Double> getMaxConfidenceValueMap() {		 
		return maxConfidenceValueMap ;
    }
	
	/* FIXME don't expose this list directly */
	public List<Short> getConfidenceTypeList()
	{
		return confidenceTypeList;
	}

	public Short getSelectedConfidenceType()
	{
		return selectedConfidenceType;
	}	

    public void setSelectedConfidenceType(Short type )
	{
    	selectedConfidenceType = type;
	}
}
