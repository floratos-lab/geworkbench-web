package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowComment;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class WorkflowCommentsPanel extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6641895295511102113L;
	SimpleDateFormat fmt = new SimpleDateFormat("F/M/yy h:mm a");
	private GenSpaceLogin login;
	private Table table = new Table();
	private Button newButton = new Button("New");
	private Button removeButton = new Button("Remove");
	private IndexedContainer model = new IndexedContainer();

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
		table.setSizeFull();
		this.addComponent(hLayout);
		this.addComponent(table);
	}
	
	public void setGenSpaceLogin(GenSpaceLogin login) {
		this.login = login;
	}
	
	public void setWorkflow(Workflow workflow) {
		List<WorkflowComment> wfComments = login.getGenSpaceServerFactory().getUsageOps().getWFComments(workflow);
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

}
