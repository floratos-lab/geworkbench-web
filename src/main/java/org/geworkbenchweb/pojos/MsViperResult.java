package org.geworkbenchweb.pojos;

 
import java.util.List;
import java.util.Map;
 

import javax.persistence.Entity;
import javax.persistence.Table;

import org.geworkbenchweb.visualizations.Barcode;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="msviperresult")
public class MsViperResult extends AbstractPojo {
   
	private static final long serialVersionUID = -2037036813434099354L;

	private String label;

	private String[] mrs;
	private String[][] mrsResult;
	private String[][] shadowResult;
	private Map<String, List<String>> shadow_pairs;  	 
	private Map<String, List<String>> leadingEdges;  
	private Map<String, List<String>> regulons; 	 
	private Map<String, Double> mrs_signatures;
	private Map<String, Integer> ranks;
	private double minVal = 0;
	private double maxVal = 0;
	
	private Map<String, List<Barcode>> barcodes;
	
	public MsViperResult() {
	}
	
	public String getLabel() {
		return label;
	}	
	public void setLabel(String label) {
		this.label = label;
	} 

	public String[] getMrs() {
		return this.mrs;
	}
	public void setMrs(String[] mrs) {
		this.mrs = mrs;
	}
	 

	public String[][] getMrsResult() {
		return mrsResult;
	}

	public void setMrsResult(String[][] mrsResult) {
		this.mrsResult = mrsResult;
	}
	
	public String[][] getShadowResult() {
		return shadowResult;
	}

	public void setShadowResult(String[][] shadowResult) {
		this.shadowResult = shadowResult;
	}
	
	public Map<String, List<String>> getShadow_pairs() {
		return this.shadow_pairs;
	}
	public void setShadow_pairs(Map<String, List<String>> shadow_pairs) {
		this.shadow_pairs = shadow_pairs;
	}
	

	public Map<String, List<String>> getLeadingEdges() {
		return this.leadingEdges;
	}
	public void setLeadingEdges(Map<String, List<String>> leadingEdges) {
		this.leadingEdges = leadingEdges;
	}
	 
	public Map<String, List<String>> getRegulons() {
		return this.regulons;
	}
	public void setRegulons(Map<String, List<String>> regulons) {
		this.regulons = regulons;
	}
	 
	public Map<String, Double> getMrs_signatures() {
		return this.mrs_signatures;
	}
	public void setMrs_signatures(Map<String, Double> mrs_signatures) {
		this.mrs_signatures = mrs_signatures;
	}
	public Map<String, Integer> getRanks() {
		return this.ranks;
	}
	public void setRanks(Map<String, Integer> ranks) {
		this.ranks = ranks;
	}
	public double getMinVal() {
		return this.minVal;
	}
	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}
	
	public double getMaxVal() {
		return this.maxVal;
	}
	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}
	
	public Map<String, List<Barcode>> getBarcodes() {
		return this.barcodes;
	}
	public void setBarcodes(Map<String, List<Barcode>> barcodes) {
		this.barcodes = barcodes;
	}
}
