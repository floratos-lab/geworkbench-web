package org.geworkbenchweb.genspace.ui;

import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.ui.component.RealTimeWorkflowSuggestion;
import org.geworkbenchweb.genspace.ui.component.SocialNetworkHome;
import org.geworkbenchweb.genspace.ui.component.WorkflowStatistics_1;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualization;
import org.geworkbenchweb.genspace.ui.component.notebook.NotebookPanel;
import org.geworkbenchweb.genspace.ui.component.workflowRepository.WorkflowRepository;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class GenspaceLayout extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7869811260105248499L;
	/**
	 * 
	 */
	final private GenspaceLogger genspaceLogger;
	final private GenSpacePluginView pluginView = new GenSpacePluginView();
	final private GenspaceToolBar genspaceToolBar 	= 	new GenspaceToolBar(pluginView);
	private GenSpaceLogin_1 genspaceLogin;
	private WorkflowVisualization workflowVisualization;
	private RealTimeWorkflowSuggestion realTimeWorkflowSuggestion;
	private WorkflowStatistics_1 workflowStatistics;
	private NotebookPanel notebookPanel;
	private WorkflowRepository workflowRepository;
	private SocialNetworkHome socialNetworkHome;
	private GenspaceLayout genspaceLayout;
	private ICEPush pusher;
	
	public ICEPush getPusher()
	{
		if (this.pusher == null) {
			this.pusher = new ICEPush();
			this.addComponent(this.pusher);
			
			//System.out.println("Layout pusher is null");
		} else if (this.pusher.getApplication() == null) {
			this.addComponent(this.pusher);
			
			//System.out.println("Layout pusher is not attached");
		}
		
		return this.pusher;
	}
	
	public GenspaceLayout(GenspaceLogger genspaceLogger, ICEPush pusher){	
		
		this.genspaceLogger = genspaceLogger;
		this.pusher = pusher;
		this.addComponent(this.pusher);
		//this.addComponent(new Panel("Test 123"));
		
		this.genspaceToolBar.setPusher(this.pusher);
		this.genspaceToolBar.setGenSpaceLogger(this.genspaceLogger);
		HorizontalLayout p = new HorizontalLayout();
		p.setHeight("24px");
		p.setWidth("100%");
		p.setStyleName("menubar");
		p.setSpacing(false);
		p.setMargin(false);
		p.setImmediate(true);
		p.addComponent(genspaceToolBar);
		genspaceToolBar.setStyleName("transparent");
		//setExpandRatio(genspaceToolBar, 1);
		//setComponentAlignment(genspaceToolBar, Alignment.TOP_CENTER);
		this.addComponent(p);
		// this.genspaceLogin = new GenSpaceLogin_1(this);
		GenSpaceLogin_1 login = this.genspaceLogger.getGenSpaceLogin();
		login.resetParent(this);
		this.genspaceLogin = login;
		//this.addComponent(genspaceLogin);
		//this.addComponent(new Panel("1234"));
		//pluginView.setContent(genspaceLogin, "Home", "Please use this interface to login.", genspaceLogin);
		this.addComponent(pluginView);
		//System.out.println(pluginView.toString());
		//System.out.println("Check login in GenSpaceLayoutout: " + this.genspaceLogin);
		workflowVisualization = new WorkflowVisualization(genspaceLogin);
		realTimeWorkflowSuggestion = new RealTimeWorkflowSuggestion(genspaceLogin);
		workflowStatistics = new WorkflowStatistics_1(genspaceLogin);
		notebookPanel = new NotebookPanel(genspaceLogin);
		workflowRepository = new WorkflowRepository(genspaceLogin);
		socialNetworkHome = new SocialNetworkHome(genspaceLogin);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(socialNetworkHome);
		this.genspaceToolBar.setGenSpaceLogin(this.genspaceLogin);
		this.genspaceToolBar.setWorkflowVisual(this.workflowVisualization);
		this.genspaceToolBar.setRealtimeWorkflowSuggestion(this.realTimeWorkflowSuggestion);
		this.genspaceToolBar.setWorkflowStatistics(this.workflowStatistics);
		this.genspaceToolBar.setNotebookPanel(this.notebookPanel);
		this.genspaceToolBar.setWorkflowRepository(this.workflowRepository);
		this.genspaceToolBar.setSocialNetworkHome(this.socialNetworkHome);
	}
	
	public void fireLoggedIn(){
		genspaceToolBar.fireLoggedIn();
	}
	
	public void fireLoggedOut(){
		genspaceToolBar.fireLoggedOut();
	}
	public WorkflowRepository getWorkflowRepository(){
		return this.workflowRepository;
	}
	public SocialNetworkHome getSocialNetworkHome(){
		return this.socialNetworkHome;
	} 
	
	public GenSpaceLogin_1 getGenSpaceLogin_1() {
		return this.genspaceLogin;
	}
	
	
	public GenspaceLogger getLogger() {
		return this.genspaceLogger;
	}
}
