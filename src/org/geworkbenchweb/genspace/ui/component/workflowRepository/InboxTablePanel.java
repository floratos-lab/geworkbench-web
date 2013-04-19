package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class InboxTablePanel extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4050450412088605268L;
	private static final String NAME = "Name";
	private static final String USER = "User";
	private static final String DATE = "Date";
	
	private Table table = new Table();
	private IndexedContainer model = new IndexedContainer();
	private Button addButton = new Button("Add");
	private Button deleteButton = new Button("Delete");
	
	public InboxTablePanel() {
		getModel().addContainerProperty(NAME, String.class, null);
		getModel().addContainerProperty(USER, String.class, null);
		getModel().addContainerProperty(DATE, XMLGregorianCalendar.class, null);
		table.setContainerDataSource(getModel());
		table.setSizeFull();
		table.setColumnHeaders(new String [] {NAME, USER, DATE});
		this.addComponent(table);
		this.setExpandRatio(table, 1.0f);
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.addComponent(addButton);
		hLayout.addComponent(deleteButton);
		this.addComponent(hLayout);
	}
	
	public void setData(List<IncomingWorkflow> list) {
		getModel().removeAllItems();
		for (int i=0; i<list.size(); i++) {
			IncomingWorkflow workflow = list.get(i);
			Item item = getModel().addItem(i);
			item.getItemProperty(NAME).setValue(workflow.getName());
			item.getItemProperty(USER).setValue(workflow.getSender().getUsername());
			item.getItemProperty(NAME).setValue(workflow.getCreatedAt());
		}
	}

	public IndexedContainer getModel() {
		return model;
	}

	public void setModel(IndexedContainer model) {
		this.model = model;
	}
}
