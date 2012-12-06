package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="subsetcontext")
public class SubSetContext extends AbstractPojo {
	private static final long serialVersionUID = 3227433291566280727L;
	private Long subsetid;
	private Long contextid;

	public Long getSubsetId(){
		return subsetid;
	}
	public void setSubsetId(Long id){
		subsetid = id;
	}
	public Long getContextId(){
		return contextid;
	}
	public void setContextId(Long id){
		contextid = id;
	}
}
