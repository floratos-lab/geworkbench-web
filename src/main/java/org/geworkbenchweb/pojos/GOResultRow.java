package org.geworkbenchweb.pojos;

import javax.persistence.Entity;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
public class GOResultRow extends AbstractPojo {

	private static final long serialVersionUID = 595561473364599648L;

	private String name;
	private String namespace;
	private double p;
	private double pAdjusted;
	private int popCount;
	private int studyCount;
	
	public GOResultRow() {}
	
	public GOResultRow(String name, String namespace, double p, double pAdjusted, int popCount, int studyCount) {
		this.name = name;
		this.namespace = namespace;
		this.p = p;
		this.pAdjusted = pAdjusted;
		this.popCount = popCount;
		this.studyCount = studyCount;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public double getP() {
		return p;
	}
	public void setP(double p) {
		this.p = p;
	}
	public double getpAdjusted() {
		return pAdjusted;
	}
	public void setpAdjusted(double pAdjusted) {
		this.pAdjusted = pAdjusted;
	}
	public int getPopCount() {
		return popCount;
	}
	public void setPopCount(int popCount) {
		this.popCount = popCount;
	}
	public int getStudyCount() {
		return studyCount;
	}
	public void setStudyCount(int studyCount) {
		this.studyCount = studyCount;
	}

}
