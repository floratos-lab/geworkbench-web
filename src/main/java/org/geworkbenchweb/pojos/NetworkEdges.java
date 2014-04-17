package org.geworkbenchweb.pojos;

import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;

/** The collection of the edges that start from one node. */
@Entity
public class NetworkEdges extends AbstractPojo {
	private static Log log = LogFactory.getLog(NetworkEdges.class);
	
	private static final long serialVersionUID = -6631142457019747243L;
	
	private String[] node2s = null;
	private double[] weights = null;
	
	public double[] getWeights() {
		return weights;
	}
	public void setWeights(double[] weights) {
		this.weights = weights;
	}
	public String[] getNode2s() {
		return node2s;
	}
	public void setNode2s(String[] node2s) {
		this.node2s = node2s;
	}

	public NetworkEdges() {}
	
	public NetworkEdges(List<String> node2s, List<Double> weights) {
		if(node2s.size()!=weights.size()) {
			log.error("The numbers of node2s and weights do not match.");
			return;
		}
		this.node2s = node2s.toArray(new String[0]);
		this.weights = new double[weights.size()];
		for(int i=0; i<weights.size(); i++) {
			this.weights[i] = weights.get(i);
		}
	}
	
	public int getCount() {
		return node2s.length;
	}
}
