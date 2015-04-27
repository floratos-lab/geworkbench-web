package org.geworkbenchweb.visualizations;

import java.io.Serializable;

public class Barcode  implements Serializable{	
	 
	private static final long serialVersionUID = -6329206956190111539L;
	private String gene;
	private int position;
	private int colorIndex;
	private int arrayIndex;

	public Barcode(String gene, int position, int colorIndex, int arrayIndex)
	{
		this.gene = gene;
		this.position = position;
		this.colorIndex = colorIndex;
		this.arrayIndex = arrayIndex;		 
	}
	
	public String getGene() {
		return this.gene;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public int getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getColorIndex() {
		return this.colorIndex;
	}

	public void setColorIndex(int colorIndex) {
		this.colorIndex = colorIndex;
	}

	public int getArrayIndex() {
		return this.arrayIndex;
	}

	public void setArrayIndex(int arrayIndex) {
		this.arrayIndex = arrayIndex;
	}

}
