package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.genspace.GenSpaceServerFactory;
import org.geworkbenchweb.genspace.GenspaceLogger;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.ui.GenspaceLayout;
import org.geworkbenchweb.genspace.ui.chat.RosterFrame;
import org.geworkbenchweb.plugins.tabularview.TabularViewUI;
import org.geworkbenchweb.plugins.uploaddata.UploadDataUI;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * Main Menu Bar on the right-hand side of the application.
 * @author Nikhil
 */
public class UMainToolBar extends MenuBar {

	private static final long serialVersionUID = 1L;
	private final VisualPluginView pluginView;
	private UploadDataUI uploadDataUI;

	private Long currentWorkspace; /* the practice of always querying db for active workspace does not make sense */
	

	private String username;
	private String password;
	

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
					MessageBox mb = new MessageBox(getWindow(), 
							"Upload in progress", 
							MessageBox.Icon.INFO, 
							"Data upload is in progress. ",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
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

		                        		List<ActiveWorkspace> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
		                        		FacadeFactory.getFacade().delete(activeWorkspace.get(0));
		                        		currentWorkspace = activeWorkspace.get(0).getWorkspace();

		                        		/* Setting active workspace */
		                        		ActiveWorkspace active = new ActiveWorkspace();
		                        		active.setOwner(SessionHandler.get().getId());
		                        		active.setWorkspace((Long) event.getProperty().getValue());
		                        		FacadeFactory.getFacade().store(active);

		                        		getApplication().getMainWindow().removeWindow(workspaceTable);
		                        		try {
											getApplication().getMainWindow().setContent(new UMainLayout());
										} catch (Exception e) {
											e.printStackTrace();
										} 

		                        	}
		                        }
						 });
					}

				});		
				workspaceTable.addComponent(workspaceSelect);
				getApplication().getMainWindow().addWindow(workspaceTable);
			}
		});
		
		workspace.addItem("Delete Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				DeleteWorkspaceDialog dialog = new DeleteWorkspaceDialog("Delete Workspace", UMainToolBar.this);
				Application app = getApplication();
				Window mainWindow = app.getMainWindow();
				mainWindow.addWindow(dialog);
			}
		});
		
		this.addItem("Account", new Command() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void menuSelected(MenuItem selectedItem) {
				AccountUI accountUI = new AccountUI();
				UMainToolBar.this.pluginView.setContent(accountUI, "Account", "Update Account");
			}
		});
		
		/* Add an entry to genSpace */
		this.addItem("genSpace", new Command() {
			private static final long serialVersionUID = 1L;

			@Override 
			public void menuSelected(MenuItem selectedItem) {	
				RosterFrame rf = genSpaceLogger.getGenSpaceLogin().getRf();
				if (rf != null) {
					GenSpaceWindow.getGenSpaceBlackboard().removeListener(rf);
				}
				ICEPush pusher = new ICEPush();
				GenspaceLayout layout = new GenspaceLayout(genSpaceLogger, pusher);
				UMainToolBar.this.pluginView.showGenSpace(layout);
				layout.getGenSpaceLogin_1().setUIMainWindow(getApplication().getMainWindow());
				
				if (!layout.getGenSpaceLogin_1().autoLogin(username, password)) {
					layout.getGenSpaceLogin_1().authorize();
				}
				
			}
		});
		
		this.addItem("Logout", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				if (uploadPending()) {
					MessageBox mb = new MessageBox(
							getWindow(),
							"Logout confirmation",
							MessageBox.Icon.QUESTION,
							"File upload is in progress. Logging out will cancel it. Do you really want to log out?",
							new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
							new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
					mb.show(new MessageBox.EventListener() {
						private static final long serialVersionUID = -7400025137319016325L;
						@Override
						public void buttonClicked(ButtonType buttonType) {
							if (buttonType.toString() == "YES") {
								uploadDataUI.cancelUpload();
								
								GenSpaceServerFactory tmpServer = genSpaceLogger.getGenSpaceLogin().getGenSpaceServerFactory();
								if (tmpServer != null) {
									//Push a "logout" message
									GenSpaceWindow.getGenSpaceBlackboard().fire(
											new ChatStatusChangeEvent(tmpServer.getUsername()));
								}
								
								clearTabularView();
								SessionHandler.logout();
								getApplication().close();
							}
						}
					});
				}else{
					/* FIXME genSpace team can only fix this by Friday, so I have to make this temporary change so the normal development and test can be carried out.*/
					/*
					GenSpaceServerFactory tmpServer = genSpaceLogger.getGenSpaceLogin().getGenSpaceServerFactory();
					if (tmpServer != null) {
						//Push a "logout" message
						GenSpaceWindow.getGenSpaceBlackboard().fire(
								new ChatStatusChangeEvent(tmpServer.getUsername()));
					}
					*/
					clearTabularView();
					SessionHandler.logout();
					getApplication().close();
				}
			}
		});
	
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("owner", SessionHandler.get().getId());
		List<ActiveWorkspace> list = FacadeFactory.getFacade().list(
				"Select p from ActiveWorkspace as p where p.owner=:owner",
				param);
		/* list size must be 1 */
		currentWorkspace = list.get(0).getWorkspace();
	}
	
	private void clearTabularView(){
		Iterator<Component> it = pluginView.getComponentIterator();
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

	public Long getCurrentWorkspace() {
		return currentWorkspace;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public String getUsername() {
		return this.username;
	}


	public String getPassword() {
		return this.password;
	}
	
}
