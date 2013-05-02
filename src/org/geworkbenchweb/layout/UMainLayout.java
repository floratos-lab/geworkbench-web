package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.Comment;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.CurrentContext;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.DataSetAnnotation;
import org.geworkbenchweb.pojos.ExperimentInfo;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.alump.fancylayouts.FancyCssLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * UMainLayout sets up the basic layout and style of the application.
 * @author Nikhil Reddy
 */
@SuppressWarnings("deprecation")
public class UMainLayout extends VerticalLayout {
	
	private static Log log = LogFactory.getLog(UMainLayout.class);

	private static final long serialVersionUID = 6214334663802788473L;

	private final SplitPanel mainSplit;

	private ComboBox search;

	private final Tree navigationTree;

	final private VisualPluginView pluginView = new VisualPluginView();

	private HorizontalLayout dataNavigation;

	private Long dataSetId;

	final private User user = SessionHandler.get();
			
	final private CssLayout leftMainLayout;

	private ICEPush pusher;
	
	private MenuBar annotationBar;
	
	final MenuBar toolBar = new MenuBar();
 
	private FancyCssLayout annotationLayout;
	
	private Button annotButton; 		
	
	private Button removeButton;	
	
	private Button removeSetButton;
	
	private Button openSetButton, saveSetButton;
			
	final private GenspaceLogger genspaceLogger = new GenspaceLogger();;

	static private ThemeResource pendingIcon 	=	new ThemeResource("../custom/icons/pending.gif");
	static private ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	static private ThemeResource CancelIcon 	= 	new ThemeResource("../runo/icons/16/cancel.png");
	static private ThemeResource openSetIcon	=	new ThemeResource("../custom/icons/open_set.png");
	static private ThemeResource saveSetIcon	=	new ThemeResource("../custom/icons/save_set.png");

	final UMainToolBar mainToolBar 	= 	new UMainToolBar(pluginView, genspaceLogger);
	
	public UMainLayout() {

		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);

		/*Enable genspace logger in geWorkbench*/
		GeworkbenchRoot.getBlackboard().addListener(genspaceLogger);
		
		setSizeFull();
		setImmediate(true);
		
		pusher = GeworkbenchRoot.getPusher();
		addComponent(pusher);

		HorizontalLayout topBar 		= 	new HorizontalLayout();
		
		addComponent(topBar);
		topBar.setHeight("44px");
		topBar.setWidth("100%");
		topBar.setStyleName("topbar");
		topBar.setSpacing(true);

		dataNavigation = new HorizontalLayout();
		dataNavigation.setHeight("24px");
		dataNavigation.setWidth("100%");
		dataNavigation.setStyleName("menubar");
		dataNavigation.setSpacing(false);
		dataNavigation.setMargin(false);
		dataNavigation.setImmediate(true);

		annotButton 	= 	new Button();
		removeButton	=	new Button();
		removeSetButton =	new Button();
		openSetButton	=	new Button();
		saveSetButton	=	new Button();
		
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
				annotationLayout.removeAllComponents();
				annotationLayout.setMargin(true);
				annotationLayout.setHeight("250px");
				annotationLayout.setWidth("100%");
				for(int i=0; i<annotationBar.getItems().size(); i++) {
					if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
						annotationBar.getItems().get(i).setVisible(true);	
					} else {
						annotationBar.getItems().get(i).setVisible(false);	
					}
				}
				annotationLayout.addComponent(buildAnnotationTabSheet());	
				addComponent(annotationLayout);
			}
		});
		
		toolBar.setEnabled(false);
		toolBar.setImmediate(true);
		toolBar.setStyleName("transparent");
		final SetViewCommand setViewCommand = new SetViewCommand(this);
		final MenuItem set = toolBar.addItem("Set View", setViewCommand);
		set.setEnabled(false);
		final MenuItem project = toolBar.addItem("Project View", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				removeButton.setVisible(true);
				annotButton.setVisible(true);
				removeSetButton.setVisible(false);
				openSetButton.setVisible(false);
				saveSetButton.setVisible(false);
				navigationTree.setVisible(true);
				setViewCommand.hideSetView();
				selectedItem.setEnabled(false);
				set.setEnabled(true);
				pluginView.setEnabled(true);
				mainToolBar.setEnabled(true);
			}
		});

		project.setEnabled(false);
		
		/* Deletes the data set and its dependencies from DB */
		removeButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				
				MessageBox mbMain = new MessageBox(getWindow(), 
						"Information", 
						MessageBox.Icon.INFO, 
						"This action will delete the selected data.", 
						new MessageBox.ButtonConfig(ButtonType.CANCEL, "Cancel"),
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				
				mbMain.show(new MessageBox.EventListener() {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public void buttonClicked(ButtonType buttonType) {    	
						if(buttonType == ButtonType.OK) {
							Long dataId = dataSetId;

							DataSet data =  FacadeFactory.getFacade().find(DataSet.class, dataId);
							if(data != null) {
								
								Map<String, Object> params 		= 	new HashMap<String, Object>();
								params.put("parent", dataId);

								List<?> SubSets =  FacadeFactory.getFacade().list("Select p from SubSet as p where p.parent =:parent", params);
								if(SubSets.size() != 0){
									for(int i=0;i<SubSets.size();i++) {
										FacadeFactory.getFacade().delete((SubSet) SubSets.get(i));
									}
								}
								
								Map<String, Object> param 		= 	new HashMap<String, Object>();
								param.put("parent", dataId);

								List<?> resultSets =  FacadeFactory.getFacade().list("Select p from ResultSet as p where p.parent =:parent", param);
								if(resultSets.size() != 0){
									for(int i=0;i<resultSets.size();i++) {
										Map<String, Object> cParam 		= 	new HashMap<String, Object>();
										cParam.put("parent", ((ResultSet) resultSets.get(i)).getId());

										List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", cParam);
										if(comments.size() != 0){
											for(int j=0;j<comments.size();j++) {
												FacadeFactory.getFacade().delete((Comment) comments.get(j));
											}
										}
										boolean success =  UserDirUtils.deleteResultSet(((ResultSet) resultSets.get(i)).getId());
										if(!success) {
											MessageBox mb = new MessageBox(getWindow(), 
													"Error", 
													MessageBox.Icon.ERROR, 
													"Unable to delete the selected data. Please contact administrator.", 
													new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
											mb.show();
										}
										FacadeFactory.getFacade().delete((ResultSet) resultSets.get(i));
										navigationTree.removeItem(((ResultSet) resultSets.get(i)).getId());
									}
								}
								Map<String, Object> cParam 		= 	new HashMap<String, Object>();
								cParam.put("parent", dataId);

								List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", cParam);
								if(comments.size() != 0){
									for(int j=0;j<comments.size();j++) {
										FacadeFactory.getFacade().delete((Comment) comments.get(j));
									}
								}

								cParam.clear();
								cParam.put("datasetid", dataId);
								List<DataSetAnnotation> dsannot = FacadeFactory.getFacade().list("Select p from DataSetAnnotation as p where p.datasetid=:datasetid", cParam);
								if (dsannot.size() > 0){
									Long annotId = dsannot.get(0).getAnnotationId();
									FacadeFactory.getFacade().delete(dsannot.get(0));

									Annotation annot = FacadeFactory.getFacade().find(Annotation.class, annotId);
									if (annot!=null && annot.getOwner()!=null){
										cParam.clear();
										cParam.put("annotationid", annotId);
										List<Annotation> annots = FacadeFactory.getFacade().list("select p from DataSetAnnotation as p where p.annotationid=:annotationid", cParam);
										if (annots.size()==0) FacadeFactory.getFacade().delete(annot);
									}
								}

								List<Context> contexts = SubSetOperations.getAllContexts(dataId);
								for (Context c : contexts) {
									cParam.clear();
									cParam.put("contextid", c.getId());	
									List<SubSetContext> subcontexts = FacadeFactory.getFacade().list("Select a from SubSetContext a where a.contextid=:contextid", cParam);
									FacadeFactory.getFacade().deleteAll(subcontexts);
									FacadeFactory.getFacade().delete(c);
								}

								cParam.clear();
								cParam.put("datasetid", dataId);
								List<CurrentContext> cc =  FacadeFactory.getFacade().list("Select p from CurrentContext as p where p.datasetid=:datasetid", cParam);
								if (cc.size()>0) FacadeFactory.getFacade().delete(cc.get(0));

								boolean success = UserDirUtils.deleteDataSet(data.getId());
								if(!success) {
									MessageBox mb = new MessageBox(getWindow(), 
											"Error", 
											MessageBox.Icon.ERROR, 
											"Unable to delete the selected data. Please contact administrator.", 
											new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
									mb.show();
								}
								FacadeFactory.getFacade().delete(data);
							}else {
								Map<String, Object> cParam 		= 	new HashMap<String, Object>();
								cParam.put("parent", dataId);

								List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", cParam);
								if(comments.size() != 0){
									for(int j=0;j<comments.size();j++) {
										FacadeFactory.getFacade().delete((Comment) comments.get(j));
									}
								}
								ResultSet result =  FacadeFactory.getFacade().find(ResultSet.class, dataId);
								boolean success = UserDirUtils.deleteResultSet(result.getId());
								if(!success) {
									MessageBox mb = new MessageBox(getWindow(), 
											"Error", 
											MessageBox.Icon.ERROR, 
											"Unable to delete the selected data. Please contact administrator.", 
											new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
									mb.show();
								}
								FacadeFactory.getFacade().delete(result);
							} 
							navigationTree.removeItem(dataId);
						}
					}
				});	
				
				annotButton.setEnabled(false);
				removeButton.setEnabled(false);
				set.setEnabled(false);
				pluginView.showToolList();
			}
		});

		/* Deletes seletected subset from the datatree. */
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

		dataNavigation.addComponent(toolBar);
		dataNavigation.addComponent(annotButton);
		dataNavigation.setComponentAlignment(annotButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(removeButton);
		dataNavigation.setComponentAlignment(removeButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(removeSetButton);
		dataNavigation.setComponentAlignment(removeSetButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(openSetButton);
		dataNavigation.setComponentAlignment(openSetButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(saveSetButton);
		dataNavigation.setComponentAlignment(saveSetButton, Alignment.MIDDLE_LEFT);
		dataNavigation.setComponentAlignment(toolBar, Alignment.TOP_LEFT);
		dataNavigation.addComponent(mainToolBar);
		dataNavigation.setExpandRatio(mainToolBar, 1);
		dataNavigation.setComponentAlignment(mainToolBar, Alignment.TOP_RIGHT);

		addComponent(dataNavigation);

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
		leftMainLayout.addStyleName("mystyle");

		navigationTree = createMenuTree();
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

		Component searchComponent = createSearch();
		quicknav.addComponent(searchComponent);

		Component treeSwitch = createTreeSwitch();
		quicknav.addComponent(treeSwitch);
		
		annotationLayout = new FancyCssLayout();
		annotationLayout.setSlideEnabled(true);
		
		annotationBar = new MenuBar();
		annotationBar.setWidth("100%");
		MenuBar.MenuItem up = annotationBar.addItem("", new ThemeResource(
				"../runo/icons/16/arrow-up.png"), new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				annotationLayout.removeAllComponents();
				annotationLayout.setMargin(true);
				annotationLayout.setHeight("250px");
				annotationLayout.setWidth("100%");
				annotationLayout.setImmediate(true);
				annotationLayout.addComponent(buildAnnotationTabSheet());	
				addComponent(annotationLayout);	

				for(int i=0; i<annotationBar.getItems().size(); i++) {
					if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
						annotationBar.getItems().get(i).setVisible(true);	
					} else {
						annotationBar.getItems().get(i).setVisible(false);	
					}
				}
			}
		});
		up.setDescription("View Annotation");
		MenuBar.MenuItem down = annotationBar.addItem("", new ThemeResource(
				"../runo/icons/16/arrow-down.png"), new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				removeComponent(annotationLayout);
				for(int i=0; i<annotationBar.getItems().size(); i++) {
					if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
						annotationBar.getItems().get(i).setVisible(false);	
					} else {
						annotationBar.getItems().get(i).setVisible(true);	
					}
				}
			}
		});
		down.setDescription("Close Annotation");
		down.setVisible(false);
		annotationBar.setVisible(false);
		addComponent(annotationBar);
		
		pluginView.showToolList();

		AnalysisListener analysisListener = new AnalysisListener(this, pusher);
		GeworkbenchRoot.getBlackboard().addListener(analysisListener);
	} // end of the constructor. TODO too long

	void removeItem(Long itemId) {
		navigationTree.removeItem(itemId);
		pusher.push();
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
		tree.setSelectable(true);
		tree.setMultiSelect(false);
		tree.addListener(new Tree.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {

				Item selectedItem = tree.getItem(event.getProperty().getValue());
				try {				
					if( event.getProperty().getValue()!=null ) {

						annotButton.setEnabled(true);
						removeButton.setEnabled(true);
						String className = (String) selectedItem.getItemProperty("Type").getValue();
						annotationBar.setVisible(true);
						for(int i=0; i<annotationBar.getItems().size(); i++) {
							if(annotationBar.getItems().get(i).getDescription().equalsIgnoreCase("Close Annotation")) {
								annotationBar.getItems().get(i).setVisible(false);	
							} else {
								annotationBar.getItems().get(i).setVisible(true);	
							}
						}

						if (className.contains("Results") && selectedItem.getItemProperty("Name").toString().contains("Pending")){
							pluginView.removeAllComponents();
							return;
						}
						
						dataSetId = (Long) event.getProperty().getValue();    
						
						toolBar.setEnabled(false);

						// TODO special things to do for microarray set should be considered as part of the overall design
						// not as a special case patched as aftermath fix
						if (className.equals("org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet")){
							// (1)
							DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(dataSetId));
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
							((CSMicroarraySet)maSet).getMarkers().correctMaps();

							// (2)
							toolBar.setEnabled(true);
							for(int i=0; i<toolBar.getItems().size(); i++) {
								if(toolBar.getItems().get(i).getText().equalsIgnoreCase("SET VIEW")) {
									toolBar.getItems().get(i).setEnabled(true);	
								}
							}
						}
						
						ClassLoader classLoader = this.getClass().getClassLoader();
						Class<?> aClass = classLoader.loadClass(className);
						Class<? extends DataTypeMenuPage> uiComponentClass = GeworkbenchRoot.getPluginRegistry().getDataUI(aClass);
						Class<? extends Component> resultUiClass = GeworkbenchRoot.getPluginRegistry().getResultUI(aClass);
						removeComponent(annotationLayout);
						if(uiComponentClass!=null) { // "not result" - menu page. For now, we only expect CSMcrioarraySet and CSProteinStructure
							DataTypeMenuPage dataUI = uiComponentClass.getDeclaredConstructor(Long.class).newInstance(dataSetId);
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
					removeComponent(annotationLayout);
					annotationBar.setVisible(false);
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
					if (object==null || !(object instanceof byte[])) { // FIXME temporary we should not use byte[][ in the first place
						// this may happen when the file is corrupted or for
						// other reason does not exist any more
						log.warn("the deserialized data object is null");
						return null;
					}
					try {
						byte[] byteArray = (byte[])object;
						DSDataSet<? extends DSBioObject> df = (DSDataSet<? extends DSBioObject>) ObjectConversion.toObject(byteArray);
						return df.getDescription();
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

	/**
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
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getResultIcon(Class.forName(type));
				navigationTree.getContainerProperty(res.getId(), "Icon").setValue(icon);
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
		try {
			ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getIcon(Class.forName(className));
			navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(icon);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		navigationTree.getContainerProperty(dS.getId(), "Name").setValue(dS.getName());
		navigationTree.getContainerProperty(dS.getId(), "Type").setValue(className);
		navigationTree.select(dS.getId());
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

	private TabSheet buildAnnotationTabSheet() {
		HorizontalSplitPanel dLayout 	=  	new HorizontalSplitPanel();
		dLayout.setSplitPosition(60);
		dLayout.setSizeFull();
		dLayout.setImmediate(true);
		dLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		dLayout.setLocked(true);
		
		final VerticalLayout commentsLayout = new VerticalLayout();
		commentsLayout.setImmediate(true);
		commentsLayout.setMargin(true);
		commentsLayout.setSpacing(true);
		commentsLayout.setSizeUndefined();
		
		Label cHeading 		=	new Label("User Comments:");
		cHeading.setStyleName(Reindeer.LABEL_H2);
		cHeading.setContentMode(Label.CONTENT_PREFORMATTED);
		commentsLayout.addComponent(cHeading);
		
		Map<String, Object> params 		= 	new HashMap<String, Object>();
		params.put("parent", dataSetId);

		List<?> comments =  FacadeFactory.getFacade().list("Select p from Comment as p where p.parent =:parent", params);
		if(comments.size() != 0){
			for(int i=0;i<comments.size();i++) {
				java.sql.Date date = ((Comment) comments.get(i)).getDate();
				Label comment = new Label(date.toString()+
						" - " +
						((Comment) comments.get(i)).getComment());
				commentsLayout.addComponent(comment);
			}
		}

		dLayout.setFirstComponent(commentsLayout);
		
		VerticalLayout commentArea = new VerticalLayout();
		commentArea.setImmediate(true);
		commentArea.setMargin(true);
		commentArea.setSpacing(true);
		
		Label commentHead 		=	new Label("Enter new comment here:");
		commentHead.setStyleName(Reindeer.LABEL_H2);
		commentHead.setContentMode(Label.CONTENT_PREFORMATTED);
		final TextArea dataArea = 	new TextArea();
		dataArea.setRows(6);
		dataArea.setWidth("100%");
		Button submitComment	=	new Button("Add Comment", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(!dataArea.getValue().toString().isEmpty()) {
					java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
					
					Comment c = new Comment();
					c.setParent(dataSetId);
					c.setComment(dataArea.getValue().toString());
					c.setDate(date);
					FacadeFactory.getFacade().store(c);
					
					Label comment = new Label(date.toString()+
							" - " +
							dataArea.getValue().toString());
					commentsLayout.addComponent(comment);
					dataArea.setValue("");
				}
			}
		});
		submitComment.setClickShortcut(KeyCode.ENTER);
		commentArea.addComponent(commentHead);
		commentArea.setComponentAlignment(commentHead, Alignment.MIDDLE_LEFT);
		commentArea.addComponent(dataArea);
		commentArea.setComponentAlignment(dataArea, Alignment.MIDDLE_LEFT);
		commentArea.addComponent(submitComment);
		commentArea.setComponentAlignment(submitComment, Alignment.MIDDLE_LEFT);
		dLayout.setSecondComponent(commentArea);
		
		HorizontalSplitPanel infoSplit 	=  	new HorizontalSplitPanel();
		infoSplit.setSplitPosition(50);
		infoSplit.setSizeFull();
		infoSplit.setImmediate(true);
		infoSplit.setStyleName(Reindeer.SPLITPANEL_SMALL);
		infoSplit.setLocked(true);
				
		VerticalLayout dataHistory 	= 	new VerticalLayout();
		VerticalLayout expInfo		=	new VerticalLayout();
		
		dataHistory.setSizeUndefined();
		dataHistory.setMargin(true);
		dataHistory.setSpacing(true);
		dataHistory.setImmediate(true);

		Label historyHead 		=	new Label("Data History:");
		historyHead.setStyleName(Reindeer.LABEL_H2);
		historyHead.setContentMode(Label.CONTENT_PREFORMATTED);
		dataHistory.addComponent(historyHead);
		
		Map<String, Object> eParams 		= 	new HashMap<String, Object>();
		eParams.put("parent", dataSetId);

		List<?> histories =  FacadeFactory.getFacade().list("Select p from DataHistory as p where p.parent =:parent", eParams);
		for(int i=0; i<histories.size(); i++) {
			DataHistory dH = (DataHistory) histories.get(i);
			Label d = new Label((String) ObjectConversion.toObject(dH.getData()));
			d.setContentMode(Label.CONTENT_PREFORMATTED);
			dataHistory.addComponent(d);
		}
		
		expInfo.setSizeUndefined();
		expInfo.setMargin(true);
		expInfo.setSpacing(true);
		
		Label infoHead 		=	new Label("Experiment Information:");
		infoHead.setStyleName(Reindeer.LABEL_H2);
		infoHead.setContentMode(Label.CONTENT_PREFORMATTED);
		expInfo.addComponent(infoHead);
		
		Map<String, Object> iParams 		= 	new HashMap<String, Object>();
		iParams.put("parent", dataSetId);

		List<?> info =  FacadeFactory.getFacade().list("Select p from ExperimentInfo as p where p.parent =:parent", iParams);
		for(int i=0; i<info.size(); i++) {
			ExperimentInfo eI = (ExperimentInfo) info.get(i);
			Label d = new Label((String) ObjectConversion.toObject(eI.getInfo()));
			d.setContentMode(Label.CONTENT_PREFORMATTED);
			expInfo.addComponent(d);
		}
	
		infoSplit.setFirstComponent(dataHistory);
		infoSplit.setSecondComponent(expInfo);
		TabSheet data = new TabSheet();
		data.setStyleName(Reindeer.TABSHEET_SMALL);
		data.setSizeFull();
		data.setImmediate(true);
	
		data.addTab(infoSplit, "Data Information");
		data.addTab(dLayout, "User Comments");	
		return data;
	}
	
	void switchToSetView() {
		removeButton.setVisible(false);
		annotButton.setVisible(false);
		navigationTree.setVisible(false);
		
		removeSetButton.setVisible(true);
		openSetButton.setVisible(true);
		saveSetButton.setVisible(true);

		pluginView.setEnabled(false);
		mainToolBar.setEnabled(false);

		for(int i=0; i<toolBar.getItems().size(); i++) {
			if(toolBar.getItems().get(i).getText().equalsIgnoreCase("PROJECT VIEW")) {
				toolBar.getItems().get(i).setEnabled(true);	
			}
		}
		
		// TODO this is a naughty way to get the previous set view away. definitely should be changed.
		leftMainLayout.removeAllComponents();
		leftMainLayout.addComponent(navigationTree);
	}

	// TODO not a good idea. temporary solution for set view
	CssLayout getLeftMainLayout() {
		return leftMainLayout;
	}

	Long getCurrentDatasetId() {
		return dataSetId;
	}
}

