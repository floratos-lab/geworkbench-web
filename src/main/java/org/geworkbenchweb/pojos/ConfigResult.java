package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="configresult")
public class ConfigResult extends AbstractPojo {

	private static final long serialVersionUID = 1L;
	private Float[] kernel;
	private Float[] threshold;

	public ConfigResult(){}
	
	public ConfigResult(Float[] kernel, Float[] threshold){
		this.kernel = kernel;
		this.threshold = threshold;
	}
		
	public Float[] getKernel() {
		return kernel;
	}
	
	public void setKernel(Float[] kernel) {
		this.kernel = kernel;
	}

	public Float[] getThreshold() {
		return threshold;
	}

	public void setThreshold(Float[] threshold) {
		this.threshold = threshold;
	}
}
