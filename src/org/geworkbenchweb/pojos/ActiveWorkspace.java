package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="activeworkspace")
public class ActiveWorkspace extends AbstractPojo {

	private static final long serialVersionUID = -7606985180530547138L;
	
	private Long owner;
	private Long workspace;
	
	public Long getOwner() {
		return owner;
	}
	
	public void setOwner(Long owner) {
		this.owner = owner;
	}
	
	public Long getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Long workspace) {
		this.workspace = workspace;
	}
	
}
