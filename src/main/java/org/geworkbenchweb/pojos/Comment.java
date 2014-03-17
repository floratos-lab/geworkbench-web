package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="comment")
public class Comment extends AbstractPojo {

	private static final long serialVersionUID = -2720207271844335675L;

	private String comment;
	private Long parent;
	private java.sql.Timestamp timestamp;
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Long getParent() {
		return parent;
	}
	
	public void setParent(Long parent) {
		this.parent = parent;
	}
	
	public java.sql.Timestamp getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(java.sql.Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}
