package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.CWFListener;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

public class RealTimeWorkflowSuggestion extends AbstractGenspaceTab implements GenSpaceTab, CWFListener, Button.ClickListener {

	private static final long serialVersionUID = 4806046453151557609L;
	
	private final static String NAME = "Name";

	private final static String ORDER = "Order";

	public WorkflowWrapper cwf  = null;

	public ArrayList<WorkflowWrapper> usedWorkFlowToday = new ArrayList<WorkflowWrapper>();

	private WorkflowVisualizationPanel workflowVisualizationPanel = new WorkflowVisualizationPanel();

	private BorderLayout workflowViewerPanel = new BorderLayout();

	private Label viewerStatus = new Label();

	private TextField infoArea = new TextField();
	
	private IndexedContainer model = new IndexedContainer();
	
	private List<Tool> results;
	
	private ListSelect toolListing = new ListSelect();
	
	private Button button = new Button("Search");

	private TextField wfsPane;

	private int preference; // the logging preference

	private VerticalLayout splitter = new VerticalLayout();
	
	private Panel p = new Panel();
	
	
	public RealTimeWorkflowSuggestion(GenSpaceLogin_1 login) {
		super(login);
		model.addContainerProperty(NAME, String.class, null);
		model.addContainerProperty(ORDER, Integer.class, null);
		toolListing.setContainerDataSource(model);
		toolListing.setMultiSelect(true);
		buildMainLayout();
		setCompositionRoot(splitter);
	}

	private AbstractLayout buildMainLayout() {
		/**
		 * setup general layout of panel
		 * */
		// setup viewer status
		viewerStatus.setValue("No analysis has occured yet.");
		workflowViewerPanel.addComponent(viewerStatus, BorderLayout.Constraint.NORTH);
		workflowViewerPanel.addComponent(workflowVisualizationPanel, BorderLayout.Constraint.CENTER);
		workflowVisualizationPanel.setHeight("60px");
		workflowVisualizationPanel.setGenSpaceLogin(login);

		// the info panel
		GridLayout suggestionsPanel = new GridLayout(2, 1);
		suggestionsPanel.setSizeFull();
		suggestionsPanel.setSpacing(true);
		BorderLayout toolListPanel = new BorderLayout();

		Label label1 = new Label("Advanced suggestions");
		label1.setStyleName(Reindeer.LABEL_H2);
		Label label2 = new Label("Get suggestions for people who use these tools:");
		
		GridLayout headerPanel = new GridLayout(1,2);
		VerticalLayout resultsPanel = new VerticalLayout();
		resultsPanel.setHeight("250px");
		wfsPane = new TextField();
		wfsPane.setValue("No results yet");
		wfsPane.setWidth("100%");
		wfsPane.setHeight("200px");
		
		Label resultsLabel = new Label("Results:");
		HorizontalLayout buttonPanel = new HorizontalLayout();
		
		headerPanel.addComponent(label1);
		headerPanel.setComponentAlignment(label1, Alignment.MIDDLE_CENTER);
		headerPanel.addComponent(label2);
		headerPanel.setComponentAlignment(label2, Alignment.MIDDLE_CENTER);
		
		resultsPanel.addComponent(resultsLabel);
		resultsPanel.addComponent(wfsPane);

		buttonPanel.addComponent(button);
		
		updateAllToolList();
		
		toolListing.setSizeFull();
		HorizontalLayout toolPanel = new HorizontalLayout();
		toolPanel.setSpacing(true);
		toolPanel.setWidth("100%");
		toolPanel.addComponent(toolListing);
		toolPanel.setComponentAlignment(toolListing, Alignment.MIDDLE_CENTER);
		toolPanel.addComponent(buttonPanel);
		toolPanel.setComponentAlignment(buttonPanel, Alignment.MIDDLE_CENTER);
		
		toolListPanel.setSpacing(true);
		toolListPanel.addComponent(headerPanel, BorderLayout.Constraint.NORTH);
		toolListPanel.addComponent(toolPanel, BorderLayout.Constraint.CENTER);
		toolListPanel.addComponent(resultsPanel, BorderLayout.Constraint.SOUTH);

		button.addListener(new Button.ClickListener(){
			
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event){
				wfsPane.removeAllValidators();
				List<Tool> toolList = new ArrayList<Tool>();
				Iterator toolListingIT = toolListing.getItemIds().iterator();
								
				//List<Tool> results = login.getGenSpaceServerFactory().getUsageOps().getAllTools();
				while(toolListingIT.hasNext()){
					Object itemID = (Object)toolListingIT.next();
					Item tmpItem;
					int tmpOrder;
					Tool tmpTool;
					if(toolListing.isSelected(itemID)){
						tmpItem = model.getItem(itemID);
						tmpOrder = (Integer)tmpItem.getItemProperty(ORDER).getValue();
						tmpTool = results.get(tmpOrder);
						toolList.add(tmpTool);
						//System.out.println("DEBUG Tool selected: " + tmpTool.getName() + " " + tmpTool.getId());
					}
				}
				
				if (toolList.size() == 0) {
					wfsPane.setValue("No tool is selected");
					return ;
				}
				
				/*String criteriaString = "(";
				List<Integer> toolsIds = new ArrayList<Integer>();
				for (Tool t : toolList) {
					toolsIds.add(t.getId());
					criteriaString += "wt.tool_id=" + t.getId() + " or ";
				}
				criteriaString = criteriaString.substring(0, criteriaString.length()-4);
				criteriaString += ") ";
				System.out.println("DEBUG CriteriaString: " + criteriaString);*/
				
				List<Workflow> workFlowList;
				if(toolList.size() > 0){
					workFlowList = login.getGenSpaceServerFactory().getUsageOps().getMahoutSimilarWorkflowsSuggestion(toolList);
					// System.out.println("Suggested workflow list: " + workFlowList.size());
					
					if (workFlowList == null || workFlowList.size() == 0) {
						wfsPane.setValue("No Workflow found");
					} else {
						displayWorkflow(workFlowList);
					}
				}
			}
			
			public void displayWorkflow(List<Workflow> workFlowList){
				int lim = 10;
				int wfCounter = 1;
				String wfsString = "";
				WorkflowWrapper wfw;
				for(Workflow tmp: workFlowList){
					wfw = new WorkflowWrapper(tmp);
					wfw.loadToolsFromCache();
					wfsString = wfsString + wfCounter++ + " " + wfw.toString() + "\n";
					lim--;
					if (lim <= 0)
						break;
				}
				wfsPane.setValue(wfsString);
			}
		});
		
		String string = "You haven't used any tools!\n";
		string += "Next best rated tool to use: none.";
		infoArea.setValue(string);
		infoArea.setSizeFull();
		infoArea.setHeight("500px");
		

		toolListPanel.setSizeFull();
		suggestionsPanel.addComponent(infoArea);
		suggestionsPanel.addComponent(toolListPanel);
//		toolListPanel.setExpandRatio(resultsPanel, 1.0f);
		
		// add both panels
		//splitter.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);  
		
		Panel leftPanel = new Panel();
		leftPanel.setHeight("500px");
		leftPanel.setScrollable(true);
		leftPanel.addComponent(workflowViewerPanel);
		
		Panel rightPanel = new Panel();
		rightPanel.setHeight("500px");
		rightPanel.setScrollable(true);
		rightPanel.addComponent(suggestionsPanel);
		
		//splitter.addComponent(leftPanel);     
		//splitter.addComponent(rightPanel);    
		splitter.setSpacing(true);
		splitter.addComponent(workflowViewerPanel);
		splitter.addComponent(suggestionsPanel);
		/*splitter.addComponent(workflowViewerPanel);

		splitter.addComponent(suggestionsPanel);*/

		
		toolPanel.setExpandRatio(toolListing, 1.0f);
		
		Workflow curWorkFlow = this.login.getGenSpaceParent().getLogger().
								getObjectLogger().getCurWorkflow();
		if (curWorkFlow != null ) {
			cwfUpdated(curWorkFlow);
		}
		return splitter;
	}
	
	private void updateAllToolList() {
		toolListing.removeAllItems();
		results = login.getGenSpaceServerFactory().getUsageOps().getAllTools();
		model.removeAllItems();
		String toolName;
		for (int i = 0; i < results.size(); i++) {
			toolName = results.get(i).getName();
			
			if (toolName == null || toolName.isEmpty()) {
				toolName = "";
			}
			Item item = this.model.addItem(toolName);
			item.getItemProperty(NAME).setValue(toolName);
			item.getItemProperty(ORDER).setValue(i);
		}
	}


	public void displayCWF() {
		// -----------------show finished work flows today
		String finishedWF = "";
		for (int i = 0; i < usedWorkFlowToday.size(); i++) {
			

			finishedWF = finishedWF + usedWorkFlowToday.get(i)
					+ "\n";

		}
		if (usedWorkFlowToday.size() == 0) {
			finishedWF = "No finished workflows!";
		}

		List<Workflow> suggestions = getRealTimeWorkFlowSuggestion(cwf);
		String nextSteps = "";
		Tool nextBestRated = null;

		if(suggestions != null)
		{
			int curIndexIntoTools = cwf.getTools().size();
			
			HashMap<Tool, Integer> toolRatings = new HashMap<Tool, Integer>();
			for(Workflow wa : suggestions)
			{
				WorkflowWrapper w = new WorkflowWrapper(wa);
				w.loadToolsFromCache();
				if(curIndexIntoTools < w.getTools().size())
				{
					Tool t = w.getTools().get(curIndexIntoTools).getTool();
					if(toolRatings.containsKey(t))
						toolRatings.put(t, toolRatings.get(t) + w.getUsageCount());
					else
						toolRatings.put(t, w.getUsageCount());
				}
				for(int i = curIndexIntoTools; i < w.getTools().size(); i++)
				{
					nextSteps += w.getTools().get(i).getTool().getName() + ", ";
				}
				if(nextSteps.length() > 2)
					nextSteps = nextSteps.substring(0,nextSteps.length()-2) + "\n";
			}
			
			int bestRating = 0;
			for(Tool t : toolRatings.keySet())
			{
				if(toolRatings.get(t) > bestRating)
				{
					bestRating = toolRatings.get(t);
					nextBestRated = t;
				}
			}
		}

		StringBuilder stringBuilder = new StringBuilder(); 

		stringBuilder.append("Your current workflow: \n");
		stringBuilder.append(cwf + "\n\n");

		stringBuilder.append("Suggestions for your next steps: "
				+ "\n");
		stringBuilder.append("-----------------------------------------------------------------------\n\n");
		stringBuilder.append("Current workflow usage:" + "\n");
		stringBuilder.append("Your current work flow has been used " + cwf.getUsageCount()
				+ " times by genSpace users." + "\n\n");
		stringBuilder.append("Next steps:" + "\n");
		stringBuilder.append(nextSteps + "\n\n");
		
		if (nextBestRated != null)
			stringBuilder.append("Next best rated tool to use: " + nextBestRated.getName()
					+ ".\n\n");
		
		infoArea.setValue(stringBuilder.toString());
	}

	private boolean emptyPanel = true;
	
	@Override
	public void cwfUpdated(Workflow newCWF) {
		List<WorkflowWrapper> ret = new ArrayList<WorkflowWrapper>();
		cwf = new WorkflowWrapper(newCWF);
		ret.add(cwf);
		workflowVisualizationPanel.render(ret);
		login.currentWorkflow = new WorkflowWrapper(newCWF);
		displayCWF();
		this.viewerStatus.setValue("Your current analysis: " + cwf.getLastTool().getName());
	}

	private List<Workflow> getRealTimeWorkFlowSuggestion(WorkflowWrapper cwf) {
		
		try {
			return (login.getGenSpaceServerFactory().getUsageOps().getToolSuggestion(cwf.getId()));
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public void buttonClick(Button.ClickEvent evt) {
		
	}

	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub
		
	}
	

	/*
	 * 
	 * 
	 * public void actionPerformed(ActionEvent e) { if (e.getSource() == save) {
	 * //System.out.println("Save pressed with " +
	 * group.getSelection().getActionCommand()); preference =
	 * Integer.parseInt(group.getSelection().getActionCommand());
	 * ObjectHandler.setLogStatus(preference); save.setEnabled(false); // write
	 * it to the properties file try { PropertiesManager properties =
	 * PropertiesManager.getInstance();
	 * properties.setProperty(GenSpaceLogPreferences.class, PROPERTY_KEY,
	 * group.getSelection().getActionCommand()); } catch (Exception ex) { }
	 * 
	 * } else if (e.getSource() == reset) {
	 * //System.out.println("Reset pressed"); logAnon.setSelected(true);
	 * save.setEnabled(true); } else if (e.getSource() == log) { if (preference
	 * == 0) { save.setEnabled(false); } else { save.setEnabled(true); } } else
	 * if (e.getSource() == logAnon) { if (preference == 1) {
	 * save.setEnabled(false); } else { save.setEnabled(true); } } else if
	 * (e.getSource() == noLog) { if (preference == 2) { save.setEnabled(false);
	 * } else { save.setEnabled(true); } } }
	 */

}

