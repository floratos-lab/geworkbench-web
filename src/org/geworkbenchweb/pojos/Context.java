package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="context")
public class Context  extends AbstractPojo{
	private static final long serialVersionUID = -2698052502056966328L;

	private String name;
	private Long datasetid;
	
	public Context(){}

	public Context(String c, Long i){
		name = c;
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
	public String toString(){
		return name;
	}
}
