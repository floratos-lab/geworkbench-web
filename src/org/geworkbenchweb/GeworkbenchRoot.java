package org.geworkbenchweb;

import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.layout.UMainLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.vaadin.Application;
import com.vaadin.ui.*;

/**
 * This is the application entry point.
 * @author Nikhil Reddy
 */


public class GeworkbenchRoot extends Application {
	
	private static final long serialVersionUID = 6853924772669700361L;
	
	private Window mainWindow;

	@Override
	public void init() {
		
		setTheme("geworkbench");
		SessionHandler.initialize(this);
		
		User user 	= 	SessionHandler.get();
		mainWindow 	= 	new Window("geWorkbench");
		
		mainWindow.setSizeFull();
		setMainWindow(mainWindow);
		
		if (user != null) {
		
			initView();
		
		}else {	
			
			loginView();
		
		}
		
	}
		
	public void loginView() {
	
		mainWindow.setContent(new UUserAuth());
	
	}
	
	public void initView()  {
		
		mainWindow.setContent(new UMainLayout());
	
	}

}

