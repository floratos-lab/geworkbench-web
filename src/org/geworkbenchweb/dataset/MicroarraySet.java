package org.geworkbenchweb.dataset;

/**
 * 
 * A microarray data set.
 * 
 * @author zji
 * 
 */
public class MicroarraySet {
	public final int arrayNumber, markerNumber;
	public final String[] arrayLabels, markerLabels;
	public final float[][] values;
	/*
	 * For the reason of efficiency, this should be null in case no 'confidence'
	 * values are available.
	 */
	public final float[][] confidence;

	public MicroarraySet(int arrayNumber, int markerNumber, String[] arrayLabels,
			String[] markerLabels, float[][] values, float[][] confidence) {
		this.markerNumber = markerNumber;
		this.arrayNumber = arrayNumber;
		this.markerLabels = markerLabels;
		this.arrayLabels = arrayLabels;
		this.values = values;
		this.confidence = confidence;
	}
}