/**
 * 
 */
package org.geworkbenchweb.analysis;

import java.io.Serializable;

/**
 * @author zji
 *
 */
public class HierClusterTestResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4977671549370119994L;
	ClusterNode root = null;
	
	// this is required by GWT compiler to be 'default instantiable'
	public HierClusterTestResult() {
	}

	private double[][] expressionValue = null;
	public double[][] getExpressionValue() {
		return expressionValue;
	}

	private double maxValue, minValue, sigma;
	public HierClusterTestResult(ClusterNode c, double[][] v, double minValue, double maxValue, double sigma) {
		root = c;
		expressionValue = v;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.sigma = sigma;
	}
	
	public ClusterNode getRoot() {
		return root;
	}

	@Override
	public String toString() {
		return "hierarchical clustering result:\n"+root;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getSigma() {
		return sigma;
	}
}