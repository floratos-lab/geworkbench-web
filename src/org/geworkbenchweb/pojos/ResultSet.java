package org.geworkbenchweb.pojos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
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
	@Column(name = "DATE_FIELD")
	private java.sql.Date date;
	
	
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
	
	public Long getParent() {
		return parent;
	}
	
	public void setParent(Long parent) {
		this.parent = parent;
	}
	
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

	public java.sql.Date getDateField() {
		return date;
	}

	public void setDateField(java.sql.Date dateField) {
		this.date = dateField;
	}
	
}
