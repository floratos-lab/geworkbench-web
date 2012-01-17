package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.cytographer.Cytographer;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;

public class VisualPlugin extends TabSheet implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID 				= 	1L;
	
	User user = SessionHandler.get();
	
	private Table dataTable;
	
	private static final String DATA_OPERATIONS 			= 	"Data Operations";
	
	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";
	
	private static final String MARKER_HEADER 				= 	"Marker";
	
	private DSMicroarraySet maSet;
	
	public VisualPlugin(Object dataSet, String dataType, String action) {

		addListener(this);
		setSizeFull();
	
		if(dataType.contentEquals("Expression File")) {

			maSet 							= 	(DSMicroarraySet) dataSet;
			DataTab dataOp					= 	new DataTab(maSet, action);
			dataTable 						= 	new Table();
			dataOp.setCaption(DATA_OPERATIONS);
			dataTable.setStyleName("small striped");
			dataTable.setSizeFull();
			dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
		
			dataOp.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataOp);
		
			dataTable.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataTable);

		} else {

			VerticalLayout dataRes = new VerticalLayout();
			dataRes.setSizeFull();
			dataRes.setCaption("Analysis Results");
			CyNetwork cyNetwork = Cytoscape.createNetwork("network1", false);

			CyNode node0 = Cytoscape.getCyNode("rain", true);
			CyNode node1 = Cytoscape.getCyNode("rainbow", true);
			CyNode node2 = Cytoscape.getCyNode("rabbit", true);
			CyNode node3 = Cytoscape.getCyNode("yellow", true);

			cyNetwork.addNode(node0);
			cyNetwork.addNode(node1);
			cyNetwork.addNode(node2);
			cyNetwork.addNode(node3);

			CyEdge edge0 = Cytoscape.getCyEdge(node0, node1, Semantics.INTERACTION, "pp", true);
			CyEdge edge1 = Cytoscape.getCyEdge(node0, node2, Semantics.INTERACTION, "pp", true);
			CyEdge edge2 = Cytoscape.getCyEdge(node0, node3, Semantics.INTERACTION, "pp", true);

			cyNetwork.addEdge(edge0);
			cyNetwork.addEdge(edge1);
			cyNetwork.addEdge(edge2);
			
			CyNetworkView view = Cytoscape.createNetworkView(cyNetwork);
			Cytographer graph = new Cytographer(cyNetwork, view , "test", 800, 600);
			dataRes.addComponent(graph);
			dataRes.setComponentAlignment(graph, Alignment.TOP_CENTER);
			
			dataRes.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataRes);

		}
		
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		
		if(event.getTabSheet().getSelectedTab().getCaption() == MICROARRAY_TABLE_CAPTION){
		
			dataTable.setContainerDataSource(tabularView(maSet));
			dataTable.setColumnWidth(MARKER_HEADER, 150);

		}
	} 
	
	public IndexedContainer tabularView(DSMicroarraySet maSet) {
	
		String[] colHeaders 			= 	new String[(maSet.size())+1];
		IndexedContainer dataIn 		= 	new IndexedContainer();

		for(int j=0; j<maSet.getMarkers().size();j++) {
			
			Item item 					= 	dataIn.addItem(j);
			
			for(int k=0;k<=maSet.size();k++) {
				
				if(k == 0) {
					
					colHeaders[k] 		= 	MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getMarkers().get(j));
				
				} else {
					
					colHeaders[k] 		= 	maSet.get(k-1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getValue(j, k-1));
				
				}
			}
		}
		
		return dataIn;
	
	}
	
}
