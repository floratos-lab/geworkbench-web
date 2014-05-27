package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowFolder;
import org.geworkbenchweb.genspace.ui.component.AbstractGenspaceTab;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.ui.component.GenSpaceTab;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualizationPanel;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class WorkflowRepository extends AbstractGenspaceTab implements GenSpaceTab, ItemClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2042763019136606119L;

	private RepositoryPanel repositoryPanel = new RepositoryPanel();
	private WorkflowVisualizationPanel graphPanel = new WorkflowVisualizationPanel();
	private WorkflowDetailsPanel workflowDetailsPanel = new WorkflowDetailsPanel();
	private WorkflowCommentsPanel workflowCommentsPanel = new WorkflowCommentsPanel();
	private InboxTablePanel inboxTable = new InboxTablePanel();
	private GridLayout gridLayout = new GridLayout();
	private Label infoLabel = new Label(
			"Please login to genSpace to access this area.");
	private Panel mainLayout = new Panel();
	private VerticalLayout compRepoPanel = new VerticalLayout();
	private Panel rootRepoPanel = new Panel();
	private Button delete = new Button("Delete Selected");
	
	public WorkflowRepository(GenSpaceLogin_1 login) {
		super(login);
		buildMainLayout();
		setCompositionRoot(mainLayout);
		
		workflowDetailsPanel.setGenSpaceLogin(login);
		workflowCommentsPanel.setGenSpaceLogin(login);
		inboxTable.setGenSpaceLogin(login);
		graphPanel.setGenSpaceLogin(login);
		
		inboxTable.setWorkflowRepository(this);
	}

	private Panel buildMainLayout() {
		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		tabSheet.addTab(workflowCommentsPanel, "Workflow Comments");
		tabSheet.addTab(workflowDetailsPanel, "Workflow Details");
		tabSheet.setStyleName(Reindeer.TABSHEET_MINIMAL);
		gridLayout.setMargin(false);
		gridLayout.setColumns(2);
		gridLayout.setRows(2);
		gridLayout.setSizeFull();
		gridLayout.setSpacing(true);
		gridLayout.addComponent(compRepoPanel, 0, 0);
		gridLayout.addComponent(inboxTable, 0, 1);
		gridLayout.addComponent(graphPanel, 1, 0);
		gridLayout.addComponent(tabSheet, 1, 1);

		repositoryPanel.addListener(this);
		graphPanel.setSizeFull();
		graphPanel.setStyleName(Reindeer.PANEL_LIGHT);
		rootRepoPanel.setSizeFull();
		rootRepoPanel.setHeight("320px");
		rootRepoPanel.addComponent(repositoryPanel);
		compRepoPanel.setSpacing(true);
		compRepoPanel.addComponent(rootRepoPanel);
		compRepoPanel.addComponent(delete);
		compRepoPanel.setSizeFull();
		compRepoPanel.setStyleName(Reindeer.PANEL_LIGHT);
		delete.addListener(new Button.ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent evt) {
				UserWorkflow curWorkflow = repositoryPanel.getCurWorkFlow();
				
				if (curWorkflow == null) {
					return ;
				}
				login.getGenSpaceServerFactory().getWorkflowOps().deleteMyWorkflow(curWorkflow.getId());
				login.getGenSpaceParent().getWorkflowRepository().updateFormFieldsBG();
				repositoryPanel.recalculateAndReload();
				workflowDetailsPanel.clear();
				workflowCommentsPanel.clear();
				graphPanel.removeAllComponents();
				repositoryPanel.setCurWorkFlow(null);
			}
		});
	
		
		mainLayout.addComponent(infoLabel);
		mainLayout.setSizeFull();
		return mainLayout;
	}

	@Override
	public void tabSelected() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void loggedIn() {
		mainLayout.removeAllComponents();
		mainLayout.addComponent(gridLayout);
		updateFormFieldsBG();
	}

	@Override
	public void loggedOut() {
		//Set null for current workflow, once user login again and objects still exist.
		repositoryPanel.getContainer().removeAllItems();
		repositoryPanel.setCurWorkFlow(null);
		graphPanel.removeAllComponents();

		workflowCommentsPanel.getModel().removeAllItems();
		workflowCommentsPanel.setWorkflow(null);
		workflowDetailsPanel.removeAllComponents();
		inboxTable.getModel().removeAllItems();
		mainLayout.removeAllComponents();
		mainLayout.addComponent(infoLabel);
	}

	public void updateFormFieldsBG() {
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			try {
				WorkflowFolder folder = login.getGenSpaceServerFactory().getUserOps()
						.getRootFolder();
				repositoryPanel.setWorkflowFolder(folder);
				repositoryPanel.recalculateAndReload();
				
				List<IncomingWorkflow> list = login.getGenSpaceServerFactory().getWorkflowOps().getIncomingWorkflows();
				inboxTable.setData(list);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void itemClick(ItemClickEvent event) {
		Item item = event.getItem();
		Object value = item.getItemProperty(RepositoryPanel.RepositoryContainer.OBJ_PROP).getValue();
		if (!(value instanceof UserWorkflow)) {
			return;
		}
		UserWorkflow userWorkflow = (UserWorkflow) value;
		List<WorkflowWrapper> list = new ArrayList<WorkflowWrapper>();
		WorkflowWrapper wrapper = new WorkflowWrapper(userWorkflow.getWorkflow());
		wrapper.loadToolsFromCache();
		list.add(wrapper);
		graphPanel.render(list);
		workflowDetailsPanel.setAndPrintWorkflow(userWorkflow);
		workflowCommentsPanel.setWorkflow(userWorkflow.getWorkflow());
		repositoryPanel.setCurWorkFlow(userWorkflow);
	}
	
	public RepositoryPanel getRepositoryPanel() {
		return this.repositoryPanel;
	}
	
	public WorkflowCommentsPanel getWorkflowCommentsPanel() {
		return this.workflowCommentsPanel;
	}

}
