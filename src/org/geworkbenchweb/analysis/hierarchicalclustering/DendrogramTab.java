package org.geworkbenchweb.analysis.hierarchicalclustering;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;

import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DendrogramTab extends VerticalLayout{

	public DendrogramTab(Object dataSet, DSMicroarraySet maSet) {
		Dendrogram dendrogram = new Dendrogram();
		//dendrogram.setCaption("Dendrogram");
		dendrogram.setSizeFull();
		
		addComponent(dendrogram);
	}

}
