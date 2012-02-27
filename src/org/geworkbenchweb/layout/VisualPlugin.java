package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbenchweb.analysis.hierarchicalclustering.DendrogramTab;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window.Notification;

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
			//dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
			MenuBar menubar = new MenuBar();
			menubar.setWidth("100%");
			// Save reference to individual items so we can add sub-menu items to
	        // them
	        final MenuBar.MenuItem file = menubar.addItem("File", null);
	        final MenuBar.MenuItem newItem = file.addItem("New", null);
	        file.addItem("Open file...", menuCommand);
	        file.addSeparator();

	        // Add a style name for a menu item, then use CSS to alter the visuals
	        file.setStyleName("file");

	        newItem.addItem("File", menuCommand);
	        newItem.addItem("Folder", menuCommand);
	        newItem.addItem("Project...", menuCommand);

	        file.addItem("Close", menuCommand);
	        file.addItem("Close All", menuCommand).setStyleName("close-all");
	        file.addSeparator();

	        file.addItem("Save", menuCommand);
	        file.addItem("Save As...", menuCommand);
	        file.addItem("Save All", menuCommand);

	        final MenuBar.MenuItem edit = menubar.addItem("Edit", null);
	        edit.addItem("Undo", menuCommand);
	        edit.addItem("Redo", menuCommand).setEnabled(false);
	        edit.addSeparator();

	        edit.addItem("Cut", menuCommand);
	        edit.addItem("Copy", menuCommand);
	        edit.addItem("Paste", menuCommand);
	        edit.addSeparator();

	        final MenuBar.MenuItem find = edit.addItem("Find/Replace", menuCommand);

	        // Actions can be added inline as well, of course
	        find.addItem("Google Search", new Command() {
	            /**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void menuSelected(MenuItem selectedItem) {
	                getWindow().open(new ExternalResource("http://www.google.com"));
	            }
	        });
	        find.addSeparator();
	        find.addItem("Find/Replace...", menuCommand);
	        find.addItem("Find Next", menuCommand);
	        find.addItem("Find Previous", menuCommand);

	        final MenuBar.MenuItem view = menubar.addItem("View", null);
	        view.addItem("Show/Hide Status Bar", menuCommand);
	        view.addItem("Customize Toolbar...", menuCommand);
	        view.addSeparator();

	        view.addItem("Actual Size", menuCommand);
	        view.addItem("Zoom In", menuCommand);
	        view.addItem("Zoom Out", menuCommand);
			
			VerticalSplitPanel newLayout = new VerticalSplitPanel();
			newLayout.setCaption(MICROARRAY_TABLE_CAPTION);
			newLayout.setImmediate(true);
			newLayout.setSizeFull();
			newLayout.setLocked(true);
			newLayout.setStyleName("small previews");
			newLayout.setSplitPosition(22, UNITS_PIXELS);
			
			newLayout.setFirstComponent(menubar);
			newLayout.setSecondComponent(dataTable);
			
			dataOp.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataOp); 
		
			newLayout.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(newLayout);

		} else {

			CSHierClusterDataSet results 	=  	(CSHierClusterDataSet) dataSet;
	        DendrogramTab dendrogramTab 	= 	new DendrogramTab(results);
	        setWidth("3000px");
	        setHeight("1500px");
			addTab(dendrogramTab, "Dendrogram", null);		
		}
	}

	private Command menuCommand = new Command() {
     
		private static final long serialVersionUID = 1L;

		public void menuSelected(MenuItem selectedItem) {
            getWindow().showNotification("U clicked microarray table action : " + selectedItem.getText(), Notification.TYPE_WARNING_MESSAGE);
        }
    };
	
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
