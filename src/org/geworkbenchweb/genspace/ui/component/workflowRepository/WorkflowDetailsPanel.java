package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class WorkflowDetailsPanel extends VerticalLayout implements ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2333272779885676592L;
	private TextField textArea = new TextField();
	private Workflow workflow;
	private TextField receiver = new TextField("Receiver");
	private Button sendButton = new Button("Send Selected Workflow");
	private GenSpaceLogin login;
	
	public WorkflowDetailsPanel() {
		this.setSizeFull();
		getTextArea().setSizeFull();
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.addComponent(receiver);
		hLayout.addComponent(sendButton);
		this.addComponent(hLayout);
		this.addComponent(getTextArea());
		this.setExpandRatio(getTextArea(), 1.0f);
		sendButton.addListener(this);
	}
	
	public void setGenSpaceLogin(GenSpaceLogin login)
	{
		this.login = login;
	}
	
	public void setAndPrintWorkflow(Workflow workflow) {
		this.workflow = workflow;
		String string = getWorkflowDetailsString(workflow);
		getTextArea().setValue(string);
	}

	public String getWorkflowDetailsString(Workflow w) {
		WorkflowWrapper wr = new WorkflowWrapper(w);
		String result = "ID: " + w.getId() + "\n";
		result += "Creator: " + ( w.getCreator() == null ? "system" : wr.getCreator().getUsername()) + "\n";
		if(w.getCreatedAt() != null)
			result += "Creation date: " + wr.getCreatedAt().toString()	 + "\n";
		result += "Average rating: " + wr.getAvgRating() + "\n";
		result += "Usage count: " + wr.getUsageCount() + "\n";
		result += "Comments count: " + wr.getNumComments() + "\n";
		result += "Ratings count: " + wr.getNumRating()+ "\n";
		result += "Tools list: " + wr.toString() + "\n";
		return result;
	}

	public TextField getTextArea() {
		return textArea;
	}

	public void setTextArea(TextField textArea) {
		this.textArea = textArea;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getSource() == sendButton) {
			boolean b = send();
			if (b) {
				receiver.setValue("");
			}
		}
	}

	private boolean send() {
		IncomingWorkflow newW = new IncomingWorkflow();
		try {
			newW.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		}
		catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		newW.setName(workflow.getIdstr());
		newW.setWorkflow(workflow);
		newW.setSender(login.getGenSpaceServerFactory().getUser());
		return login.getGenSpaceServerFactory().getWorkflowOps().sendWorkflow(newW, (String) receiver.getValue());
	}

}
