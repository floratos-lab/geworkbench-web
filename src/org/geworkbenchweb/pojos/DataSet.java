package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Lob;
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
	
	// TODO the way that actual data type is wiped out is very dangerous. we need a reasonable design here
	@Lob
	private byte[] data;
	
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	// FIXME the idea of hard-coded and inconsistent use of type name is very dangerous and already broke many reasonable behaviors of the application 
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
