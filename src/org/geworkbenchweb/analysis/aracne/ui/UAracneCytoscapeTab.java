package org.geworkbenchweb.analysis.aracne.ui;

import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbenchweb.visualizations.Cytoscape;

import com.vaadin.ui.VerticalLayout;

/**
 * Builds ARACne results tab using Cytoscape 
 * @author Nikhil
 *
 */

public class UAracneCytoscapeTab extends VerticalLayout {


	private static final long serialVersionUID = 1L;

	public UAracneCytoscapeTab(AdjacencyMatrixDataSet adjMatrix) {
		
		setImmediate(true);
		
		/* Preparing data for cytoscape */
		ArrayList<String> nodes = new ArrayList<String>();
		ArrayList<String> edges = new ArrayList<String>();
		
		for(int i=0; i<adjMatrix.getMatrix().getEdges().size(); i++) {
			
			String edge 	= 	adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel() 
									+ "," 
									+ adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel();
			
			String node1 	= adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel(); 
			String node2 	= adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel()   ; 
			
			
			if(edges.isEmpty()) {
				
				edges.add(edge);
			
			}else if(!edges.contains(edge)) {
				
				edges.add(edge);
			}
			
			if(nodes.isEmpty()) {

				nodes.add(node1);
				nodes.add(node2);

			} else { 

				if(!nodes.contains(node1)) {

					nodes.add(node1);

				}

				if(!nodes.contains(node2)) {

					nodes.add(node2);

				}
			}
			
		}
		
		Cytoscape cy = new Cytoscape();
		cy.setImmediate(true);
		cy.setSizeFull();
		
		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
	    edgeArray = edges.toArray(edgeArray);
	    
		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);
		
		addComponent(cy);
	}
}
