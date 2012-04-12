 package org.geworkbenchweb.layout;

import org.geworkbenchweb.GeworkbenchApplication;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalSplitPanel;

public class UMainLayout extends AbsoluteLayout {

	private static final long serialVersionUID = 6214334663802788473L;
	
	private static HorizontalSplitPanel mainPanel;
	
	private CustomLayout welcome;
	
	private VerticalSplitPanel toolPanel;
	
	@SuppressWarnings("unused")
	private GeworkbenchApplication app;
	
	User user = SessionHandler.get();
	
	public UMainLayout(GeworkbenchApplication app) {
		
		
		this.app 						= 	app;
		toolPanel 						=   new VerticalSplitPanel();
		mainPanel 						= 	new HorizontalSplitPanel();
		Accordion tabs 					= 	new UAccordionPanel(true);
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
        
        toolbar.setSizeUndefined();
        toolbar.addStyleName("right");
        toolbar.addComponent(welcomeUser);
        toolbar.addComponent(logoutButton);
        mainHeader.addComponent(toolbar);
        
        tabs.setStyleName("borderless");
        tabs.setSizeFull();
        
		mainPanel.setSizeFull();
        mainPanel.setImmediate(true);
		mainPanel.setStyleName("small previews");
        mainPanel.setSplitPosition(20);   
		mainPanel.setFirstComponent(tabs);
		
		welcome.setSizeFull();
		mainPanel.setSecondComponent(welcome);
		
		toolPanel.setFirstComponent(mainHeader);
        toolPanel.setSecondComponent(mainPanel);
		
        addComponent(toolPanel);
	
	}
	/**
	 * Sets the second Component of the Main Panel
	 * @param Component
	 */
	public static void setMainPanelSecondComponent(Component component) {
		
		mainPanel.setSecondComponent(component);
	
	}
	public static void mainPanelRequestRepaint() {
		
		mainPanel.requestRepaint();
		
	}
}
