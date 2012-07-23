package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
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
	
	private static USetsTabSheet setTabs;
	
	User user = SessionHandler.get();
	
	public UMainLayout() {
		
		setStyleName(Reindeer.LAYOUT_BLUE);
		
		mainPanel 						= 	new HorizontalSplitPanel();
		welcomeLayout					=	new HorizontalLayout();
		welcome 						= 	new CustomLayout("welcome");
		setTabs							= 	new USetsTabSheet();
		Accordion tabs 					= 	new UAccordionPanel(true);
		VerticalLayout mainLayout		=	new VerticalLayout();
		VerticalSplitPanel setLayout	=	new VerticalSplitPanel();
		VerticalLayout setTabLayout		= 	new VerticalLayout();	
		
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
	
	public static void populateSets(DSMicroarraySet maSet) {

		setTabs.populateTabSheet(maSet);
		
	}
	
	class H2 extends Label {
   
		private static final long serialVersionUID = 1L;

		public H2(String caption) {
            super(caption);
            setSizeUndefined();
            setStyleName(Reindeer.LABEL_H1);
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
