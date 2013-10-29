package org.geworkbenchweb.dataset;

/**
 * 
 * A microarray data set.
 * 
 * @author zji
 * @version $Id$
 * 
 */
public class MicroarraySet {
	final int arrayNumber, markerNumber;
	final String[] arrayLabels, markerLabels;
	final float[][] values;
	/*
	 * For the reason of efficiency, this should be null in case no 'confidence'
	 * values are available.
	 */
	final float[][] confidence;

	MicroarraySet(int arrayNumber, int markerNumber, String[] arrayLabels,
			String[] markerLabels, float[][] values, float[][] confidence) {
		this.markerNumber = markerNumber;
		this.arrayNumber = arrayNumber;
		this.markerLabels = markerLabels;
		this.arrayLabels = arrayLabels;
		this.values = values;
		this.confidence = confidence;
	}
}