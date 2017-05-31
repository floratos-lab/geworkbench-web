package org.geworkbenchweb.pojos;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="PbqdiResult")
public class PbqdiResult extends AbstractPojo {
	private static final long serialVersionUID = 1L;

	private Long owner;
	private java.sql.Timestamp timestamp;

	private String tumorType;
	private String sampleFileName;
	private int jobId;
	private Map<String, Integer> subtypes;

	public PbqdiResult() {}
	
	public PbqdiResult(Long owner, String tumorType, String sampleFileName, int jobId, Map<String, Integer> subtypes) {
		this.owner = owner;
		this.tumorType = tumorType;
		this.sampleFileName = sampleFileName;
		this.jobId = jobId;
		this.subtypes = subtypes;
	}

	public Long getOwner() {
		return owner;
	}

	public void setOwner(Long owner) {
		this.owner = owner;
	}

	public String getTumorType() {
		return tumorType;
	}

	public void setTumorType(String tumorType) {
		this.tumorType = tumorType;
	}

	public String getSampleFileName() {
		return sampleFileName;
	}

	public void setSampleFileName(String sampleFileName) {
		this.sampleFileName = sampleFileName;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public Map<String, Integer> getSubtypes() {
		return subtypes;
	}
	public void setSubtypes(Map<String, Integer> subtypes) {
		this.subtypes = subtypes;
	}
	public java.sql.Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(java.sql.Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}
