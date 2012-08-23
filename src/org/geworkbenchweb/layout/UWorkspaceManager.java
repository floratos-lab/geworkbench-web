package org.geworkbenchweb.layout;

import java.util.List;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.UDataSetUpload;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.ProjectOperations;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.peter.multibutton.MultiButton;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

/**
 * UWorkspaceManager handles all the workspace details.
 * @author np2417
 */
public class UWorkspaceManager extends MultiButton {
	
	private static final long serialVersionUID = -6898992321189378943L;
	
	public UWorkspaceManager() {
		
		setCaption("WorkSpace Manager");
		setSizeFull(); 
		setPopupButtonPixelWidth(50); 
		setPopupButtonEnabled(true); 
		
		Button createWorkspace 	= 	new Button("Create New Workspace");
		Button importWorkspace 	= 	new Button("Import Workspace");
		Button createProject 	= 	new Button("Create Project");
		Button uploadDataset 	= 	new Button("Upload Dataset", new Button.ClickListener() {
			
			private static final long serialVersionUID = -6393819962372106745L;

			@Override
			public void buttonClick(ClickEvent event) {
				UDataSetUpload dataWindow = new UDataSetUpload();
				getApplication().getMainWindow().addWindow(dataWindow);
			}
		});
		Button switchWorkspace 	= 	new Button("Switch Workspace");
		
		addButton(uploadDataset);
		addButton(createProject);
		addButton(createWorkspace);
		addButton(importWorkspace);
		addButton(switchWorkspace);
		
		this.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L; 

			public void buttonClick(ClickEvent event) {

				WorkspaceWindow window = new WorkspaceWindow();
				getApplication().getMainWindow().addWindow(window);
			}
		});
	}
	
	/**
	 * Workspace Manager Window with all the options
	 */
	private class WorkspaceWindow extends Window {
	
		private static final long serialVersionUID = 867444216969708459L;
		
		private HorizontalSplitPanel workPanel;

		@SuppressWarnings("deprecation")
		private WorkspaceWindow(){
			
			center();
			setModal(true);
			setHeight("600px");
			setWidth("800px");
			setCaption("Workspace Manager");
			setClosable(true);
			setDraggable(false);
			setImmediate(true);
			setScrollable(false);
			setResizable(false);
			
			workPanel = new HorizontalSplitPanel();
	
			workPanel.setSizeFull();
			workPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
			workPanel.setSplitPosition(25);
			workPanel.setMargin(false);
			workPanel.setLocked(true);
			workPanel.setImmediate(true);
			
			final ComboBox workspaces 			= 	new ComboBox();
			VerticalLayout workSpaceLayout 	=	new VerticalLayout();
			
			Panel wActions 		= 	new Panel();
			Button createNew	=	new Button("Create Workspace", new Button.ClickListener() {
				
				private static final long serialVersionUID = -6393819962372106745L;

				@Override
				public void buttonClick(ClickEvent event) {
					
					workspaces.select(null);
					buildWorkSpaceForm();
					
				}
			});
			Button deleteWSpace = 	new Button("Delete Workspace");
			Button importWSpace	=	new Button("Import WorkSpace");
			
			createNew.setStyleName(Reindeer.BUTTON_LINK);
			deleteWSpace.setStyleName(Reindeer.BUTTON_LINK);
			importWSpace.setStyleName(Reindeer.BUTTON_LINK);
			
			wActions.setCaption("Workspace Actions");
			((AbstractOrderedLayout) wActions.getLayout()).setSpacing(true);
			wActions.addComponent(createNew);
			wActions.addComponent(deleteWSpace);
			wActions.addComponent(importWSpace);
			
			workspaces.setInputPrompt("Select Workspace");
			workspaces.setMultiSelect(false);
			workspaces.setImmediate(true);
			workspaces.setNullSelectionAllowed(false);

			/* Adding items to the combobox */
			List<Workspace> spaces = WorkspaceUtils.getAvailableWorkspaces();
			for(int i=0; i<spaces.size(); i++) {
				workspaces.addItem(spaces.get(i).getId());
				workspaces.setItemCaption(spaces.get(i).getId(), spaces.get(i).getName());
			}

			workspaces.setImmediate(true);
			workspaces.addListener(new ComboBox.ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					
					ProjectLayout layout = new ProjectLayout();
					layout.addProjectTable((Long) event.getProperty().getValue());
					workPanel.setSecondComponent(layout);
					
				}
				
			});
			
			workSpaceLayout.setMargin(true);
			workSpaceLayout.setSpacing(true);
			workSpaceLayout.addComponent(workspaces);
			workSpaceLayout.setComponentAlignment(workspaces, Alignment.TOP_LEFT);
			workSpaceLayout.addComponent(new Label("<br/>", Label.CONTENT_XHTML));
			workSpaceLayout.addComponent(wActions);
			workSpaceLayout.setComponentAlignment(wActions, Alignment.MIDDLE_CENTER);
			
			workPanel.setFirstComponent(workSpaceLayout);
			setContent(workPanel);
		}

		protected void buildWorkSpaceForm() {
			
			FormLayout workspaceForm = new FormLayout();
			
			final TextField name 	= 	new TextField();
			Button submit 			= 	new Button("Submit", new Button.ClickListener() {
				
				private static final long serialVersionUID = -6393819962372106745L;

				@Override
				public void buttonClick(ClickEvent event) {
	
					Workspace workspace = 	new Workspace();
					
					workspace.setOwner(SessionHandler.get().getId());	
					workspace.setName(name.getValue().toString());
				    FacadeFactory.getFacade().store(workspace);
				    
					
				}
			});
			
			name.setCaption("Enter Name");
			
			workspaceForm.setMargin(true);
			workspaceForm.setImmediate(true);
			workspaceForm.setSpacing(true);
			workspaceForm.addComponent(name);
			workspaceForm.addComponent(submit);
			 
			workPanel.setSecondComponent(workspaceForm);
			
		}
	}
	
	private class ProjectLayout extends VerticalLayout {

		private static final long serialVersionUID = -1202432681409804573L;
		
		private VerticalSplitPanel ProjectPanelTop;

		public ProjectLayout() {
			setSizeFull();
			setMargin(true);
			setSpacing(true);
			setImmediate(true);
		}
 
		public void addProjectTable(Long workspaceId) {
			
			ProjectPanelTop = new VerticalSplitPanel();
			
			ProjectPanelTop.setImmediate(true);
			ProjectPanelTop.setSizeFull();
			ProjectPanelTop.setStyleName(Reindeer.SPLITPANEL_SMALL);
			ProjectPanelTop.setSplitPosition(40);
			ProjectPanelTop.setLocked(true);
			
			Table projectTable = new Table();
			projectTable.setSizeFull();
			projectTable.setStyleName(Reindeer.TABLE_STRONG);
			projectTable.setSelectable(true);
			projectTable.addListener(new ItemClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void itemClick(ItemClickEvent event) {
					
					projectOperations((Long) event.getItemId());
					
				}
				
			});
			IndexedContainer dataContainer = new IndexedContainer();
			dataContainer.addContainerProperty("Project Name", String.class, null);
			dataContainer.addContainerProperty("Description", String.class, null);
			
			/* Adding projects to the Table */
			List<Project> projects = ProjectOperations.getProjects(workspaceId);
			for(int i=0; i<projects.size(); i++) {
				Item item = dataContainer.addItem(projects.get(i).getId());
				item.getItemProperty("Project Name").setValue(projects.get(i).getName());
				item.getItemProperty("Description").setValue(projects.get(i).getDescription());
			}
			
			projectTable.setContainerDataSource(dataContainer);
			ProjectPanelTop.setFirstComponent(projectTable);
			
			this.addComponent(ProjectPanelTop);
		}
		
		public void projectOperations(Long projectId) {
			
			final VerticalSplitPanel projectPanel = new VerticalSplitPanel();
			
			projectPanel.setImmediate(true);
			projectPanel.setSplitPosition(20);
			projectPanel.setLocked(true);
			projectPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
			
			final HorizontalLayout projectLayout = new HorizontalLayout();
			
			projectLayout.setSizeFull();
			projectLayout.setImmediate(true);
			projectLayout.setSpacing(true);
			projectLayout.setMargin(true);
			
			Button createNewProject 	= 	new Button("Create New Project", new Button.ClickListener() {
	
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					
					FormLayout newProject = new FormLayout();
					
					newProject.setImmediate(true);
					
					final TextField projectName 	= 	new TextField("Project Name");
					final TextField projectDes 	= 	new TextField("Project Description");
					
					Button submitProject = new Button("Submit", new Button.ClickListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(ClickEvent event) {
							
							if(projectName.getValue() == null || projectDes.getValue() == null) {
								getApplication().getMainWindow().showNotification("Please Fill ProjectName and Description", 
										Notification.TYPE_ERROR_MESSAGE);
							} else {
								
								Project newData = new Project();
								
								newData.setName((String) projectName.getValue());
								newData.setDescription((String) projectDes.getValue());
								newData.setOwner(SessionHandler.get().getId());
								newData.setWorkspaceId(WorkspaceUtils.getActiveWorkSpace());
								
								FacadeFactory.getFacade().store(newData);
								
								NodeAddEvent resultEvent = new NodeAddEvent(newData.getId(), newData.getName(), null);
								GeworkbenchRoot.getBlackboard().fire(resultEvent);							}
						}
						
					});
					
					newProject.addComponent(projectName);
					newProject.addComponent(projectDes);
					newProject.addComponent(submitProject);
					
					projectPanel.setSecondComponent(newProject);
				}
			});
			Button deleteProject 		= 	new Button("Delete Selected Project");
			Button uploadData			= 	new Button("Upload DataSet");
			
			createNewProject.setStyleName(Reindeer.BUTTON_LINK);
			deleteProject.setStyleName(Reindeer.BUTTON_LINK);
			uploadData.setStyleName(Reindeer.BUTTON_LINK);
			
			projectLayout.addComponent(createNewProject);
			projectLayout.addComponent(deleteProject);
			projectLayout.addComponent(uploadData);
			
			projectPanel.setFirstComponent(projectLayout);
			ProjectPanelTop.setSecondComponent(projectPanel);
		}
		
	}
	
}
