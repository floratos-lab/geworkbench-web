package org.geworkbenchweb;
 
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.NodeAddEvent.NodeAddEventListener;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.events.PluginEvent.PluginEventListener;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.PluginRegistry;
import org.geworkbenchweb.plugins.anova.Anova;
import org.geworkbenchweb.plugins.anova.AnovaUI;
import org.geworkbenchweb.plugins.aracne.Aracne;
import org.geworkbenchweb.plugins.aracne.AracneUI;
import org.geworkbenchweb.plugins.cnkb.CNKB;
import org.geworkbenchweb.plugins.cnkb.CNKBUI;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClustering;
import org.geworkbenchweb.plugins.hierarchicalclustering.HierarchicalClusteringUI;
import org.geworkbenchweb.plugins.marina.Marina;
import org.geworkbenchweb.plugins.marina.MarinaUI;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.artur.icepush.ICEPush;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.ui.Window;

/**
 * This is the application entry point.
 * @author Nikhil Reddy
 */
public class GeworkbenchRoot extends Application implements TransactionListener {
	
	private static final long serialVersionUID = 6853924772669700361L;
	
	private static ThreadLocal<PluginRegistry> pluginRegistry		= 	new ThreadLocal<PluginRegistry>();
	private static ThreadLocal<Blackboard> BLACKBOARD 				= 	new ThreadLocal<Blackboard>();
	private static ThreadLocal<ICEPush> PUSHER 						= 	new ThreadLocal<ICEPush>();
	private static ThreadLocal<GeworkbenchRoot> currentApplication 	= 	new ThreadLocal<GeworkbenchRoot>();
	
	private final Blackboard blackboardInstance 		= 	new Blackboard();
	private final ICEPush pusherInstance 				=	new ICEPush();
	
	private static final String APP_THEME_NAME 			= 	"geworkbench";
	private static final String PROPERTIES_FILE 		= 	"application.properties";
    private static String APP_URL 						= 	null;
	
	private Window mainWindow;
	
	private static Properties prop; 
	
	@Override
	public void init() {
		
		prop = new Properties();
		
		try {
			prop.load(getClass().getResourceAsStream(
					"/" + PROPERTIES_FILE));
		} catch (IOException e) {
			 
			e.printStackTrace();
		}
		 
		getContext().addTransactionListener(this);
		BLACKBOARD.set(blackboardInstance);
		PUSHER.set(pusherInstance);
		
		setTheme(APP_THEME_NAME);
		SessionHandler.initialize(this);
		
		User user 	= 	SessionHandler.get();
		mainWindow 	= 	new Window("geWorkbench");
		
		mainWindow.setSizeFull();
		setMainWindow(mainWindow);
		
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
                    + APP_THEME_NAME + "/");
            return uri.normalize().toString();
        } catch (Exception e) {
            System.err.println("Theme location could not be resolved:" + e);
        }
        return "/VAADIN/themes/" + APP_THEME_NAME + "/";
    }
		
	public void loginView() {
		UUserAuth auth = new UUserAuth(); 
		auth.buildLoginForm();
		mainWindow.setContent(auth);
	}
	
	public void initView()  {
		mainWindow.setContent(new UMainLayout());
	}

	@Override
	public void transactionStart(Application application, Object transactionData) {
		if (application == GeworkbenchRoot.this) {
			BLACKBOARD.set(blackboardInstance);
			PUSHER.set(pusherInstance);
			currentApplication.set(this);
		}
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
		if (application == GeworkbenchRoot.this) {
			// to avoid keeping an Application hanging, and mitigate the 
			// possibility of user session crosstalk
			BLACKBOARD.set(null);
			PUSHER.set(null);
			currentApplication.set(null);
            currentApplication.remove();
		}
	}
	
	/**
	 * Method supplies Application Instance 
	 * @return Current Application Instance
	 */
	public static GeworkbenchRoot getInstance() {
        return currentApplication.get();
    }
	
	/**
	 * Method supplies Blackboard instance to the entire Application
	 * @return Blackboard Instance for the application
	 */
	public static Blackboard getBlackboard() {
		return BLACKBOARD.get();
	}
	
	/**
	 * Method supplies Pusher instance to the entire Application
	 * @return Pusher Instance for the application
	 */
	public static ICEPush getPusher() {
		return PUSHER.get();
	}
	
	
	public static Properties getAppProperties() {
		return prop;
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

	// TODO compare whether registration of class is a better idea than doing it for instance; 
	// TODO use configuration file (say, plugins.xml) to control the registration, so this class does not have to know each analysis plug-ins
	// TODO verify when .get() returns null and code accordingly to be explicit
	public static PluginRegistry getPluginRegistry() {
		PluginRegistry pr = pluginRegistry.get();
		if(pr==null) {
			pr = new PluginRegistry();
			pr.register(new Anova(), new AnovaUI(0L));
			pr.register(new Aracne(), new AracneUI(0L));
			pr.register(new CNKB(), new CNKBUI(0L));
			pr.register(new HierarchicalClustering(), new HierarchicalClusteringUI(0L));
			pr.register(new Marina(), new MarinaUI(0L)); 
			pluginRegistry.set(pr);
		}
		return pr;
	}
}

