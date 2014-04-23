package org.geworkbenchweb.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.Workspace;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.Item;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * UMainLayout sets up the basic layout and style of the application.
 * 
 * @author Nikhil Reddy
 */
public class UMainLayout extends VerticalLayout {
	
	private static Log log = LogFactory.getLog(UMainLayout.class);

	private static final long serialVersionUID = 6214334663802788473L;

	static private ThemeResource pendingIcon 	=	new ThemeResource("../custom/icons/pending.gif");
	static private ThemeResource annotIcon 		= 	new ThemeResource("../custom/icons/icon_info.gif");
	static private ThemeResource CancelIcon 	= 	new ThemeResource("../runo/icons/16/cancel.png");
	static private ThemeResource openSetIcon	=	new ThemeResource("../custom/icons/open_set.png");
	static private ThemeResource saveSetIcon	=	new ThemeResource("../custom/icons/save_set.png");

	final private HorizontalSplitPanel mainSplit = new HorizontalSplitPanel();

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
	
	final private NavigationTree navigationTree;
	
	final private HorizontalLayout navigationPanel;

	private SetViewLayout setViewLayout;
	
	public void push() {
		pusher.push();
	}
	
	public UMainLayout() {

		/*Enable genspace logger in geWorkbench*/
		if(GeworkbenchRoot.genespaceEnabled()) {
			GeworkbenchRoot.getBlackboard().addListener(genspaceLogger);
		}
		
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
		setViewMeuItem = toolBar.addItem("Set View", new MenuBar.Command() {

			private static final long serialVersionUID = -3200891031850457832L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				selectedItem.setEnabled(false);
				
				removeButton.setVisible(false);
				annotButton.setVisible(false);
				
				removeSetButton.setVisible(true);
				openSetButton.setVisible(true);
				saveSetButton.setVisible(true);

				workspaceViewMenuItem.setEnabled(true);
				
				Long microarraySetId = navigationTree.getMicroarraySetId();
				setViewLayout = new SetViewLayout(microarraySetId);
				mainSplit.setFirstComponent(setViewLayout);
			}
			
		});
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
		removeSetButton.addListener(new RemoveSetListener(this));

		openSetButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5166425513891423653L;
			@Override
			public void buttonClick(ClickEvent event) {
	        	Long dataSetId = navigationTree.geDataSetId();
				getWindow().addWindow(new OpenSetWindow(dataSetId , setViewLayout));
			}
		});

		saveSetButton.addListener(new SaveSetListener(this));

		navigationPanel = createTopNavigationPanel();
		addComponent(navigationPanel);

		Button logo = new NativeButton();
		logo.setDescription("geWorkbench Home");
		logo.setStyleName(BaseTheme.BUTTON_LINK);
		logo.addStyleName("logo");
		topBar.addComponent(logo);
		topBar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);

		Label userName = new Label("<span style=color:white>User name: "
				+ user.getName() + "</span>", Label.CONTENT_XHTML);
		Long wsid = mainToolBar.getCurrentWorkspace();
		Workspace current = FacadeFactory.getFacade().find(Workspace.class,
				wsid);
		Label workspaceName = new Label("<span style=color:white>Workspace: "
				+ current.getName() + "</span>", Label.CONTENT_XHTML);
		topBar.addComponent(userName);
		topBar.addComponent(workspaceName);
		topBar.setComponentAlignment(userName, Alignment.MIDDLE_CENTER);
		topBar.setComponentAlignment(workspaceName, Alignment.MIDDLE_CENTER);

		mainSplit.setSizeFull();
		mainSplit.setStyleName("main-split");

		addComponent(mainSplit);
		setExpandRatio(mainSplit, 1);

		leftMainLayout.setImmediate(true);
		leftMainLayout.setSizeFull();
		leftMainLayout.addStyleName("mystyle");

		Long userId = user.getId();
		navigationTree = new NavigationTree(annotButton, removeButton, annotationPanel, toolBar, setViewMeuItem, pluginView, userId, pendingIcon);

		navigationTree.setImmediate(true);
		
		navigationTree.setItemCaptionPropertyId("Name");
		navigationTree.setItemIconPropertyId("Icon");
		navigationTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		leftMainLayout.addComponent(navigationTree);
		mainSplit.setFirstComponent(leftMainLayout);
		mainSplit.setSplitPosition(275, HorizontalSplitPanel.UNITS_PIXELS);
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
	} // end of the constructor.

	/* This locks GUI except for the plugin view panel.
	 * The code that calls this, which is meant to be data upload UI, is responsible to enable it later. */
	public void lockGuiForUpload() {
		navigationPanel.setEnabled(false);
		leftMainLayout.setEnabled(false);
		annotationPanel.setEnabled(false);
	}

	public void unlockGuiForUpload() {
		navigationPanel.setEnabled(true);
		leftMainLayout.setEnabled(true);
		annotationPanel.setEnabled(true);
	}
	
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
	
	public void removeItem(Long itemId) {
		navigationTree.removeItem(itemId);
		pusher.push();
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
					mainSplit.setSplitPosition(250, HorizontalSplitPanel.UNITS_PIXELS);
					mainSplit.setLocked(false);
					navigationTree.setVisible(true);
				}
			}
		});
		mainSplit.setSplitPosition(250, HorizontalSplitPanel.UNITS_PIXELS);
		return b;
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

		navigationTree.select(res.getId());
		log.debug("result node is added");
	}
	
	// this may need to be public if we don't use event listener to trigger it.
	private void addDataSet(final DataSet dS) {
		Long id = dS.getId();
		String name = dS.getName();
		String className = dS.getType();
		
		Item item = navigationTree.addItem(id);
		if(item==null) {
			// this happens because pending node has the same ID as the ultimate node
			item = navigationTree.getItem(id);
		}
		item.getItemProperty("description").setValue(dS.getDescription());
		// TODO let item holding the entire DataSet may be the better idea than the current way to read all fields from it
		navigationTree.setChildrenAllowed(id, false);
		boolean pending = name.contains("Pending");
		if(pending) {
			navigationTree.getContainerProperty(id, "Icon").setValue(pendingIcon);
		} else {
			try {
				ThemeResource icon = GeworkbenchRoot.getPluginRegistry().getIcon(Class.forName(className));
				navigationTree.getContainerProperty(id, "Icon").setValue(icon);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		navigationTree.getContainerProperty(id, "Name").setValue(name);
		navigationTree.getContainerProperty(id, "Type").setValue(className);
		if(!pending) {
			navigationTree.select(id);
		}
	}
	
	public void addNode(Object object) {
		if (object instanceof ResultSet) {
			ResultSet res = (ResultSet) object;
			addResultSetNode(res);
		} else if (object instanceof DataSet) {
			DataSet dS = (DataSet) object;
			addDataSet(dS);
		} else {
			log.error("cannot add node of an object of this "
					+ object.getClass().getName());
		}
	}

	private void switchToWorkspaceView() {
		removeButton.setVisible(true);
		annotButton.setVisible(true);
		removeSetButton.setVisible(false);
		openSetButton.setVisible(false);
		saveSetButton.setVisible(false);

		workspaceViewMenuItem.setEnabled(false);
		setViewMeuItem.setEnabled(true);
		pluginView.setEnabled(true);
		mainToolBar.setEnabled(true);
		
		mainSplit.setFirstComponent(navigationTree);
	}

	Long getCurrentDatasetId() {
		return navigationTree.geDataSetId();
	}

	public SetViewLayout getSetViewLayout() {
		return setViewLayout;
	}
	
	public UMainToolBar getMainToolBar() {
		return mainToolBar;
	}
}
