package org.geworkbenchweb.pojos;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="annotation")
public class Annotation extends AbstractPojo {

	private static final long serialVersionUID = 71847911925116119L;
	
	private String name;
	private String type;
	private Long owner;
	
	@OneToMany(cascade = CascadeType.PERSIST)
	private List<AnnotationEntry> annotationEntries;
	
	public Annotation(){}

	public Annotation(String name, String type, List<AnnotationEntry> entries){
		this.name = name;
		this.type = type;
		annotationEntries = entries;
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

	public List<AnnotationEntry> getAnnotationEntries() {
		return annotationEntries;
	}

	public void setAnnotationEntries(List<AnnotationEntry> annotationEntries) {
		this.annotationEntries = annotationEntries;
	}

}
