package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="dataset")
public class DataSet extends AbstractPojo {

	private static final long serialVersionUID = -2720207271844335675L;

	private String name;
	private String type;
	private Long owner;
	private Long workspace;

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
	
}
