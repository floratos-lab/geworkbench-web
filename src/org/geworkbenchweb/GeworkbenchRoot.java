package org.geworkbenchweb;

import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.layout.UMainLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.ui.*;

/**
 * This is the application entry point.
 * @author Nikhil Reddy
 */


public class GeworkbenchRoot extends Application implements TransactionListener {
	
	private static final long serialVersionUID = 6853924772669700361L;
	
	private static ThreadLocal<Blackboard> BLACKBOARD = new ThreadLocal<Blackboard>();
	
	private final Blackboard blackboardInstance = new Blackboard();
	
	private Window mainWindow;

	@Override
	public void init() {
		
		BLACKBOARD.set(blackboardInstance);
		
		setTheme("geworkbench");
		SessionHandler.initialize(this);
		
		User user 	= 	SessionHandler.get();
		mainWindow 	= 	new Window("geWorkbench");
		
		mainWindow.setSizeFull();
		setMainWindow(mainWindow);
		
		getContext().addTransactionListener(this);
		registerAllEventsForApplication();
		
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

	@Override
	public void transactionStart(Application application, Object transactionData) {
		if (application == this) {
		      BLACKBOARD.set(blackboardInstance);
		    }
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
		 if (application == this) {
		      // to avoid keeping an Application hanging, and mitigate the 
		      // possibility of user session crosstalk
		      BLACKBOARD.set(null);
		    }
	}
	
	/**
	 * Method supplies Blackboard instance to the entire Application
	 * @return Blackboard Instance for the application
	 */
	public static Blackboard getBlackboard() {
		return BLACKBOARD.get();
	}
	
	/**
	 * All the Events in geWorkbench Application are strictly registered here.
	 */
	private void registerAllEventsForApplication() {
		
		/* This event should be fired whenever new ResultNode is added */
		getBlackboard().register(NodeAddEventListener.class, NodeAddEvent.class);
	}

}

