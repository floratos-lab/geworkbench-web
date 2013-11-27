package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowFolder;
import org.geworkbenchweb.genspace.ui.component.AbstractGenspaceTab;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.ui.component.GenSpaceTab;
import org.geworkbenchweb.genspace.ui.component.WorkflowVisualizationPanel;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.geworkbenchweb.utils.LayoutUtil;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

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

	private HorizontalSplitPanel jSplitPane1 = new HorizontalSplitPanel();
	private VerticalSplitPanel jSplitPane2 = new VerticalSplitPanel();
	private VerticalSplitPanel jSplitPane3 = new VerticalSplitPanel();
	private Label infoLabel = new Label(
			"Please login to genSpace to access this area.");
	private VerticalLayout mainLayout = new VerticalLayout();
	private Panel compRepoPanel = new Panel();
	private Panel rootRepoPanel = new Panel();
	private Button delete = new Button("Delete Selected");
	
	public WorkflowRepository(GenSpaceLogin login) {
		super(login);
		buildMainLayout();
		setCompositionRoot(mainLayout);
		
		workflowDetailsPanel.setGenSpaceLogin(login);
		workflowCommentsPanel.setGenSpaceLogin(login);
		inboxTable.setGenSpaceLogin(login);
		graphPanel.setGenSpaceLogin(login);
		
		inboxTable.setWorkflowRepository(this);
	}

	private AbstractLayout buildMainLayout() {
		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		tabSheet.addTab(workflowCommentsPanel, "Workflow Comments");
		tabSheet.addTab(workflowDetailsPanel, "Workflow Details");

		repositoryPanel.addItemClickListener(this);
		graphPanel.setSizeFull();
		
		rootRepoPanel.setSizeFull();
		rootRepoPanel.setHeight("320px");
		rootRepoPanel.setContent(LayoutUtil.addComponent(repositoryPanel));
		//compRepoPanel.addComponent(repositoryPanel);
		VerticalLayout layout = LayoutUtil.addComponent(rootRepoPanel);
		layout.addComponent(delete);
		compRepoPanel.setContent(layout);
		compRepoPanel.setSizeFull();
		delete.addClickListener(new Button.ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent evt) {
				//System.out.println("Delete test");
				UserWorkflow curWorkflow = repositoryPanel.getCurWorkFlow();
				
				if (curWorkflow == null) {
					return ;
				}
				
				login.getGenSpaceServerFactory().getWorkflowOps().deleteMyWorkflow(curWorkflow.getId());
				login.getGenSpaceParent().getWorkflowRepository().updateFormFieldsBG();
				repositoryPanel.recalculateAndReload();
			}
		});
		jSplitPane3.addComponent(compRepoPanel);
		
		jSplitPane3.addComponent(inboxTable);
		jSplitPane2.addComponent(graphPanel);
		jSplitPane2.addComponent(tabSheet);
		jSplitPane1.addComponent(jSplitPane3);
		jSplitPane1.addComponent(jSplitPane2);
		
		jSplitPane1.setSplitPosition(20, Unit.PERCENTAGE);
		jSplitPane2.setSplitPosition(70, Unit.PERCENTAGE);
		jSplitPane3.setSplitPosition(50, Unit.PERCENTAGE);
		
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
		mainLayout.addComponent(jSplitPane1);
		updateFormFieldsBG();
	}

	@Override
	public void loggedOut() {
		//Set null for current workflow, once user login again and objects still exist.
		repositoryPanel.getContainer().removeAllItems();
		repositoryPanel.setCurWorkFlow(null);
		graphPanel.setContent(null);

		workflowCommentsPanel.getModel().removeAllItems();
		workflowCommentsPanel.setWorkflow(null);
		workflowDetailsPanel.setAndPrintWorkflow(null);
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
		//workflowDetailsPanel.setAndPrintWorkflow(userWorkflow.getWorkflow());
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
