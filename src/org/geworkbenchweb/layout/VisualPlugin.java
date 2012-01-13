package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.cytographer.client.ui.VCytographer;
import org.vaadin.cytographer.client.ui.VFocusDrawingArea;
import org.vaadin.cytographer.client.ui.VGraph;
import org.vaadin.cytographer.client.ui.VVisualStyle;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualPropertyType;

public class VisualPlugin extends TabSheet implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID 				= 	1L;
	
	User user = SessionHandler.get();
	
	private Table dataTable;
	
	private static final String DATA_OPERATIONS 			= 	"Data Operations";
	
	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";
	
	private static final String MARKER_HEADER 				= 	"Marker";
	
	public VisualPlugin(Object dataSet, String dataType) {

		addListener(this);
		setSizeFull();
	
		if(dataType.contentEquals("Expression File")) {

			DataTab dataOp					= 	new DataTab((DSMicroarraySet) dataSet);
			dataTable 						= 	new Table();
			dataOp.setCaption(DATA_OPERATIONS);
			dataTable.setStyleName("small striped");
			dataTable.setSizeFull();
			dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
			dataTable.setContainerDataSource(tabularView((DSMicroarraySet) dataSet));

			addTab(dataOp);
			addTab(dataTable);

		} else {

			VerticalLayout dataRes = new VerticalLayout();
			dataRes.setSizeFull();
			dataRes.setCaption("Analysis Results");
			dataRes.addComponent(new Label("Implementing the visualization for the results is pending !!"));

			addTab(dataRes);

		}
		
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		
		if(event.getTabSheet().getSelectedTab().getCaption() == MICROARRAY_TABLE_CAPTION){
		
			
			
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
