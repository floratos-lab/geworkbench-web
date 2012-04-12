 package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbenchweb.GeworkbenchApplication;
import org.geworkbenchweb.components.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.dataset.DataSetUpload;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SetOperations;
import org.geworkbenchweb.layout.VisualPlugin;

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
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.VerticalSplitPanel;

public class MainLayout extends AbsoluteLayout {

	private static final long serialVersionUID = 6214334663802788473L;
	
	private GenSpaceWindow genSpaceWindow;
	
	private GeworkbenchApplication app;
	
	private DataSetUpload dataWindow;
	
	private HorizontalSplitPanel mainPanel;
	
	private CustomLayout welcome;
	
	private VerticalSplitPanel toolPanel;
	
	User user = SessionHandler.get();

	public Long dataSetId;
	
	// Actions for the context menu
	
	private static final Action ACTION_DELETE	 	= 	new Action("Delete");
	
	private static final Action ACTION_ANALYZE		= 	new Action("Analyze Data"); 
	
	private static final Action ACTION_NORMALIZE	= 	new Action("Normalize Data");
	
	private static final Action ACTION_FILTER		= 	new Action("Filter Data");
	
	private static final Action ACTION_INTERACTIONS =	new Action("Get Interactions");
	
    private static final Action[] ACTIONS 			= 	new Action[] { ACTION_ANALYZE, ACTION_INTERACTIONS, ACTION_NORMALIZE, ACTION_FILTER, ACTION_DELETE };
	
	public MainLayout(GeworkbenchApplication app) {
		
		
		this.app 						= 	app;
		toolPanel 						=   new VerticalSplitPanel();
		mainPanel 						= 	new HorizontalSplitPanel();
		Accordion tabs 					= 	new AccordionPanels(true);
		CssLayout toolbar 				= 	new CssLayout();
		welcome 						= 	new CustomLayout("welcome");
		Label headerText 				=  	new Label("geWorkbench-Web 1.0");
		CssLayout mainHeader 			= 	new CssLayout();
		Label welcomeUser 				= 	new Label("Welcome  " + user.getUsername() + "  |");
		
		
		headerText.addStyleName("h4");
		welcomeUser.addStyleName("h4");
		
		setSizeFull();
		toolPanel.setSizeFull();
		toolPanel.setStyleName("small previews");
		toolPanel.setSplitPosition(33, UNITS_PIXELS);
		toolPanel.setLocked(true);
		
        mainHeader.setWidth("100%");
        mainHeader.addStyleName("toolbar-invert");
		mainHeader.addComponent(headerText);
    
        Button logoutButton = new Button("Logout", new ClickListener() {
			
        	private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				SessionHandler.logout();
				getApplication().close();
				
			}

		});
        
        logoutButton.addStyleName("borderless");
        logoutButton.setIcon(new ThemeResource("../runo/icons/16/user.png"));
        
        genSpaceWindow = new GenSpaceWindow();
        Button genspaceButton = new Button("genSpace", new ClickListener() {
			
        	private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(!getApplication().getMainWindow().getChildWindows().contains(genSpaceWindow))
					getApplication().getMainWindow().addWindow(genSpaceWindow);
				genSpaceWindow.setWidth(600,UNITS_PIXELS);
				genSpaceWindow.setHeight(400,UNITS_PIXELS);
				genSpaceWindow.setPositionX((int) (getApplication().getMainWindow().getWidth()/2 - genSpaceWindow.getWidth()));
				genSpaceWindow.setPositionY((int) (getApplication().getMainWindow().getHeight()/2 - genSpaceWindow.getHeight()));
			}

		});
        
        genspaceButton.addStyleName("borderless");
        
        toolbar.setSizeUndefined();
        toolbar.addStyleName("right");
        toolbar.addComponent(welcomeUser);
        toolbar.addComponent(genspaceButton);
        toolbar.addComponent(logoutButton);
        mainHeader.addComponent(toolbar);
        
        tabs.setStyleName("borderless");
        tabs.setSizeFull();
        
		mainPanel.setSizeFull();
        mainPanel.setStyleName("small previews");
        mainPanel.setSplitPosition(20);   
		mainPanel.setFirstComponent(tabs);
		
		welcome.setSizeFull();
		mainPanel.setSecondComponent(welcome);
		
		toolPanel.setFirstComponent(mainHeader);
        toolPanel.setSecondComponent(mainPanel);
		
        addComponent(toolPanel);
	
	}

	class AccordionPanels extends  Accordion implements Property.ValueChangeListener, Button.ClickListener, Action.Handler {

		private Tree dataTree;

		private static final long serialVersionUID = 4523693969296820932L;

		private Table markerTable;

		private Table arrayTable;
		
		private TreeTable markerSets;
		
		private	TreeTable arraySets;

		final Action ACTION_SUBSET 		= new Action("Create SubSet");

		final Action[] ACTIONS_CREATE 	= new Action[] { ACTION_SUBSET };
		
		private String setType;

		protected String selectedValues = null;
		
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

			updateDataset.setStyleName("small default");
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
			
			markerTable = new Table();
			markerTable.addStyleName("small striped");
			markerTable.setSizeFull();
			markerTable.setSelectable(true);
			markerTable.setMultiSelect(true);
			markerTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
			
			markerTable.addListener(new Table.ValueChangeListener() {
		        
				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {
	      
					String value = (event.getProperty().getValue()).toString();
					setType = "marker";
	                setSelectedValues(value);
	                
	            }

	        });
			
			markerTable.addActionHandler(new Action.Handler() {
				
				private static final long serialVersionUID = 1L;

				public Action[] getActions(Object target, Object sender) {
					
	            	return ACTIONS_CREATE;
	                
	            }
	            
	            public void handleAction(Action action, Object sender, Object target) {

	            	if(selectedValues == null) {

	            		getApplication().getMainWindow().showNotification("Please select atleast one marker",  
	            				Notification.TYPE_ERROR_MESSAGE );

	            	} else {
	            		
	            		final Window nameWindow = new Window();
		            	nameWindow.setModal(true);
		            	nameWindow.setClosable(true);
		            	nameWindow.setWidth("300px");
		            	nameWindow.setHeight("150px");
		            	nameWindow.setResizable(false);
		            	nameWindow.setCaption("Add Markers to Set");
		            	nameWindow.setImmediate(true);
		            	
		            	final TextField setName = new TextField();
		            	setName.setInputPrompt("Please enter set name");
		            	setName.setImmediate(true);
		            	
	            		Button addSet = new Button("Add Set", new ClickListener() {

	            			private static final long serialVersionUID = 1L;

	            			@Override
	            			public void buttonClick(ClickEvent event) {
	            				
	            				String setN = (String) setName.getValue();
	            				if(setN != "") {

	            					SetOperations setOp = new SetOperations();

	            					if( setOp.storeData(selectedValues, setType, setN, dataSetId ) == true ) {

	            						getApplication().getMainWindow().removeWindow(nameWindow);
	            						markerSets.requestRepaint();
	            					}
	            				} else {

	            					getApplication().getMainWindow().showNotification("Set Name cannot be empty.",
	            							Notification.TYPE_ERROR_MESSAGE );
	            					getApplication().getMainWindow().removeWindow(nameWindow);
	            				}
	            			}

	            		});

	            		nameWindow.addComponent(setName);
	            		nameWindow.addComponent(addSet);
	            		getApplication().getMainWindow().addWindow(nameWindow);
	            		//selectedValues = null;
	            	}
	            }	 
	        });
			
			Tab t1 = addTab(markerTable);
			t1.setCaption("Makers");
			t1.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
			
			arrayTable = new Table();
			arrayTable.addStyleName("small striped");
			arrayTable.setSizeFull();
			arrayTable.setSelectable(true);
			arrayTable.setMultiSelect(true);
			arrayTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
			arrayTable.addListener(new Table.ValueChangeListener() {
	        
				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {
	      
					String value = (event.getProperty().getValue()).toString();
					setType = "Microarray";
	                setSelectedValues(value);
	                
	            }

	        });
			
			arrayTable.addActionHandler(new Action.Handler() {
				
				private static final long serialVersionUID = 1L;

				public Action[] getActions(Object target, Object sender) {
					
	            	return ACTIONS_CREATE;
	                
	            }
	            public void handleAction(Action action, Object sender, Object target) {

	            	if(selectedValues == null) {

	            		getApplication().getMainWindow().showNotification("Please select atleast one phenotype",  
	            				Notification.TYPE_ERROR_MESSAGE );

	            	} else {
	            		
	            		final Window nameWindow = new Window();
		            	nameWindow.setModal(true);
		            	nameWindow.setClosable(true);
		            	nameWindow.setWidth("300px");
		            	nameWindow.setHeight("150px");
		            	nameWindow.setResizable(false);
		            	nameWindow.setCaption("Add Phenotypes to Set");
		            	nameWindow.setImmediate(true);
		            	
		            	final TextField setName = new TextField();
		            	setName.setInputPrompt("Please enter set name");
		            	setName.setImmediate(true);
		            	
		            	Button addSet = new Button("Add Set", new ClickListener() {

		            		private static final long serialVersionUID = 1L;

		            		@Override
		            		public void buttonClick(ClickEvent event) {

		            			String setN = (String) setName.getValue();
		            			if(setN != "") {
		            				
		            				SetOperations setOp = new SetOperations();

		            				if( setOp.storeData(selectedValues, setType, setN, dataSetId ) == true ) {

		            					getApplication().getMainWindow().removeWindow(nameWindow);
		            					arraySets.requestRepaint();
		            				}
		            			} else {

		            				getApplication().getMainWindow().showNotification("Set Name cannot be empty.",
		            						Notification.TYPE_ERROR_MESSAGE );
		            				getApplication().getMainWindow().removeWindow(nameWindow);
		            			}
	            					            				
		            		}

		            	});

	            		nameWindow.addComponent(setName);
	            		nameWindow.addComponent(addSet);
	            		getApplication().getMainWindow().addWindow(nameWindow);
	            		//selectedValues = null;
	            	}
	            }	 
	        });
			Tab t2 = addTab(arrayTable);
			t2.setCaption("Phenotypes");
			t2.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
			
			
			markerSets = new TreeTable();
			markerSets.setImmediate(true);
			markerSets.addStyleName("small striped");
			Tab t3 = addTab(markerSets);
			t3.setCaption("MarkerSets");
			t3.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
			
			
			arraySets = new TreeTable();
			arraySets.addStyleName("small striped");
			arraySets.setImmediate(true);
			Tab t4 = addTab(arraySets);
			t4.setCaption("ArraySets");
			t4.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
		}
		

		protected void setSelectedValues(String value) {
			
			this.selectedValues = value;
		
		}

		@Override
		public void buttonClick(ClickEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void valueChange(ValueChangeEvent event) {
			
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

				dataSetId 					=	dataSet.getId();
				byte[] dataByte 			= 	dataSet.getData();
				DSMicroarraySet maSet 		= 	(DSMicroarraySet) toObject(dataByte);

				markerTable.setContainerDataSource(markerTableView(maSet));
				arrayTable.setContainerDataSource(arrayTableView(maSet));

				SetOperations setOp = new SetOperations();

				markerSetContainer(setOp.getMarkerSets(dataSetId), maSet);
				arraySetContainer(setOp.getArraySets(dataSetId), maSet);


				VisualPlugin tabSheet = new VisualPlugin(maSet, dataSet.getType(), null);
				mainPanel.setSecondComponent(tabSheet);

			} else {
 
				String querySub 					= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
				Map<String, Object> params 			= 	new HashMap<String, Object>();

				params.put("name", dataPeru);
				params.put("owner", user.getId());

				ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
				if(resultSet != null) {
					
					byte[] dataByte 					= 	resultSet.getData();
					CSHierClusterDataSet hierResults 	= 	(CSHierClusterDataSet) toObject(dataByte);
					VisualPlugin tabSheet 				= 	new VisualPlugin(hierResults, resultSet.getType(), null);
					mainPanel.setSecondComponent(tabSheet);
				
				}
			}

			mainPanel.requestRepaint();		
		}

		private void arraySetContainer(List<?> list, DSMicroarraySet maSet) {
			
			if(!list.isEmpty()) {
				arraySets.removeAllItems();
				arraySets.setSizeFull();
				arraySets.addContainerProperty("Name", String.class, "");
				arraySets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);

				for(int i=0; i<list.size(); i++ ) {

					String name 		= 	((SubSet) list.get(i)).getName();
					String positions 	= 	(((SubSet) list.get(i)).getPositions()).trim();
					Object item 		= 	arraySets.addItem(new Object[] { name }, null);

					String[] temp =  (positions.substring(1, positions.length()-1)).split(",");

					for(int j = 0; j<temp.length; j++) {

						Object subItem = arraySets.addItem(new Object[] { maSet.get(Integer.parseInt(temp[j].trim())).getLabel() }, null);
						arraySets.setChildrenAllowed(subItem, false);
						arraySets.setParent(subItem, item);
					}

				}
			}
	
		}


		private void markerSetContainer(List<?> list, DSMicroarraySet maSet) {
			
			if(list.size() != 0) {
				markerSets.removeAllItems();
				markerSets.setSizeFull();
				markerSets.addContainerProperty("Name", String.class, "");
				markerSets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);

				for(int i=0; i<list.size(); i++ ) {

					String name 		= 	((SubSet) list.get(i)).getName();
					String positions 	= 	(((SubSet) list.get(i)).getPositions()).trim();
					Object item 		= 	markerSets.addItem(new Object[] { name }, null);

					String[] temp =  (positions.substring(1, positions.length()-1)).split(",");

					for(int j = 0; j<temp.length; j++) {

						Object subItem = markerSets.addItem(new Object[] { maSet.getMarkers().get(Integer.parseInt(temp[j].trim())).getLabel() }, null);
						markerSets.setChildrenAllowed(subItem, false);
						markerSets.setParent(subItem, item);
					}

				}
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

			}else if(action == ACTION_ANALYZE || action == ACTION_INTERACTIONS) {

				String dataPeru					= 	(target.toString());
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
					
					if(action == ACTION_ANALYZE) {
						VisualPlugin tabSheet = new VisualPlugin(maSet, dataSet.getType(), "Analyze Data");
						mainPanel.setSecondComponent(tabSheet);
					}else {
						VisualPlugin tabSheet = new VisualPlugin(maSet, dataSet.getType(), "Get Interactions");
						mainPanel.setSecondComponent(tabSheet);
					}
				
				}else {
					
					getApplication().getMainWindow().showNotification("Please select dataSet node or subset node for analysis",  
							Notification.TYPE_ERROR_MESSAGE );
					
				}
				
			}else if(action == ACTION_NORMALIZE) {
				
				getApplication().getMainWindow().showNotification("No normalizers are implemented yet !!",  
						Notification.TYPE_ERROR_MESSAGE );
				
			}else if(action == ACTION_FILTER) {
				
				getApplication().getMainWindow().showNotification("No filters are implemented yet !!",  
						Notification.TYPE_ERROR_MESSAGE );
				
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
		    
		    Map<String, Object> params 	= 	new HashMap<String, Object>();
		    params.put("owner", user.getId());
		    params.put("parent", id);
		    String wClause = "p.owner = :owner and p.parent = :parent" ;
		    List<?> results = FacadeFactory.getFacade().getFieldValues(ResultSet.class, "name", wClause, params);
		    
		    for(int j=0; j<results.size(); j++) {
		    	
		    	String subId = (String) results.get(j);
		    	dataSets.addItem(subId);
		    	dataSets.setChildrenAllowed(subId, false);
		    	dataSets.setParent(subId, id);
		    	
		    }
		 
		}
		
	    return dataSets;
	
	}
}
