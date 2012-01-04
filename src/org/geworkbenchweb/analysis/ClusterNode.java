package org.geworkbenchweb.analysis;

import java.io.Serializable;

public class ClusterNode implements Serializable {
	private static final long serialVersionUID = 4940422039713813925L;
	
	private boolean isLeaf = false;
	private ClusterNode child1 = null;
	private ClusterNode child2 = null;
	private String text = null;
	
	// required by GWT architecture
	ClusterNode() {
	}
	
	public ClusterNode(ClusterNode child1, ClusterNode child2) {
		this.child1 = child1;
		this.child2 = child2;
		isLeaf = false;
	}
	
	public ClusterNode(String text) {
		this.text = text;
		isLeaf = true;
	}
	
	public String getText() {
		if(isLeaf) return text;
		else return "Non-leaf";
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}
	
	public ClusterNode getChild1() {
		return child1;
	}

	public ClusterNode getChild2() {
		return child2;
	}
	
	public String toString() {
		if(isLeaf) return text;
		
		StringBuffer sb = new StringBuffer(this.hashCode()+" children: "+child1.hashCode()+" "+child1.hashCode()+"\n");
		sb.append(child1+"\n");
		sb.append(child2+"\n");
		return sb.toString();
	}
}