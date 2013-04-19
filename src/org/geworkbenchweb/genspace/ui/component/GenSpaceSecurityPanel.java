package org.geworkbenchweb.genspace.ui.component;

import org.geworkbenchweb.genspace.ui.GenSpaceComponent;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class GenSpaceSecurityPanel extends CustomComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 702655942127334031L;
	private Button logout = new Button("logout");
	private GenSpaceLogin login;
	
	private GenSpaceComponent genSpaceParent;
	
	public GenSpaceSecurityPanel(String uName, GenSpaceComponent genSpaceParent, GenSpaceLogin login) {
		this.genSpaceParent = genSpaceParent;
		this.login = login;
		BorderLayout borderLayout = new BorderLayout();
		setCompositionRoot(borderLayout);
		
		HorizontalLayout panel = new HorizontalLayout();
//		panel.setPreferredSize(new Dimension(1024, 500));

		TabSheet mainPanel = new TabSheet();
		DataVisibility dataPanel = new DataVisibility(login);
//		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		mainPanel.addTab(dataPanel, "Data Visibility", null);

		// NetworkVisibility nwPanel = new NetworkVisibility(uName);
		// mainPanel.addTab("User Visibility", nwPanel);

		GenSpaceProfile genPanel = new GenSpaceProfile(login);
		mainPanel.addTab(genPanel, "General Profile", null);
//		mainPanel.setMaximumSize(new Dimension(500,500));
//		panel.setMaximumSize(new Dimension(500,500));
		panel.addComponent(mainPanel);
		panel.addComponent(logout);
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
