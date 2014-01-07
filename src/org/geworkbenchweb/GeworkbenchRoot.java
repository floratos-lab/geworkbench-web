package org.geworkbenchweb;
 
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.PluginRegistry;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * This is the application entry point.
 * @author Nikhil Reddy
 * @version $Id$
 */
@Theme("geworkbench")
@PreserveOnRefresh
public class GeworkbenchRoot extends UI {
	
	private static final long serialVersionUID = 6853924772669700361L;
	
	private final Blackboard blackboardInstance 		= 	new Blackboard();
	
	private static final String APP_THEME_NAME 			= 	"geworkbench";
	private static final String PROPERTIES_FILE 		= 	"application.properties";
    private static String APP_URL 						= 	null;
	
	private static Properties prop = new Properties();
	private static final String ATTR_REGISTRY			=	"attributePluginRegistry";
	private static final String ATTR_BLACKBOARD			=	"attributeBlackboard";
	
	@Override
	public void init(VaadinRequest request) {
		
		VerticalLayout mainWindow = new VerticalLayout();
		mainWindow.setSizeFull();
		setContent(mainWindow);
		
		try {
			prop.load(getClass().getResourceAsStream(
					"/" + PROPERTIES_FILE));
		} catch (IOException e) {
			mainWindow.addComponent(new Label("failed to read application properties file "+PROPERTIES_FILE));
			e.printStackTrace();
			return;
		}
		
		// make sure the back-end data directory is there
		File dataDirectory = new File(getBackendDataDirectory());
		boolean dataDirectoryExist = true;
		if(!dataDirectory.exists()) {
			dataDirectoryExist = dataDirectory.mkdir();
		}
		if(!dataDirectoryExist || !dataDirectory.isDirectory()) {
			mainWindow.addComponent(new Label(
					"Back-end data directory cannot be set up at "
							+ getBackendDataDirectory() + "\nfull path "
							+ dataDirectory.getAbsolutePath() + " exist?"
							+ dataDirectory.exists() + " dir?"
							+ dataDirectory.isDirectory()));
			return;
		}
		
		// checking the database connection - do nothing if database is OK
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("default"); // persistence-unit name in the xml file
		try {
			EntityManager checkingEm = factory.createEntityManager();
			checkingEm.close();
		} catch (Exception e) {
			mainWindow.addComponent(new Label("No database is set up to support this application: "+e.getMessage()));
			e.printStackTrace();
			return;
		}
		 
		initBlackboard();
		
		SessionHandler.initialize(this);
		
		registerAllEventsForApplication();
		
		User user 	= 	SessionHandler.get();
		if (user != null) {
			mainWindow.addComponent(new UMainLayout());
		} else {
			UUserAuth auth = new UUserAuth(); 
			mainWindow.addComponent(auth);
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
                    + APP_THEME_NAME + "/");
            return uri.normalize().toString();
        } catch (Exception e) {
            System.err.println("Theme location could not be resolved:" + e);
        }
        return "/VAADIN/themes/" + APP_THEME_NAME + "/";
    }
		
	private void initBlackboard(){
		if(GeworkbenchRoot.getBlackboard() == null){
			try{
				VaadinSession.getCurrent().getLockInstance().lock();
				VaadinSession.getCurrent().setAttribute(ATTR_BLACKBOARD, blackboardInstance);
			}finally{
				VaadinSession.getCurrent().getLockInstance().unlock();
			}
		}
	}

	/**
	 * Method supplies Application Instance 
	 * @return Current Application Instance
	 */
	public static GeworkbenchRoot getInstance() {
        return (GeworkbenchRoot)UI.getCurrent();
    }
	
	/**
	 * Method supplies Blackboard instance to the entire Application
	 * @return Blackboard Instance for the application
	 */
	public static Blackboard getBlackboard() {
		Object value = VaadinSession.getCurrent().getAttribute(ATTR_BLACKBOARD);
		return value == null ? null : (Blackboard)value;
	}
	
	/**
	 * All the Events in geWorkbench Application are strictly registered here.
	 */
	private void registerAllEventsForApplication() {
		
		/* This event should be fired whenever new ResultNode is added */
		getBlackboard().register(NodeAddEventListener.class, NodeAddEvent.class);
		getBlackboard().register(AnalysisSubmissionEventListener.class, AnalysisSubmissionEvent.class);
		
		/* Register two new events for genSpace. */
		/*getBlackboard().register(LogCompleteEventListener.class, LogCompleteEvent.class);
		getBlackboard().register(ChatStatusChangeEventListener.class, ChatStatusChangeEvent.class);*/
	}	

	// TODO verify when .get() returns null and code accordingly to be explicit
	public static PluginRegistry getPluginRegistry() {
		Object value = VaadinSession.getCurrent().getAttribute(ATTR_REGISTRY);
		if(value != null) return (PluginRegistry)value;
		else {
			PluginRegistry pr = new PluginRegistry();
			pr.init();
			try{
				VaadinSession.getCurrent().getLockInstance().lock();
				VaadinSession.getCurrent().setAttribute(ATTR_REGISTRY, pr);
			}finally{
				VaadinSession.getCurrent().getLockInstance().unlock();
			}
			return pr;
		}
	 }

/*	@Override
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
		if (request != null) {
			String requestURL= request.getRequestURL().toString();
			if (requestURL.endsWith("geworkbench")){
				try{
					//bug fix #3264
					response.sendRedirect(requestURL+"/");
				}catch(IOException e){
					e.printStackTrace();
				}
			}
	 	}
	}

	@Override
	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {}
*/
	/* it is a little better than passing over a private member directly */
	public static String getAppProperty(String serviceUrlProperty) {
		return prop.getProperty(serviceUrlProperty);
	}
	
	public static String getBackendDataDirectory() {
		return System.getProperty("user.home")
				+ System.getProperty("file.separator")
				+ prop.getProperty("data.directory");
	}

	public static boolean genespaceEnabled() {
		if(prop.getProperty("genspace").equals("on")) {
			return true;
		} else {
			return false;
		}
	}

	public static String getPublicAnnotationDirectory() {
		return System.getProperty("user.home")
				+ System.getProperty("file.separator")
				+ prop.getProperty("public.annotation.directory");
	}
}
