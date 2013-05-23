package org.geworkbenchweb.genspace.rating;

import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbenchweb.genspace.RuntimeEnvironmentSettings;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;
import org.geworkbenchweb.genspace.ui.component.SocialNetworkHome;
import org.geworkbenchweb.genspace.ui.component.UserSearchWindow;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class WorkflowVisualizationPopup extends Window implements Button.ClickListener{

	/*private JMenuItem addWorkflowRepository = new JMenuItem(
			"Add workflow to your repository");*/

	// we store the tool name each time the menu is invoked
	// so we can speed up the process
	private VerticalLayout vLayout;
	
	private Panel workflowPanel;
	
	private Panel wFlowToolPanel;

	private Panel expertPanel;
	
	private Panel viewPanel;
	
	private StarRatingPanel wfRatePanel;
	
	private StarRatingPanel toolRatePanel;
	
	private Label addWkflowLabel = new Label("Add workflow to your repository");
	
	private Label gotoPageLabel = new Label();
	
	private Label expertLabel = new Label();
	
	private Label viewComment = new Label("View/add workflow comments");
	
	private Button addWkButton;
	
	private Button gotoPage;
	
	private Button contact;
	
	private Button view;
	
	private Workflow workflow;
	
	private Tool selectedTool;
	
	private User expert;
	
	private GenSpaceLogin login;
	
	private String gotoCaption;
	
	private String contactCaption;
	
	public WorkflowVisualizationPopup(GenSpaceLogin login, Workflow workflow, Tool selectedTool) {
		this.login = login;
		this.workflow = workflow;
		this.selectedTool = selectedTool;
		this.expert = this.login.getGenSpaceServerFactory().getUsageOps().getExpertUserFor(selectedTool.getId());
		
		this.vLayout = new VerticalLayout();
		this.addComponent(vLayout);
		this.createWorkflowPanel();
	}
	
	private void createWorkflowPanel() {		
		this.workflowPanel = new Panel();
		
		HorizontalLayout wLayout = new HorizontalLayout();
		this.workflowPanel.addComponent(wLayout);
		wLayout.addComponent(this.addWkflowLabel);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		
		wLayout.addComponent(emptyLabel);
		
		this.addWkButton = new Button("Add");
		this.addWkButton.addListener(this);
		wLayout.addComponent(addWkButton);
		
		wLayout = new HorizontalLayout();
		this.wFlowToolPanel = new Panel();
		this.wFlowToolPanel.addComponent(wLayout);
		this.gotoCaption = "Goto GenSpace page of " + this.selectedTool.getName();
		this.gotoPageLabel.setCaption(gotoCaption);
		wLayout.addComponent(this.gotoPageLabel);
		
		emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		
		wLayout.addComponent(emptyLabel);
		
		this.gotoPage = new Button("Go");
		this.gotoPage.addListener(this);
		wLayout.addComponent(gotoPage);
		
		wLayout = new HorizontalLayout();
		this.expertPanel = new Panel();
		this.expertPanel.addComponent(wLayout);
		this.contactCaption = "Contact expert user: " + (new UserWrapper(this.expert, this.login)).getFullName();
		this.expertLabel.setCaption(contactCaption);
		wLayout.addComponent(this.expertLabel);
		
		emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		wLayout.addComponent(emptyLabel);
		
		this.contact = new Button("Contact");
		this.contact.addListener(this);
		wLayout.addComponent(contact);
		
		wLayout = new HorizontalLayout();
		this.viewPanel = new Panel();
		this.viewPanel.addComponent(wLayout);
		wLayout.addComponent(this.viewComment);
		
		emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		wLayout.addComponent(emptyLabel);
		
		this.view = new Button("View");
		this.view.addListener(this);
		wLayout.addComponent(this.view);
		
		this.toolRatePanel = new StarRatingPanel(this.login);
		this.toolRatePanel.setTitle("Rate " + this.selectedTool.getName());
		this.toolRatePanel.loadRating(this.selectedTool);
		
		this.wfRatePanel = new StarRatingPanel(this.login);
		this.wfRatePanel.setTitle("Rate workflow until here");
		this.wfRatePanel.loadRating(workflow);
	
		this.vLayout.addComponent(wFlowToolPanel);
		this.vLayout.addComponent(expertPanel);
		this.vLayout.addComponent(workflowPanel);
		this.vLayout.addComponent(viewPanel);
		this.vLayout.addComponent(toolRatePanel);
		this.vLayout.addComponent(wfRatePanel);
	}
	
	public void buttonClick(Button.ClickEvent evt) {
		String buttonCaption = evt.getButton().getCaption();
		String args = "";
		boolean browser = true;
		
		if (buttonCaption.equals("Add")) {
			if (!login.getGenSpaceServerFactory().isLoggedIn()) {
				getApplication().getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
				return ;
			}
			
			browser = false;
			addWorkFlowToRepository();
		} else if (buttonCaption.equals("Go") && selectedTool.getId() > 0) {
			args = "tool/index/" + selectedTool.getId();
			System.out.println("Test args: " + args);
		} else if (buttonCaption.equals("View") && workflow.getId() > 0) {
			args = "workflow/index/" + workflow.getId();
		} else if (buttonCaption.equals("Contact")) {
			if (login.getGenSpaceServerFactory().isLoggedIn()) {
				UserSearchWindow usw = new UserSearchWindow(expert, login, login.getGenSpaceParent().getSocialNetworkHome());
				getApplication().getMainWindow().addWindow(usw);
				browser = false;
			} else {
				getApplication().getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
				return ;
			}
		}
		
		if (browser) {
			try {
				BrowserLauncher.openURL(RuntimeEnvironmentSettings.GS_WEB_ROOT + args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 
	
	private void addWorkFlowToRepository() {
		final Window nameWindow = new Window();
		nameWindow.setCaption("Workflow Confirmation");
		nameWindow.setWidth("450px");
		nameWindow.setHeight("100px");
		
		this.getApplication().getMainWindow().addWindow(nameWindow);
		VerticalLayout nLayout = new VerticalLayout();
		nameWindow.addComponent(nLayout);
		
		HorizontalLayout contLayout = new HorizontalLayout();
		nLayout.addComponent(contLayout);
		
		Label tLabel = new Label("Type a name for the workflow to be added:");
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		
		final TextField inputField = new TextField();
		contLayout.addComponent(tLabel);
		contLayout.addComponent(emptyLabel);
		contLayout.addComponent(inputField);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		Button confirm = new Button("Confirm");
		confirm.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				
				String name = inputField.getValue().toString();
				
				if (name != null && name.trim().length() > 0) {
					UserWorkflow uw = new UserWorkflow();
					uw.setName(name);
					uw.setWorkflow(workflow);
					uw.setFolder(login.getGenSpaceServerFactory().getUserOps().getRootFolder());
					uw.setOwner(login.getGenSpaceServerFactory().getUser());
					
					try {
						uw.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
						login.getGenSpaceServerFactory().getWorkflowOps().addWorkflow(uw, uw.getFolder().getId());
					} catch (Exception e) {
						login.getGenSpaceServerFactory().handleException(e);
						return ;
					}
					login.getGenSpaceParent().getWorkflowRepository().updateFormFieldsBG();
					getApplication().getMainWindow().showNotification("Workflow added succesfully to repository");
				} else {
					getApplication().getMainWindow().showNotification("Operation cancelled: A valid name has to be specified");
				}

				getApplication().getMainWindow().removeWindow(nameWindow);
				getApplication().getMainWindow().removeWindow(WorkflowVisualizationPopup.this);
			}
		});
		buttonLayout.addComponent(confirm);
		nLayout.addComponent(buttonLayout);
		
	}
}
