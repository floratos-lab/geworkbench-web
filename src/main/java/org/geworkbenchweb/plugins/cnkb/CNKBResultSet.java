package org.geworkbenchweb.plugins.cnkb;

import java.util.Vector;
import java.io.Serializable;

import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.CellularNetworkPreference;

public class CNKBResultSet implements Serializable{
 
	private static final long serialVersionUID = -2518573713456876911L;
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	private CellularNetworkPreference cnkbPref = null;
	
	public CNKBResultSet(Vector<CellularNetWorkElementInformation> hits, CellularNetworkPreference cnkbPref)
	{
		this.hits = hits;
		this.cnkbPref = cnkbPref;
	}
	
	public Vector<CellularNetWorkElementInformation> getCellularNetWorkElementInformations()
	{
		return hits;
	}
	
	public CellularNetworkPreference getCellularNetworkPreference()
	{
		return cnkbPref;
		
	}
}
