package org.geworkbenchweb;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.AnalysisSubmissionEvent.AnalysisSubmissionEventListener;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.PluginRegistry;
import org.geworkbenchweb.utils.GeneOntologyTree;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * This is the application entry point.
 */
public class GeworkbenchRoot extends Application implements TransactionListener, HttpServletRequestListener {

	private static final long serialVersionUID = 6853924772669700361L;
	private static Log log = LogFactory.getLog(GeworkbenchRoot.class);

	private static ThreadLocal<PluginRegistry> pluginRegistry = new ThreadLocal<PluginRegistry>();
	private static ThreadLocal<Blackboard> BLACKBOARD = new ThreadLocal<Blackboard>();
	private static ThreadLocal<GeworkbenchRoot> currentApplication = new ThreadLocal<GeworkbenchRoot>();

	private final Blackboard blackboardInstance = new Blackboard();

	private static final String APP_THEME_NAME = "geworkbench";
	private static final String PROPERTIES_FILE = "application.properties";

	private static Properties prop = new Properties();

	@Override
	public void init() {
		if (FacadeFactory.getFacade() == null) {
			try {
				FacadeFactory.registerFacade("default", true);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		Window mainWindow = new Window("geWorkbench");
		mainWindow.setSizeFull();
		setMainWindow(mainWindow);

		try {
			prop.load(getClass().getResourceAsStream(
					"/" + PROPERTIES_FILE));
		} catch (IOException e) {
			mainWindow.addComponent(new Label("failed to read application properties file " + PROPERTIES_FILE));
			e.printStackTrace();
			return;
		}
		System.setProperty("authentication.password.salt", prop.getProperty("authentication.password.salt"));

		// make sure the back-end data directory is there
		File dataDirectory = new File(getBackendDataDirectory());
		boolean dataDirectoryExist = true;
		if (!dataDirectory.exists()) {
			dataDirectoryExist = dataDirectory.mkdir();
		}
		if (!dataDirectoryExist || !dataDirectory.isDirectory()) {
			mainWindow.addComponent(new Label(
					"Back-end data directory cannot be set up at "
							+ getBackendDataDirectory() + "\nfull path "
							+ dataDirectory.getAbsolutePath() + " exist?"
							+ dataDirectory.exists() + " dir?"
							+ dataDirectory.isDirectory()));
			return;
		}

		// checking the database connection - do nothing if database is OK
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("default"); // persistence-unit name in
																							// the xml file
		try {
			EntityManager checkingEm = factory.createEntityManager();
			checkingEm.close();
		} catch (Exception e) {
			mainWindow.addComponent(new Label("No database is set up to support this application: " + e.getMessage()));
			e.printStackTrace();
			return;
		}

		getContext().addTransactionListener(this);
		BLACKBOARD.set(blackboardInstance);

		setTheme(APP_THEME_NAME);
		SessionHandler.initialize(this);

		registerAllEventsForApplication();

		User user = SessionHandler.get();
		if (user != null) {
			try {
				mainWindow.setContent(new UMainLayout());
			} catch (Exception e) {
				mainWindow.setContent(new UUserAuth());
			}
		} else {
			UUserAuth auth = new UUserAuth();
			mainWindow.setContent(auth);
		}

		GeneOntologyTree.getInstance();
	}

	@Override
	public void transactionStart(Application application, Object transactionData) {
		if (application == GeworkbenchRoot.this) {
			BLACKBOARD.set(blackboardInstance);
			currentApplication.set(this);
		}
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
		if (application == GeworkbenchRoot.this) {
			// to avoid keeping an Application hanging, and mitigate the
			// possibility of user session crosstalk
			BLACKBOARD.set(null);
			currentApplication.set(null);
			currentApplication.remove();
		}
	}

	/**
	 * Method supplies Blackboard instance to the entire Application
	 * 
	 * @return Blackboard Instance for the application
	 */
	public static Blackboard getBlackboard() {
		return BLACKBOARD.get();
	}

	/**
	 * All the Events in geWorkbench Application are strictly registered here.
	 */
	private void registerAllEventsForApplication() {

		getBlackboard().register(AnalysisSubmissionEventListener.class, AnalysisSubmissionEvent.class);
	}

	// TODO verify when .get() returns null and code accordingly to be explicit
	public static PluginRegistry getPluginRegistry() {
		PluginRegistry pr = pluginRegistry.get();
		if (pr == null) {
			pr = new PluginRegistry();
			pr.init();
			pluginRegistry.set(pr);
		}
		return pr;
	}

	@Override
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
		if (request != null) {
			String requestURL = request.getRequestURL().toString();
			if (requestURL.endsWith("geworkbench")) {
				try {
					// bug fix #3264
					response.sendRedirect(requestURL + "/");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
	}

	/* it is a little better than passing over a private member directly */
	public static String getAppProperty(String serviceUrlProperty) {
		return prop.getProperty(serviceUrlProperty);
	}

	public static String getBackendDataDirectory() {
		return System.getProperty("user.home")
				+ System.getProperty("file.separator")
				+ prop.getProperty("data.directory");
	}

	public static String getPublicAnnotationDirectory() {
		return System.getProperty("user.home")
				+ System.getProperty("file.separator")
				+ prop.getProperty("public.annotation.directory");
	}

	public void addNode(Object node) {
		Window w = getMainWindow();
		ComponentContainer content = w.getContent();
		if (content instanceof UMainLayout) {
			((UMainLayout) content).addNode(node);
		} else {
			log.error("main container content is an incorrect type " + content);
		}
	}

	/* create new layout and make sure the old listener is removed first */
	public void createNewMainLayout() {
		Window mainWindow = getMainWindow();
		if (mainWindow == null) {
			log.error("null main winwdow");
			return;
		}
		ComponentContainer content = mainWindow.getContent();
		if (content instanceof UMainLayout) {
			UMainLayout mainLayout = (UMainLayout) content;
			boolean removed = GeworkbenchRoot.getBlackboard().removeListener(
					mainLayout.getAnalysisListener());
			log.debug("analysis listener found and removed? " + removed);
		} else {
			log.error("main window content is not UMainLayout");
		}
		try {
			mainWindow.setContent(new UMainLayout());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
