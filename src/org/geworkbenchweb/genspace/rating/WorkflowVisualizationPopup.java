package org.geworkbenchweb.genspace.rating;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class WorkflowVisualizationPopup extends Window {

	/*private JMenuItem addWorkflowRepository = new JMenuItem(
			"Add workflow to your repository");*/

	// we store the tool name each time the menu is invoked
	// so we can speed up the process
	private VerticalLayout vLayout;
	
	private HorizontalLayout wLayout;
	
	private Panel workflowPanel;
	
	private Label addWkflowLabel = new Label("Add workflow to your repository");
	
	private Button addWkButton;
	
	private Tool tool;
	
	private Workflow workflow;
	
	private User expert;
	
	private GenSpaceLogin login;

	public WorkflowVisualizationPopup(GenSpaceLogin login, Workflow workflow) {
		this.login = login;
		this.workflow = workflow;
		this.vLayout = new VerticalLayout();
		this.addComponent(vLayout);
		this.createWorkflowPanel();
	}
	
	private void createWorkflowPanel() {
		this.workflowPanel = new Panel();
		this.vLayout.addComponent(workflowPanel);
		
		this.wLayout = new HorizontalLayout();
		this.workflowPanel.addComponent(wLayout);
		this.wLayout.addComponent(this.addWkflowLabel);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		
		this.wLayout.addComponent(emptyLabel);
		
		this.addWkButton = new Button("Add");
		this.addWkButton.addListener(new ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					getApplication().getMainWindow().showNotification("You need to be logged in to use GenSpace's social features.");
					return ;
				}
				
				addWorkFlowToRepository();
				
			}
		});
		this.wLayout.addComponent(addWkButton);
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
				System.out.println("Attempt to close window");
				
			}
		});
		buttonLayout.addComponent(confirm);
		nLayout.addComponent(buttonLayout);
		
	}

	/*private void addWorkFlowToRepository() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				if (GenSpaceServerFactory.isLoggedIn()) {
					String name = JOptionPane.showInputDialog("Type a name for the workflow to be added:",
							"");
					if (name != null && name.trim().length() > 0) {
						UserWorkflow uw = new UserWorkflow();
						uw.setName(name);
						uw.setWorkflow(workflow);
						uw.setFolder(GenSpaceServerFactory.getUserOps().getRootFolder());
						uw.setOwner(GenSpaceServerFactory.getUser());
						
						try {
							uw.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
							GenSpaceServerFactory.getWorkflowOps().addWorkflow(uw, uw.getFolder().getId());
						} catch (Exception e) {
							GenSpaceServerFactory.handleExecutionException(e);
							return null;
						}
						GenSpace.getInstance().getWorkflowRepository().updateFormFieldsBG();
							JOptionPane
							.showMessageDialog(null,
									"Workflow added succesfully to repository");
						
					} else {
						JOptionPane
								.showMessageDialog(null,
										"Operation cancelled: A valid name has to be specified");
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"You need to be logged in to manage the repository.");
				}
				return null;
			}
		};
		worker.execute();
	}*/

}
