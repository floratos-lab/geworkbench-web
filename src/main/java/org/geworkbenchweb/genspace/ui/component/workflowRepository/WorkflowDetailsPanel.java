package org.geworkbenchweb.genspace.ui.component.workflowRepository;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class WorkflowDetailsPanel extends VerticalLayout implements ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2333272779885676592L;
	private TextField textArea = new TextField();
	private Workflow workflow;
	private UserWorkflow usrWorkflow;
	private TextField receiver = new TextField("Receiver");
	private Button sendButton = new Button("Send Selected Workflow");
	private GenSpaceLogin_1 login;
	
	public WorkflowDetailsPanel() {
		this.setSizeFull();
		this.setSpacing(true);
		getTextArea().setSizeFull();
		/*HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSpacing(true);
		hLayout.addComponent(receiver);
		hLayout.addComponent(sendButton);*/
		this.addComponent(new Label(""));
		this.addComponent(receiver);
		this.addComponent(sendButton);
		this.addComponent(getTextArea());
		this.setExpandRatio(getTextArea(), 1.0f);
		this.sendButton.addListener(this);
		this.textArea.setImmediate(true);
		this.textArea.setSizeFull();
		this.textArea.setHeight("270px");
		//System.out.println(receiver.getCaption());
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login)
	{
		this.login = login;
	}
	
	public void setAndPrintWorkflow(UserWorkflow usrWorkflow) {
		this.usrWorkflow = usrWorkflow;
		//this.workflow = workflow;
		//System.out.println("set print detail! "+this.usrWorkflow);
		if (this.usrWorkflow == null) {
			getTextArea().setValue("");
		} else {
			String string = getWorkflowDetailsString(this.usrWorkflow.getWorkflow());
			//System.out.println("check detail: "+string);
			getTextArea().setValue(string);
		}
	}

	public void clear(){
		getTextArea().setValue("");
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

	@Override
	public void buttonClick(ClickEvent event) {
		if (this.usrWorkflow == null) {
			return ;
		}
		
		if (event.getSource() == sendButton) {
			boolean b = send();
			if (b) {
				receiver.setValue("");
				getApplication().getMainWindow().showNotification("Your workflow has been sent successfully");
			} else {
				getApplication().getMainWindow().showNotification("Invalid receiver. Please input again");
			}
		}
	}

	private boolean send() {
		String receiver = this.receiver.getValue().toString();
		
		if (receiver == null || receiver.isEmpty())
			return false;
		
		IncomingWorkflow newW = new IncomingWorkflow();
		try {
			newW.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		}
		catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Workflow tmpWorkflow = this.usrWorkflow.getWorkflow();
		newW.setName(this.usrWorkflow.getName());
		newW.setWorkflow(tmpWorkflow);
		newW.setSender(login.getGenSpaceServerFactory().getUser());
		//return login.getGenSpaceServerFactory().getWorkflowOps().sendWorkflow(newW, (String) receiver.getValue());
		return login.getGenSpaceServerFactory().getWorkflowOps().sendWorkflow(newW, receiver);
	}

}
