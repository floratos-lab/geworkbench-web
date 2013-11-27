package org.geworkbenchweb.pojos;

 
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="preference")
public class Preference extends AbstractPojo {  
  
	private static final long serialVersionUID = 748965788408502114L;

	@Column(columnDefinition="BLOB")
	private byte[] value; 	
	 
	private String name;
	
	private String type;
	
	private Long owner;
	private Long dataSet;
	 	
	
	public byte[] getValue() {
		return value;
	}
	
	public void setValue(byte[] value) {
		this.value = value;
		 
	}
	
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	public Long getDataSet() {
		return dataSet;
	}

	public void setDataSet(Long dataSet) {
		this.dataSet = dataSet;
	}
	
	 
	
	
}
