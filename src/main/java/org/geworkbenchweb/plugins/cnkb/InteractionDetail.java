package org.geworkbenchweb.plugins.cnkb;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * The detail of one interaction.
 */
public class InteractionDetail implements Serializable {

	private static final long serialVersionUID = 8301576364169764124L;
	
	private final String dSGeneId;
	private final String dSGeneName;
	private final String interactionType;
	private final List<Confidence> confidenceList;

	public InteractionDetail(String dSGeneId, String dSGeneName,
			String dbSource, String interactionType,
			String interactionId, Short evidenceId) {

		this.dSGeneId = dSGeneId;
		this.dSGeneName = dSGeneName;
		/* dbSource is ignored */		 
		this.interactionType = interactionType;
		/* evidenceId is ignored */
		confidenceList = new ArrayList<Confidence>();
	}

	public String getdSGeneId() {
		return dSGeneId;
	}

	public String getdSGeneName() {
		return dSGeneName;
	}

	public double getConfidenceValue(int usedConfidenceType) {
		for (int i=0; i<confidenceList.size(); i++)
			if (confidenceList.get(i).getType() == usedConfidenceType)
				return confidenceList.get(i).getScore();
		//if usedConfidenceType is not found, return 0.
		return 0;
	}

	public List<Short> getConfidenceTypes() {
		List<Short> types = new ArrayList<Short>();
		for (int i=0; i<confidenceList.size(); i++)
			types.add(confidenceList.get(i).getType());
		return types;
	}
	 
	public void addConfidence(double score, short type) {
		confidenceList.add(new Confidence(score, type));
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
	
	private class Confidence implements Serializable
	{
		 
		private static final long serialVersionUID = 4151510293677929250L;
		private double score;
		private short type;
		
		Confidence(double score, short type)
		{
			this.score = score;
			this.type = type;
		}
		
		public double getScore()
		{
			return score;
		}
		
		public short getType()
		{
			return type;
		}
		
	}

}
