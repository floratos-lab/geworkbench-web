package org.geworkbenchweb;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
import org.vaadin.artur.icepush.ICEPush;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * This is the application entry point.
 */
@Theme("geworkbench")
public class GeworkbenchRoot extends UI {

	private static final long serialVersionUID = 6853924772669700361L;
	private static Log log = LogFactory.getLog(GeworkbenchRoot.class);

	private ICEPush pusher = new ICEPush();
	public void push() {
		pusher.push();
	}

	private static final PluginRegistry pluginRegistry = new PluginRegistry();
	static {
		pluginRegistry.init();
	}

	private final Blackboard blackboardInstance = new Blackboard();

	private static final String PROPERTIES_FILE = "application.properties";

	private static Properties prop = new Properties();

	@Override
	public void init(VaadinRequest request) {
		if (FacadeFactory.getFacade() == null) {
			try {
				FacadeFactory.registerFacade("default", true);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		pusher.extend(this);

		try {
			prop.load(getClass().getResourceAsStream(
					"/" + PROPERTIES_FILE));
		} catch (IOException e) {
			setContent(new Label("failed to read application properties file " + PROPERTIES_FILE));
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
			setContent(new Label(
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
			setContent(new Label("No database is set up to support this application: " + e.getMessage()));
			e.printStackTrace();
			return;
		}

		SessionHandler.initialize(this);

		registerAllEventsForApplication();

		User user = SessionHandler.get();
		if (user != null) {
			try {
				UMainLayout uMainLayout = new UMainLayout();
				blackboardInstance.addListener(uMainLayout.getAnalysisListener());
				setContent(uMainLayout);
			} catch (Exception e) {
				setContent(new UUserAuth());
			}
		} else {
			UUserAuth auth = new UUserAuth();
			setContent(auth);
		}

		GeneOntologyTree.getInstance();
	}

	public Blackboard getBlackboard() {
		return blackboardInstance;
	}

	/**
	 * All the Events in geWorkbench Application are strictly registered here.
	 */
	private void registerAllEventsForApplication() {
		blackboardInstance.register(AnalysisSubmissionEventListener.class, AnalysisSubmissionEvent.class);
	}

	public static PluginRegistry getPluginRegistry() {
		if (pluginRegistry == null) {
			log.error("PluginRegistry not initialized properly");
		}
		return pluginRegistry;
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
		Component content = getContent();
		if (content instanceof UMainLayout) {
			((UMainLayout) content).addNode(node);
		} else {
			log.error("main container content is an incorrect type " + content);
		}
	}

	/* create new layout and make sure the old listener is removed first */
	public void createNewMainLayout() {
		Component content = getContent();
		if (content instanceof UMainLayout) {
			UMainLayout mainLayout = (UMainLayout) content;
			boolean removed = blackboardInstance.removeListener(
					mainLayout.getAnalysisListener());
			log.debug("analysis listener found and removed? " + removed);
		} else {
			log.error("main window content is not UMainLayout");
		}
		try {
			UMainLayout uMainLayout = new UMainLayout();
			blackboardInstance.addListener(uMainLayout.getAnalysisListener());
			setContent(uMainLayout);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
