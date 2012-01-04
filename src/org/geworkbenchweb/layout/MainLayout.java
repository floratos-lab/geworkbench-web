package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.GeworkbenchApplication;
import org.geworkbenchweb.dataset.DataSetUpload;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
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
        mainPanel.setSplitPosition(15);        
              
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
		
		AccordionPanels(boolean closable) {
			
			super();
			for (int i=1; i<4; i++) {
				
				VerticalLayout l 	= 	new VerticalLayout();
				l.setMargin(true);
				Tab t = addTab(l);
				if(i == 1) {
					
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
					dataTree.addContainerProperty("DataSet Name", String.class, "");
					dataTree.setContainerDataSource(getDataContainer());
					dataSets.addComponent(dataTree);
					
					dataTree.addActionHandler(this);
					dataTree.addListener(this);
			        dataTree.setImmediate(true);
					
					l.addComponent(dataSets);
					l.setHeight("100%");
				
				}
				
				if(i == 2) {
					
					t.setCaption("Markers");
					l.addComponent(new Label("Markers of the selected datasets if  available"));
				
				}
				
				if(i == 3) {
					
					t.setCaption("Arrays");
					l.addComponent(new Label("Arrays of the selected datasets if available"));
				
				}
				
				t.setIcon(new ThemeResource("../runo/icons/16/folder.png"));
				t.setClosable(closable);
			
			}
		}

		@Override
		public void buttonClick(ClickEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void valueChange(ValueChangeEvent event) {
			
			if (event.getProperty().getValue() != null) {
				
				VisualPlugin tabSheet = new VisualPlugin((String) event.getProperty().getValue());
				mainPanel.setSecondComponent(tabSheet);
				mainPanel.requestRepaint();				
				
			} else {
				
				//TODO:Handle it later
		
			}
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
				FacadeFactory.getFacade().delete(dataSet);
				dataTree.removeItem(target);
				
			}
			
		}
	}
	
	public IndexedContainer getDataContainer() {
		
		IndexedContainer dataSets 		= 	new IndexedContainer();
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("owner", user.getId());
		String whereClause = "p.owner = :owner";
		List<?> data = FacadeFactory.getFacade().getFieldValues(DataSet.class, "name", whereClause, parameters);
		
		for(int i=0; i<data.size(); i++) {
			
			String id =  (String) data.get(i);
		    dataSets.addItem(id);	   
		
		}
		
		return dataSets;
	
	}
}
