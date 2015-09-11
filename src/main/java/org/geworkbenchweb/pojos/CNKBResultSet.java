package org.geworkbenchweb.pojos;

import java.util.Vector;

import javax.persistence.Entity;

import org.geworkbenchweb.plugins.cnkb.CellularNetWorkElementInformation;
import org.geworkbenchweb.plugins.cnkb.CellularNetworkPreference;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
public class CNKBResultSet extends AbstractPojo {
 
	private static final long serialVersionUID = -2518573713456876911L;
	
	private Vector<CellularNetWorkElementInformation> hits = null;
	private CellularNetworkPreference cnkbPref = null;
	private Long datasetId;
	
	public CNKBResultSet() {}
	
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CNKBResultSet for ");
		sb.append("Dataset ID ").append(datasetId).append("\nCellularNetWorkElementInformation vector:");
		int h = 0;
		for(CellularNetWorkElementInformation element : hits) {
			sb.append('\n').append(h++).append(':').append(element);
		}
		return sb.toString();
	}
}
