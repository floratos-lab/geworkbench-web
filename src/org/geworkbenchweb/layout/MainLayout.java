package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchApplication;
import org.geworkbenchweb.analysis.HierClusterTestResult;
import org.geworkbenchweb.dataset.DataSetUpload;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalSplitPanel;
import org.geworkbenchweb.layout.VisualPlugin;

public class MainLayout extends AbsoluteLayout {

	private static final long serialVersionUID = 6214334663802788473L;
	
	private GeworkbenchApplication app;
	
	private DataSetUpload dataWindow;
	
	private HorizontalSplitPanel mainPanel;
	
	private CustomLayout welcome;
	
	User user = SessionHandler.get();
	
	// Actions for the context menu
	
	private static final Action ACTION_DELETE	 	= 	new Action("Delete");
	
    private static final Action[] ACTIONS 			= 	new Action[] { ACTION_DELETE };
	
	public MainLayout(GeworkbenchApplication app) {
		
		
		this.app 						= 	app;
		mainPanel 						= 	new HorizontalSplitPanel();
		Accordion tabs 					= 	new AccordionPanels(true);
		VerticalLayout toolbar 			= 	new VerticalLayout();
		welcome 						= 	new CustomLayout("welcome");
		VerticalSplitPanel dataPanel 	=	new VerticalSplitPanel();
		
		setSizeFull();
        mainPanel.setSizeFull();
        mainPanel.setStyleName("small previews");
        mainPanel.setSplitPosition(20);        
              
        tabs.setStyleName("opaque");
        tabs.setSizeFull();
            
        Button logoutButton = new Button("Logout", new ClickListener() {
			
        	private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				SessionHandler.logout();
				getApplication().close();
				
			}

		});
        
        logoutButton.setStyleName("wide");
        logoutButton.setIcon(new ThemeResource("../runo/icons/16/user.png"));
        toolbar.setWidth("100%");
        toolbar.setStyleName("toolbar");
        toolbar.addComponent(logoutButton);
        toolbar.setComponentAlignment(logoutButton, Alignment.BOTTOM_CENTER);

        dataPanel.setSplitPosition(27, UNITS_PIXELS, true);
        dataPanel.setStyleName("small previews");
        dataPanel.setLocked(true);
        dataPanel.setFirstComponent(tabs);
        dataPanel.setSecondComponent(toolbar);
        
		welcome.setSizeFull();
		mainPanel.setFirstComponent(dataPanel);
		mainPanel.setSecondComponent(welcome);
		
        addComponent(mainPanel);
	
	}

	class AccordionPanels extends  Accordion implements Property.ValueChangeListener, Button.ClickListener, Action.Handler {

		private Tree dataTree;
		
		private static final long serialVersionUID = 4523693969296820932L;
		
		private Table markerTable;
		
		private Table arrayTable;
		
		AccordionPanels(boolean closable) {
			
			super();
			VerticalLayout l 	= 	new VerticalLayout();
			l.setMargin(true);
			Tab t = addTab(l);


			t.setCaption("DataSets");
			t.setStyleName("sidebar-menu");

			VerticalLayout dataSets = 	new VerticalLayout();
			Button updateDataset 	= 	new Button("Upload DataSet", new ClickListener() {

				private static final long serialVersionUID = 658137872256766310L;

				@Override
				public void buttonClick(ClickEvent event) {

					dataWindow = new DataSetUpload();
					app.getMainWindow().addWindow(dataWindow);
					app.initView(getApplication().getMainWindow());
				}

			});

			updateDataset.setStyleName("small");
			updateDataset.setIcon(new ThemeResource("../runo/icons/16/document-add.png"));

			dataSets.addComponent(updateDataset);
			dataSets.setComponentAlignment(updateDataset, Alignment.TOP_CENTER);

			dataTree = new Tree();
			dataTree.areChildrenAllowed(true);

			dataTree.addContainerProperty("DataSet Name", String.class, "");
			dataTree.setContainerDataSource(getDataContainer());
			dataSets.addComponent(dataTree);

			dataTree.addActionHandler(this);
			dataTree.addListener(this);
			dataTree.setImmediate(true);

			l.addComponent(dataSets);
			l.setHeight("100%");

			t.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
			
			markerTable 	= 	new Table();
			markerTable.addStyleName("small striped");
			markerTable.setSizeFull();
			markerTable.setSelectable(true);
			markerTable.setMultiSelect(true);
			markerTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
			
			Tab t1 = addTab(markerTable);
			t1.setCaption("Makers");
			t1.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
			
			arrayTable 	= 	new Table();
			arrayTable.addStyleName("small striped");
			arrayTable.setSizeFull();
			arrayTable.setSelectable(true);
			arrayTable.setMultiSelect(true);
			arrayTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
			
			Tab t2 = addTab(arrayTable);
			t2.setCaption("Phenotypes");
			t2.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
			
		}

		@Override
		public void buttonClick(ClickEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void valueChange(ValueChangeEvent event) {
			
			try {
			
				if (event.getProperty().getValue().toString().contains("Analysis") && event.getProperty().getValue().toString().contains("SubSets")) {
				
						//TODO
				
				} else {
					
					String dataPeru					= 	(String) event.getProperty().getValue();
					String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
					Map<String, Object> parameters 	= 	new HashMap<String, Object>();
					
					parameters.put("name", dataPeru);
					parameters.put("owner", user.getId());
					
					DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);
					
					if(dataSet != null) {
						/*
						 * if this is a dataSet
						 */
						
						byte[] dataByte 			= 	dataSet.getData();
						DSMicroarraySet maSet 		= 	(DSMicroarraySet) toObject(dataByte);
						
						markerTable.setContainerDataSource(markerTableView(maSet));
						arrayTable.setContainerDataSource(arrayTableView(maSet));
						VisualPlugin tabSheet = new VisualPlugin(maSet, dataSet.getType());
						mainPanel.setSecondComponent(tabSheet);
					
					} else {
						
						//it should be analysis results
						
						String querySub 					= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
						Map<String, Object> params 			= 	new HashMap<String, Object>();

						params.put("name", dataPeru);
						params.put("owner", user.getId());
						ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
						
						byte[] dataByte 			= 	resultSet.getData();
						HierClusterTestResult maSet = 	(HierClusterTestResult) toObject(dataByte);

						VisualPlugin tabSheet = new VisualPlugin(maSet, resultSet.getType());
						mainPanel.setSecondComponent(tabSheet);
						
					}
					
					mainPanel.requestRepaint();		
				}
				
			} catch (NullPointerException e) {
				
				//TODO
			}
		}

		/**
		 * Method is used to populate Phenotype Panel
		 * @param maSet
		 * @return - Indexed container with array labels
		 */
		
		private Container arrayTableView(DSMicroarraySet maSet) {
			
			IndexedContainer tableData 		= 	new IndexedContainer();

				for(int k=0;k<maSet.size();k++) {
					
						Item item 					= 	tableData.addItem(k);
						tableData.addContainerProperty("Labels", String.class, null);
						item.getItemProperty("Labels").setValue(maSet.get(k));
						 
				}
			
			
			return tableData;
		}

		/**
		 * Method is used to populate Marker Panel
		 * @param maSet
		 * @return - Indexed container with marker labels
		 */
		
		private IndexedContainer markerTableView(DSMicroarraySet maSet) {
	
			IndexedContainer tableData 		= 	new IndexedContainer();

			for(int j=0; j<maSet.getMarkers().size();j++) {
				
				Item item 					= 	tableData.addItem(j);
				
				for(int k=0;k<=maSet.size();k++) {
					
					if(k == 0) {
						
						tableData.addContainerProperty("Labels", String.class, null);
						item.getItemProperty("Labels").setValue(maSet.getMarkers().get(j));
						
					} 
				}
			}
			return tableData;
	
		}




		@SuppressWarnings("deprecation")
		public Object toObject(byte[] bytes){ 
			
			Object object = null; 
			
			try{ 
				
				object = new java.io.ObjectInputStream(new 
						java.io.ByteArrayInputStream(bytes)).readObject(); 
			
			}catch(java.io.IOException ioe){ 
				
				java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
						ioe.getMessage()); 
			
			}catch(java.lang.ClassNotFoundException cnfe){ 
				
				java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
						cnfe.getMessage()); 
			
			} 
			
			return object; 
		
		}
		
		@Override
		public Action[] getActions(Object target, Object sender) {
			
			return ACTIONS;
		
		}

		@Override
		public void handleAction(Action action, Object sender, Object target) {
			
			if (action == ACTION_DELETE) {

				String dataName 				=	(target.toString());

				String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
				Map<String, Object> parameters 	= 	new HashMap<String, Object>();

				parameters.put("name", dataName);
				parameters.put("owner", user.getId());

				DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);


				if(dataSet != null) {

					FacadeFactory.getFacade().delete(dataSet);
					dataTree.removeItem(target + " - Analysis Results");
					dataTree.removeItem(target + " - SubSets");
					dataTree.removeItem(target);
					

				} else {

					String querySub 					= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
					Map<String, Object> params 			= 	new HashMap<String, Object>();

					params.put("name", dataName);
					params.put("owner", user.getId());
					ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
					FacadeFactory.getFacade().delete(resultSet);
					dataTree.removeItem(target);
					

				}

			}
			
		}
	}
	
	public HierarchicalContainer getDataContainer() {
		
		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("owner", user.getId());
		String whereClause = "p.owner = :owner";
		List<?> data = FacadeFactory.getFacade().getFieldValues(DataSet.class, "name", whereClause, parameters);
		
		for(int i=0; i<data.size(); i++) {
			
			String id = (String) data.get(i);
		    dataSets.addItem(id);
		    
		    String analysisDone = id + " - Analysis Results";
		    
		    dataSets.addItem(analysisDone);
		    dataSets.setChildrenAllowed(analysisDone, true);
		    dataSets.setParent(analysisDone, id);
		    
		    Map<String, Object> params 	= 	new HashMap<String, Object>();
		    params.put("owner", user.getId());
		    params.put("parent", id);
		    String wClause = "p.owner = :owner and p.parent = :parent" ;
		    List<?> results = FacadeFactory.getFacade().getFieldValues(ResultSet.class, "name", wClause, params);
		    
		    for(int j=0; j<results.size(); j++) {
		    	
		    	String subId = (String) results.get(j);
		    	dataSets.addItem(subId);
		    	dataSets.setChildrenAllowed(subId, false);
		    	dataSets.setParent(subId, analysisDone);
		    	
		    }
		    
		    String subSets = id + " - SubSets";
			dataSets.addItem(subSets);
		    dataSets.setChildrenAllowed(subSets, true);
		    dataSets.setParent(subSets, id);
		 
		}
		
	    return dataSets;
	
	}
}
