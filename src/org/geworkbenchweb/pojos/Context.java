package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="context")
public class Context  extends AbstractPojo{
	private static final long serialVersionUID = -2698052502056966328L;

	private String name;
	private String type;
	private Long datasetid;
	
	public Context(){}

	public Context(String c, String t, Long i){
		name = c;
		type = t;
		datasetid = i;
	}
	public Long getDatasetId(){
		return datasetid;
	}
	public void setDatasetId(Long id){
		datasetid = id;
	}
	public String getName(){
		return name;
	}
	public void setName(String cont){
		name = cont;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String toString(){
		return name;
	}
}
