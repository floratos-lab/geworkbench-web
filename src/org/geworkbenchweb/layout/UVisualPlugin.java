package org.geworkbenchweb.layout;

import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.analysis.CNKB.ui.UCNKBTab;
import org.geworkbenchweb.analysis.hierarchicalclustering.ui.UClustergramTab;
import org.geworkbenchweb.visualizations.Cytoscape;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;

public class UVisualPlugin extends TabSheet implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID 				= 	1L;
	
	User user = SessionHandler.get();
	
	private Table dataTable;
	
	private static final String DATA_OPERATIONS 			= 	"Data Operations";
	
	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";
	
	private static final String MARKER_HEADER 				= 	"Marker";
	
	private DSMicroarraySet maSet;

	public UVisualPlugin(Object dataSet, String dataType, String action) {

		addListener(this);
		setSizeFull();
		
		if(dataType.contentEquals("Expression File")) {

			maSet 							= 	(DSMicroarraySet) dataSet;
			UDataTab dataOp					= 	new UDataTab(maSet, action);
			dataTable 						= 	new Table();
			
			dataOp.setCaption(DATA_OPERATIONS);
			dataOp.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataOp); 
		
			dataTable.setStyleName("small striped");
			dataTable.setSizeFull();
			dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
			dataTable.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataTable);

		} else if (dataType.equalsIgnoreCase("CNKB")) {
			
			@SuppressWarnings("unchecked")
			Vector<CellularNetWorkElementInformation> hits 	=	(Vector<CellularNetWorkElementInformation>) dataSet;
	        UCNKBTab cnkbTab 								= 	new UCNKBTab(hits);
	        
			addTab(cnkbTab, "CNKB Results", null);		
			
			Cytoscape cy = new Cytoscape();
			cy.setImmediate(true);
			cy.setSizeFull();
			cy.setCaption("Cytoscape");
			addTab(cy);
			
		} else {
			
			CSHierClusterDataSet results 	=  	(CSHierClusterDataSet) dataSet;
	        UClustergramTab dendrogramTab 	= 	new UClustergramTab(results);
	        CSMicroarraySet	data			= 	(CSMicroarraySet) results.getParentDataSet();
	        
	        //Height and width of the visualization are calculted based on number of phenotypes and markers
	        setHeight(((data.getMarkers().size()*5) + 600) + "px");
	        setWidth(((data.size()*20) + 600) + "px");
			addTab(dendrogramTab, "Dendrogram", null);		
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
