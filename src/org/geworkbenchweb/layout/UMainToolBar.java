package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Menu Bar class which will be common for all the Visual Plugins
 * @author Nikhil
 */
public class UMainToolBar extends MenuBar {

	private static final long serialVersionUID = 1L;

	public UMainToolBar() {
		
		setImmediate(true);
		setStyleName("transparent");
		
		@SuppressWarnings("unused")
		final MenuBar.MenuItem uploadData = this.addItem("Upload  Data", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PluginEvent loadPlugin = new PluginEvent("UploadData", WorkspaceUtils.getActiveWorkSpace());
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);
			}
			
		});
		this.addItem("Tools", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PluginEvent loadPlugin = new PluginEvent("Tools", WorkspaceUtils.getActiveWorkSpace());
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);
			}
			
		});
		final MenuBar.MenuItem workspace = this.addItem("Workspaces",
				null);
		
		workspace.addItem("Create WorkSpace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				final Window newWorkspace 		= 	new Window("Create New Workspace");
				
				newWorkspace.setModal(true);
				newWorkspace.setDraggable(false);
				newWorkspace.setResizable(false);
				newWorkspace.setWidth("300px");
				
				FormLayout workspaceForm 	= 	new FormLayout();
				
				final TextField name 	= 	new TextField();
				Button submit 			= 	new Button("Submit", new Button.ClickListener() {
					
					private static final long serialVersionUID = -6393819962372106745L;

					@Override
					public void buttonClick(ClickEvent event) {
		
						Workspace workspace = 	new Workspace();
						
						workspace.setOwner(SessionHandler.get().getId());	
						workspace.setName(name.getValue().toString());
					    FacadeFactory.getFacade().store(workspace);
					    
					    Map<String, Object> param 		= 	new HashMap<String, Object>();
						param.put("owner", SessionHandler.get().getId());

						List<?> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
						FacadeFactory.getFacade().delete((ActiveWorkspace) activeWorkspace.get(0));
						
						/* Setting active workspace */
					    ActiveWorkspace active = new ActiveWorkspace();
					    active.setOwner(SessionHandler.get().getId());
					    active.setWorkspace(workspace.getId());
					    FacadeFactory.getFacade().store(active);
						
					    getApplication().getMainWindow().removeWindow(newWorkspace);
					    try {
					    	MessageBox mb = new MessageBox(getWindow(), 
					    			"New Workspace", 
					    			MessageBox.Icon.INFO, 
					    			"New Workspace is created and set as Active Workspace",  
					    			new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));

					    	mb.show();
					    	getApplication().getMainWindow().setContent(new UMainLayout());					    	

					    } catch(Exception e) {
					    	e.printStackTrace();
					    }
					}
				});
				
				name.setCaption("Enter Name");
				
				workspaceForm.setMargin(true);
				workspaceForm.setImmediate(true);
				workspaceForm.setSpacing(true);
				workspaceForm.addComponent(name);
				workspaceForm.addComponent(submit);
				
				newWorkspace.addComponent(workspaceForm);
				getApplication().getMainWindow().addWindow(newWorkspace);
				
			}
		});
		
		workspace.addItem("Switch Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				final Window workspaceTable = new Window("Switch Workspace");
				
				((AbstractOrderedLayout) workspaceTable.getLayout()).setSpacing(true);
				workspaceTable.setModal(true);
				workspaceTable.setClosable(true);
				workspaceTable.setDraggable(false);
				workspaceTable.setResizable(false);
				workspaceTable.setWidth("300px");
				workspaceTable.setHeight("300px");
				workspaceTable.setImmediate(true);
				
				ListSelect workspaceSelect = new ListSelect("Select Workspace");
				workspaceSelect.setNullSelectionAllowed(false);
				workspaceSelect.setMultiSelect(false);
				workspaceSelect.setImmediate(true);
				workspaceSelect.setWidth("250px");
				workspaceSelect.setHeight("200px");
				
				/* Adding items to the combobox */
				List<Workspace> spaces = WorkspaceUtils.getAvailableWorkspaces();
				for(int i=0; i<spaces.size(); i++) {
					workspaceSelect.addItem(spaces.get(i).getId());
					workspaceSelect.setItemCaption(spaces.get(i).getId(), spaces.get(i).getName());
				}
				
				workspaceSelect.addListener(new ListSelect.ValueChangeListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void valueChange(final ValueChangeEvent event) {

						MessageBox mb = new MessageBox(getWindow(), 
								"Switch Workspace", 
								MessageBox.Icon.INFO, 
								"Activating selected workspace",  
								new MessageBox.ButtonConfig(ButtonType.CANCEL, "Cancel"),
								new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));


						 mb.show(new MessageBox.EventListener() {
                             
		                        private static final long serialVersionUID = 1L;

		                        @Override
		                        public void buttonClicked(ButtonType buttonType) {
		                        	
		                        	if(buttonType == ButtonType.OK) {
		                        		Map<String, Object> param 		= 	new HashMap<String, Object>();
		                        		param.put("owner", SessionHandler.get().getId());

		                        		List<?> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
		                        		FacadeFactory.getFacade().delete((ActiveWorkspace) activeWorkspace.get(0));

		                        		/* Setting active workspace */
		                        		ActiveWorkspace active = new ActiveWorkspace();
		                        		active.setOwner(SessionHandler.get().getId());
		                        		active.setWorkspace((Long) event.getProperty().getValue());
		                        		FacadeFactory.getFacade().store(active);

		                        		getApplication().getMainWindow().removeWindow(workspaceTable);
		                        		getApplication().getMainWindow().setContent(new UMainLayout()); 

		                        	}
		                        }
						 });
					}

				});		
				workspaceTable.addComponent(workspaceSelect);
				getApplication().getMainWindow().addWindow(workspaceTable);
			}
		});
		
		/*workspace.addItem("Delete Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
			}
		});*/
		
		this.addItem("Account", null);
		
		this.addItem("Logout", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				SessionHandler.logout();
				getApplication().close();
			}
		});
		
	}
	
}
