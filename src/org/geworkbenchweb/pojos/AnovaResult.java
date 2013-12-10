package org.geworkbenchweb.pojos;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.geworkbench.components.anova.data.AnovaOutput;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

@Entity
@Table(name="AnovaResult")
public class AnovaResult extends AbstractPojo {

	private static final long serialVersionUID = 6128433647769241559L;

	private  double[][] result2DArray;
	private int[] featuresIndexes;
	private double[] significances;

	private String[] selectedArraySetNames;	

	public AnovaResult(){}

	public AnovaResult(AnovaOutput anovaOutput, String[] selectedArraySetNames){
		result2DArray = anovaOutput.getResult2DArray();
		featuresIndexes = anovaOutput.getFeaturesIndexes();
		significances = anovaOutput.getSignificances();
		
		this.selectedArraySetNames = selectedArraySetNames;
	}

	public double[][] getResult2DArray() {
		return result2DArray;
	}

	public void setResult2DArray(double[][] result2dArray) {
		result2DArray = result2dArray;
	}

	public int[] getFeaturesIndexes() {
		return featuresIndexes;
	}

	public void setFeaturesIndexes(int[] featuresIndexes) {
		this.featuresIndexes = featuresIndexes;
	}

	public double[] getSignificances() {
		return significances;
	}

	public void setSignificances(double[] significances) {
		this.significances = significances;
	}

	public String[] getSelectedArraySetNames() {
		return selectedArraySetNames;
	}

	public void setSelectedArraySetNames(String[] selectedArraySetNames) {
		this.selectedArraySetNames = selectedArraySetNames;
	}
}
