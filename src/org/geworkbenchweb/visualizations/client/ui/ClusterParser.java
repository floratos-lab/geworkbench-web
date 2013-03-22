/**
 * 
 */
package org.geworkbenchweb.visualizations.client.ui;

/**
 * The parser to read string presentation and create a tree used by VDendrogram.
 * 
 * @author zji
 *
 */
public class ClusterParser {
	private int index = 0; // keep track of the position parsed

	final static private int deltaH = 5; // the increment of the dendrogram height

	ClusterNode parse(String clusterString, int deltaX) {
		int begin = 0;
		int end = clusterString.length() - 1;
		ClusterNode root = prepareClusterNodeTree(begin, end, clusterString.toCharArray(), deltaX);
	
		index = 0; // reset the pointer once the job is done
		
		return root;
	}
	
	/**
	 * Prepare the tree of cluster node coordinates to draw the dendrogram.
	 * 
	 * precondition: clusters[left]=='(', clusters[right]=')'
	 */
	private ClusterNode prepareClusterNodeTree(int left, int right, final char[] clusters,
			int deltaX) { // side-way width
		if(right-left<=1) { // 1: leaf node; -1: empty cluster
			ClusterNode m = new ClusterNode(index*deltaX, (index+1)*deltaX, 0, null, null);
			index++;
			return m;
		}

		// the general case that includes child clusters, and they must be two.
		int split = split(left + 1, right -1, clusters);

		// by now, [0, index) is the left child; [index, length-1] is the right child
		ClusterNode leftChild = prepareClusterNodeTree(left+1, split-1, clusters, deltaX);
		ClusterNode rightChild = prepareClusterNodeTree(split, right-1, clusters, deltaX);
		double x1 = leftChild.getMidPointX();
		double x2 = rightChild.getMidPointX();
		double y = 0.5+(int)( Math.max(leftChild.y, rightChild.y) + deltaH);
	
		return new ClusterNode(x1, x2, y, leftChild, rightChild);
	}

	// assume [begin, end] contains two nodes, returns the starting position of the second one
	static private int split(int begin, int end, char[] clusters) {
		int split = begin;
		int count = 0;
		do {
			if(clusters[split]=='(') {
				count++;
			} else { // this must be ')'. 
				count--;
			}
			split++;
		} while (count>0 || split==end);
		return split;
	}

}
