package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.UserDirUtils;
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
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * UMainLayout sets up the basic layout and style of the application.
 * @author Nikhil Reddy
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class UMainLayout extends VerticalLayout {
	
	private static Log log = LogFactory.getLog(UMainLayout.class);

	private static final long serialVersionUID = 6214334663802788473L;

	static private ThemeResource pendingIcon 	=	new ThemeResource("../custom/icons/pending.gif");
	static private ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	static private ThemeResource CancelIcon 	= 	new ThemeResource("../runo/icons/16/cancel.png");
	static private ThemeResource openSetIcon	=	new ThemeResource("../custom/icons/open_set.png");
	static private ThemeResource saveSetIcon	=	new ThemeResource("../custom/icons/save_set.png");

	final private SplitPanel mainSplit = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);

	final private VisualPluginView pluginView = new VisualPluginView();

	final private User user = SessionHandler.get();
			
	final private CssLayout leftMainLayout = new CssLayout();

	final private ICEPush pusher = GeworkbenchRoot.getPusher();
	
	final private MenuBar toolBar = new MenuBar();
 
	final private DataAnnotationPanel annotationPanel = new DataAnnotationPanel();;
	
	final private Button annotButton = new Button(); 		
	
	final private Button removeButton = new Button();	
	
	final private Button removeSetButton = new Button();
	
	final private Button openSetButton = new Button(), saveSetButton = new Button();
			
	final private GenspaceLogger genspaceLogger = new GenspaceLogger();;

	final private UMainToolBar mainToolBar 	= 	new UMainToolBar(pluginView, genspaceLogger);

	final private MenuItem setViewMeuItem;
	final private MenuItem workspaceViewMenuItem;
	final private SetViewCommand setViewCommand;
	
	final private Tree navigationTree = createNavigationTree();;
	
	private Long dataSetId;
	
	public UMainLayout() {

		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);

		/*Enable genspace logger in geWorkbench*/
		GeworkbenchRoot.getBlackboard().addListener(genspaceLogger);
		
		setSizeFull();
		setImmediate(true);
		
		addComponent(pusher);

		HorizontalLayout topBar 		= 	new HorizontalLayout();
		
		addComponent(topBar);
		topBar.setHeight("44px");
		topBar.setWidth("100%");
		topBar.setStyleName("topbar");
		topBar.setSpacing(true);

		annotButton.setDescription("Show Annotation");
		annotButton.setStyleName(BaseTheme.BUTTON_LINK);
		annotButton.setIcon(annotIcon);
		
		removeButton.setDescription("Delete selected data");
		removeButton.setStyleName(BaseTheme.BUTTON_LINK);
		removeButton.setIcon(CancelIcon);
		
		removeSetButton.setDescription("Delete selected subset");
		removeSetButton.setStyleName(BaseTheme.BUTTON_LINK);
		removeSetButton.setIcon(CancelIcon);
		
		openSetButton.setDescription("Open Set");
		openSetButton.setStyleName(BaseTheme.BUTTON_LINK);
		openSetButton.setIcon(openSetIcon);
		
		saveSetButton.setDescription("Save Set");
		saveSetButton.setStyleName(BaseTheme.BUTTON_LINK);
		saveSetButton.setIcon(saveSetIcon);
		
		annotButton.setEnabled(false);
		removeButton.setEnabled(false);
		removeSetButton.setVisible(false);
		openSetButton.setVisible(false);
		saveSetButton.setVisible(false);
		
		annotButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				annotationPanel.expand();
			}
		});
		
		toolBar.setEnabled(false);
		toolBar.setImmediate(true);
		toolBar.setStyleName("transparent");
		setViewCommand = new SetViewCommand(this);
		setViewMeuItem = toolBar.addItem("Set View", setViewCommand);
		setViewMeuItem.setEnabled(false);
		workspaceViewMenuItem = toolBar.addItem("Workspace View", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				switchToWorkspaceView();
			}
		});
		workspaceViewMenuItem.setEnabled(false);
		
		/* Deletes the data set and its dependencies from DB */
		removeButton.addListener(new RemoveButtonListener(this));

		/* Deletes selected subset from the datatree. */
		removeSetButton.addListener(new Button.ClickListener() {
		
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {	
				setViewCommand.removeSelectedSubset();

			}
		});

		openSetButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5166425513891423653L;
			@Override
			public void buttonClick(ClickEvent event) {
				setViewCommand.openOpenSetWindow();
			}
		});

		saveSetButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5166425513891423653L;
			@Override
			public void buttonClick(ClickEvent event) {
				setViewCommand.saveSelectedSet();
			}
		});

		addComponent(createTopNavigationPanel());

		Button logo = new NativeButton();
		logo.setDescription("geWorkbench Home");
		logo.setStyleName(BaseTheme.BUTTON_LINK);
		logo.addStyleName("logo");
		topBar.addComponent(logo);
		topBar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);

		mainSplit.setSizeFull();
		mainSplit.setStyleName("main-split");

		addComponent(mainSplit);
		setExpandRatio(mainSplit, 1);

		leftMainLayout.setImmediate(true);
		leftMainLayout.setSizeFull();
		leftMainLayout.addStyleName("mystyle");

		navigationTree.setImmediate(true);
		
		navigationTree.setItemCaptionPropertyId("Name");
		navigationTree.setItemIconPropertyId("Icon");
		navigationTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		navigationTree.setStyleName(Reindeer.TREE_CONNECTORS);
		 
	
		leftMainLayout.addComponent(navigationTree);
		mainSplit.setFirstComponent(leftMainLayout);
		mainSplit.setSplitPosition(275, SplitPanel.UNITS_PIXELS);
		mainSplit.setSecondComponent(pluginView);

		HorizontalLayout quicknav = new HorizontalLayout();
		topBar.addComponent(quicknav);
		topBar.setComponentAlignment(quicknav, Alignment.MIDDLE_RIGHT);
		quicknav.setStyleName("segment");
		quicknav.addComponent(createTreeSwitch());
		
		annotationPanel.hide();
		this.addComponent(annotationPanel.menuBar);
		this.addComponent(annotationPanel); // invisible until a dataset ID is set
		
		pluginView.showToolList();

		AnalysisListener analysisListener = new AnalysisListener(this, pusher);
		GeworkbenchRoot.getBlackboard().addListener(analysisListener);
		GeworkbenchRoot.getBlackboard().addListener(new UploadDataListener(this, pusher));
	} // end of the constructor.

	private HorizontalLayout createTopNavigationPanel() {
		HorizontalLayout p = new HorizontalLayout();
		p.setHeight("24px");
		p.setWidth("100%");
		p.setStyleName("menubar");
		p.setSpacing(false);
		p.setMargin(false);
		p.setImmediate(true);
		
		p.addComponent(toolBar);
		p.addComponent(annotButton);
		p.setComponentAlignment(annotButton, Alignment.MIDDLE_LEFT);
		p.addComponent(removeButton);
		p.setComponentAlignment(removeButton, Alignment.MIDDLE_LEFT);
		p.addComponent(removeSetButton);
		p.setComponentAlignment(removeSetButton, Alignment.MIDDLE_LEFT);
		p.addComponent(openSetButton);
		p.setComponentAlignment(openSetButton, Alignment.MIDDLE_LEFT);
		p.addComponent(saveSetButton);
		p.setComponentAlignment(saveSetButton, Alignment.MIDDLE_LEFT);
		p.setComponentAlignment(toolBar, Alignment.TOP_LEFT);
		p.addComponent(mainToolBar);
		p.setExpandRatio(mainToolBar, 1);
		p.setComponentAlignment(mainToolBar, Alignment.TOP_RIGHT);
		return p;
	}
	
	void noSelection() {
		annotButton.setEnabled(false);
		removeButton.setEnabled(false);
		setViewMeuItem.setEnabled(false);
		pluginView.showToolList();
	}
	
	void removeItem(Long itemId) {
		navigationTree.removeItem(itemId);
		pusher.push();
	}
	
	/**
	 * Creates the data tree for the project panel
	 * @return Tree
	 */
	private Tree createNavigationTree() {
		final Tree tree = new Tree();
		tree.setImmediate(true);
		tree.setStyleName("menu");
		tree.setContainerDataSource(getDataContainer());
		tree.setSelectable(true);
		tree.setMultiSelect(false);
		tree.addListener(new Tree.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				Item selectedItem = tree.getItem(event.getProperty().getValue());
				try {				
					if( event.getProperty().getValue()!=null ) {

						annotButton.setEnabled(true);
						removeButton.setEnabled(true);
						String className = (String) selectedItem.getItemProperty("Type").getValue();

						/* this is the only place that dataset ID may change */
						dataSetId = (Long) event.getProperty().getValue();    
						annotationPanel.setDatasetId(dataSetId);
						
						if (className.equals("org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet")){
							toolBar.setEnabled(true);
							setViewMeuItem.setEnabled(true);
						} else {
							toolBar.setEnabled(false);
						}

						/* FIXME this block of valid code (exactly the same behavior as when it was active) is commented out for now because:
						 * 1. it is very slow (due to deserialization);
						 * 2. it does't do its job of supporting annotation correctly;
						 * 3. even if it does (2.) right, it doesn't have to be here and invoked every time the user switches between data nodes.
						 */
						/*
						if (className.equals("org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet")){
							CSMicroarraySet maSet = (CSMicroarraySet) UserDirUtils.deserializeDataSet(dataSetId, CSMicroarraySet.class);
							Map<String, Object> parameters = new HashMap<String, Object>();	
							parameters.put("datasetid", dataSetId);	
							List<Annotation> annots = FacadeFactory.getFacade().list(
									"Select a from Annotation a, DataSetAnnotation da where a.id=da.annotationid and da.datasetid=:datasetid", parameters);
							if (!annots.isEmpty()){
								APSerializable aps = (APSerializable) ObjectConversion.toObject(UserDirUtils.getAnnotation(annots.get(0).getId()));
								AnnotationParser.setFromSerializable(aps);
							}else {
								AnnotationParser.setCurrentDataSet(maSet);
							}
							maSet.getMarkers().correctMaps();
						}
						*/
						
						ClassLoader classLoader = this.getClass().getClassLoader();
						Class<?> aClass = classLoader.loadClass(className);
						Class<? extends DataTypeMenuPage> uiComponentClass = GeworkbenchRoot.getPluginRegistry().getDataUI(aClass);
						Class<? extends Component> resultUiClass = GeworkbenchRoot.getPluginRegistry().getResultUI(aClass);
						if(uiComponentClass!=null) { // "not result" - menu page. For now, we only expect CSMcrioarraySet and CSProteinStructure
							DataTypeMenuPage dataUI = uiComponentClass.getDeclaredConstructor(Long.class).newInstance(dataSetId);
							dataUI.setVisualPluginView(pluginView);
							pluginView.setContent(dataUI, dataUI.getTitle(), dataUI.getDescription());
						} else if(resultUiClass!=null) { // "is result" - visualizer
							toolBar.setEnabled(false);
							for (int i = 0; i < toolBar.getItems().size(); i++) {
								toolBar.getItems().get(i).setEnabled(false);
							}

							pluginView.setContentUsingCache(resultUiClass, dataSetId);
						} 
					}
				} catch (Exception e) { // FIXME what kind of exception is expected here? why?
					e.printStackTrace();
					pluginView.showToolList();
					annotationPanel.hide();
				}
			}
		});
		
		tree.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
            
			private static final long serialVersionUID = -3576690826530527342L;

			@SuppressWarnings("unchecked")
			@Override
			public String generateDescription(Component source, Object itemId,
					Object propertyId) {
				if (!(itemId instanceof Long)) {
					System.out.println("does this happen at all? I don't think this should ever happen.");
					return null;
				}

				Item item = tree.getItem(itemId);
				String className = (String) item.getItemProperty("Type").getValue();
				Class<?> clazz;
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}

				// show tooltip only for DSDataSet for now
				if (DSDataSet.class.isAssignableFrom(clazz)) {
					Object object = UserDirUtils.getData((Long) itemId);
					try {
						if(object instanceof DSDataSet) {
							DSDataSet<? extends DSBioObject> df = (DSDataSet<? extends DSBioObject>) object;
							return df.getDescription();
						} else {
							log.warn("the deserialized data object is "+object);
							return null;
						}
					} catch (NullPointerException e) { // CSProteinStrcuture has null pointer exception not handled
						e.printStackTrace();
						return null;
					}
				} else {
					return null;
				}
			}
		});
		
		return tree;
	}
	
	/**
	 * Creates the tree switch for the mainsplit 
	 * @return
	 */
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
					mainSplit.setSplitPosition(250, SplitPanel.UNITS_PIXELS);
					mainSplit.setLocked(false);
					navigationTree.setVisible(true);
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
	private HierarchicalContainer getDataContainer() {

		HierarchicalContainer dataSets 		= 	new HierarchicalContainer();
		dataSets.addContainerProperty("Name", String.class, null);
		dataSets.addContainerProperty("Type", String.class, null);
		dataSets.addContainerProperty("Icon", Resource.class, null);
		
		Map<String, Object> param 		= 	new HashMap<String, Object>();
		param.put("owner", user.getId());
		param.put("workspace", WorkspaceUtils.getActiveWorkSpace());

		List<?> data =  FacadeFactory.getFacade().list("Select p from DataSet as p where p.owner=:owner and p.workspace =:workspace", param);

		for(int i=0; i<data.size(); i++) {

			String id 		=	((DataSet) data.get(i)).getName();
			Long dataId		=	((DataSet) data.get(i)).getId();	

			Item subItem = dataSets.addItem(dataId);
			subItem.getItemProperty("Name").setValue(id);
			String className = ((DataSet) data.get(i)).getType();
			subItem.getItemProperty("Type").setValue(className);
			try {
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getIcon(Class.forName(className));
				subItem.getItemProperty("Icon").setValue(icon);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				//subItem.getItemProperty("Icon").setValue(icon);
			}

			Map<String, Object> params 	= 	new HashMap<String, Object>();
			params.put("owner", user.getId());
			params.put("parent", dataId);
			List<?> results = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.owner=:owner and p.parent=:parent ORDER by p.date", params);

			if(results.size() == 0) {
				dataSets.setChildrenAllowed(dataId, false);
			}

			for(int j=0; j<results.size(); j++) {
				String subId 		=	((ResultSet) results.get(j)).getName();
				Long subSetId		=	((ResultSet) results.get(j)).getId();	
				String type			=	((ResultSet) results.get(j)).getType();

				Item res = dataSets.addItem(subSetId);
				res.getItemProperty("Name").setValue(subId);
				res.getItemProperty("Type").setValue(type);
				if(subId.contains("Pending")) {
					res.getItemProperty("Icon").setValue(pendingIcon);
				} else {
					try {
						ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getResultIcon(Class.forName(type));
						res.getItemProperty("Icon").setValue(icon);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				dataSets.setChildrenAllowed(subSetId, false);
				dataSets.setParent(subSetId, dataId);
			}
		}
		return dataSets;
	}

	// this may need to be public if we don't use event listener to trigger it.
	private void addResultSetNode(ResultSet res) {
		navigationTree.setChildrenAllowed(res.getParent(), true);
		navigationTree.addItem(res.getId());
		navigationTree.getContainerProperty(res.getId(), "Name").setValue(res.getName());
		navigationTree.getContainerProperty(res.getId(), "Type").setValue(res.getType());
		if(res.getName().contains("Pending")) {
			navigationTree.getContainerProperty(res.getId(), "Icon").setValue(pendingIcon);
		} else {
			try {
				String type = res.getType();
				Class<?> visualizerClass = Class.forName(type);
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getResultIcon(visualizerClass);
				navigationTree.getContainerProperty(res.getId(), "Icon").setValue(icon);
				Class<? extends Component> resultUiClass = GeworkbenchRoot.getPluginRegistry().getResultUI(visualizerClass);
				pluginView.setContentUpdatingCache(resultUiClass, res.getId());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		navigationTree.setChildrenAllowed(res.getId(), false);
		navigationTree.setParent(res.getId(), res.getParent());
		if (res.getType().equals("MarkusResults")) navigationTree.select(res.getId()); // FIXME if this is necessary, probably it is necessary for other result type too
	}
	
	// this may need to be public if we don't use event listener to trigger it.
	private void addDataSet(DataSet dS) {
		String className = dS.getType();
		navigationTree.addItem(dS.getId());
		navigationTree.setChildrenAllowed(dS.getId(), false);
		boolean pending = dS.getName().contains("Pending");
		if(pending) {
			navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(pendingIcon);
		} else {
			try {
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getIcon(Class.forName(className));
				navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(icon);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		navigationTree.getContainerProperty(dS.getId(), "Name").setValue(dS.getName());
		navigationTree.getContainerProperty(dS.getId(), "Type").setValue(className);
		if(!pending) {
			navigationTree.select(dS.getId());
		}
	}
	
	/**
	 * Adds the node to the dataTree  
	 */
	public class NodeAddListener implements NodeAddEventListener {
		@Override
		public void addNode(NodeAddEvent event) {	
			if(event.getData() instanceof ResultSet ) {
				ResultSet  res = (ResultSet) event.getData();
				addResultSetNode(res);
			} else if(event.getData() instanceof DataSet) {
				DataSet dS = (DataSet) event.getData();
				addDataSet(dS);
			}
		}
	}

	void switchToSetView() {
		removeButton.setVisible(false);
		annotButton.setVisible(false);
		navigationTree.setVisible(false);
		
		removeSetButton.setVisible(true);
		openSetButton.setVisible(true);
		saveSetButton.setVisible(true);

		workspaceViewMenuItem.setEnabled(true);
		
		// TODO this is a naughty way to get the previous set view away. definitely should be changed.
		leftMainLayout.removeAllComponents();
		leftMainLayout.addComponent(navigationTree);
	}
	
	private void switchToWorkspaceView() {
		removeButton.setVisible(true);
		annotButton.setVisible(true);
		removeSetButton.setVisible(false);
		openSetButton.setVisible(false);
		saveSetButton.setVisible(false);
		navigationTree.setVisible(true);
		setViewCommand.hideSetView();
		workspaceViewMenuItem.setEnabled(false);
		setViewMeuItem.setEnabled(true);
		pluginView.setEnabled(true);
		mainToolBar.setEnabled(true);
	}

	// TODO not a good idea. temporary solution for set view
	CssLayout getLeftMainLayout() {
		return leftMainLayout;
	}

	Long getCurrentDatasetId() {
		return dataSetId;
	}
}
