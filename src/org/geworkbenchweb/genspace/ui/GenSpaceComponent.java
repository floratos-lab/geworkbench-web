package org.geworkbenchweb.genspace.ui;


import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.genspace.ObjectLogger;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.ui.component.GenSpaceTab;
import org.geworkbenchweb.genspace.ui.component.RealTimeWorkflowSuggestion;
import org.geworkbenchweb.genspace.ui.component.SocialNetworkHome;
import org.geworkbenchweb.genspace.ui.component.WorkflowStatistics;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualization;
import org.geworkbenchweb.genspace.ui.component.notebook.NotebookPanel;
import org.geworkbenchweb.genspace.ui.component.workflowRepository.WorkflowRepository;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class GenSpaceComponent extends CustomComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2313724995641020659L;
	@AutoGenerated
	private AbsoluteLayout mainLayout;
	@AutoGenerated
	private TabSheet tabSheet_1;
	@AutoGenerated
	private WorkflowStatistics workflowStatistics_1;
	@AutoGenerated
	private RealTimeWorkflowSuggestion realTimeWorkflowSuggestion_1;
	@AutoGenerated
	private WorkflowVisualization workflowVisualization_1;
	@AutoGenerated
	private GenSpaceLogin genSpaceLogin_1;
	@AutoGenerated
	private NotebookPanel notebookPanel;
	@AutoGenerated
	private WorkflowRepository workflowRepository;
	private SocialNetworkHome socialNetworkHome;

	
	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	private GenspaceLogger genSpaceLogger;
		
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public GenSpaceComponent(GenspaceLogger genspaceLogger) {
		this.genSpaceLogger = genspaceLogger;
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// TODO add user code here
		tabSheet_1.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 196426300082517308L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				((GenSpaceTab)tabSheet_1.getSelectedTab()).tabSelected();
			}
		});
	}
		
	public void fireLoggedIn()
	{
		genSpaceLogin_1.loggedIn();
		realTimeWorkflowSuggestion_1.loggedIn();
		workflowStatistics_1.loggedIn();
		workflowVisualization_1.loggedIn();
		notebookPanel.loggedIn();
		workflowRepository.loggedIn();
		socialNetworkHome.loggedIn();

		genSpaceLogger.setGenSpaceLogin(genSpaceLogin_1);
		ObjectLogger objectLogger = genSpaceLogger.getObjectLogger();
		objectLogger.addCWFListener(realTimeWorkflowSuggestion_1);
		objectLogger.addNotebookDataListener(notebookPanel);
//		objectLogger.setGenSpaceLogin(genSpaceLogin_1);		
//		tabSheet_1.getTab(genSpaceLogin_1).setEnabled(false);
	}
	public void fireLoggedOut()
	{
		genSpaceLogin_1.loggedOut();
		realTimeWorkflowSuggestion_1.loggedOut();
		workflowStatistics_1.loggedOut();
		workflowVisualization_1.loggedOut();		
		notebookPanel.loggedOut();
		workflowRepository.loggedOut();
		socialNetworkHome.loggedOut();
		
		tabSheet_1.getTab(genSpaceLogin_1).setEnabled(true);
		tabSheet_1.setSelectedTab(genSpaceLogin_1);
	}
	@AutoGenerated
	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		//mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// tabSheet_1
		tabSheet_1 = buildTabSheet_1();
		mainLayout.addComponent(tabSheet_1, "top:0.0px;right:9.0px;bottom:0.0px;left:0.0px;");
		
		return mainLayout;
	}

	@AutoGenerated
	private TabSheet buildTabSheet_1() {
		// common part: create layout
		tabSheet_1 = new TabSheet();
		tabSheet_1.setImmediate(true);
		tabSheet_1.setWidth("100.0%");
		tabSheet_1.setHeight("100.0%");
		
		// genSpaceLogin_1
		genSpaceLogin_1 = new GenSpaceLogin(this);
		genSpaceLogin_1.setImmediate(false);
		genSpaceLogin_1.setWidth("100.0%");
		genSpaceLogin_1.setHeight("100.0%");
		tabSheet_1.addTab(genSpaceLogin_1, "Login", null);
		
		// workflowVisualization_1
		workflowVisualization_1 = new WorkflowVisualization(genSpaceLogin_1);
		workflowVisualization_1.setImmediate(false);
		workflowVisualization_1.setWidth("100.0%");
		//workflowVisualization_1.setHeight("100.0%");
		tabSheet_1.addTab(workflowVisualization_1, "Workflow Visualization", null);
		
		// realTimeWorkflowSuggestion_1
		realTimeWorkflowSuggestion_1 = new RealTimeWorkflowSuggestion(genSpaceLogin_1);
		realTimeWorkflowSuggestion_1.setImmediate(false);
		realTimeWorkflowSuggestion_1.setWidth("100.0%");
		realTimeWorkflowSuggestion_1.setHeight("100.0%");
		tabSheet_1.addTab(realTimeWorkflowSuggestion_1, "Real Time Workflow Suggestion", null);
		
		// workflowStatistics_1
		workflowStatistics_1 = new WorkflowStatistics(genSpaceLogin_1);
		workflowStatistics_1.setImmediate(false);
		workflowStatistics_1.setWidth("100.0%");
		//workflowStatistics_1.setHeight("100.0%");
		tabSheet_1.addTab(workflowStatistics_1, "Workflow Statistics", null);
				
		notebookPanel = new NotebookPanel(genSpaceLogin_1);
		notebookPanel.setImmediate(false);
		notebookPanel.setWidth("100%");
		notebookPanel.setHeight("100%");
		tabSheet_1.addTab(notebookPanel, "Research Notebook", null);
		
		workflowRepository = new WorkflowRepository(genSpaceLogin_1);
		workflowRepository.setImmediate(false);
		workflowRepository.setWidth("100%");
		workflowRepository.setHeight("100%");
		tabSheet_1.addTab(workflowRepository, "Workflow Repository", null);
		
		socialNetworkHome = new SocialNetworkHome(genSpaceLogin_1);
		socialNetworkHome.setImmediate(false);
		socialNetworkHome.setWidth("100.0%");
		//socialNetworkHome.setHeight("100.0%");
		GenSpaceWindow.getGenSpaceBlackboard().addListener(socialNetworkHome);
		tabSheet_1.addTab(socialNetworkHome, "Communicator", null);
		
		return tabSheet_1;
	}
	
	public WorkflowRepository getWorkflowRepository() {
		return this.workflowRepository;
	}
	
	public SocialNetworkHome getSocialNetworkHome() {
		return this.socialNetworkHome;
	}

}
