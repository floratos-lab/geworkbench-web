package org.geworkbenchweb.visualizations;

 

public class Regulator {	 
	private String gene;
	private double pvalue;
	private String daColor;
	private String deColor;
	private int deRank = 0;
	
	public Regulator(String gene, double pvalue, String daColor, String deColor, int deRank)
	{
		this.gene = gene;
		this.pvalue = pvalue;
		this.daColor = daColor;
		this.deColor = deColor;
		this.deRank  = deRank;
		
	}
	 
	public String getGene() {
		return this.gene;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public double getPvalue() {
		return this.pvalue;
	}

	public void setScore(double pvalue) {
		this.pvalue = pvalue;
	}

	public String getDaColor() {
		return this.daColor;
	}

	public void setDaColor(String daColor) {
		this.daColor = daColor;
	}

	public String getDeColor() {
		return this.deColor;
	}

	public void setDeColor(String deColor) {
		this.deColor = deColor;
	}

	public int getDeRank() {
		return this.deRank;
	}

	public void setDeRank(int deRank) {
		this.deRank = deRank;
	}
	 

}
