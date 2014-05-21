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
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.geworkbenchweb.genspace.ui.component.UserSearchWindow;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class WorkflowVisualizationPopup extends Window implements Button.ClickListener{

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
	
	private Link gotoPage;
	
	private Button contact;
	
	private Link view;
	
	private Workflow workflow;
	
	private Tool selectedTool;
	
	private User expert;
	
	private GenSpaceLogin_1 login;
	
	private String gotoCaption;
	
	private String contactCaption;
	
	public WorkflowVisualizationPopup(GenSpaceLogin_1 login2, Workflow workflow, Tool selectedTool) {
		this.login = login2;
		this.workflow = workflow;
		this.selectedTool = selectedTool;
		this.expert = this.login.getGenSpaceServerFactory().getUsageOps().getExpertUserFor(selectedTool.getId());
		
		this.addWkflowLabel.setWidth("240px");
		this.gotoPageLabel.setWidth("240px");
		this.expertLabel.setWidth("240px");
		this.viewComment.setWidth("240px");
		this.createWorkflowPanel();
		this.addComponent(vLayout);
	}
	
	public VerticalLayout getVLayout() {
		return this.vLayout;
	}
	
	private void createWorkflowPanel() {
		this.vLayout = new VerticalLayout();
		this.workflowPanel = new Panel();
		
		BorderLayout wLayout = new BorderLayout();
		this.workflowPanel.addComponent(wLayout);
		wLayout.addComponent(this.addWkflowLabel, BorderLayout.Constraint.WEST);

		this.addWkButton = new Button("Add");
		this.addWkButton.addListener(this);
		wLayout.addComponent(addWkButton, BorderLayout.Constraint.EAST);
		
		wLayout = new BorderLayout();
		this.wFlowToolPanel = new Panel();
		this.wFlowToolPanel.addComponent(wLayout);
		this.gotoCaption = "Goto GenSpace page of " + this.selectedTool.getName();
		this.gotoPageLabel.setCaption(gotoCaption);
		wLayout.addComponent(this.gotoPageLabel, BorderLayout.Constraint.WEST);

		this.gotoPage = new Link("Go", new ExternalResource(RuntimeEnvironmentSettings.GS_WEB_ROOT + "tool/index/" + selectedTool.getId(), "_blank"));
		this.gotoPage.setTargetName("_blank");
		wLayout.addComponent(gotoPage, BorderLayout.Constraint.EAST);
		
		wLayout = new BorderLayout();
		this.expertPanel = new Panel();
		this.expertPanel.addComponent(wLayout);
		
		this.contact = new Button("Contact");
		
		if (expert != null) {
			this.contactCaption = "Contact expert user: " + (new UserWrapper(this.expert, this.login)).getFullName();
			this.expertLabel.setCaption(contactCaption);
			wLayout.addComponent(this.expertLabel, BorderLayout.Constraint.WEST);
			
			this.contact.addListener(this);
			wLayout.addComponent(contact, BorderLayout.Constraint.EAST);
		} else {
			this.contactCaption = "No current expert";
			this.expertLabel.setCaption(contactCaption);
			wLayout.addComponent(this.expertLabel, BorderLayout.Constraint.WEST);
			
			this.contact.setEnabled(false);
			wLayout.addComponent(contact, BorderLayout.Constraint.EAST);
		}

		wLayout = new BorderLayout();
		this.viewPanel = new Panel();
		this.viewPanel.addComponent(wLayout);
		wLayout.addComponent(this.viewComment, BorderLayout.Constraint.WEST);

		this.view = new Link("View", new ExternalResource(RuntimeEnvironmentSettings.GS_WEB_ROOT + "workflow/index/" + workflow.getId(), "_blank"));
		this.view.setTargetName("_blank");
		wLayout.addComponent(this.view, BorderLayout.Constraint.EAST);
		
		this.addWkButton.setWidth("80px");
		this.gotoPage.setWidth("80px");
		this.contact.setWidth("80px");
		this.view.setWidth("80px");
		
		this.vLayout.addComponent(wFlowToolPanel);
		this.vLayout.addComponent(expertPanel);
		this.vLayout.addComponent(workflowPanel);
		this.vLayout.addComponent(viewPanel);
		
		//If user login, they can rate workflow and tool.
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			this.toolRatePanel = new StarRatingPanel(this.login);
			this.toolRatePanel.setTitle("Rate " + this.selectedTool.getName());
			this.toolRatePanel.loadRating(this.selectedTool);
			
			this.wfRatePanel = new StarRatingPanel(this.login);
			this.wfRatePanel.setTitle("Rate workflow until here");
			this.wfRatePanel.loadRating(this.workflow);
		
			
			this.vLayout.addComponent(toolRatePanel);
			this.vLayout.addComponent(wfRatePanel);
		}
	}
	
	public void attachAllPushers() {
		if(toolRatePanel == null || wfRatePanel == null){
			
		}else{
			this.toolRatePanel.attachPusher();
			this.wfRatePanel.attachPusher();
		}
	}
	
	public void detachAndAttachListener(final Application app) {
		this.addWkButton.removeListener(this);
		this.addWkButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(Button.ClickEvent evt) {
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					app.getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
					return ;
				}
				addWorkFlowToRepository(app);
			}
		});
		
		this.contact.removeListener(this);
		this.contact.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(Button.ClickEvent evt) {
				if (login.getGenSpaceServerFactory().isLoggedIn()) {
					UserSearchWindow usw = new UserSearchWindow(expert, login, login.getGenSpaceParent().getSocialNetworkHome());
					app.getMainWindow().addWindow(usw);
				} else {
					app.getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
					return ;
				}
			}
		});
	}
	
	public void buttonClick(Button.ClickEvent evt) {
		String buttonCaption = evt.getButton().getCaption();
		
		if (buttonCaption.equals("Add")) {
			if (!login.getGenSpaceServerFactory().isLoggedIn()) {
				getApplication().getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
				return ;
			}
			addWorkFlowToRepository(getApplication());
		} else if (buttonCaption.equals("Contact")) {
			if (login.getGenSpaceServerFactory().isLoggedIn()) {
				UserSearchWindow usw = new UserSearchWindow(expert, login, login.getGenSpaceParent().getSocialNetworkHome());
				getApplication().getMainWindow().addWindow(usw);
			} else {
				getApplication().getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
				return ;
			}
		}
	} 
	
	private void addWorkFlowToRepository(final Application app) {
		final Window nameWindow = new Window();
		nameWindow.setCaption("Workflow Confirmation");
		nameWindow.setWidth("450px");
		nameWindow.setHeight("100px");
		
		app.getMainWindow().addWindow(nameWindow);
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
					app.getMainWindow().showNotification("Workflow added succesfully to repository");
				} else {
					app.getMainWindow().showNotification("Operation cancelled: A valid name has to be specified");
				}

				app.getMainWindow().removeWindow(nameWindow);
				app.getMainWindow().removeWindow(WorkflowVisualizationPopup.this);
			}
		});
		buttonLayout.addComponent(confirm);
		nLayout.addComponent(buttonLayout);
	}
}
