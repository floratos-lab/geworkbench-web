package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="datahistory")
public class DataHistory extends AbstractPojo {

	private static final long serialVersionUID = 1L;

	private Long parent;
	private int flag;
	
	@Lob
	private byte[] dataHistory;

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public byte[] getData() {
		return dataHistory;
	}

	public void setData(byte[] data) {
		this.dataHistory = data;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
}
