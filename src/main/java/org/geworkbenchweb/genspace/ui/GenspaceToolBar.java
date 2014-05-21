package org.geworkbenchweb.genspace.ui;

import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.genspace.ObjectLogger;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.ui.component.RealTimeWorkflowSuggestion;
import org.geworkbenchweb.genspace.ui.component.SocialNetworkHome;
import org.geworkbenchweb.genspace.ui.component.WorkflowStatistics_0;
import org.geworkbenchweb.genspace.ui.component.WorkflowStatistics_1;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualization;
import org.geworkbenchweb.genspace.ui.component.notebook.NotebookPanel;
import org.geworkbenchweb.genspace.ui.component.workflowRepository.WorkflowRepository;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.Application;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

public class GenspaceToolBar extends MenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final GenSpacePluginView pluginView;
	//private GenSpaceLogin_1 genspaceLogin;
	private WorkflowVisualization workflowVisualization;
	private RealTimeWorkflowSuggestion realTimeWorkflowSuggestion;
	private WorkflowStatistics_1 workflowStatistics;
	private NotebookPanel notebookPanel;
	private WorkflowRepository workflowRepository;
	private SocialNetworkHome socialNetworkHome;
	private GenSpaceLogin_1 genSpaceLogin;
	private GenspaceLayout genspaceLayout;
	private ICEPush pusher;
	private GenspaceLogger genspaceLogger;


	public GenspaceToolBar(final GenSpacePluginView pluginView){
		this.pluginView = pluginView;

		this.addItem("Home", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				GenspaceToolBar.this.pluginView.setContent(genSpaceLogin, "Home", "Please use this interface to login.", genSpaceLogin);
			}
			
		});
		
		this.addItem("Workflow Visualization", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				GenspaceToolBar.this.pluginView.setContent(workflowVisualization, "Workflow Visualization", "Please select an action and a tool to search for.", genSpaceLogin);
			}
			
		});
		
		this.addItem("Real Time Workflow Suggestion", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				GenspaceToolBar.this.pluginView.setContent(realTimeWorkflowSuggestion, "Real Time Workflow Suggestion", "Please use this interface to get suggestions.", genSpaceLogin);
			}
			
		});
		
		this.addItem("Workflow Statistics", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				GenspaceToolBar.this.pluginView.setContent(workflowStatistics, "Workflow Statistics", "Select a tool to see its statistics.", genSpaceLogin);
			}
			
		});
		
		this.addItem("Research Notebook", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				GenspaceToolBar.this.pluginView.setContent(notebookPanel, "Research Notebook", "Enter your search query or use the dropdown.", genSpaceLogin);
			}
			
		});
		
		this.addItem("Workflow Repository", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				GenspaceToolBar.this.pluginView.setContent(workflowRepository, "Workflow Repository", "...", genSpaceLogin);
			}
			
		});
		
		this.addItem("Communicator", new Command(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				GenspaceToolBar.this.pluginView.setContent(socialNetworkHome, "Communicator", "Please use this interface to communicate with others.", genSpaceLogin);
			}
			
		});

	}
	
	public void setGenSpaceLogger(GenspaceLogger genSpaceLogger) {
		this.genspaceLogger = genSpaceLogger;
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login) {
		this.genSpaceLogin = login;
	}
	
	public void setWorkflowVisual(WorkflowVisualization workflowVisualization){
		this.workflowVisualization = workflowVisualization;
	}
	
	public void setRealtimeWorkflowSuggestion(RealTimeWorkflowSuggestion realtimeWorkflowSuggestion){
		this.realTimeWorkflowSuggestion = realtimeWorkflowSuggestion;
	}
	
	public void setWorkflowStatistics(WorkflowStatistics_1 workflowStatistics){
		this.workflowStatistics = workflowStatistics;
	}
	
	public void setNotebookPanel(NotebookPanel notebookPanel){
		this.notebookPanel = notebookPanel;
	}
	
	public void setWorkflowRepository(WorkflowRepository workflowRepository){
		this.workflowRepository = workflowRepository;
	}
	
	public void setSocialNetworkHome(SocialNetworkHome socialNetworkHome){
		this.socialNetworkHome = socialNetworkHome;
	}
	
	public void fireLoggedIn()
	{
		genSpaceLogin.loggedIn();
		realTimeWorkflowSuggestion.loggedIn();
		workflowStatistics.loggedIn();
		workflowVisualization.loggedIn();
		notebookPanel.loggedIn();
		workflowRepository.loggedIn();
		socialNetworkHome.loggedIn();
		
		genspaceLogger.setGenSpaceLogin(genSpaceLogin);
		ObjectLogger objectLogger = genspaceLogger.getObjectLogger();
		objectLogger.addCWFListener(realTimeWorkflowSuggestion);
		objectLogger.addNotebookDataListener(notebookPanel);
		
		GenspaceToolBar.this.pluginView.setContent(genSpaceLogin, "Home", "Please use this interface to login.", genSpaceLogin);
		
	}
	
	public void fireLoggedOut() {
		
		genSpaceLogin.loggedOut();
		realTimeWorkflowSuggestion.loggedOut();
		workflowStatistics.loggedOut();
		workflowVisualization.loggedOut();		
		notebookPanel.loggedOut();
		workflowRepository.loggedOut();
		socialNetworkHome.loggedOut();
		GenspaceToolBar.this.pluginView.setContent(genSpaceLogin, "Login", "Please use this interface to login.", genSpaceLogin);
		GenspaceToolBar.this.pluginView.clearAf();
		GenspaceToolBar.this.pluginView.clearChat();
	} 
	
	public void setPusher(ICEPush pusher) {
		this.pusher = pusher;  
	}
	
}
