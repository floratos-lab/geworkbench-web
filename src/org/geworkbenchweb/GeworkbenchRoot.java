package org.geworkbenchweb;

import java.net.URI;

import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.events.PluginEvent.PluginEventListener;
import org.geworkbenchweb.layout.UMainLayout;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.artur.icepush.ICEPush;

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
	
	private static ThreadLocal<ICEPush> PUSHER = new ThreadLocal<ICEPush>();
	
	private final ICEPush pusherInstance = new ICEPush();
	
	private Window mainWindow;
	
	private static final String SAMPLER_THEME_NAME = "geworkbench";

    // used when trying to guess theme location
    private static String APP_URL = null;

	@Override
	public void init() {
		
		BLACKBOARD.set(blackboardInstance);
		PUSHER.set(pusherInstance);
		
		setTheme(SAMPLER_THEME_NAME);
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
	
	/**
     * Tries to guess theme location.
     * 
     * @return
     */
    public static String getThemeBase() {
        try {
            URI uri = new URI(APP_URL + "../VAADIN/themes/"
                    + SAMPLER_THEME_NAME + "/");
            return uri.normalize().toString();
        } catch (Exception e) {
            System.err.println("Theme location could not be resolved:" + e);
        }
        return "/VAADIN/themes/" + SAMPLER_THEME_NAME + "/";
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
			PUSHER.set(pusherInstance);
		}
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
		if (application == this) {
			// to avoid keeping an Application hanging, and mitigate the 
			// possibility of user session crosstalk
			BLACKBOARD.set(null);
			PUSHER.set(null);
		}
	}
	
	/**
	 * Method supplies Blackboard instance to the entire Application
	 * @return Blackboard Instance for the application
	 */
	public static Blackboard getBlackboard() {
		return BLACKBOARD.get();
	}
	
	public static ICEPush getPusher() {
		return PUSHER.get();
	}
	
	/**
	 * All the Events in geWorkbench Application are strictly registered here.
	 */
	private void registerAllEventsForApplication() {
		
		/* This event should be fired whenever new ResultNode is added */
		getBlackboard().register(NodeAddEventListener.class, NodeAddEvent.class);
		getBlackboard().register(PluginEventListener.class, PluginEvent.class);
		getBlackboard().register(AnalysisSubmissionEventListener.class, AnalysisSubmissionEvent.class);
	}

}

