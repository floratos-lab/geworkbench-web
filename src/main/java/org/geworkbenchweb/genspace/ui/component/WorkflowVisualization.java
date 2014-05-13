package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.ui.GenSpacePluginView;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class WorkflowVisualization extends AbstractGenspaceTab implements
		GenSpaceTab, ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1472121123400742285L;

	private Log log = LogFactory.getLog(this.getClass());
	//private NativeSelect tools = new NativeSelect();
	private HashMap<String, Tool> toolsMap = new HashMap<String, Tool>();
	//private NativeSelect actions = new NativeSelect();
	private ComboBox actions;
	private ComboBox tools;
	private Button button = new Button("Search");
	private Label label = new Label();
	private VerticalLayout selectPanel = new VerticalLayout();
	private HorizontalLayout select = new HorizontalLayout();
	private HorizontalLayout search = new HorizontalLayout();
	private VerticalLayout left = new VerticalLayout();
	private VerticalLayout right = new VerticalLayout();
	private WorkflowVisualizationPanel vis = new WorkflowVisualizationPanel();
	private Panel borderLayout = new Panel();
	private VerticalLayout result = new VerticalLayout();
	//private Panel selectRootPanel = new Panel();
	private GenSpaceLogin_1 login;

	public WorkflowVisualization(GenSpaceLogin_1 login) {
		super(login);
		this.login = login;
		//selectRootPanel.setScrollable(true);
		borderLayout.setScrollable(true);
		vis.setScrollable(true);
		vis.setIsWindow(false);
		initComponents();
		setCompositionRoot(borderLayout);
	}

	private void initComponents() {
		actions = new ComboBox("-- select action --");
		//actions.setCaption("-- select action --"); 
		actions.addItem("Most common workflow starting with");
		actions.addItem("Most common workflow including");
		actions.addItem("All workflows including");
		actions.setNullSelectionAllowed(false);
		//System.out.println("Check login in workflow visualization: " + this.login);

		tools = new ComboBox("-- select tool --");
		//tools.setCaption("-- select tool --");
		for (Tool tool : this.login.getGenSpaceServerFactory().getUsageOps().getAllTools()) {
			String name = tool.getName();
			toolsMap.put(name, tool);
			tools.addItem(name);
		}
		tools.setNullSelectionAllowed(false);
		button.addListener(this); 
		//label.setValue("Please select an action and a tool to search for");

		//selectPanel.setSpacing(true);
		select.addComponent(actions);
		select.addComponent(tools);
		select.setSizeFull();
		selectPanel.addComponent(select);
		search.addComponent(button);
		select.setComponentAlignment(actions, Alignment.BOTTOM_LEFT);
		select.setComponentAlignment(tools, Alignment.BOTTOM_LEFT);
		//left.setComponentAlignment(button, Alignment.MIDDLE_LEFT);
		//right.addComponent(button);
		search.addComponent(label);
		search.setSizeFull();
		//right.setComponentAlignment(button, Alignment.MIDDLE_LEFT);
		label.setStyleName(Reindeer.LABEL_H2);
		label.setContentMode(Label.CONTENT_PREFORMATTED);
		selectPanel.addComponent(search);
		search.setComponentAlignment(button, Alignment.BOTTOM_LEFT);
		search.setComponentAlignment(label, Alignment.BOTTOM_LEFT);

		
		//selectPanel.setComponentAlignment(select, Alignment.BOTTOM_CENTER);
		//selectPanel.setComponentAlignment(search, Alignment.BOTTOM_CENTER);
		
		//selectRootPanel.addComponent(selectPanel);

		/*borderLayout.addComponent(selectPanel, BorderLayout.Constraint.NORTH);
		borderLayout.addComponent(vis, BorderLayout.Constraint.CENTER);*/
		//borderLayout.addComponent(selectPanel);
		borderLayout.addComponent(selectPanel);
		borderLayout.addComponent(result);
		selectPanel.setSpacing(true);
		result.setSpacing(true);
		result.addComponent(vis);
		vis.setGenSpaceLogin(login);
		result.setExpandRatio(vis, 1);
		vis.setStyleName(Reindeer.PANEL_LIGHT);
		//vis.setSizeFull();
		
		
	}

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void buttonClick(ClickEvent event) {
		// get the name of the selected tool and the action
		if (tools.getValue() == null || actions.getValue() == null) {
			return;
		}
		String tool = (String) tools.getValue();
		String action = (String) actions.getValue();
		List<Workflow> reta = null;
		if (action.equals("All workflows including")) {
			 reta = login.getGenSpaceServerFactory().getUsageOps().getAllWorkflowsIncluding(
					toolsMap.get(tool).getId());
		}
		else if (action.equals("Most common workflow starting with")) {
			reta = login.getGenSpaceServerFactory().getUsageOps()
					.getMostPopularWorkflowStartingWith(toolsMap.get(tool).getId());
		}
		else if (action.equals("Most common workflow including")) {
			reta = login.getGenSpaceServerFactory().getUsageOps()
					.getMostPopularWorkflowIncluding(toolsMap.get(tool).getId());
		}
		
		List<WorkflowWrapper> ret = new ArrayList<WorkflowWrapper>();
		for(Workflow zz : reta)
		{
			WorkflowWrapper za = new WorkflowWrapper(zz);
			za.loadToolsFromCache();
			ret.add(za);
		}
		String noun = "workflow";
		if (ret.size() > 1)
			noun = "workflows";
		label.setValue(ret.size() + " " + noun + " found");
		
		vis.render(ret);
	}

}
