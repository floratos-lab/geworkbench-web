package org.geworkbenchweb.pojos;

import javax.persistence.Entity;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
public class DataSet extends AbstractPojo {

	private static final long serialVersionUID = -2720207271844335675L;

	private String name;
	private String type;
	private Long owner;
	private Long workspace;
	private Long dataId; /* ID for the JPA representation of the actual data, for now microarray expression data MicroarrayDataset. */

	/* a short description. used in places like tool tip. */
	private String description = "";
	/* time stamp: additional information to identify the dataset, also useful to maintain an order of datasets. */
	private java.sql.Timestamp timestamp;
	
	public Long getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Long workspace) {
		this.workspace = workspace;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * Code is not changed since last revision,
	 * but now this field strictly refers to the class name of the data set, subclass of DSDataSet 
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Long getOwner() {
		return owner;
	}

	public void setOwner(Long owner) {
		this.owner = owner;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if(description==null) 
			this.description = "";
		else
			this.description = description;
	}

	public java.sql.Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(java.sql.Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Long getDataId() {
		return dataId;
	}

	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}
}
