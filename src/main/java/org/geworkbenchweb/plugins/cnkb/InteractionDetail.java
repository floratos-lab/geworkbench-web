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
	
	private List<InteractionParticipant> participantList; 
	private final String interactionType;
	private final Map<Short, Double> confidence;
	private Short evidenceId;

	public InteractionDetail(InteractionParticipant participant,
			String interactionType,
			Short evidenceId) {	 
		
		participantList = new ArrayList<InteractionParticipant>();
		participantList.add(participant);
		this.interactionType = interactionType;
		this.evidenceId = evidenceId;
		confidence = new HashMap<Short, Double>();
	}

	public void  addParticipant(InteractionParticipant participant) {
		if (participantList == null)	 
			participantList = new ArrayList<InteractionParticipant>();
		participantList.add(participant);
	}
	
	public List<InteractionParticipant> getParticipantList() {
		return participantList;
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
	
	public Short getEvidenceId() {
		return this.evidenceId;
	}

	 
}
