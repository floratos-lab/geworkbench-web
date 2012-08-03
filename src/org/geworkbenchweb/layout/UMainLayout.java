package org.geworkbenchweb.layout;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.Affy3ExpressionAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.UDataSetUpload;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
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
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

/**
 * UMainLayout sets up the basic layout and style of the application.
 * @author Nikhil Reddy
 */
public class UMainLayout extends HorizontalLayout {

	private static final long serialVersionUID = 6214334663802788473L;
	
	private HorizontalSplitPanel mainPanel;
	
	private CustomLayout welcome;
	
	private HorizontalLayout visualPluginLayout;
	
	private USetsTabSheet setTabs;
	
	private Accordion tabs;
	
	private VerticalLayout mainLayout;
	
	private VerticalSplitPanel setLayout;
	
	private VerticalLayout setTabLayout;
	
	private Tree dataTree;
	
	private DSMicroarraySet maSet;
	
	private DSDataSet<? extends DSBioObject> parentSet = null;
	
	private VerticalSplitPanel menuPanel;
	
	User user = SessionHandler.get();
	
	public UMainLayout() {
		
		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);
		
		setSizeFull();
		setStyleName(Reindeer.LAYOUT_BLUE);
		
		mainPanel 			= 	new HorizontalSplitPanel();
		visualPluginLayout	=	new HorizontalLayout();
		welcome 			= 	new CustomLayout("welcome");
		setTabs				= 	new USetsTabSheet();
		tabs 				= 	new UAccordionPanel(true);
		mainLayout			=	new VerticalLayout();
		setLayout			=	new VerticalSplitPanel();
		setTabLayout		= 	new VerticalLayout();	
		
		setTabLayout.setSizeFull();
		setTabLayout.setImmediate(true);
		setTabLayout.setStyleName(Reindeer.LAYOUT_WHITE);
		
		setTabs.removeData();
		setTabs.setImmediate(true);
		setTabLayout.addComponent(setTabs);
		
		setLayout.setSplitPosition(60);
		setLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLayout.setImmediate(true);
		setLayout.setFirstComponent(tabs);
		setLayout.setSecondComponent(setTabLayout);
	
        CssLayout margin = new CssLayout();
        margin.setMargin(false, true, true, true);
        margin.setSizeFull();
        margin.addComponent(mainPanel);

		mainLayout.setSizeFull();
        mainLayout.addComponent(getHeader());
        mainLayout.addComponent(margin);
        mainLayout.setExpandRatio(margin, 1);
        
        tabs.setStyleName(Reindeer.TABSHEET_SMALL);
        tabs.setSizeFull();
        
		mainPanel.setSizeFull();
        mainPanel.setImmediate(true);
        mainPanel.setSplitPosition(20);   
		mainPanel.setFirstComponent(setLayout);
		
		visualPluginLayout.setStyleName(Reindeer.LAYOUT_WHITE);
		visualPluginLayout.setSizeFull();
		
		setMainPanelSecondComponent(welcome);
		
        addComponent(mainLayout);
	}
	
	/**
	 * This method clears and then resets the VisualPlugin area with the component provided.
	 * @param Component
	 */
	private void setMainPanelSecondComponent(Component c) {
		
		visualPluginLayout.removeAllComponents();
		visualPluginLayout.addComponent(c);
		mainPanel.setSecondComponent(visualPluginLayout);
		
	}
	
	/**
	 * Method sets up the title and basic headers of the application
	 * @return Layout
	 */
	private Layout getHeader() {
		
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setMargin(true);
        header.setSpacing(true);

        CssLayout titleLayout = new CssLayout();
        H2 title = new H2("geWorkbench");
        titleLayout.addComponent(title);

        SmallText description = new SmallText(
                "A Platform for Integrated Genomics");
        
        description.setSizeUndefined();
        titleLayout.addComponent(description);
        
        header.addComponent(titleLayout);

        titleLayout = new CssLayout();
        Label user = new Label("Welcome, " + SessionHandler.get().getUsername());
        user.setSizeUndefined();
        titleLayout.addComponent(user);


        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Label help = new Label("<div class=\"v-button\"><span class=\"v-button-wrap\"><a href=\"http:///wiki.c2b2.columbia.edu/workbench/index.php/Home\" target=\"_blank\" class=\"v-button-caption\">Help</a></div></div>", Label.CONTENT_XHTML);
        help.setWidth(null);
        
        buttons.addComponent(help);
        buttons.setComponentAlignment(help, Alignment.MIDDLE_RIGHT);
        
        Button logout 	= 	new Button("Logout", new Button.ClickListener() {
            
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
                
				SessionHandler.logout();
				getApplication().close();
				
				/**
				 * Vaadin 7
				 * Application.getCurrent().close(); 
				 */
				
			}
        });
     
        buttons.addComponent(logout);
        titleLayout.addComponent(buttons);

        header.addComponent(titleLayout);
        header.setComponentAlignment(titleLayout, Alignment.MIDDLE_RIGHT);

        return header;
    }
	
	/**
	 * Sets the style for the title of the application
	 * @author np2417
	 */
	class H2 extends Label {
   
		private static final long serialVersionUID = 1L;

		public H2(String caption) {
            super(caption);
            setSizeUndefined();
            setStyleName(Reindeer.LABEL_H2);
        }
    }

	/**
	 * Sets the style for the Sub-title of the application
	 * @author np2417
	 */
    class SmallText extends Label {
       
		private static final long serialVersionUID = 1L;

		public SmallText(String caption) {
            super(caption);
            setStyleName(Reindeer.LABEL_SMALL);
        }
    }
    
    /**
	 * Supplies the container for the dataset and result tree to display. 
	 * @return dataset and resultset container 
	 */
	public HierarchicalContainer getDataContainer() {

		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();

		parameters.put("owner", user.getId());
		
		List<?> data = FacadeFactory.getFacade().list("Select p from DataSet as p where p.owner=:owner ", parameters);

		for(int i=0; i<data.size(); i++) {

			String id = ((DataSet) data.get(i)).getName();
			dataSets.addItem(id);

			Map<String, Object> params 	= 	new HashMap<String, Object>();
			params.put("owner", user.getId());
			params.put("parent", id);
			List<?> results = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.date", params);

			for(int j=0; j<results.size(); j++) {

				String subId = ((ResultSet) results.get(j)).getName();
				dataSets.addItem(subId);
				dataSets.setChildrenAllowed(subId, false);
				dataSets.setParent(subId, id);

			}

		}
		return dataSets;
	}
    
    /**
     * UAccordionPanel builds the DataSet area of geWorkbench.
     * It inlcudes dataset & resultset tree, Markers table, Phenotypes table and Sets Tab sheet. 
     * @author Nikhil Reddy
     */
    class UAccordionPanel extends  Accordion implements Property.ValueChangeListener, Action.Handler {

    	private static final long serialVersionUID = 4523693969296820932L;

    	private Table arrayTable;

    	private Table markerTable;

    	final Action ACTION_SUBSET 		= 	new Action("Create SubSet");

    	final Action ACTION_LINKOUT		=	new Action("Link Out");

    	final Action[] ACTIONS_CREATE 	= 	new Action[] { ACTION_SUBSET, ACTION_LINKOUT };

    	private String setType;

    	protected String selectedValues = null;

    	public Long dataSetId;

    	private Action ACTION_DELETE	 	= 	new Action("Delete");

    	private final Action ACTION_ANALYZE		= 	new Action("Analyze Data"); 

    	private final Action ACTION_NORMALIZE	= 	new Action("Normalize Data");

    	private final Action ACTION_FILTER		= 	new Action("Filter Data");

    	private final Action ACTION_INTERACTIONS =	new Action("Get Interactions");

    	private final Action[] ACTIONS 			= 	new Action[] { ACTION_ANALYZE, ACTION_INTERACTIONS, ACTION_NORMALIZE, ACTION_FILTER, ACTION_DELETE };

    	public UAccordionPanel(boolean closable) {
    		
    		super();
    		
    		/* Menu Bar initialization */
    		menuPanel 	= 	new VerticalSplitPanel();
			menuPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
			menuPanel.setImmediate(true);
			menuPanel.setLocked(true);
			menuPanel.setSplitPosition(23, Sizeable.UNITS_PIXELS);

			UMenuBar toolBar 	= 	new UMenuBar();
			toolBar.setImmediate(true);

			menuPanel.setFirstComponent(toolBar);

    		
    		VerticalLayout l 	= 	new VerticalLayout();
    		l.setMargin(true);
    		Tab t = addTab(l);

    		t.setCaption("DataSets");

    		VerticalLayout dataSets = 	new VerticalLayout();
    		Button updateDataset 	= 	new Button("Upload DataSet", new ClickListener() {

    			private static final long serialVersionUID = 658137872256766310L;

    			@Override
    			public void buttonClick(ClickEvent event) {

    				UDataSetUpload dataWindow = new UDataSetUpload();
    				
    				getApplication().getMainWindow().addWindow(dataWindow);
    				/** 
    				 * Vaadin 7
    				 * Root.getCurrent().addWindow(dataWindow);
    				 */

    			}

    		});

    		updateDataset.setIcon(new ThemeResource("../runo/icons/16/document-add.png"));

    		dataSets.addComponent(updateDataset);
    		dataSets.setComponentAlignment(updateDataset, Alignment.TOP_CENTER);

    		dataTree = new Tree();
    		dataTree.setImmediate(true);
    		dataTree.areChildrenAllowed(true);
    		dataTree.setStyleName(Reindeer.TABLE_BORDERLESS);
    		dataTree.addContainerProperty("DataSet Name", String.class, "");
    		dataTree.setContainerDataSource(getDataContainer());
    		dataSets.addComponent(dataTree);

    		dataTree.addActionHandler(this);
    		dataTree.addListener(this);

    		l.addComponent(dataSets);
    		l.setHeight("100%");

    		t.setIcon(new ThemeResource("../runo/icons/16/folder.png"));

    		markerTable = new Table();
    		markerTable.setStyleName(Reindeer.TABLE_BORDERLESS);
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

    				if(action == ACTION_LINKOUT) {
    					
    					if(selectedValues == null) {
    					
    						getApplication().getMainWindow().showNotification("Select marker with Gene Name");
    						/**
    						 * Vaadin 7
    						 * Notification.show("Select marker with Gene Name",  
    						 *		Notification.TYPE_ERROR_MESSAGE );
    						 */
    						
    					}else if(selectedValues.contains(",")) {
    						getApplication().getMainWindow().showNotification("Select marker only one marker");
    						/**
    						 * Vaadin 7
    						 * Notification.show("Select marker only one marker",  
    						 *		Notification.TYPE_ERROR_MESSAGE );
    						 */
    						
    					}else {

    						int positionValue = Integer.parseInt((selectedValues.substring(1)).substring(0, selectedValues.length() - 2));
    						getApplication().getMainWindow().addWindow(new ULinkOutWindow(maSet.getMarkers().get(positionValue).getGeneName()));
    						/**
    						 * Vaadin 7
    						 * Root.getCurrent().addWindow(new ULinkOutWindow(maSet.getMarkers().get(positionValue).getGeneName()));
    						 */
    					}
    				}else {
    					if(selectedValues == null) {

    						getApplication().getMainWindow().showNotification("Please select atleast one marker");
    						/**
    						 * Vaadin 7
    						 * Notification.show("Please select atleast one marker",  
    						 *		Notification.TYPE_ERROR_MESSAGE );
    						 */
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

    									if( SubSetOperations.storeData(selectedValues, setType, setN, dataSetId ) == true ) {

    										getApplication().getMainWindow().removeWindow(nameWindow);
    										setTabs.populateTabSheet(maSet);
    										/**
    										 * Vaadin 7
    										 * Root.getCurrent().removeWindow(nameWindow);
    										 */

    									}
    								} else {

    									getApplication().getMainWindow().removeWindow(nameWindow);
    									/**
    									 *  Vaadin 7
    									 * Notification.show("Set Name cannot be empty.",
    									 *	Notification.TYPE_ERROR_MESSAGE );
    									 * Root.getCurrent().removeWindow(nameWindow);
    									 */
    									
    								}
    							}

    						});

    						nameWindow.addComponent(setName);
    						nameWindow.addComponent(addSet);
    						
    						getApplication().getMainWindow().addWindow(nameWindow);
    						/**
    						 * Vaadin 7
    						 * Root.getCurrent().addWindow(nameWindow);
    						 */
    						//selectedValues = null;

    					}
    				}
    			}	 
    		});

    		Tab t1 = addTab(markerTable);
    		t1.setCaption("Makers");
    		t1.setIcon(new ThemeResource("../runo/icons/16/folder.png"));

    		arrayTable = new Table();
    		arrayTable.setStyleName(Reindeer.TABLE_BORDERLESS);
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
    				
    					getApplication().getMainWindow().showNotification("Please select atleast one phenotype");	
    					
    					/**
    					 * Vaadin 7
    					 * Notification.show("Please select atleast one phenotype",  
    					 *			Notification.TYPE_ERROR_MESSAGE ); 
    					 */
    					
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

    								if( SubSetOperations.storeData(selectedValues, setType, setN, dataSetId ) == true ) {

    									getApplication().getMainWindow().removeWindow(nameWindow);
    									setTabs.populateTabSheet(maSet);
    									/**
    									 * Vaadin 7
    									 * Root.getCurrent().removeWindow(nameWindow);
    									 */
    								}
    							} else {
    								
    								getApplication().getMainWindow().showNotification("Set Name cannot be empty.");
    								getApplication().getMainWindow().removeWindow(nameWindow);
    								
    								/**
    								 * Vaadin 7
    								 * 
    								 * Notification.show("Set Name cannot be empty.",
    								 * 		Notification.TYPE_ERROR_MESSAGE ); 
    								 * Root.getCurrent().removeWindow(nameWindow);
    								 */
    							}

    						}

    					});

    					nameWindow.addComponent(setName);
    					nameWindow.addComponent(addSet);
    					
    					getApplication().getMainWindow().addWindow(nameWindow);
    					/**
    					 * Vaadin 7
    					 * Root.getCurrent().addWindow(nameWindow);
    					 */
    					//selectedValues = null;
    				}
    			}	 
    		});
    		Tab t2 = addTab(arrayTable);
    		t2.setCaption("Phenotypes");
    		t2.setIcon(new ThemeResource("../runo/icons/16/folder.png"));

    	}


    	protected void setSelectedValues(String value) {

    		this.selectedValues = value;

    	}
    	
    	/**
    	 * This method handles data tree click events (data set nodes and result set nodes)
    	 * @param Tree event
    	 */
    	@Override
    	public void valueChange(ValueChangeEvent event) {
    		try {
    			if((String) event.getProperty().getValue() != null ) {

    				String dataPeru					= 	(String) event.getProperty().getValue();
    				String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
    				Map<String, Object> parameters 	= 	new HashMap<String, Object>();

    				parameters.put("name", dataPeru);
    				parameters.put("owner", user.getId());

    				DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

    				if(dataSet != null) {

    					dataSetId 					=	dataSet.getId();
    					byte[] dataByte 			= 	dataSet.getData();

    					UVisualPlugin tabSheet = null;
    					if (dataSet.getType().equals("PDB File")){
    						DSProteinStructure pSet	=	(DSProteinStructure) ObjectConversion.toObject(dataByte);
    						parentSet				=	pSet;
    						tabSheet 				=	new UVisualPlugin(pSet, dataSet.getType(), null);
    					}else{
	    					maSet 					= 	(DSMicroarraySet) ObjectConversion.toObject(dataByte);
	    					parentSet				=	maSet;
	    					
	    					AffyAnnotationParser parser = new Affy3ExpressionAnnotationParser();
	    					File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
	    					AnnotationParser.cleanUpAnnotatioAfterUnload(maSet);
	    					AnnotationParser.loadAnnotationFile(maSet, annotFile, parser);
	
	    					markerTable.setContainerDataSource(markerTableView(maSet));
	    					arrayTable.setContainerDataSource(arrayTableView(maSet));
	
	    					tabSheet 				= 	new UVisualPlugin(maSet, dataSet.getType(), null);
	    					setTabs.populateTabSheet(maSet);
    					}

    					menuPanel.setSecondComponent(tabSheet);
    					setMainPanelSecondComponent(menuPanel);

    				} else {

    					String querySub 					= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
    					Map<String, Object> params 			= 	new HashMap<String, Object>();

    					params.put("name", dataPeru);
    					params.put("owner", user.getId());

    					ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
    					if(resultSet != null) {

    						byte[] dataByte 					= 	resultSet.getData();
    						UVisualPlugin tabSheet = null;

    						if(resultSet.getType().equalsIgnoreCase("CNKB")) {

    							@SuppressWarnings("unchecked")
    							Vector<CellularNetWorkElementInformation> hits 	=	(Vector<CellularNetWorkElementInformation>) ObjectConversion.toObject(dataByte);
    							tabSheet	= 	new UVisualPlugin(hits, resultSet.getType(), null);

    						}else if(resultSet.getType().equalsIgnoreCase("Hierarchical Clustering")) {

    							CSHierClusterDataSet hierResults 	= 	(CSHierClusterDataSet) ObjectConversion.toObject(dataByte);
    							tabSheet 	= 	new UVisualPlugin(hierResults, resultSet.getType(), null);

    						}else if(resultSet.getType().equalsIgnoreCase("ARACne")) {

    							AdjacencyMatrixDataSet dSet 	= 	(AdjacencyMatrixDataSet) ObjectConversion.toObject(dataByte);
    							tabSheet 	= 	new UVisualPlugin(dSet, resultSet.getType(), null);

    						}else if(resultSet.getType().equalsIgnoreCase("MarkUs")) {

    							MarkUsResultDataSet prtSet		= 	(MarkUsResultDataSet) ObjectConversion.toObject(dataByte);
    							tabSheet 	= 	new UVisualPlugin(prtSet, resultSet.getType(), null);

    						}

    						menuPanel.setSecondComponent(tabSheet);
    						setMainPanelSecondComponent(menuPanel);
        					
    					}
    				}		

    			}else {

    				setTabs.removeData();
    				setMainPanelSecondComponent(welcome);

    			}
    		}catch(Exception e) {

    			e.printStackTrace();

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
    			item.getItemProperty("Labels").setValue(maSet.get(k).getLabel());

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
    					item.getItemProperty("Labels").setValue(maSet.getMarkers().get(j).getLabel() 
    							+ " (" 
    							+ maSet.getMarkers().get(j).getGeneName()
    							+ ")");
    				} 
    			}
    		}
    		return tableData;

    	}

    	@Override
    	public Action[] getActions(Object target, Object sender) {

    		return ACTIONS;

    	}

    	/**
    	 * Method handles Actions of the context menu on the dataset tree.
    	 */
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
    				dataSet.getId();
    				
    				/* Deleting result sets if there are any */
    				if(dataTree.hasChildren(target)) {
    					String querySub 			= 	"Select p from ResultSet as p where p.owner=:owner";
    					Map<String, Object> params 	= 	new HashMap<String, Object>();

    					params.put("owner", user.getId());
    					List<ResultSet> resultSets 		= 	FacadeFactory.getFacade().list(querySub, params);

    					for(ResultSet result : resultSets) {
    						FacadeFactory.getFacade().delete(result);
    						dataTree.removeItem(result.getName());
    					}
    				}
    				
    				/* Deleting subsets if there are any */
    				String querySub 			= 	"Select p from SubSet as p where p.owner=:owner and p.parent=:parent";
					Map<String, Object> params 	= 	new HashMap<String, Object>();

					params.put("owner", user.getId());
					params.put("parent", dataSet.getId());
					List<SubSet> subSets 		= 	FacadeFactory.getFacade().list(querySub, params);
    				if(!subSets.isEmpty()) {
    					for(SubSet set : subSets) {
    						FacadeFactory.getFacade().delete(set);
    					}
    					setTabs.populateTabSheet(null);
    				}
    				FacadeFactory.getFacade().delete(dataSet);
    				dataTree.removeItem(target);

    				if (dataSet.getType().equals("PDB File")){
    					File dataFile = new File(System.getProperty("user.home") + "/temp/", dataName);
    					if (dataFile.exists())  dataFile.delete();
    				}
    				
    			} else {

    				String querySub 			= 	"Select p from ResultSet as p where p.name=:name and p.owner=:owner";
    				Map<String, Object> params 	= 	new HashMap<String, Object>();

    				params.put("name", dataName);
    				params.put("owner", user.getId());
    				ResultSet resultSet 		= 	FacadeFactory.getFacade().find(querySub, params);
    				FacadeFactory.getFacade().delete(resultSet);
    				dataTree.removeItem(target);
    			}

    			setMainPanelSecondComponent(welcome);

    		}else if(action == ACTION_ANALYZE || action == ACTION_INTERACTIONS) {

    			String dataPeru					= 	(target.toString());
    			String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
    			Map<String, Object> parameters 	= 	new HashMap<String, Object>();

    			parameters.put("name", dataPeru);
    			parameters.put("owner", user.getId());

    			DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

    			if(dataSet != null) {

    				byte[] dataByte 			= 	dataSet.getData();
    				
    				if (dataSet.getType().equals("PDB File")){
    					if (action == ACTION_ANALYZE){
    						DSProteinStructure pSet	=	(DSProteinStructure) ObjectConversion.toObject(dataByte);
    						parentSet				=	pSet;

    						UVisualPlugin tabSheet = new UVisualPlugin(pSet, dataSet.getType(), "Analyze Data");
    						menuPanel.setSecondComponent(tabSheet);
    						setMainPanelSecondComponent(menuPanel);
    					}
	    				return;
					}

					DSMicroarraySet maSet 		= 	(DSMicroarraySet) ObjectConversion.toObject(dataByte);
					parentSet					=	maSet;

    				if(maSet.getAnnotationFileName() != null){
    					AffyAnnotationParser parser = new Affy3ExpressionAnnotationParser();
    					File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
    					AnnotationParser.loadAnnotationFile(maSet, annotFile, parser);
    				}

    				markerTable.setContainerDataSource(markerTableView(maSet));
    				arrayTable.setContainerDataSource(arrayTableView(maSet));
    				
    				if(action == ACTION_ANALYZE) {
    					UVisualPlugin tabSheet = new UVisualPlugin(maSet, dataSet.getType(), "Analyze Data");
    					menuPanel.setSecondComponent(tabSheet);
    				}else {
    					
    					UVisualPlugin tabSheet = new UVisualPlugin(maSet, dataSet.getType(), "Get Interactions");
    					menuPanel.setSecondComponent(tabSheet);
    				}
    				setMainPanelSecondComponent(menuPanel);
    			}else {

    				getApplication().getMainWindow().showNotification("Please select dataSet node or subset node for analysis");
    				/** 
    				 * Vaadin 7
    				 * Notification.show("Please select dataSet node or subset node for analysis",  
    				 *		Notification.TYPE_ERROR_MESSAGE );
    				 */
    			}

    		}else if(action == ACTION_NORMALIZE) {

    			getApplication().getMainWindow().showNotification("No normalizers are implemented yet !!");
    			/**
    			 * Vaadin 7
    			 * Notification.show("No normalizers are implemented yet !!",  
    			 *		Notification.TYPE_ERROR_MESSAGE );
    			 */

    		}else if(action == ACTION_FILTER) {

    			getApplication().getMainWindow().showNotification("No filters are implemented yet !!");
    			/**
    			 * Vaadin 7
    			 * Notification.show("No filters are implemented yet !!",  
    			 *		Notification.TYPE_ERROR_MESSAGE );
    			 */
    			
    		}

    	}
    }
    
    /**
     * Adds the node to the dataTree  
     */
    public class NodeAddListener implements NodeAddEventListener {
    	@Override
    	public void addNode(NodeAddEvent event) {	
    		dataTree.addItem(event.getDataSetName());
    		if(event.getDataType() != "Data Node") {
        		dataTree.setChildrenAllowed(event.getDataSetName(), false);
        		dataTree.setParent(event.getDataSetName(), parentSet.getDataSetName());
        		dataTree.expandItem(parentSet.getDataSetName());
    		}
    		dataTree.select(event.getDataSetName());
    	}
    }

}
