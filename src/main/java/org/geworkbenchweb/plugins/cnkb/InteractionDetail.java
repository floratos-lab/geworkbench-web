package org.geworkbenchweb.plugins.cnkb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * The detail of one interaction.
 * 
 * This class maintains only one end of the interaction. 
 * Each instance would belong to a CellularNetWorkElementInformation, 
 * where the other end is maintained.
 */
public class InteractionDetail implements Serializable {

	private static final long serialVersionUID = 8301576364169764124L;
	
	private final String dSGeneId; /* unique identifier */
	private final String dSGeneName;
	private final String interactionType;
	private final Map<Short, Double> confidence;

	public InteractionDetail(String dSGeneId, String dSGeneName,
			String dbSource, String interactionType,
			String interactionId, Short evidenceId) {

		this.dSGeneId = dSGeneId;
		this.dSGeneName = dSGeneName;
		/* dbSource is ignored */		 
		this.interactionType = interactionType;
		/* evidenceId is ignored */
		confidence = new HashMap<Short, Double>();
	}

	public String getdSGeneId() {
		return dSGeneId;
	}

	public String getdSGeneName() {
		return dSGeneName;
	}

	public double getConfidenceValue(int usedConfidenceType) {
		Double d = confidence.get((short)usedConfidenceType);
		if(d!=null) return d;
		else return 0; //if usedConfidenceType is not found, return 0.
	}

	public List<Short> getConfidenceTypes() {
		return new ArrayList<Short>(confidence.keySet());
	}
	 
	public void addConfidence(double score, short type) {
		confidence.put(type, score);
	}

	public String getInteractionType() {
		return this.interactionType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InteractionDetail) {
			InteractionDetail mInfo = (InteractionDetail) obj;
			return dSGeneId.toString()
					.equals(mInfo.dSGeneId.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return dSGeneId.hashCode();
	}
}
