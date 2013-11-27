package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="PdbFileInfo")
public class PdbFileInfo extends AbstractPojo {
	
	private static final long serialVersionUID = 533814689112371101L;
	
	private String filename;
	// TODO chain number
	
	public PdbFileInfo(){}

	public PdbFileInfo(String filename){
		this.setFilename(filename);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
