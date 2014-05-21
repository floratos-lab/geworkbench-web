package org.geworkbenchweb.pojos;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="resultset")
public class ResultSet extends AbstractPojo {

	private static final long serialVersionUID = -2720207271844335675L;

	private String name;
	private Long parent;
	private String type;
	private Long owner;
	private Long dataId; /* ID for the JPA representation of the actual data, for now microarray expression data MicroarrayDataset. */
	private java.sql.Timestamp timestamp;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Long getParent() {
		return parent;
	}
	
	public void setParent(Long parent) {
		this.parent = parent;
	}
	
	public String getType() {
		return type;
	}

	// this field strictly refers to the class name of the result. for now it can be anything; but eventually it should be subclass of a specific interface 
	public void setType(String type) {
		this.type = type;
	}
	
	public Long getOwner() {
		return owner;
	}

	public void setOwner(Long owner) {
		this.owner = owner;
	}

	public java.sql.Timestamp getTimestamp() {
		//return timestamp;
		Timestamp t =	Timestamp.valueOf("2014-05-02 14:32:29");
		return t;
	}

	public void setTimestamp(java.sql.Timestamp timestamp) {
		//this.timestamp = timestamp;
	}

	public Long getDataId() {
		return dataId;
	}

	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}
	
}
