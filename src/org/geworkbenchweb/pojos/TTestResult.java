package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.geworkbench.components.ttest.data.TTestOutput;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/* This maps TTestOutput - at least for the initial design. */
@Entity
@Table(name="TTestResult")
public class TTestResult extends AbstractPojo {

	private static final long serialVersionUID = -7016923859167201590L;
	
	private double[] foldChange;
	private double[] pValue;
	private int[] significantIndex;
	private double[] tValue;
	
	public TTestResult(){}

	public TTestResult(TTestOutput tTestOutput){
		foldChange = tTestOutput.foldChange;
		pValue = tTestOutput.pValue;
		significantIndex = tTestOutput.significanceIndex;
		tValue = tTestOutput.tValue;
	}

	public double[] getFoldChange() {
		return foldChange;
	}

	public void setFoldChange(double[] foldChange) {
		this.foldChange = foldChange;
	}

	public double[] getpValue() {
		return pValue;
	}

	public void setpValue(double[] pValue) {
		this.pValue = pValue;
	}

	public int[] getSignificantIndex() {
		return significantIndex;
	}

	public void setSignificantIndex(int[] significantIndex) {
		this.significantIndex = significantIndex;
	}

	public double[] gettValue() {
		return tValue;
	}

	public void settValue(double[] tValue) {
		this.tValue = tValue;
	}
}
