package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="datahistory")
public class DataHistory extends AbstractPojo {

	private static final long serialVersionUID = 1L;

	private Long parent;
	
	private String dataHistory;

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public String getData() {
		return dataHistory;
	}

	public void setData(String data) {
		this.dataHistory = data;
	}
}
