package org.geworkbenchweb.plugins.cnkb;

import java.util.Vector;
import java.io.Serializable;

public class CNKBResultSet implements Serializable{
 
	private static final long serialVersionUID = -2518573713456876911L;
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	private CellularNetworkPreference cnkbPref = null;
	final private Long datasetId;
	
	public CNKBResultSet(Vector<CellularNetWorkElementInformation> hits, CellularNetworkPreference cnkbPref, Long datasetId)
	{
		this.hits = hits;
		this.cnkbPref = cnkbPref;
		this.datasetId = datasetId;
	}
	
	public Vector<CellularNetWorkElementInformation> getCellularNetWorkElementInformations()
	{
		return hits;
	}
	
	public CellularNetworkPreference getCellularNetworkPreference()
	{
		return cnkbPref;
		
	}

	public Long getDatasetId() {
		return datasetId;
	}
}
