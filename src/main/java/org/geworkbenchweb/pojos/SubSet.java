package org.geworkbenchweb.pojos;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="subset")
public class SubSet extends AbstractPojo {

	private static final long serialVersionUID = -2720207271844335675L;

	/* two possible types */
	public final static String SET_TYPE_MICROARRAY = "microarray";
	public final static String SET_TYPE_MARKER = "marker";

	private String name;
	private String type;
	private Long owner;
	private Long parent;
	private List<String> positions;


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
	
	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}
	
	public List<String> getPositions() {
		return positions;
	}
	
	public void setPositions(List<String> positions) {
		this.positions = positions;
	}
	
	
}
