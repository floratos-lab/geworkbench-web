package org.geworkbenchweb.layout;

import org.geworkbenchweb.GeworkbenchApplication;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

public class UMainLayout extends HorizontalLayout {

	private static final long serialVersionUID = 6214334663802788473L;
	
	private static HorizontalSplitPanel mainPanel;
	
	private CustomLayout welcome;
	
	private HorizontalLayout welcomeLayout;
	
	@SuppressWarnings("unused")
	private GeworkbenchApplication app;
	
	User user = SessionHandler.get();
	
	public UMainLayout(GeworkbenchApplication app) {
		
		setStyleName(Reindeer.LAYOUT_BLUE);
		this.app 						= 	app;
		mainPanel 						= 	new HorizontalSplitPanel();
		Accordion tabs 					= 	new UAccordionPanel(true);
		welcomeLayout					=	new HorizontalLayout();
		welcome 						= 	new CustomLayout("welcome");
		VerticalLayout mainLayout		=	new VerticalLayout();
		VerticalSplitPanel setLayout	=	new VerticalSplitPanel();
		USetsTabSheet setTabs			= 	USetsTabSheet.getSetsTabSheetObject();
		
		setTabs.removeData();
		setTabs.setImmediate(true);
		
		setLayout.setSplitPosition(60);
		setLayout.setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLayout.setImmediate(true);
		setLayout.setFirstComponent(tabs);
		setLayout.setSecondComponent(setTabs);
		
		mainLayout.setSizeFull();
        mainLayout.addComponent(getHeader());

        CssLayout margin = new CssLayout();
        margin.setMargin(false, true, true, true);
        margin.setSizeFull();
        
        margin.addComponent(mainPanel);
        mainLayout.addComponent(margin);
        mainLayout.setExpandRatio(margin, 1);
		
		setSizeFull();
        
        tabs.setStyleName(Reindeer.TABSHEET_SMALL);
        tabs.setSizeFull();
        
		mainPanel.setSizeFull();
        mainPanel.setImmediate(true);
        mainPanel.setSplitPosition(20);   
		mainPanel.setFirstComponent(setLayout);
		
	
		
		welcome.setSizeFull();
		welcomeLayout.setStyleName(Reindeer.LAYOUT_WHITE);
		welcomeLayout.setSizeFull();
		welcomeLayout.addComponent(welcome);
		mainPanel.setSecondComponent(welcomeLayout);
		
        addComponent(mainLayout);
	
	}
	/**
	 * Sets the second Component of the Main Panel
	 * @param Component
	 */
	public static void setMainPanelSecondComponent(Component component) {
		
		mainPanel.removeComponent(mainPanel.getSecondComponent());
		
		HorizontalLayout welcomeLayout1 = new HorizontalLayout();
		welcomeLayout1.setStyleName(Reindeer.LAYOUT_WHITE);
		welcomeLayout1.setSizeFull();
		welcomeLayout1.addComponent(component);
		
		mainPanel.setSecondComponent(welcomeLayout1);
	
	}
	/**
	 * Repaint the Main Panel
	 */
	public static void mainPanelRequestRepaint() {
		
		mainPanel.requestRepaint();
		
	}
	
	@SuppressWarnings("deprecation")
	Layout getHeader() {
		
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setMargin(true);
        header.setSpacing(true);

        CssLayout titleLayout = new CssLayout();
        H2 title = new H2("geWorkbench-Web");
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
        
        Button help 	=	new Button("Help");
        help.setStyleName(Reindeer.BUTTON_SMALL);
        buttons.addComponent(help);
        buttons.setComponentAlignment(help, "right");
        
        Button logout 	= 	new Button("Logout", new Button.ClickListener() {
            
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
                
				SessionHandler.logout();
				getApplication().close();
			}
        });
        logout.setStyleName(Reindeer.BUTTON_SMALL);
        buttons.addComponent(logout);
        titleLayout.addComponent(buttons);

        header.addComponent(titleLayout);
        header.setComponentAlignment(titleLayout, "right");

        return header;
    }
	
	class H2 extends Label {
   
		private static final long serialVersionUID = 1L;

		public H2(String caption) {
            super(caption);
            setSizeUndefined();
            setStyleName(Reindeer.LABEL_H2);
        }
    }

    class SmallText extends Label {
       
		private static final long serialVersionUID = 1L;

		public SmallText(String caption) {
            super(caption);
            setStyleName(Reindeer.LABEL_SMALL);
        }
    }

}
