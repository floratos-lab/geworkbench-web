package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.plugins.tabularview.TabularViewUI;
import org.geworkbenchweb.plugins.uploaddata.UploadDataUI;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.LayoutUtil;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;
import de.steinwedel.messagebox.MessageBoxListener;

/**
 * Menu Bar class which will be common for all the Visual Plugins
 * @author Nikhil
 */
public class UMainToolBar extends MenuBar {

	private static final long serialVersionUID = 1L;
	private final VisualPluginView pluginView;
	private UploadDataUI uploadDataUI;

	public UMainToolBar(final VisualPluginView pluginView, final GenspaceLogger genSpaceLogger) {
		this.pluginView = pluginView;
		
		setImmediate(true);
		setStyleName("transparent");
		
		@SuppressWarnings("unused")
		final MenuBar.MenuItem uploadData = this.addItem("Upload  Data", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				if (uploadPending()) {
					MessageBox.showPlain(Icon.INFO, 
							"Upload in progress", 
							"Data upload is in progress. ",
							ButtonId.OK);
				}else{
					uploadDataUI = new UploadDataUI();
					UMainToolBar.this.pluginView.setContent(uploadDataUI, "Upload Data", "Please use this interface to upload data");
				}
			}
			
		});
		this.addItem("Tools", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				UMainToolBar.this.pluginView.showToolList();
			}
			
		});
		final MenuBar.MenuItem workspace = this.addItem("Workspaces",
				null);
		
		workspace.addItem("Create Workspace", new Command() {

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

						List<AbstractPojo> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
						FacadeFactory.getFacade().delete((ActiveWorkspace) activeWorkspace.get(0));
						
						/* Setting active workspace */
					    ActiveWorkspace active = new ActiveWorkspace();
					    active.setOwner(SessionHandler.get().getId());
					    active.setWorkspace(workspace.getId());
					    FacadeFactory.getFacade().store(active);
						
					    UI.getCurrent().removeWindow(newWorkspace);
					    try {
					    	MessageBox.showPlain(Icon.INFO, 
					    			"New Workspace", 
					    			"New Workspace is created and set as Active Workspace",  
					    			ButtonId.OK);
					    	UI.getCurrent().setContent(new UMainLayout());				    	

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
				
				newWorkspace.setContent(workspaceForm);
				UI.getCurrent().addWindow(newWorkspace);
				
			}
		});
		
		workspace.addItem("Switch Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				final Window workspaceTable = new Window("Switch Workspace");
				
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
				
				workspaceSelect.addValueChangeListener(new ListSelect.ValueChangeListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void valueChange(final ValueChangeEvent event) {

						MessageBox.showPlain(Icon.INFO, 
								"Switch Workspace", 
								"Activating selected workspace", 
								new MessageBoxListener() {

									@Override
									public void buttonClicked(ButtonId buttonId) {
			                        	if(buttonId == ButtonId.OK) {
			                        		Map<String, Object> param 		= 	new HashMap<String, Object>();
			                        		param.put("owner", SessionHandler.get().getId());

			                        		List<AbstractPojo> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
			                        		FacadeFactory.getFacade().delete((ActiveWorkspace) activeWorkspace.get(0));

			                        		/* Setting active workspace */
			                        		ActiveWorkspace active = new ActiveWorkspace();
			                        		active.setOwner(SessionHandler.get().getId());
			                        		active.setWorkspace((Long) event.getProperty().getValue());
			                        		FacadeFactory.getFacade().store(active);

			                        		UI.getCurrent().removeWindow(workspaceTable);
			                        		UI.getCurrent().setContent(new UMainLayout()); 

			                        	}
									}
								},
								ButtonId.CANCEL,
								ButtonId.OK);
					}

				});		
				workspaceTable.setContent(LayoutUtil.addComponent(workspaceSelect));
				UI.getCurrent().addWindow(workspaceTable);
			}
		});
		
		/*workspace.addItem("Delete Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
			}
		});*/
		
		this.addItem("Account", null);
		
		/* Add an entry to genSpace */
		this.addItem("genSpace", new Command() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void menuSelected(MenuItem selectedItem) {
				GenSpaceWindow genSpaceWindow = new GenSpaceWindow(genSpaceLogger);
				genSpaceWindow.setWidth("70%");
				genSpaceWindow.setHeight("70%");
				UI.getCurrent().addWindow(genSpaceWindow);
			}
		});
		
		this.addItem("Logout", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				if (uploadPending()) {
					MessageBox.showPlain(
							Icon.QUESTION,
							"Logout confirmation",
							"File upload is in progress. Logging out will cancel it. Do you really want to log out?",
							new MessageBoxListener() {
								@Override
								public void buttonClicked(ButtonId buttonId) {
									if (buttonId == ButtonId.YES) {
										uploadDataUI.cancelUpload();
										clearTabularView();
										SessionHandler.logout();
										Page.getCurrent().setLocation("/geworkbench/");
										VaadinSession.getCurrent().close();
									}
								}
							},
							ButtonId.YES,
							ButtonId.NO);
				}else{
					clearTabularView();
					SessionHandler.logout();
					Page.getCurrent().setLocation("/geworkbench/");
					VaadinSession.getCurrent().close();
				}
			}
		});
		
	}
	
	private void clearTabularView(){
		Iterator<Component> it = pluginView.iterator();
		while(it.hasNext()){
			Component c = it.next();
			if(c instanceof TabularViewUI){
				((TabularViewUI)c).clearTable();
			}
		}
	}

	private boolean uploadPending(){
		Map<String, Object> parameters = new HashMap<String, Object>();	
		parameters.put("owner", SessionHandler.get().getId());	
		parameters.put("name", "% - Pending");
		List<DataSet> datasets = FacadeFactory.getFacade().list(
				"Select d from DataSet d where d.owner=:owner and d.name like :name", parameters);
		return !datasets.isEmpty();
	}

}
