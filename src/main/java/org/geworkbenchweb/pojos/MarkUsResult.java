package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="MarkusResult")
public class MarkUsResult extends AbstractPojo {

	private static final long serialVersionUID = -4354273009526225587L;
	
	private String result;
	
	public MarkUsResult(){}

	public MarkUsResult(String result){
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
