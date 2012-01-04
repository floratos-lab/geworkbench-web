package org.geworkbenchweb;

import org.geworkbenchweb.authentication.UserAuth;
import org.geworkbenchweb.layout.MainLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.github.wolfie.sessionguard.SessionGuard;
import com.vaadin.Application;
import com.vaadin.ui.*;

@SuppressWarnings("serial")
public class GeworkbenchApplication extends Application {
	
	@Override
	public void init() {
		
		setTheme("geworkbench");
		SessionHandler.initialize(this);
		
		Window mainWindow 			= 	new Window("geWorkbench");
		SessionGuard sessionGuard 	= 	new SessionGuard();
		User user 					= 	SessionHandler.get();
		
		mainWindow.setSizeFull();
		setMainWindow(mainWindow);
		sessionGuard.setKeepalive(true);
		mainWindow.addComponent(sessionGuard);
		
		if (user != null) {
		
			initView(mainWindow);
		
		}else {	
			
			loginView(mainWindow);
		
		}
	}
	
	public void loginView(Window mainWindow) {
	
		mainWindow.setContent(new UserAuth(this));
	
	}
	
	public void initView(Window mainWindow)  {
		
		mainWindow.setContent(new MainLayout(this));
	
	}

}

