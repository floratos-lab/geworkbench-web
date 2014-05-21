package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowComment;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class WorkflowCommentsPanel extends VerticalLayout implements Button.ClickListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6641895295511102113L;
	SimpleDateFormat fmt = new SimpleDateFormat("M/d/yy h:mm a");
	private GenSpaceLogin_1 login;
	private Table table = new Table();
	private Button newButton = new Button("New");
	private Button removeButton = new Button("Remove");
	private IndexedContainer model = new IndexedContainer();
	private Workflow curWorkflow;
	private List<WorkflowComment> wfComments;

	public WorkflowCommentsPanel() {
		getModel().addContainerProperty("User", String.class, null);
		getModel().addContainerProperty("Date", String.class, null);
		getModel().addContainerProperty("Comment", String.class, null);
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSpacing(true);
		hLayout.addComponent(newButton);
		hLayout.addComponent(removeButton);
		table.setContainerDataSource(getModel());
		table.setColumnHeaders(new String [] {"User", "Date", "Comment"});
		table.setSelectable(true);
		table.setSizeFull();
		newButton.addListener(this);
		removeButton.addListener(this);
		this.addComponent(hLayout);
		this.addComponent(table);
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login) {
		this.login = login;
	}
	
	public void clear(){
		table.removeAllItems();
	}
	
	public void setWorkflow(Workflow workflow) {
		this.curWorkflow = workflow;
		
		if (this.curWorkflow == null)
			return ;
		
		this.wfComments = login.getGenSpaceServerFactory().getUsageOps().getWFComments(workflow);
		getModel().removeAllItems();
		for (int i=0; i<wfComments.size(); i++) {
			WorkflowComment wi = wfComments.get(i);
			String user = wi.getCreator().getUsername();
			String date = fmt.format(DatatypeConverter.parseDateTime(wi.getCreatedAt().toString()).getTime());
			String comment = wi.getComment();
			Item item = getModel().addItem(i);
			item.getItemProperty("User").setValue(user);
			item.getItemProperty("Date").setValue(date);
			item.getItemProperty("Comment").setValue(comment);
		}		
	}

	public IndexedContainer getModel() {
		return model;
	}

	public void setModel(IndexedContainer model) {
		this.model = model;
	}
	
	public void buttonClick(Button.ClickEvent evt) {
		if (curWorkflow == null) {
			return ;
		}
		
		String bCaption = evt.getButton().getCaption();
		
		if (bCaption.equals("New")) {			
			final Window newWindow = new Window("Input");
			newWindow.setWidth("200px");
			newWindow.setHeight("150px");
			getApplication().getMainWindow().addWindow(newWindow);
			
			VerticalLayout vLayout = new VerticalLayout();
			newWindow.addComponent(vLayout);
			
			Label inputLabel = new Label("Input comment text");
			vLayout.addComponent(inputLabel);
			
			final TextField tf = new TextField();
			vLayout.addComponent(tf);
			
			HorizontalLayout hLayout = new HorizontalLayout();
			Button cButton = new Button("Cancel");
			Button oButton = new Button("OK");
			hLayout.addComponent(cButton);
			hLayout.addComponent(oButton);
			vLayout.addComponent(hLayout);
			
			oButton.addListener(new Button.ClickListener() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent evt) {
					newComment(tf.getValue().toString());
					getApplication().getMainWindow().removeWindow(newWindow);
				}
			});
			
			cButton.addListener(new Button.ClickListener() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void buttonClick(Button.ClickEvent evt) {
					getApplication().getMainWindow().removeWindow(newWindow);
				}
			});
		} else if (bCaption.equals("Remove")) {
			if (table.getValue() == null) {
				getApplication().getMainWindow().showNotification("Please select a comment before deletion");
				return ;
			}
			
			Object itemID = getModel().getIdByIndex(Integer.valueOf(table.getValue().toString()));
			Item item = getModel().getItem(itemID);
			
			int idx = Integer.parseInt(table.getValue().toString());
			WorkflowComment wc = this.wfComments.get(idx);
			this.removeComment(wc);
		}
	}
	
	public Table getTable() {
		return this.table;
	}
	
	private void newComment(String comment) {
		WorkflowComment wc = new WorkflowComment();
		try {
			wc.setComment(comment);
			wc.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			wc.setCreator(login.getGenSpaceServerFactory().getUser());
			
			WorkflowComment ret = login.getGenSpaceServerFactory().getWorkflowOps().addCommentToWf(wc, curWorkflow);
			
			login.getGenSpaceServerFactory().updateCachedUser();
			
			if (login.getGenSpaceServerFactory().isLoggedIn()) {
				login.getGenSpaceParent().getWorkflowRepository().getRepositoryPanel().setWorkflowFolder(login.getGenSpaceServerFactory().getUserOps().getRootFolder());
				login.getGenSpaceParent().getWorkflowRepository().getRepositoryPanel().recalculateAndReload();
			}
			
			this.setWorkflow(this.curWorkflow);
			this.table.refreshRowCache();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			login.getGenSpaceServerFactory().handleException(e);
		}
	}
	
	private void removeComment(WorkflowComment wc) {
		Boolean ret = login.getGenSpaceServerFactory().getWorkflowOps().removeComment(wc.getId());
		login.getGenSpaceServerFactory().updateCachedUser();
		
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			login.getGenSpaceParent().getWorkflowRepository().getRepositoryPanel().setWorkflowFolder(login.getGenSpaceServerFactory().getUserOps().getRootFolder());
			login.getGenSpaceParent().getWorkflowRepository().getRepositoryPanel().recalculateAndReload();
		}
		
		if (ret) {
			this.setWorkflow(this.curWorkflow);
			this.table.refreshRowCache();
		}
	}
}
