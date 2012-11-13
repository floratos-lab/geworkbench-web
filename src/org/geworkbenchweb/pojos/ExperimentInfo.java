package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="experimentinfo")
public class ExperimentInfo extends AbstractPojo {

	private static final long serialVersionUID = 1L;

	private Long parent;
	
	@Lob
	private byte[] dataInfo;

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public byte[] getInfo() {
		return dataInfo;
	}

	public void setInfo(byte[] info) {
		this.dataInfo = info;
	}
}
