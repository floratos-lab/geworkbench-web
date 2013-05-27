package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class InboxTablePanel extends VerticalLayout implements Button.ClickListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4050450412088605268L;
	private static final String NAME = "Name";
	private static final String USER = "User";
	private static final String DATE = "Date";
	
	private WorkflowRepository workflowRepository;
	private Table table = new Table();
	private IndexedContainer model = new IndexedContainer();
	private List<IncomingWorkflow> iwfList;
	private Button addButton = new Button("Add");
	private Button deleteButton = new Button("Delete");
	private Button refreshButton = new Button("Refresh");
	private GenSpaceLogin login;
	
	public InboxTablePanel() {
		getModel().addContainerProperty(NAME, String.class, null);
		getModel().addContainerProperty(USER, String.class, null);
		getModel().addContainerProperty(DATE, XMLGregorianCalendar.class, null);
		table.setContainerDataSource(getModel());
		table.setSizeFull();
		table.setColumnHeaders(new String [] {NAME, USER, DATE});
		table.setSelectable(true);
		this.addComponent(table);
		this.setExpandRatio(table, 1.0f);
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.addComponent(addButton);
		hLayout.addComponent(deleteButton);
		hLayout.addComponent(refreshButton);
		this.addComponent(hLayout);
		
		addButton.addListener(this);
		deleteButton.addListener(this);
		refreshButton.addListener(this);
	}
	
	public void setData(List<IncomingWorkflow> list) {
		this.iwfList = list;
		getModel().removeAllItems();
		for (int i=0; i < this.iwfList.size(); i++) {
			IncomingWorkflow workflow = list.get(i);
			Item item = getModel().addItem(i);
			item.getItemProperty(NAME).setValue(workflow.getName());
			item.getItemProperty(USER).setValue(workflow.getSender().getUsername());
			item.getItemProperty(DATE).setValue(workflow.getCreatedAt());
		}
	}
	
	public void setWorkflowRepository(WorkflowRepository wfr) {
		this.workflowRepository = wfr;
	}

	public IndexedContainer getModel() {
		return model;
	}

	public void setModel(IndexedContainer model) {
		this.model = model;
	}
	
	public void setGenSpaceLogin(GenSpaceLogin login) {
		this.login = login;
	}
	
	private void removeFromInbox(IncomingWorkflow im) {
		Boolean ret = login.getGenSpaceServerFactory().getWorkflowOps().deleteFromInbox(im.getId());
		
		if (ret) {
			this.refreshInbox();
		}
	}
	
	public void buttonClick(Button.ClickEvent evt) {
		
		Object idxTmp = table.getValue();
		
		if (idxTmp == null) {
			return ;
		}
		
		String bCaption = evt.getButton().getCaption();

		if (bCaption.equals("Delete")) {
			int idx = Integer.parseInt(idxTmp.toString());
			IncomingWorkflow tmp = this.iwfList.get(idx);
			this.removeFromInbox(tmp);
		} else if (bCaption.equals("Add")) {
			int idx = Integer.parseInt(idxTmp.toString());
			IncomingWorkflow tmp = this.iwfList.get(idx);
			UserWorkflow ret = login.getGenSpaceServerFactory().getWorkflowOps().addToRepository(tmp.getId());
			this.workflowRepository.updateFormFieldsBG();
			
			if (ret != null) {
				workflowRepository.getRepositoryPanel().recalculateAndReload();
				removeFromInbox(tmp);
			}
		} else if (bCaption.equals("Refresh")) {
			this.refreshInbox();
		}
	}
	
	private void refreshInbox() {
		List<IncomingWorkflow> iwfList = login.getGenSpaceServerFactory().getWorkflowOps().getIncomingWorkflows();
		this.setData(iwfList);
		this.table.refreshRowCache();
	}
}
