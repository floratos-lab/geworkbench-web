package org.geworkbenchweb.genspace.ui.component;

import org.geworkbenchweb.genspace.ui.GenspaceLayout;
import org.geworkbenchweb.genspace.ui.GenspaceToolBar;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class GenSpaceSecurityPanel extends CustomComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 702655942127334031L;
	private Button logout = new Button("logout");
	private GenSpaceLogin_1 login;
	
	private GenspaceLayout genSpaceParent;
	
	public GenSpaceSecurityPanel(String uName, /*GenSpaceComponent*/ GenspaceLayout genSpaceParent2, GenSpaceLogin_1 login) {
		this.genSpaceParent = genSpaceParent2;
		this.login = login;
		BorderLayout borderLayout = new BorderLayout();
		setCompositionRoot(borderLayout);
		
		VerticalLayout panel = new VerticalLayout();
//		panel.setPreferredSize(new Dimension(1024, 500));

		TabSheet mainPanel = new TabSheet();
		mainPanel.setStyleName(Reindeer.TABSHEET_MINIMAL);//mainPanel.setSizeFull();
		DataVisibility_1 dataPanel = new DataVisibility_1(login); 
//		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		//mainPanel.addTab(dataPanel);
		mainPanel.addTab(dataPanel, "Data Visibiliy", null); 
		// NetworkVisibility nwPanel = new NetworkVisibility(uName);
		// mainPanel.addTab("User Visibility", nwPanel);

		GenSpaceProfile genPanel = new GenSpaceProfile(login);
		mainPanel.addTab(genPanel, "General Profile", null);
		
		
//		mainPanel.setMaximumSize(new Dimension(500,500));
//		panel.setMaximumSize(new Dimension(500,500));
		panel.setSpacing(true);
//		panel.addComponent(logout);
		panel.addComponent(mainPanel);
		panel.setSizeFull(); 
		logout.addListener(ClickEvent.class, this, "logoutPerformed");
		
		borderLayout.addComponent(panel, BorderLayout.Constraint.CENTER);
	}

	public void logoutPerformed(ClickEvent event) {
		// TODO
		Application application = getApplication();
		Window mainWindow = application.getMainWindow();
		Notification notification = new Notification("User logged out",
				Notification.TYPE_TRAY_NOTIFICATION);
		mainWindow.showNotification(notification);
		
		//GenSpaceServerFactory.logout();
		login.getGenSpaceServerFactory().logout();
		genSpaceParent.fireLoggedOut();		
	}

}
