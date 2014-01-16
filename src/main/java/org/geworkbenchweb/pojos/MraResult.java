package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="MraResult")
public class MraResult extends AbstractPojo {

	private static final long serialVersionUID = 2016757666577260694L;
	
	private String label;

	private String[][] result;
	
	public MraResult() {
	}
	
	public MraResult(String label, String[][] result) {
		this.label  = label;
		this.result = result;
	}

	public String[][] getResult() {
		return result;
	}

	public void setResult(String[][] result) {
		this.result = result;
	}

	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
}
