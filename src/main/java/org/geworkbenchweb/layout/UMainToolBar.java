package org.geworkbenchweb.layout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.authentication.UUserAuth;
import org.geworkbenchweb.plugins.tabularview.TabularViewUI;
import org.geworkbenchweb.plugins.uploaddata.UploadDataUI;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.UserActivityLog;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.MessageBox;

/**
 * Main Menu Bar on the right-hand side of the application.
 */
public class UMainToolBar extends MenuBar {

	private static Log log = LogFactory.getLog(UMainToolBar.class);

	private static final long serialVersionUID = 1L;
	private final VisualPluginView pluginView;
	private UploadDataUI uploadDataUI;

	private Long currentWorkspace; /*
									 * the practice of always querying db for
									 * active workspace does not make sense
									 */
	private String username;
	private String password;

	public UMainToolBar(final VisualPluginView pluginView) {

		this.pluginView = pluginView;
		setImmediate(true);
		setStyleName("transparent");

		@SuppressWarnings("unused")
		final MenuBar.MenuItem uploadData = this.addItem("Upload  Data",
				new Command() {
					private static final long serialVersionUID = 1L;

					@Override
					public void menuSelected(MenuItem selectedItem) {
						if (uploadPending()) {
							MessageBox.createInfo().withCaption("Upload in progress")
									.withMessage("Data upload is in progress.").withOkButton().open();
						} else {
							uploadDataUI = new UploadDataUI();
							UMainToolBar.this.pluginView.setContent(
									uploadDataUI, "Upload Data",
									"Please use this interface to upload data");
						}
					}

				});
		MenuItem tools = this.addItem("Tools", null);
		tools.addItem("All Tools", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				UMainToolBar.this.pluginView.showToolList();
			}

		});
		tools.addItem("Standalone Tools", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				UMainToolBar.this.pluginView.showStandaloneTools();
			}

		});
		final MenuBar.MenuItem workspace = this.addItem("Workspaces", null);

		workspace.addItem("Create Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {

				final Window newWorkspace = new Window("Create New Workspace");

				newWorkspace.setModal(true);
				newWorkspace.setDraggable(false);
				newWorkspace.setResizable(false);
				newWorkspace.setWidth("300px");

				FormLayout workspaceForm = new FormLayout();

				final TextField name = new TextField();
				Button submit = new Button("Submit",
						new Button.ClickListener() {

							private static final long serialVersionUID = -6393819962372106745L;

							@Override
							public void buttonClick(ClickEvent event) {

								Workspace workspace = new Workspace();

								workspace
										.setOwner(SessionHandler.get().getId());
								workspace.setName(name.getValue().toString());
								FacadeFactory.getFacade().store(workspace);

								Map<String, Object> param = new HashMap<String, Object>();
								param.put("owner", SessionHandler.get().getId());

								List<AbstractPojo> activeWorkspace = FacadeFactory
										.getFacade()
										.list("Select p from ActiveWorkspace as p where p.owner=:owner",
												param);
								FacadeFactory.getFacade().delete(
										(ActiveWorkspace) activeWorkspace
												.get(0));

								/* Setting active workspace */
								ActiveWorkspace active = new ActiveWorkspace();
								active.setOwner(SessionHandler.get().getId());
								active.setWorkspace(workspace.getId());
								FacadeFactory.getFacade().store(active);

								UI.getCurrent().removeWindow(newWorkspace);
								try {
									MessageBox.createInfo().withCaption("New Workspace")
											.withMessage("New Workspace is created and set as Active Workspace.")
											.withOkButton().open();
									UI app = UI.getCurrent();
									if (app instanceof GeworkbenchRoot) {
										((GeworkbenchRoot) app)
												.createNewMainLayout();
									} else {
										log.error("application is not GeworkbenchRoot: "
												+ app);
									}
								} catch (Exception e) {
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

			@SuppressWarnings("deprecation")
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
				List<Workspace> spaces = WorkspaceUtils
						.getAvailableWorkspaces();
				for (int i = 0; i < spaces.size(); i++) {
					workspaceSelect.addItem(spaces.get(i).getId());
					workspaceSelect.setItemCaption(spaces.get(i).getId(),
							spaces.get(i).getName());
				}

				workspaceSelect
						.addListener(new ListSelect.ValueChangeListener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void valueChange(final ValueChangeEvent event) {
								MessageBox.createInfo().withCaption("Switch Workspace")
										.withMessage("Activating selected workspace").withCancelButton()
										.withOkButton(() -> {
											Map<String, Object> param = new HashMap<String, Object>();
											param.put("owner", SessionHandler
													.get().getId());

											List<ActiveWorkspace> activeWorkspace = FacadeFactory
													.getFacade()
													.list("Select p from ActiveWorkspace as p where p.owner=:owner",
															param);
											FacadeFactory.getFacade().delete(
													activeWorkspace.get(0));
											currentWorkspace = activeWorkspace
													.get(0).getWorkspace();

											/* Setting active workspace */
											ActiveWorkspace active = new ActiveWorkspace();
											active.setOwner(SessionHandler
													.get().getId());
											active.setWorkspace((Long) event
													.getProperty().getValue());
											FacadeFactory.getFacade().store(
													active);

											UI.getCurrent().removeWindow(workspaceTable);
											UI app = UI.getCurrent();
											if (app instanceof GeworkbenchRoot) {
												((GeworkbenchRoot) app)
														.createNewMainLayout();
											} else {
												log.error("application is not GeworkbenchRoot: "
														+ app);
											}
										}).open();
							}

						});
				workspaceTable.setContent(workspaceSelect);
				UI.getCurrent().addWindow(workspaceTable);
			}
		});

		workspace.addItem("Delete Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				DeleteWorkspaceDialog dialog = new DeleteWorkspaceDialog(
						"Delete Workspace", UMainToolBar.this);
				UI.getCurrent().addWindow(dialog);
			}
		});

		this.addItem("Account", new Command() {
			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				AccountUI accountUI = new AccountUI();
				UMainToolBar.this.pluginView.setContent(accountUI, "Account",
						"Update Account");
			}
		});

		final MenuBar.MenuItem aboutItem = this.addItem("About", null);
		buildAboutMenuItem(aboutItem);

		this.addItem("Logout", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {

				if (uploadPending()) {
					MessageBox.createInfo().withCaption("Logout confirmation").withMessage(
							"File upload is in progress. Logging out will cancel it. Do you really want to log out?")
							.withYesButton(() -> {
								if (uploadDataUI != null)
									uploadDataUI.cancelUpload(); // TODO this needs to be reviewed: whether it should be
																	// allowed to be null or not
								logout();
							}).withNoButton().open();
				} else {
					logout();
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
		log.debug("current workspace ID " + currentWorkspace);
	}

	private void buildAboutMenuItem(MenuItem aboutItem) {
		aboutItem.addItem("geWorkbench-web", new Command() {

			private static final long serialVersionUID = -7959889051119455878L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				/*
				 * the text is short so it seems better to have a message box, but the
				 * requirement specifies the main GUI area.
				 */
				// MessageBox mb = new MessageBox(getWindow(), "About", Icon.INFO,
				// "Release number 1.0.0 beta", new
				// MessageBox.ButtonConfig(MessageBox.ButtonType.OK,
				// "Ok"));
				// mb.show();
				pluginView.showAboutInfo();
			}

		});
		aboutItem.addItem("Quick Intro", new Command() {

			private static final long serialVersionUID = 2634675198032992450L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				pluginView.showWeclomeScreen();
			}

		});
		aboutItem.addItem("Contact Us", new Command() {

			private static final long serialVersionUID = 2634675198032992450L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				try {
					String emailURL = "mailto:geworkbench@c2b2.columbia.edu?subject=user inquiry&body=Please feel free to contact us to report any problems encountered when using the application or to offer suggestions for functionality improvements. When reporting problems please describe the sequence of actions that led to the issue you are reporting in as much detail as possible, this will help us replicate it in our environment.";
					URL windowURL = new URL(emailURL);
					Page.getCurrent().open(new ExternalResource(windowURL), "_blank", false);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		});

	}

	private void logout() {
		// clearTabularView. TODO review: why is this necessary?
		Iterator<Component> it = pluginView.iterator();
		while (it.hasNext()) {
			Component c = it.next();
			if (c instanceof TabularViewUI) {
				((TabularViewUI) c).clearTable();
			}
		}

		SessionHandler.logout();
		UI.getCurrent().setContent(new UUserAuth());
		UserActivityLog ual = new UserActivityLog(username, UserActivityLog.ACTIVITY_TYPE.LOG_OUT.toString(), null);
		FacadeFactory.getFacade().store(ual);
	}

	private boolean uploadPending() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("owner", SessionHandler.get().getId());
		parameters.put("name", "% - Pending");
		List<DataSet> datasets = FacadeFactory
				.getFacade()
				.list("Select d from DataSet d where d.owner=:owner and d.name like :name",
						parameters);
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
