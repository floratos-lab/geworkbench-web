package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;

public class WorkflowVisualization extends AbstractGenspaceTab implements
		GenSpaceTab, ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1472121123400742285L;

	private Log log = LogFactory.getLog(this.getClass());
	private NativeSelect tools = new NativeSelect();
	private HashMap<String, Tool> toolsMap = new HashMap<String, Tool>();
	private NativeSelect actions = new NativeSelect();
	private Button button = new Button("Search");
	private Label label = new Label();
	private HorizontalLayout selectPanel = new HorizontalLayout();
	private WorkflowVisualizationPanel vis = new WorkflowVisualizationPanel();
	private Panel borderLayout = new Panel();
	private Panel selectRootPanel = new Panel();

	public WorkflowVisualization(GenSpaceLogin login) {
		super(login);
		selectRootPanel.setScrollable(true);
		borderLayout.setScrollable(true);
		vis.setScrollable(true);
		initComponents();
		setCompositionRoot(borderLayout);
	}

	private void initComponents() {
		actions.setCaption("-- select action --");
		actions.addItem("Most common workflow starting with");
		actions.addItem("Most common workflow including");
		actions.addItem("All workflows including");

		tools.setCaption("-- select tool --");
		for (Tool tool : login.getGenSpaceServerFactory().getUsageOps().getAllTools()) {
			String name = tool.getName();
			toolsMap.put(name, tool);
			tools.addItem(name);
		}

		button.addListener(this);
		label.setValue("Please select an action and a tool to search for");

		//selectPanel.setSpacing(true);
		selectPanel.addComponent(actions);
		selectPanel.addComponent(tools);
		selectPanel.addComponent(button);
		selectPanel.addComponent(label);

		selectPanel.setComponentAlignment(actions, Alignment.BOTTOM_CENTER);
		selectPanel.setComponentAlignment(tools, Alignment.BOTTOM_CENTER);
		selectPanel.setComponentAlignment(button, Alignment.BOTTOM_CENTER);
		selectPanel.setComponentAlignment(label, Alignment.BOTTOM_CENTER);
		
		selectRootPanel.addComponent(selectPanel);

		/*borderLayout.addComponent(selectPanel, BorderLayout.Constraint.NORTH);
		borderLayout.addComponent(vis, BorderLayout.Constraint.CENTER);*/
		//borderLayout.addComponent(selectPanel);
		borderLayout.addComponent(selectRootPanel);
		borderLayout.addComponent(vis);
		vis.setGenSpaceLogin(login);
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
