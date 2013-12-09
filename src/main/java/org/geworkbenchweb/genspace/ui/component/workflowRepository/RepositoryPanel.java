package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowFolder;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Tree;

public class RepositoryPanel extends Tree {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7376733060873946250L;
	
	private UserWorkflow selectedWorkflow = null;

	public static class RepositoryContainer extends HierarchicalContainer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5520490698482925170L;
		public static final String NAME_PROP = "name";
		public static final String OBJ_PROP = "object";

		RepositoryContainer(WorkflowFolder workflowFolder) {
			this.addContainerProperty(NAME_PROP, String.class, null);
			this.addContainerProperty(OBJ_PROP, UserWorkflow.class, null);
			int rootId = 0;
			Item root = this.addItem(rootId);
			root.getItemProperty(NAME_PROP).setValue("Workflows");
			this.setChildrenAllowed(rootId, true);

			int itemId = rootId+1;			
			for (UserWorkflow w : workflowFolder.getWorkflows()) {
				Item node = this.addItem(itemId);
				node.getItemProperty(NAME_PROP).setValue(w.getName());
				node.getItemProperty(OBJ_PROP).setValue(w);
				this.setParent(itemId, rootId);
				this.setChildrenAllowed(itemId, false);
				itemId++;
			}
		} 
	}

	private WorkflowFolder workflowFolder;
	private RepositoryContainer container;

	public RepositoryPanel() {
		this.setImmediate(true);
		this.setItemCaptionPropertyId(RepositoryContainer.NAME_PROP);
	}
	
	public void setWorkflowFolder(WorkflowFolder workflowFolder) {
		this.workflowFolder = workflowFolder;
	}
	
	public void recalculateAndReload() {
		setContainer(new RepositoryContainer(workflowFolder));
		this.setContainerDataSource(getContainer());
	}

	public RepositoryContainer getContainer() {
		return container;
	}

	public void setContainer(RepositoryContainer container) {
		this.container = container;
	}
	
	public void setCurWorkFlow(UserWorkflow usrWorkFlow) {
		this.selectedWorkflow = usrWorkFlow;
	}
	
	public UserWorkflow getCurWorkFlow() {
		return this.selectedWorkflow;
	}
}
