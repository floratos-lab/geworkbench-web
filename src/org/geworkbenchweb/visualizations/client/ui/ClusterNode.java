package org.geworkbenchweb.visualizations.client.ui;

/* Cluster node in terms of the coordinates to draw. */
class ClusterNode {
	final double x1, x2, y;
	final ClusterNode left, right;
	boolean selected = false;
	
	ClusterNode(double x1, double x2, double y, ClusterNode left, ClusterNode right) {
		this.x1 = x1; 
		this.x2 = x2;
		this.y = y;;
		this.left = left;
		this.right = right;
	}
	
	ClusterNode getSelected() {
		if(selected) return this;
		// note that left and right children are never selected at the same time if the parent is not selected
		if(left!=null) {
			ClusterNode highest = left.getSelected();
			if(highest!=null) return highest;
		}
		if(right!=null) {
			ClusterNode highest = right.getSelected();
			if(highest!=null) return highest;
		}
		return null;
	}

	double getMidPointX() {
		return 0.5+(int)((x1+x2)*0.5); // trick to create crisp line if width 1
	}
	
	void select(boolean s) {
		selected = s;
		if(left!=null) left.select(s);
		if(right!=null) right.select(s);
	}
}