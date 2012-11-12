package org.geworkbenchweb.layout;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.Affy3ExpressionAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyAnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.events.PluginEvent.PluginEventListener;
import org.geworkbenchweb.plugins.anova.AnovaAnalysis;
import org.geworkbenchweb.plugins.anova.AnovaUI;
import org.geworkbenchweb.plugins.aracne.AracneAnalysisWeb;
import org.geworkbenchweb.plugins.cnkb.CNKBInteractions;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringWrapper;
import org.geworkbenchweb.plugins.marina.MarinaAnalysis;
import org.geworkbenchweb.plugins.microarray.Microarray;
import org.geworkbenchweb.plugins.tools.Tools;
import org.geworkbenchweb.pojos.Comment;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.alump.fancylayouts.FancyCssLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
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

	private static final long serialVersionUID = 6214334663802788473L;

	private final SplitPanel mainSplit;

	private ComboBox search;

	private final Tree navigationTree;

	VisualPluginView pluginView = new VisualPluginView();

	private HorizontalLayout dataNavigation;

	private Long dataSetId;

	User user = SessionHandler.get();

	private Tree markerTree;

	private Tree arrayTree;

	private Tree markerSetTree;

	private Tree arraySetTree;

	private CssLayout leftMainLayout;

	private ICEPush pusher;
	
	final MenuBar toolBar = new MenuBar();

	private static final Action ACTION_ADD 		= 	new Action("Add Set");

	protected static final Action[] ACTIONS 	= 	new Action[] { ACTION_ADD};
	
	private FancyCssLayout annotationLayout;
	
	private Button annotButton; 		
	
	private Button removeButton;		
	
	ThemeResource projectIcon 		= 	new ThemeResource("../custom/icons/project16x16.gif");
	ThemeResource microarrayIcon 	=	new ThemeResource("../custom/icons/chip16x16.gif");
	ThemeResource proteinIcon 		=	new ThemeResource("../custom/icons/dna16x16.gif");
	ThemeResource hcIcon	 		=	new ThemeResource("../custom/icons/dendrogram16x16.gif");
	ThemeResource pendingIcon	 	=	new ThemeResource("../custom/icons/pending.gif");
	ThemeResource networkIcon	 	=	new ThemeResource("../custom/icons/network16x16.gif");
	ThemeResource markusIcon		=	new ThemeResource("../custom/icons/icon_world.gif");
	ThemeResource anovaIcon			=	new ThemeResource("../custom/icons/significance16x16.gif");
	ThemeResource marinaIcon		=	new ThemeResource("../custom/icons/generic16x16.gif");
	ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	ThemeResource CancelIcon 		= 	new ThemeResource("../runo/icons/16/cancel.png");

	public UMainLayout() {

		/* Add listeners here */
		NodeAddListener addNodeListener = new NodeAddListener();
		GeworkbenchRoot.getBlackboard().addListener(addNodeListener);

		PluginListener pluginListener = new PluginListener();
		GeworkbenchRoot.getBlackboard().addListener(pluginListener);

		AnalysisListener analysisListener = new AnalysisListener();
		GeworkbenchRoot.getBlackboard().addListener(analysisListener);

		setSizeFull();
		setImmediate(true);
		
		pusher = GeworkbenchRoot.getPusher();
		addComponent(pusher);

		HorizontalLayout topBar = new HorizontalLayout();
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
		
		annotButton.setStyleName(BaseTheme.BUTTON_LINK);
		annotButton.setIcon(annotIcon);
		removeButton.setStyleName(BaseTheme.BUTTON_LINK);
		removeButton.setIcon(CancelIcon);
		
		annotButton.setEnabled(false);
		removeButton.setEnabled(false);
		
		annotButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				annotationLayout.removeAllComponents();
				annotationLayout.setMargin(true);

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
						comment.setStyleName(Reindeer.LABEL_SMALL);
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
							comment.setStyleName(Reindeer.LABEL_SMALL);
							commentsLayout.addComponent(comment);
							dataArea.requestRepaint();
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
				TabSheet data = new TabSheet();
				data.setStyleName(Reindeer.TABSHEET_SMALL);
				data.setSizeFull();
				data.addTab(dLayout, "User Comments");
				annotationLayout.setHeight("250px");
				annotationLayout.setWidth("100%");
				annotationLayout.addComponent(data);	
				addComponent(annotationLayout);
			}
		});
		
		toolBar.setEnabled(false);
		toolBar.setImmediate(true);
		final MenuItem set = toolBar.addItem("Set View", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				selectedItem.setEnabled(false);
				navigationTree.setVisible(false);

				markerTree	 	= 	new Tree();
				arrayTree 		=	new Tree();
				markerSetTree 	= 	new Tree();
				arraySetTree 	= 	new Tree();

				markerTree.addActionHandler(markerTreeActionHandler);
				markerTree.setImmediate(true);
				markerTree.setSelectable(true);
				markerTree.setMultiSelect(true);

				HierarchicalContainer markerData = new HierarchicalContainer();
				List<?> sets = SubSetOperations.getMarkerSets(dataSetId);
				markerData.addContainerProperty("setName", String.class, null);
				Item mainItem = markerData.addItem("MarkerSets");
				mainItem.getItemProperty("setName").setValue("Marker Sets");
				for (int i=0; i<sets.size(); i++) {
					markerData.addItem(((SubSet) sets.get(i)).getId());
					markerData.getContainerProperty(((SubSet) sets.get(i)).getId(), "setName").setValue(((SubSet) sets.get(i)).getName());
					markerData.setParent(((SubSet) sets.get(i)).getId(), "MarkerSets");
					markerData.setChildrenAllowed(((SubSet) sets.get(i)).getId(), false);
				}
				markerSetTree.setImmediate(true);
				markerSetTree.setSelectable(false);
				markerSetTree.setMultiSelect(false);
				markerSetTree.setContainerDataSource(markerData);

				HierarchicalContainer arrayData = new HierarchicalContainer();
				List<?> aSets = SubSetOperations.getArraySets(dataSetId);
				arrayData.addContainerProperty("setName", String.class, null);
				Item mainItem1 = arrayData.addItem("arraySets");
				mainItem1.getItemProperty("setName").setValue("Phenotype Sets");
				for (int i=0; i<aSets.size(); i++) {
					arrayData.addItem(((SubSet) aSets.get(i)).getId());
					arrayData.getContainerProperty(((SubSet) aSets.get(i)).getId(), "setName").setValue(((SubSet) aSets.get(i)).getName());
					arrayData.setParent(((SubSet) aSets.get(i)).getId(), "arraySets");
					arrayData.setChildrenAllowed(((SubSet) aSets.get(i)).getId(), false);
				}
				arraySetTree.setImmediate(true);
				arraySetTree.setMultiSelect(false);
				arraySetTree.setSelectable(false);
				arraySetTree.setContainerDataSource(arrayData);

				arrayTree.addActionHandler(arrayTreeActionHandler);
				arrayTree.setImmediate(true);
				arrayTree.setMultiSelect(true);
				arrayTree.setSelectable(true);

				List<DataSet> data = DataSetOperations.getDataSet(dataSetId);
				DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(data.get(0).getData());

				AffyAnnotationParser parser = new Affy3ExpressionAnnotationParser();
				File annotFile = new File((System.getProperty("user.home") + "/temp/HG_U95Av2.na32.annot.csv"));
				AnnotationParser.cleanUpAnnotatioAfterUnload(maSet);
				try {
					AnnotationParser.loadAnnotationFile(maSet, annotFile, parser);
				} catch (InputFileFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				markerTree.setContainerDataSource(markerTableView(maSet));
				arrayTree.setContainerDataSource(arrayTableView(maSet));

				markerTree.setItemCaptionPropertyId("Labels");
				markerTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				arrayTree.setItemCaptionPropertyId("Labels");
				arrayTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				markerSetTree.setItemCaptionPropertyId("setName");
				markerSetTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				arraySetTree.setItemCaptionPropertyId("setName");
				arraySetTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

				leftMainLayout.addComponent(markerTree);
				leftMainLayout.addComponent(markerSetTree);
				leftMainLayout.addComponent(arrayTree);
				leftMainLayout.addComponent(arraySetTree);

				for(int i=0; i<toolBar.getItems().size(); i++) {
					if(toolBar.getItems().get(i).getText().equalsIgnoreCase("PROJECT VIEW")) {
						toolBar.getItems().get(i).setEnabled(true);	
					}
				}
			}	
		});
		set.setEnabled(false);
		final MenuItem project = toolBar.addItem("Project View", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				navigationTree.setVisible(true);
				markerTree.setVisible(false);
				arrayTree.setVisible(false);
				markerSetTree.setVisible(false);
				arraySetTree.setVisible(false);
				selectedItem.setEnabled(false);
				set.setEnabled(true);
			}
		});

		project.setEnabled(false);
		
		removeButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
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
					FacadeFactory.getFacade().delete(result);
				} 
				navigationTree.removeItem(dataId);
				annotButton.setEnabled(false);
				removeButton.setEnabled(false);
				set.setEnabled(false);
				VisualPlugin tools = new Tools(null);
				setVisualPlugin(tools);
			}
		});

		UMainToolBar mainToolBar = new UMainToolBar();
		dataNavigation.addComponent(toolBar);
		dataNavigation.addComponent(annotButton);
		dataNavigation.setComponentAlignment(annotButton, Alignment.MIDDLE_LEFT);
		dataNavigation.addComponent(removeButton);
		dataNavigation.setComponentAlignment(removeButton, Alignment.MIDDLE_LEFT);
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
		VisualPlugin tools = new Tools(null);
		setVisualPlugin(tools);
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

			@SuppressWarnings("unchecked")
			public void valueChange(ValueChangeEvent event) {

				Item selectedItem = tree.getItem(event.getProperty().getValue());
				VisualPlugin f;
				try {				
					if(!event.getProperty().getValue().equals(null) ) {

						annotButton.setEnabled(true);
						removeButton.setEnabled(true);
						String className = (String) selectedItem.getItemProperty("Type").getValue();
						if(className.contains("Results")) {

							if (selectedItem.getItemProperty("Name").toString().contains("Pending")){
								pluginView.removeAllComponents();
								return;
							}

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
							removeComponent(annotationLayout);
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
							removeComponent(annotationLayout);
							setVisualPlugin(f);
						}
					}
				}catch (Exception e) {
					VisualPlugin tools = new Tools(null);
					setVisualPlugin(tools);
				}
			}
		});
		return tree;
	}

	/**
	 * Sets the VisualPlugin 
	 * @return
	 */
	public void setVisualPlugin(VisualPlugin f) {
		if(f instanceof Microarray) {
			toolBar.setEnabled(true);
			for(int i=0; i<toolBar.getItems().size(); i++) {
				if(toolBar.getItems().get(i).getText().equalsIgnoreCase("SET VIEW")) {
					toolBar.getItems().get(i).setEnabled(true);	
				}
			}
		} else {
			toolBar.setEnabled(false);
			for(int i=0; i<toolBar.getItems().size(); i++) {
				toolBar.getItems().get(i).setEnabled(false);	
			}
		} 
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
	public HierarchicalContainer getDataContainer() {

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
			if(((DataSet) data.get(i)).getType().equalsIgnoreCase("microarray")) {
				subItem.getItemProperty("Type").setValue("Microarray");
				subItem.getItemProperty("Icon").setValue(microarrayIcon);
			} else {
				subItem.getItemProperty("Type").setValue("ProteinStructure");
				subItem.getItemProperty("Icon").setValue(proteinIcon);
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
					if(type.equalsIgnoreCase("HierarchicalClusteringResults")) {
						res.getItemProperty("Icon").setValue(hcIcon);
					} else if(type.equalsIgnoreCase("CNKBResults")) {
						res.getItemProperty("Icon").setValue(networkIcon);
					} else if(type.equalsIgnoreCase("MarkusResults")) {
						res.getItemProperty("Icon").setValue(markusIcon);
					} else if(type.equalsIgnoreCase("AnovaResults")) {
						res.getItemProperty("Icon").setValue(anovaIcon);
					} else if (type.equalsIgnoreCase("AracneResults")) {
						res.getItemProperty("Icon").setValue(networkIcon);
					} else if(type.equalsIgnoreCase("MarinaResults")) {
						res.getItemProperty("Icon").setValue(marinaIcon);
					} 
				}
				dataSets.setChildrenAllowed(subSetId, false);
				dataSets.setParent(subSetId, dataId);
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
				navigationTree.setChildrenAllowed(res.getParent(), true);
				navigationTree.addItem(res.getId());
				navigationTree.getContainerProperty(res.getId(), "Name").setValue(res.getName());
				navigationTree.getContainerProperty(res.getId(), "Type").setValue(res.getType());
				if(res.getName().contains("Pending")) {
					navigationTree.getContainerProperty(res.getId(), "Icon").setValue(pendingIcon);
				} else {
					if(res.getType().equalsIgnoreCase("HierarchicalClusteringResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(hcIcon);
					} else if(res.getType().equalsIgnoreCase("CNKBResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(networkIcon);
					} else if (res.getType().equalsIgnoreCase("MarkusResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(markusIcon);
					} else if (res.getType().equalsIgnoreCase("AnovaResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(anovaIcon);
					} else if (res.getType().equalsIgnoreCase("AracneResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(networkIcon);
					} else if (res.getType().equalsIgnoreCase("MarinaResults")) {
						navigationTree.getContainerProperty(res.getId(), "Icon").setValue(marinaIcon);
					} 
				}
				navigationTree.setChildrenAllowed(res.getId(), false);
				navigationTree.setParent(res.getId(), res.getParent());
				if (res.getType().equals("MarkusResults")) navigationTree.select(res.getId());
			} else if(event.getData() instanceof DataSet) {
				DataSet dS = (DataSet) event.getData();
				String dataType;
				navigationTree.addItem(dS.getId());
				navigationTree.setChildrenAllowed(dS.getId(), false);
				if(dS.getType().equalsIgnoreCase("PDB File")) {
					dataType = "ProteinStructure";
					navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(proteinIcon);
				} else {
					dataType = "Microarray";
					navigationTree.getContainerProperty(dS.getId(), "Icon").setValue(microarrayIcon);
				}
				navigationTree.getContainerProperty(dS.getId(), "Name").setValue(dS.getName());
				navigationTree.getContainerProperty(dS.getId(), "Type").setValue(dataType);
				navigationTree.select(dS.getId());
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

	/**
	 * Used to submit the analysis in geWorkbench and updates the data tree with result nodes once the 
	 * analysis is complete in the background.
	 * @author Nikhil
	 */
	public class AnalysisListener implements AnalysisSubmissionEventListener {

		@Override
		public void SubmitAnalysis(final AnalysisSubmissionEvent event) {

			Thread analysisThread = new Thread() {
				@Override
				public void run() {
					final ResultSet resultSet = event.getResultSet();
					HashMap<Serializable, Serializable> params = event.getParameters();

					DSMicroarraySet dataSet = null;
					try {
						dataSet = (DSMicroarraySet) event.getDataSet();
					} catch (Exception e) {
						// FIXME catching all clause is evil; catching all and doing nothing is the evil of evils
						e.printStackTrace();
					}
					// FIXME this particular if/else structure destroys any value of OO paradigm
					if(resultSet.getType().contains("HierarchicalClusteringResults")) {
						HierarchicalClusteringWrapper analysis = new HierarchicalClusteringWrapper(
								dataSet, params);
						resultSet.setName("Hierarchical Clustering");
						resultSet.setData(ObjectConversion.convertToByte(analysis.execute()));
					} else if(resultSet.getType().contains("CNKBResults")) {
						CNKBInteractions cnkb = new CNKBInteractions();
						Vector<CellularNetWorkElementInformation> hits = cnkb.CNKB(dataSet, params);
						resultSet.setName("CNKB");
						resultSet.setData(ObjectConversion.convertToByte(hits));
					} else if(resultSet.getType().contains("AnovaResults")) {
						AnovaAnalysis analysis = new AnovaAnalysis(dataSet, (AnovaUI) params.get("form"));
						resultSet.setData(ObjectConversion.convertToByte(analysis.execute()));
						resultSet.setName("Anova");
					} else if(resultSet.getType().contains("AracneResults")) {
						AracneAnalysisWeb analyze = new AracneAnalysisWeb(dataSet, params);
						resultSet.setData(ObjectConversion.convertToByte(analyze.execute()));
						resultSet.setName("Aracne");
					} else if(resultSet.getType().contains("MarinaResults")) {
						MarinaAnalysis analyze = new MarinaAnalysis(dataSet, params);
						try{
							CSMasterRegulatorTableResultSet mraRes = analyze.execute();
							resultSet.setData(ObjectConversion.convertToByte(mraRes));
							resultSet.setName("Marina - " + mraRes.getLabel());
						}catch(RemoteException e){
							e.printStackTrace();
							String msg = e.getMessage().replaceAll("\n", "<br>");
							getWindow().showNotification("RemoteException<br>", msg, Notification.TYPE_ERROR_MESSAGE);
							FacadeFactory.getFacade().delete(resultSet);
							navigationTree.removeItem(resultSet.getId());
							pusher.push();
							return;	
						}
					}
					FacadeFactory.getFacade().store(resultSet);	
					synchronized(getApplication()) {
						MessageBox mb = new MessageBox(getWindow(), 
								"Analysis Completed", 
								MessageBox.Icon.INFO, 
								"Analysis you submitted is now completed. " +
										"Click on the node to see the results",  
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
			analysisThread.start();
		}
	}

	/**
	 * Creates context menu for Marker Tree and handles Actions
	 */
	private Action.Handler markerTreeActionHandler = new Action.Handler() {

		private static final long serialVersionUID = 1L;

		@Override
		public void handleAction(Action action, final Object sender, final Object target) {

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

			Button submit = new Button("Submit", new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						if(setName.getValue() != null) {
							ArrayList<String> markers = new ArrayList<String>();
							String mark 	= 	sender.toString();
							String[] temp 	= 	(mark.substring(1, mark.length()-1)).split(",");
							for(int i=0; i<temp.length; i++) {
								markers.add((String) markerTree.getItem(Integer.parseInt(temp[i].trim())).getItemProperty("Labels").getValue());
							}
							SubSetOperations.storeData(markers, "marker", (String) setName.getValue(), dataSetId);
							markerSetTree.addItem((String) setName.getValue());
							markerSetTree.getContainerProperty((String) setName.getValue(), "setName").setValue((String) setName.getValue());
							markerSetTree.setParent((String) setName.getValue(), "MarkerSets");
							markerSetTree.setChildrenAllowed((String) setName.getValue(), false);
							getApplication().getMainWindow().removeWindow(nameWindow);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
			submit.setClickShortcut(KeyCode.ENTER);
			nameWindow.addComponent(setName);
			nameWindow.addComponent(submit);
			getApplication().getMainWindow().addWindow(nameWindow);
		}

		@Override
		public Action[] getActions(Object target, Object sender) {
			return ACTIONS;
		}
	};

	/**
	 * Creates context menu for Phenotype Tree and handles Actions
	 */
	private Action.Handler arrayTreeActionHandler = new Action.Handler() {

		private static final long serialVersionUID = 1L;

		@Override
		public void handleAction(Action action, final Object sender, Object target) {
			final Window nameWindow = new Window();
			nameWindow.setModal(true);
			nameWindow.setClosable(true);
			((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
			nameWindow.setWidth("300px");
			nameWindow.setHeight("150px");
			nameWindow.setResizable(false);
			nameWindow.setCaption("Add Phenotypes to Set");
			nameWindow.setImmediate(true);

			final TextField setName = new TextField();
			setName.setInputPrompt("Please enter set name");
			setName.setImmediate(true);

			Button submit = new Button("Submit", new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						if(setName.getValue() != null) {
							ArrayList<String> markers = new ArrayList<String>();
							String mark 	= 	sender.toString();
							String[] temp 	= 	(mark.substring(1, mark.length()-1)).split(",");
							for(int i=0; i<temp.length; i++) {
								markers.add((String) arrayTree.getItem(Integer.parseInt(temp[i].trim())).getItemProperty("Labels").getValue());
							}
							SubSetOperations.storeData(markers, "microarray", (String) setName.getValue(), dataSetId);
							arraySetTree.addItem((String) setName.getValue());
							arraySetTree.getContainerProperty((String) setName.getValue(), "setName").setValue((String) setName.getValue());
							arraySetTree.setParent((String) setName.getValue(), "arraySets");
							arraySetTree.setChildrenAllowed((String) setName.getValue(), false);
							getApplication().getMainWindow().removeWindow(nameWindow);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
			submit.setClickShortcut(KeyCode.ENTER);
			nameWindow.addComponent(setName);
			nameWindow.addComponent(submit);
			getApplication().getMainWindow().addWindow(nameWindow);
		}

		@Override
		public Action[] getActions(Object target, Object sender) {
			return ACTIONS;
		}
	};
}

