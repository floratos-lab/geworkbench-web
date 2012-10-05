package org.geworkbenchweb.layout;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbenchweb.GeworkbenchRoot; 
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.events.PluginEvent.PluginEventListener;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringWrapper;
import org.geworkbenchweb.plugins.microarray.Microarray;

/**
 * UMainLayout sets up the basic layout and style of the application.
 * @author Nikhil Reddy
 */
@SuppressWarnings("deprecation")
public class UMainLayout extends VerticalLayout {

	private static final long serialVersionUID = 6214334663802788473L;

	private final SplitPanel mainSplit;

	private ComboBox search;

	private final Tree navigationTree;

	private final VisualPluginView pluginView = new VisualPluginView();
	
	private VerticalLayout menuLayout = new VerticalLayout(); 
	
	private HorizontalLayout dataNavigation;

	private Long dataSetId;

	User user = SessionHandler.get();
	
	private Tree markerTree;

	private Tree arrayTree;
	
	private CssLayout leftMainLayout;
	
	private ICEPush pusher;
	
	ThemeResource projectIcon 		= 	new ThemeResource("../custom/icons/project16x16.gif");
	ThemeResource microarrayIcon 	=	new ThemeResource("../custom/icons/chip16x16.gif");
	ThemeResource proteinIcon 		=	new ThemeResource("../custom/icons/dna16x16.gif");
	ThemeResource hcIcon	 		=	new ThemeResource("../custom/icons/dendrogram16x16.gif");
	ThemeResource pendingIcon	 	=	new ThemeResource("../custom/icons/pending.gif");

	public UMainLayout() {

		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);

		PluginListener pluginListener = new PluginListener();
		GeworkbenchRoot.getBlackboard().addListener(pluginListener);
		
		AnalysisListener analysisListener = new AnalysisListener();
		GeworkbenchRoot.getBlackboard().addListener(analysisListener);

		setSizeFull();
		pusher = GeworkbenchRoot.getPusher();
		addComponent(pusher);
		
		HorizontalLayout topBar = new HorizontalLayout();
		addComponent(topBar);
		topBar.setHeight("44px");
		topBar.setWidth("100%");
		topBar.setStyleName("topbar");
		topBar.setSpacing(true);
		
		dataNavigation = new HorizontalLayout();
		dataNavigation.setHeight("27px");
		dataNavigation.setWidth("100%");
		dataNavigation.setStyleName("menubar");
		dataNavigation.setSpacing(true);
		dataNavigation.setMargin(false, true, false, false);
		dataNavigation.setImmediate(true);
		
		Component logo = createLogo();
		topBar.addComponent(logo);
		topBar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);

		mainSplit = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
		mainSplit.setSizeFull();
		mainSplit.setStyleName("main-split");

		addComponent(mainSplit);
		setExpandRatio(mainSplit, 1);

		leftMainLayout = new CssLayout();
		leftMainLayout.setImmediate(true);
		leftMainLayout.setSizeFull();
			
		navigationTree = createMenuTree();
		navigationTree.setItemCaptionPropertyId("Name");
		navigationTree.setItemIconPropertyId("Icon");
		navigationTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		navigationTree.setStyleName(Reindeer.TREE_CONNECTORS);
		
		leftMainLayout.addComponent(dataNavigation);
		leftMainLayout.addComponent(navigationTree);
		mainSplit.setFirstComponent(leftMainLayout);
		mainSplit.setSplitPosition(275, SplitPanel.UNITS_PIXELS);
		mainSplit.setSecondComponent(createMainMenu());
		
		HorizontalLayout quicknav = new HorizontalLayout();
		topBar.addComponent(quicknav);
		topBar.setComponentAlignment(quicknav, Alignment.MIDDLE_RIGHT);
		quicknav.setStyleName("segment");

		Component searchComponent = createSearch();
		quicknav.addComponent(searchComponent);

		Component treeSwitch = createTreeSwitch();
		quicknav.addComponent(treeSwitch);
	}

	/**
	 * Administrative menu for the user
	 * @return Component
	 */
	private Component createMainMenu() {
		
		menuLayout.setImmediate(true);
		menuLayout.setSizeFull();
		
		HorizontalLayout menuLayout1 = new HorizontalLayout();
		menuLayout1.setImmediate(true);
		menuLayout1.setHeight("27px");
		menuLayout1.setWidth("100%");
		menuLayout1.setStyleName("menubar");
		menuLayout1.setSpacing(true);
		menuLayout1.setMargin(false, true, false, false);
		UMainToolBar toolBar = new UMainToolBar();
		menuLayout1.addComponent(toolBar);
		menuLayout1.setComponentAlignment(toolBar, Alignment.TOP_RIGHT);
		
		menuLayout.addComponent(menuLayout1);
		return menuLayout;
	}
	
	/**
	 * Creates the data tree for the project panel
	 * @return Tree
	 */
	private Tree createMenuTree() {
		final Tree tree = new Tree();
		tree.setImmediate(true);
		tree.setStyleName("menu");
		tree.setContainerDataSource(getDataContainer());
		tree.addListener(new Tree.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void valueChange(ValueChangeEvent event) {

				Item selectedItem = tree.getItem(event.getProperty().getValue());
				VisualPlugin f;

				try {				
					if(!event.getProperty().getValue().equals(null)) {
						
						String className = (String) selectedItem.getItemProperty("Type").getValue();
						if(className.contains("Results")) {
						
							ClassLoader classLoader = this.getClass().getClassLoader();
							String packageName = className.substring(0, className.length() - 7);

							String loadClass = "org.geworkbenchweb.plugins." + 
									packageName.toLowerCase()+
									".results." +
									className;
							@SuppressWarnings("rawtypes")
							Class aClass = classLoader.loadClass(loadClass);
							dataSetId = (Long) event.getProperty().getValue();

							f = (VisualPlugin) aClass.getDeclaredConstructor(Long.class).newInstance(dataSetId); 
							setVisualPlugin(f);
						}else {
							ClassLoader classLoader = this.getClass().getClassLoader();
							String loadClass = "org.geworkbenchweb.plugins." + 
									className.toLowerCase() +
									"."+
									className;
							@SuppressWarnings("rawtypes")
							Class aClass = classLoader.loadClass(loadClass);
							dataSetId = (Long) event.getProperty().getValue();

							f = (VisualPlugin) aClass.getDeclaredConstructor(Long.class).newInstance(dataSetId); 
							setVisualPlugin(f);
						}
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}

			}
		});
		return tree;
	}

	/**
	 * Sets the VisualPlugin 
	 * @return
	 */
	public void setVisualPlugin(final VisualPlugin f) {
		
		if(f instanceof Microarray) {
			dataNavigation.removeAllComponents();		
			markerTree = new Tree();
			arrayTree =	new Tree();
			
			final MenuBar toolBar = new MenuBar();
			final MenuItem project = toolBar.addItem("PROJECT VIEW", new Command() {

				private static final long serialVersionUID = 1L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					navigationTree.setVisible(true);
					markerTree.setVisible(false);
					arrayTree.setVisible(false);
					setVisualPlugin(f);
				}
			});
			toolBar.setImmediate(true);
			project.setEnabled(false);
			toolBar.addItem("SET VIEW", new Command() {

				private static final long serialVersionUID = 1L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					selectedItem.setEnabled(false);
					project.setEnabled(true);
					navigationTree.setVisible(false);
					
					markerTree.setImmediate(true);
					markerTree.setSelectable(false);
					
					arrayTree.setImmediate(true);
					arrayTree.setSelectable(false);
					
					List<DataSet> data = DataSetOperations.getDataSet(dataSetId);
					DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(data.get(0).getData());
					
					markerTree.setContainerDataSource(markerTableView(maSet));
					arrayTree.setContainerDataSource(arrayTableView(maSet));
					
					markerTree.setItemCaptionPropertyId("Labels");
					markerTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
					
					arrayTree.setItemCaptionPropertyId("Labels");
					arrayTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
					
					leftMainLayout.addComponent(markerTree);
					leftMainLayout.addComponent(arrayTree);
				}
				
			});
			
			dataNavigation.addComponent(toolBar);
			dataNavigation.setComponentAlignment(toolBar, Alignment.MIDDLE_LEFT);
		} else {
			dataNavigation.removeAllComponents();
		}
		menuLayout.addComponent(pluginView);
		menuLayout.setExpandRatio(pluginView, 1);
		pluginView.setVisualPlugin(f);
	}

	/**
	 * Creates the logo for the Application		
	 * @return
	 */
	private Component createLogo() {
		Button logo = new NativeButton("", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				
			}
		});
		logo.setDescription("geWorkbench Home");
		logo.setStyleName(Button.STYLE_LINK);
		logo.addStyleName("logo");
		return logo;
	}

	public void removeSubwindows() {
		Collection<Window> wins = getApplication().getMainWindow().getChildWindows();
		if (null != wins) {
			for (Window w : wins) {
				getApplication().getMainWindow().removeWindow(w);
			}
		}
	}

	/*
	 * Search
	 */
	private Component createSearch() {
		search = new ComboBox();
		search.setWidth("160px");
		search.setNewItemsAllowed(false);
		search.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		search.setNullSelectionAllowed(true);
		search.setImmediate(true);
		search.setInputPrompt("Search samples...");

		// TODO add icons for section/sample
		/*
		 * PopupView pv = new PopupView("", search) { public void
		 * changeVariables(Object source, Map variables) {
		 * super.changeVariables(source, variables); if (isPopupVisible()) {
		 * search.focus(); } } };
		 */
		final PopupView pv = new PopupView("<span></span>", search);
		pv.addListener(new PopupView.PopupVisibilityListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if (event.isPopupVisible()) {
					search.focus();
				}
			}
		});
		pv.setStyleName("quickjump");
		pv.setDescription("Quick jump");

		return pv;
	}

	private Component createTreeSwitch() {
		final Button b = new NativeButton();
		b.setStyleName("tree-switch");
		b.addStyleName("down");
		b.setDescription("Toggle sample tree visibility");
		b.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (b.getStyleName().contains("down")) {
					b.removeStyleName("down");
					mainSplit.setSplitPosition(0);
					navigationTree.setVisible(false);
					mainSplit.setLocked(true);
				} else {
					b.addStyleName("down");
					mainSplit
					.setSplitPosition(250, SplitPanel.UNITS_PIXELS);
					mainSplit.setLocked(false);
					navigationTree.setVisible(true);
					arrayTree.setVisible(false);
					markerTree.setVisible(false);
				}
			}
		});
		mainSplit.setSplitPosition(250, SplitPanel.UNITS_PIXELS);
		return b;
	}


	/**
	 * Supplies the container for the dataset and result tree to display. 
	 * @return dataset and resultset container 
	 */
	public HierarchicalContainer getDataContainer() {

		
		
		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		dataSets.addContainerProperty("Name", String.class, null);
		dataSets.addContainerProperty("Type", String.class, null);
		dataSets.addContainerProperty("Icon", Resource.class, null);
		
		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", user.getId());
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());

		List<?> projects =  FacadeFactory.getFacade().list("Select p from Project as p where p.owner=:owner and p.workspace =:workspace", param);

		for (int h=0; h<projects.size(); h++ ) {

			String projectName 	=	((Project) projects.get(h)).getName();
			Long realProjectId	= 	((Project) projects.get(h)).getId();
			Long projectId		=	((Project) projects.get(h)).getId();	

			Item item  = dataSets.addItem(projectId);
			item.getItemProperty("Name").setValue(projectName);
			item.getItemProperty("Type").setValue("UploadData");
			item.getItemProperty("Icon").setValue(projectIcon);

			Map<String, Object> parameters 		= 	new HashMap<String, Object>();
			parameters.put("owner", user.getId());
			parameters.put("project", realProjectId);

			List<?> data = FacadeFactory.getFacade().list("Select p from DataSet as p where p.owner=:owner and p.project=:project ", parameters);

			for(int i=0; i<data.size(); i++) {

				String id 		=	((DataSet) data.get(i)).getName();
				Long dataId		=	((DataSet) data.get(i)).getId();	
				Long realDataId =	((DataSet) data.get(i)).getId();

				Item subItem = dataSets.addItem(dataId);
				subItem.getItemProperty("Name").setValue(id);
				if(((DataSet) data.get(i)).getType().equalsIgnoreCase("Expression File")) {
					subItem.getItemProperty("Type").setValue("Microarray");
					subItem.getItemProperty("Icon").setValue(microarrayIcon);
				} else {
					subItem.getItemProperty("Type").setValue("ProteinStructure");
					subItem.getItemProperty("Icon").setValue(proteinIcon);
				}
				
				dataSets.setParent(dataId, projectId);

				Map<String, Object> params 	= 	new HashMap<String, Object>();
				params.put("owner", user.getId());
				params.put("parent", realDataId);
				List<?> results = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.date", params);

				for(int j=0; j<results.size(); j++) {

					String subId 		=	((ResultSet) results.get(j)).getName();
					Long subSetId		=	((ResultSet) results.get(j)).getId();	
					String type			=	((ResultSet) results.get(j)).getType();
					
					Item res = dataSets.addItem(subSetId);
					res.getItemProperty("Name").setValue(subId);
					res.getItemProperty("Type").setValue(type);
					if(type.equalsIgnoreCase("HierarchicalClusteringResults")) {
						res.getItemProperty("Icon").setValue(hcIcon);
					}
					dataSets.setChildrenAllowed(subSetId, false);
					dataSets.setParent(subSetId, dataId);

				}
			}
		}
		return dataSets;
	
	}

	/**
	 * Method is used to populate Phenotype Panel
	 * @param maSet
	 * @return - Indexed container with array labels
	 */
	private HierarchicalContainer arrayTableView(DSMicroarraySet maSet) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();

		tableData.addContainerProperty("Labels", String.class, null);
		Item mainItem 					= 	tableData.addItem("Phenotypes");
		mainItem.getItemProperty("Labels").setValue("Phenotypes");
		
		for(int k=0;k<maSet.size();k++) {

			Item item 					= 	tableData.addItem(k);
			tableData.setChildrenAllowed(k, false);
			item.getItemProperty("Labels").setValue(maSet.get(k).getLabel());
			tableData.setParent(k, "Phenotypes");
		}
		return tableData;
	}

	/**
	 * Method is used to populate Marker Panel
	 * @param maSet
	 * @return - Indexed container with marker labels
	 */
	private HierarchicalContainer markerTableView(DSMicroarraySet maSet) {

		HierarchicalContainer tableData 		= 	new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		Item mainItem =  tableData.addItem("Markers");
		mainItem.getItemProperty("Labels").setValue("Markers");
		
		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);
			
			for(int k=0;k<=maSet.size();k++) {
				if(k == 0) {
					item.getItemProperty("Labels").setValue(maSet.getMarkers().get(j).getLabel() 
							+ " (" 
							+ maSet.getMarkers().get(j).getGeneName()
							+ ")");
					tableData.setParent(j, "Markers");
				} 
			}
		}
		return tableData;

	}
	
	/**
	 * Adds the node to the dataTree  
	 */
	public class NodeAddListener implements NodeAddEventListener {
		@Override
		public void addNode(NodeAddEvent event) {	
			if(event.getData() instanceof ResultSet ) {
				ResultSet  res = (ResultSet) event.getData();
				navigationTree.addItem(res.getId());
				navigationTree.getContainerProperty(res.getId(), "Name").setValue(res.getName());
				navigationTree.getContainerProperty(res.getId(), "Type").setValue(res.getType());
				if(res.getName().contains("Pending")) {
					navigationTree.getContainerProperty(res.getId(), "Icon").setValue(pendingIcon);
				} else {
					if(res.getType().equalsIgnoreCase("HierarchicalClusteringResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(hcIcon);
					}
				}
				navigationTree.setChildrenAllowed(res.getId(), false);
				navigationTree.setParent(res.getId(), res.getParent());
				navigationTree.select(res.getId());
			}else if(event.getData() instanceof Project) {
				Project pro = (Project) event.getData();
				navigationTree.addItem(pro.getId());
				navigationTree.getContainerProperty(pro.getId(), "Name").setValue(pro.getName());
				navigationTree.getContainerProperty(pro.getId(), "Type").setValue("UploadData");
				navigationTree.getContainerProperty(pro.getId(), "Icon").setValue(projectIcon);
			} else if(event.getData() instanceof DataSet) {
				DataSet dS = (DataSet) event.getData();
				String dataType;
				navigationTree.addItem(dS.getId());
				if(dS.getType().equalsIgnoreCase("PDB File")) {
					dataType = "ProteinStructure";
					navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(proteinIcon);
				} else {
					dataType = "Microarray";
					navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(microarrayIcon);
				}
				navigationTree.getContainerProperty(dS.getId(), "Name").setValue(dS.getName());
				navigationTree.getContainerProperty(dS.getId(), "Type").setValue(dataType);
				navigationTree.setParent(dS.getId(), dS.getProject());
			}
		}
	}

	/**
	 * PluginListener class listenes to the PluginEvent and sets the desired VisualPlugin
	 * @author np2417
	 */
	public class PluginListener implements PluginEventListener {

		@SuppressWarnings("unchecked")
		@Override
		public void pluginSet(PluginEvent event) {

			navigationTree.unselect(event.getDataId());
			String pluginName = event.getPluginName();
			Long dataSetId = event.getDataId();

			ClassLoader classLoader = this.getClass().getClassLoader();
			String loadClass = "org.geworkbenchweb.plugins." + 
					pluginName.toLowerCase() +
					"."+
					pluginName;

			@SuppressWarnings("rawtypes")
			Class aClass = null;
			try {
				aClass = classLoader.loadClass(loadClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			VisualPlugin f = null;
			try {
				f = (VisualPlugin) aClass.getDeclaredConstructor(Long.class).newInstance(dataSetId);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} 
			setVisualPlugin(f);
		}
	}
	
	public class AnalysisListener implements AnalysisSubmissionEventListener {

		@Override
		public void SubmitAnalysis(final AnalysisSubmissionEvent event) {

			final ResultSet resultSet = event.getResultSet();
			final HashMap<Serializable, Serializable> params = event.getParameters();
			
			if(resultSet.getType().contains("HierarchicalClusteringResults") ) {
				Thread analysis = new Thread() {
					public void run() {
						synchronized(getApplication()) {
							DSMicroarraySetView<DSGeneMarker, DSMicroarray> data = 
									new CSMicroarraySetView<DSGeneMarker, DSMicroarray>((DSMicroarraySet) event.getDataSet());
							
							HierarchicalClusteringWrapper analysis 	= 	
									new HierarchicalClusteringWrapper(data, (Integer) params.get("metric"), (Integer) params.get("method"), (Integer) params.get("dimension"));
							
							HierCluster[] resultClusters = analysis.execute();
							CSHierClusterDataSet results = new CSHierClusterDataSet(resultClusters, null, false,
									"Hierarchical Clustering", data);
							resultSet.setName("Hierarchical Clustering");
							resultSet.setData(ObjectConversion.convertToByte(results));
							FacadeFactory.getFacade().store(resultSet);	

							MessageBox mb = new MessageBox(getWindow(), 
									"Analysis Completed", 
									MessageBox.Icon.INFO, 
									"Hierarchical Clustering you submitted is now completed. " +
									"Click on the result node to see the dendrogram",  
									new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
							mb.show(new MessageBox.EventListener() {
								private static final long serialVersionUID = 1L;
								@Override
								public void buttonClicked(ButtonType buttonType) {    	
									if(buttonType == ButtonType.OK) {
										NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
										GeworkbenchRoot.getBlackboard().fire(resultEvent);
									}
								}
							});	
						}
						pusher.push();
					}
				};
				analysis.start();
			}
		}
	}
}
