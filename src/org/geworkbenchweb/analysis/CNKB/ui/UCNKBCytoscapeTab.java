package org.geworkbenchweb.analysis.CNKB.ui;

import java.util.ArrayList;
import java.util.Vector;

import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.visualizations.Cytoscape;

import com.vaadin.ui.VerticalLayout;

/**
 * Create and populates cytoscape tab for CNKB
 * @author Nikhil
 *
 */
public class UCNKBCytoscapeTab extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	public UCNKBCytoscapeTab(Vector<CellularNetWorkElementInformation> hits) {
		
		setImmediate(true);
		Cytoscape cy = new Cytoscape();
		addComponent(cy);
		
		/* Preparing data for cytoscape */
		ArrayList<String> nodes = new ArrayList<String>();
		ArrayList<String> edges = new ArrayList<String>();
		
		for(CellularNetWorkElementInformation cellular: hits) {

			try {

				InteractionDetail[] interactions = cellular.getInteractionDetails();
				for(InteractionDetail interaction: interactions) {
					
					if(edges.isEmpty()) {
						
						edges.add(interaction.getdSGeneName1() + "," + interaction.getdSGeneName2());
					
					}else if(!edges.contains(interaction.getdSGeneName1() + "," + interaction.getdSGeneName2())){
						
						edges.add(interaction.getdSGeneName1() + "," + interaction.getdSGeneName2());
					}
					
					if(nodes.isEmpty()) {

						nodes.add(interaction.getdSGeneName1());
						nodes.add(interaction.getdSGeneName2());

					} else { 

						if(!nodes.contains(interaction.getdSGeneName1())) {

							nodes.add(interaction.getdSGeneName1());

						}

						if(!nodes.contains(interaction.getdSGeneName2())) {

							nodes.add(interaction.getdSGeneName2());

						}
					}
				}

			}catch (Exception e) {

				//TODO: Handle Null pointer exception
			}
		}	
		
		
		
		cy.setImmediate(true);
		cy.setSizeFull();
		
		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
	    edgeArray = edges.toArray(edgeArray);
	    
		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);
		
		//addComponent(cy);
		
	}
}
