package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="annotation")
public class Annotation extends AbstractPojo {

	private static final long serialVersionUID = 4705480457340637172L;

	//private byte[] annotation;
	private String name;
	private String type;
	private Long owner;
	
	public Annotation(){}

	public Annotation(String name, String type){
		this.name = name;
		this.type = type;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String s){
		name = s;
	}
	public String getType(){
		return type;
	}
	public void setType(String s){
		type = s;
	}
	public Long getOwner() {
		return owner;
	}
	public void setOwner(Long owner) {
		this.owner = owner;
	}

}
