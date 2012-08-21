package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="project")
public class Project extends AbstractPojo {

	private static final long serialVersionUID = -2720207271844335675L;

	private String name;
	private String description;
	private Long workspace;
	private Long owner;

	public Long getOwner() {
		return owner;
	}

	public void setOwner(Long owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	
	public Long getWorkspaceId() {
		return workspace;
	}

	public void setWorkspaceId(Long workspace) {
		this.workspace = workspace;
	}
	
}
