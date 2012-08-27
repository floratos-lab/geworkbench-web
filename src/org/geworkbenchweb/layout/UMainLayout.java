package org.geworkbenchweb.layout;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.Affy3ExpressionAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.GeworkbenchRoot; 
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
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
public class UMainLayout extends VerticalLayout {

	private static final long serialVersionUID = 6214334663802788473L;

	private HorizontalSplitPanel mainPanel;

	private VerticalLayout welcome;

	private HorizontalLayout visualPluginLayout;

	private USetsTabSheet setTabs;

	private Accordion tabs;

	private VerticalLayout mainLayout;

	private VerticalSplitPanel setLayout;

	private VerticalLayout setTabLayout;

	private TreeTable dataTree;

	private VerticalSplitPanel menuPanel;
	
	private String parentId;
	
	private String[] dataProperties;

	User user = SessionHandler.get();

	public UMainLayout() {

		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);

		setSizeFull();
		addStyleName("background");

		mainPanel 			= 	new HorizontalSplitPanel();
		visualPluginLayout	=	new HorizontalLayout();
		welcome 			= 	new VerticalLayout();
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
		setTabs.setVisible(false);

		tabs.setStyleName(Reindeer.TABSHEET_SMALL);
		tabs.setSizeFull();

		setLayout.setSplitPosition(100);
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

		mainPanel.setSizeFull();
		mainPanel.setImmediate(true);
		mainPanel.setSplitPosition(20);   
		mainPanel.setFirstComponent(setLayout);

		visualPluginLayout.setStyleName(Reindeer.LAYOUT_WHITE);
		visualPluginLayout.setSizeFull();

		ThemeResource resource = new ThemeResource("img/welcome.png");
		Embedded image = new Embedded("", resource);

		welcome.setSizeFull();
		welcome.addComponent(image);
		welcome.setComponentAlignment(image, Alignment.MIDDLE_CENTER);	
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
		HorizontalLayout titleHeaderLayout = new HorizontalLayout();

		ThemeResource resource = new ThemeResource("img/geWorkbench-Title.png");
		Embedded image = new Embedded("", resource);
		titleHeaderLayout.addComponent(image);
		titleHeaderLayout.setComponentAlignment(image, Alignment.TOP_LEFT);

		header.addComponent(titleHeaderLayout);

		CssLayout titleLayout = new CssLayout();
		Label user = new Label("Welcome, " + SessionHandler.get().getUsername());
		user.setSizeUndefined();
		titleLayout.addComponent(user);


		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		Label help = new Label("<div class=\"v-button\"><span class=\"v-button-wrap\"><a href=\"http:///wiki.c2b2.columbia.edu/workbench/index.php/Home\" target=\"_blank\" class=\"v-button-caption\">Help</a></div></div>", Label.CONTENT_XHTML);
		help.setWidth(null);

		UWorkspaceManager workspaceButtons = new UWorkspaceManager();

		buttons.addComponent(workspaceButtons);
		buttons.setComponentAlignment(workspaceButtons, Alignment.MIDDLE_RIGHT);

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
	 * Supplies the container for the dataset and result tree to display. 
	 * @return dataset and resultset container 
	 */
	public HierarchicalContainer getDataContainer() {

		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		dataSets.addContainerProperty("My Projects", String.class, null);

		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", user.getId());
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());

		List<?> projects =  FacadeFactory.getFacade().list("Select p from Project as p where p.owner=:owner and p.workspace =:workspace", param);

		for (int h=0; h<projects.size(); h++ ) {

			String projectName 	=	((Project) projects.get(h)).getName();
			Long realProjectId	= 	((Project) projects.get(h)).getId();
			String projectId	=	((Project) projects.get(h)).getId() + "P";	

			dataSets.addItem(projectId);
			dataSets.getContainerProperty(projectId, "My Projects").setValue(projectName);

			Map<String, Object> parameters 		= 	new HashMap<String, Object>();
			parameters.put("owner", user.getId());
			parameters.put("project", realProjectId);

			List<?> data = FacadeFactory.getFacade().list("Select p from DataSet as p where p.owner=:owner and p.project=:project ", parameters);

			for(int i=0; i<data.size(); i++) {

				String id 		=	((DataSet) data.get(i)).getName();
				String dataId	=	((DataSet) data.get(i)).getId() + "D";	
				Long realDataId =	((DataSet) data.get(i)).getId();

				dataSets.addItem(dataId);
				dataSets.getContainerProperty(dataId, "My Projects").setValue(id);
				dataSets.setParent(dataId, projectId);

				Map<String, Object> params 	= 	new HashMap<String, Object>();
				params.put("owner", user.getId());
				params.put("parent", realDataId);
				List<?> results = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.date", params);

				for(int j=0; j<results.size(); j++) {

					String subId 		=	((ResultSet) results.get(j)).getName();
					String subSetId		=	((ResultSet) results.get(j)).getId() + "R";	

					dataSets.addItem(subSetId);
					dataSets.getContainerProperty(subSetId, "My Projects").setValue(subId);
					dataSets.setChildrenAllowed(subSetId, false);
					dataSets.setParent(subSetId, dataId);

				}
			}
		}
		return dataSets;
	}

	/**
	 * UAccordionPanel builds the DataSet area of geWorkbench.
	 * It inlcudes dataset & resultset tree, Markers table, Phenotypes table and Sets Tab sheet. 
	 * @author Nikhil Reddy
	 */
	class UAccordionPanel extends  Accordion implements Action.Handler {

		private static final long serialVersionUID = 4523693969296820932L;

		private Table arrayTable;

		private Table markerTable;

		final Action ACTION_SUBSET 		= 	new Action("Create SubSet");

		final Action ACTION_LINKOUT		=	new Action("Link Out");

		final Action[] ACTIONS_CREATE 	= 	new Action[] { ACTION_SUBSET, ACTION_LINKOUT };

		private String setType;

		protected String selectedValues = null;

		public Long dataSetId;

		private DSMicroarraySet maSet;

		private Tab markerTab;

		private Tab arrayTab;

		private Action ACTION_DELETE	 	= 	new Action("Delete");

		private final Action ACTION_ANALYZE		= 	new Action("Analyze Data"); 

		private final Action ACTION_NORMALIZE	= 	new Action("Normalize Data");

		private final Action ACTION_FILTER		= 	new Action("Filter Data");

		private final Action ACTION_INTERACTIONS =	new Action("Get Interactions");

		private final Action[] ACTIONS 			= 	new Action[] { ACTION_ANALYZE, ACTION_INTERACTIONS, ACTION_NORMALIZE, ACTION_FILTER, ACTION_DELETE };

		public UAccordionPanel(boolean closable) {

			super();

			this.setStyleName(Reindeer.TABLE_STRONG);
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
			Tab t = addTab(l);
			t.setCaption("Project Manager");

			dataTree = new TreeTable();
			dataTree.setImmediate(true);
			dataTree.setSizeFull();
			dataTree.setSortDisabled(true);
			dataTree.areChildrenAllowed(true);
			dataTree.setStyleName("borderless strong");
			dataTree.setContainerDataSource(getDataContainer());
			dataTree.setSelectable(true);
			dataTree.setMultiSelect(false);
			dataTree.addActionHandler(this);

			
			dataTree.addListener(new ItemClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void itemClick(ItemClickEvent event) {

					try {
						String itemId 	= 	(String) event.getItemId();
						Long realId		=	Long.parseLong(itemId.substring(0, itemId.length() - 1));
						
						dataProperties 	= new String[3];

						if(itemId.contains("D")) {

							String query 					= 	"Select p from DataSet as p where p.id=:id";
							Map<String, Object> parameters 	= 	new HashMap<String, Object>();

							parameters.put("id", realId);

							parentId = itemId;
							
							DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

							if(dataSet != null) {

								dataSetId 					=	dataSet.getId();
								byte[] dataByte 			= 	dataSet.getData();

								UVisualPlugin tabSheet = null;
								
								dataProperties[0] 	= 	dataSet.getType();
								dataProperties[1]	= 	parentId;
								
								if (dataSet.getType().equals("PDB File")){

									if(markerTab.isVisible()) {

										setLayout.setSplitPosition(100);
										setTabs.setVisible(false);
										markerTab.setVisible(false);
										arrayTab.setVisible(false);
									}

									dataProperties[2]		=	null;	
									DSProteinStructure pSet	=	(DSProteinStructure) ObjectConversion.toObject(dataByte);
									tabSheet 				=	new UVisualPlugin(pSet, dataProperties);

								}else{
									if(!setTabs.isVisible()) {
										setLayout.setSplitPosition(60);
										setTabs.setVisible(true);
										setTabLayout.addComponent(setTabs);
										markerTab.setVisible(true);
										arrayTab.setVisible(true);
									}

									maSet 					= 	(DSMicroarraySet) ObjectConversion.toObject(dataByte);
									AffyAnnotationParser parser = new Affy3ExpressionAnnotationParser();
									File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
									AnnotationParser.cleanUpAnnotatioAfterUnload(maSet);
									AnnotationParser.loadAnnotationFile(maSet, annotFile, parser);

									markerTable.setContainerDataSource(markerTableView(maSet));
									arrayTable.setContainerDataSource(arrayTableView(maSet));

									tabSheet 				= 	new UVisualPlugin(maSet, dataProperties);
									setTabs.populateTabSheet(maSet);
								}

								menuPanel.setSecondComponent(tabSheet);
								setMainPanelSecondComponent(menuPanel);
							}

						}else if(itemId.contains("R")) {

							if(markerTab.isVisible()) {
								setLayout.setSplitPosition(100);
								setTabs.setVisible(false);
								markerTab.setVisible(false);
								arrayTab.setVisible(false);
							}

							String querySub 					= 	"Select p from ResultSet as p where p.id=:id";
							Map<String, Object> params 			= 	new HashMap<String, Object>();

							params.put("id", realId);

							ResultSet resultSet 				= 	FacadeFactory.getFacade().find(querySub, params);
							if(resultSet != null) {

								byte[] dataByte 					= 	resultSet.getData();
								UVisualPlugin tabSheet = null;

								dataProperties[0] 	= 	resultSet.getType();
								dataProperties[1]	= 	parentId;
								dataProperties[2]	=	null;
								
								if(resultSet.getType().equalsIgnoreCase("CNKB")) {

									@SuppressWarnings("unchecked")
									Vector<CellularNetWorkElementInformation> hits 	=	(Vector<CellularNetWorkElementInformation>) ObjectConversion.toObject(dataByte);
									tabSheet	= 	new UVisualPlugin(hits, dataProperties);

									
									
								}else if(resultSet.getType().equalsIgnoreCase("Hierarchical Clustering")) {

									CSHierClusterDataSet hierResults 	= 	(CSHierClusterDataSet) ObjectConversion.toObject(dataByte);
									tabSheet 	= 	new UVisualPlugin(hierResults, dataProperties);

								}else if(resultSet.getType().equalsIgnoreCase("ARACne")) {

									AdjacencyMatrixDataSet dSet 	= 	(AdjacencyMatrixDataSet) ObjectConversion.toObject(dataByte);
									tabSheet 	= 	new UVisualPlugin(dSet, dataProperties);

								}else if(resultSet.getType().equalsIgnoreCase("MarkUs")) {

									MarkUsResultDataSet prtSet		= 	(MarkUsResultDataSet) ObjectConversion.toObject(dataByte);
									tabSheet 	= 	new UVisualPlugin(prtSet, dataProperties);

								}
							    else if(resultSet.getType().equalsIgnoreCase("Anova")) {
							        @SuppressWarnings("unchecked")
									CSAnovaResultSet<DSGeneMarker>  anovaResultSet =	(CSAnovaResultSet<DSGeneMarker>) ObjectConversion.toObject(dataByte);							 
								    tabSheet 	= 	new UVisualPlugin(anovaResultSet, dataProperties);

							    }
								

								menuPanel.setSecondComponent(tabSheet);
								setMainPanelSecondComponent(menuPanel);

							}	

						} else if(itemId.contains("P")) {
			
							setLayout.setSplitPosition(100);
							setTabs.setVisible(false);
							markerTab.setVisible(false);
							arrayTab.setVisible(false);
							setMainPanelSecondComponent(welcome);
							
						}
					}catch(Exception e) {

						e.printStackTrace();

					}

				}

			});

			l.addComponent(dataTree);
			l.setSizeFull();

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

				@SuppressWarnings("deprecation")
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
							((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
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

			markerTab = addTab(markerTable);
			markerTab.setCaption("Makers");
			markerTab.setVisible(false);

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
				@SuppressWarnings("deprecation")
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
						((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
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
			arrayTab = addTab(arrayTable);
			arrayTab.setCaption("Phenotypes");
			arrayTab.setVisible(false);

		}


		protected void setSelectedValues(String value) {

			this.selectedValues = value;

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

			if(target != null) {
				if(target.toString().contains("P")) {
					return null;
				}else if(target.toString().contains("D")) {
					return ACTIONS;
				}else if(target.toString().contains("R")){
					return new Action[] { ACTION_DELETE};
				}
			}
			return ACTIONS;
		}

		/**
		 * Method handles Actions of the context menu on the dataset tree.
		 */
		@Override
		public void handleAction(Action action, Object sender, Object target) {

			String dataName 	=	(String) target;
			Long realItemId		=	Long.parseLong(dataName.substring(0, dataName.length() - 1));	
			
			if (action == ACTION_DELETE) {	
				
				if(dataName.contains("D")) {
					String query 					= 	"Select p from DataSet as p where p.id=:id and p.owner=:owner";
					Map<String, Object> parameters 	= 	new HashMap<String, Object>();

					parameters.put("id", realItemId);
					parameters.put("owner", user.getId());

					DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

					if(dataSet != null) {

						/* Deleting result sets if there are any */
						if(dataTree.hasChildren(target)) {
							
							String querySub 			= 	"Select p from ResultSet as p where p.parent=:parent and p.owner=:owner";
							Map<String, Object> params 	= 	new HashMap<String, Object>();

							params.put("owner", user.getId());
							params.put("parent", dataName);
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
					}

				} else if(dataName.contains("R")) {

					String querySub 			= 	"Select p from ResultSet as p where p.id=:id and p.owner=:owner";
					Map<String, Object> params 	= 	new HashMap<String, Object>();

					params.put("id", realItemId);
					params.put("owner", user.getId());
					ResultSet resultSet 		= 	FacadeFactory.getFacade().find(querySub, params);
					FacadeFactory.getFacade().delete(resultSet);
					dataTree.removeItem(target);
				}

				setMainPanelSecondComponent(welcome);

			}else if(action == ACTION_ANALYZE || action == ACTION_INTERACTIONS) {

				if(dataName.contains("D")) {
					dataProperties 	= new String[2];
					
					String query 					= 	"Select p from DataSet as p where p.id=:id and p.owner=:owner";
					Map<String, Object> parameters 	= 	new HashMap<String, Object>();

					parameters.put("id", realItemId);
					parameters.put("owner", user.getId());

					DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);

					if(dataSet != null) {

						byte[] dataByte 			= 	dataSet.getData();

						dataProperties[0]	= 	parentId;
						dataProperties[1]	=	dataSet.getType();
								
						if (dataSet.getType().equals("PDB File")){
							if (action == ACTION_ANALYZE){
								
								dataProperties[2]			=	"Analyze Data";
								DSProteinStructure pSet		=	(DSProteinStructure) ObjectConversion.toObject(dataByte);
								UVisualPlugin tabSheet 		= 	new UVisualPlugin(pSet, dataProperties);
								
								menuPanel.setSecondComponent(tabSheet);
								setMainPanelSecondComponent(menuPanel);
							}
							return;
						}

						DSMicroarraySet maSet 		= 	(DSMicroarraySet) ObjectConversion.toObject(dataByte);
						if(maSet.getAnnotationFileName() != null){
							AffyAnnotationParser parser = new Affy3ExpressionAnnotationParser();
							File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
							AnnotationParser.loadAnnotationFile(maSet, annotFile, parser);
						}

						markerTable.setContainerDataSource(markerTableView(maSet));
						arrayTable.setContainerDataSource(arrayTableView(maSet));

						if(action == ACTION_ANALYZE) {
							dataProperties[2]			=	"Analyze Data";
							UVisualPlugin tabSheet = new UVisualPlugin(maSet, dataProperties);
							menuPanel.setSecondComponent(tabSheet);
						}else {
							dataProperties[2]			=	"Get Interactions";
							UVisualPlugin tabSheet = new UVisualPlugin(maSet, dataProperties);
							menuPanel.setSecondComponent(tabSheet);
						}
						setMainPanelSecondComponent(menuPanel);
					}
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

			if(event.getDataType() == "Result Node") {
				
				dataTree.addItem(event.getDataSetId()+"R");
				dataTree.getContainerProperty(event.getDataSetId()+"R", "My Projects").setValue(event.getDataSetName());
				dataTree.setChildrenAllowed(event.getDataSetId()+"R", false);
				dataTree.setParent(event.getDataSetId()+"R", parentId);
				dataTree.setCollapsed(parentId, false);
						
			}else if(event.getDataType() != null) {
				dataTree.addItem(event.getDataSetId()+"D");
				dataTree.getContainerProperty(event.getDataSetId()+"D", "My Projects").setValue(event.getDataSetName());
				dataTree.setParent(event.getDataSetId()+"D", event.getDataType()+"P");
				dataTree.setCollapsed(event.getDataType()+"P", false);
			
			} else {
				
				dataTree.addItem(event.getDataSetId()+"P");
				dataTree.getContainerProperty(event.getDataSetId()+"P", "My Projects").setValue(event.getDataSetName());
		
			}
		}
	}

}
