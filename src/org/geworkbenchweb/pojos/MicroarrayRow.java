package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="MicroarrayRow")
public class MicroarrayRow extends AbstractPojo {

	private static final long serialVersionUID = 4584474792904545145L;

	/* calling this field "values" caused SQL syntax error! */
	private float[] valueArray;
	
	public MicroarrayRow(){}

	public MicroarrayRow(float[] values){
		this.valueArray = values;
	}

	public float[] getValueArray() {
		return valueArray;
	}
}
