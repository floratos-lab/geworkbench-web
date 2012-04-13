package org.geworkbenchweb.analysis.CNKB.ui;

import java.util.HashMap;
import java.util.Vector;

import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.vaadin.vaadinvisualizations.PieChart;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

/**
 * This class displays CNKB results in a Table and also a graph
 * @author np2417
 */
public class UCNKBTab extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private VerticalSplitPanel tabPanel;
	
	public UCNKBTab(Vector<CellularNetWorkElementInformation> hits) {
	
		setSizeFull();
		setImmediate(true);
		
		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(40);
		tabPanel.setStyleName("small previews");
		tabPanel.setLocked(true);

		VerticalLayout graphLayout = new VerticalLayout();
		graphLayout.setSizeFull();
		
		PieChart pc = new PieChart();
        
        pc.setSizeFull();
        
        pc.add("Work", 7);
        pc.add("Play", 3);
        pc.add("Eat", 1);
        pc.add("Sleep", 6);
        pc.add("Do Vaadin", 7);
        pc.setOption("width", 400);
        pc.setOption("height", 400);
        pc.setOption("is3D", true);
		graphLayout.addComponent(pc);
		
		
		/* Results Table Code */
		Table dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.addStyleName("small striped");
		dataTable.setImmediate(true);
		
		IndexedContainer dataIn  = 	new IndexedContainer();

		for(int j=0; j<hits.size();j++) {
			
			Item item 	= 	dataIn.addItem(j);
			
			HashMap<String, Integer> interactionNumMap = hits.get(j).getInteractionNumMap();
			
			dataIn.addContainerProperty("Marker", String.class, null);
			dataIn.addContainerProperty("Gene", String.class, null);
			dataIn.addContainerProperty("Gene Type", String.class, null);
			dataIn.addContainerProperty("Annotation", String.class, null);
			dataIn.addContainerProperty("Modulator-TF #", Integer.class, null);
			dataIn.addContainerProperty("Protein-DNA #", Integer.class, null);
			dataIn.addContainerProperty("Protein-Protein #", Integer.class, null);
			
			item.getItemProperty("Marker").setValue(hits.get(j).getdSGeneMarker());
			if(hits.get(j).getdSGeneMarker().getShortName() == hits.get(j).getdSGeneMarker().getGeneName()) {
			
				item.getItemProperty("Gene").setValue("--");
				
			} else {
				
				item.getItemProperty("Gene").setValue(hits.get(j).getdSGeneMarker().getGeneName());
				
			}
			
			item.getItemProperty("Gene Type").setValue(hits.get(j).getGeneType());
			item.getItemProperty("Annotation").setValue(hits.get(j).getGoInfoStr());
			item.getItemProperty("Modulator-TF #").setValue(interactionNumMap.get("modulator-TF"));
			item.getItemProperty("Protein-DNA #").setValue(interactionNumMap.get("protein-dna"));
			item.getItemProperty("Protein-Protein #").setValue(interactionNumMap.get("protein-protein"));
			
		}
		
		dataTable.setContainerDataSource(dataIn);
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth("Annotation", 150);
		dataTable.setColumnHeaders(new String[] {"Marker", "Gene", "Gene Type", "Annotation", 
				"Modulator-TF #", "Protein-DNA #", "Protein-Protein #" });
		
		
		tabPanel.setFirstComponent(graphLayout);
		tabPanel.setSecondComponent(dataTable);
		addComponent(tabPanel);
	}
}
