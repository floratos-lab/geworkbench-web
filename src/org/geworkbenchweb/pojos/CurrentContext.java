package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="currentcontext")
public class CurrentContext extends AbstractPojo {
	private static final long serialVersionUID = 8913783806524692411L;
	private Long datasetid;
	private Long contextid;

	public Long getDatasetId(){
		return datasetid;
	}
	public void setDatasetId(Long id){
		datasetid = id;
	}
	public Long getContextId(){
		return contextid;
	}
	public void setContextId(Long id){
		contextid = id;
	}
}
