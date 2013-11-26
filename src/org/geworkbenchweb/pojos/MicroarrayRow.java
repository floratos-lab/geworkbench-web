package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="MicroarrayRow")
public class MicroarrayRow extends AbstractPojo {

	private static final long serialVersionUID = 4584474792904545145L;

	@Lob
	private byte[] bytes;
	
	public MicroarrayRow(){}

	public MicroarrayRow(float[] values){
		this.bytes = ObjectConversion.convertToByte(values);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setValues(float[] values) {
		this.bytes = ObjectConversion.convertToByte(values);
	}

}
