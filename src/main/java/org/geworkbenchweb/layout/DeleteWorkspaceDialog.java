package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class DeleteWorkspaceDialog extends Window implements
		Button.ClickListener {

	private static final long serialVersionUID = -5732716556041760436L;

	private static Log log = LogFactory.getLog(DeleteWorkspaceDialog.class);

	private final ListSelect workspaceSelect = new ListSelect(
			"Select Workspace");

	final UMainToolBar uMainToolBar;

	DeleteWorkspaceDialog(String caption, final UMainToolBar uMainToolBar) {
		super(caption);

		this.uMainToolBar = uMainToolBar;

		((AbstractOrderedLayout) this.getContent()).setSpacing(true);
		this.setModal(true);
		this.setClosable(true);
		this.setDraggable(false);
		this.setResizable(false);
		this.setWidth("300px");
		this.setHeight("300px");
		this.setImmediate(true);

		workspaceSelect.setNullSelectionAllowed(false);

		/*
		 * this is explicitly required per mantis 3406
		 */
		workspaceSelect.setMultiSelect(true);

		workspaceSelect.setImmediate(true);
		workspaceSelect.setWidth("250px");
		workspaceSelect.setHeight("100px");

		/* Adding items to the combobox */
		List<Workspace> spaces = WorkspaceUtils.getAvailableWorkspaces();
		for (int i = 0; i < spaces.size(); i++) {
			workspaceSelect.addItem(spaces.get(i).getId());
			workspaceSelect.setItemCaption(spaces.get(i).getId(), spaces.get(i)
					.getName());
		}

		this.addComponent(workspaceSelect);
		Button deleteButton = new Button("Delete");
		deleteButton.addListener(this);
		this.addComponent(deleteButton);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		final Object selected = workspaceSelect.getValue();

		Application app = DeleteWorkspaceDialog.this.getApplication();
		Window mainWIndow = app.getMainWindow();

		if (!(selected instanceof Set)) { /* not expected case */
			log.error("wrong type returned by ListSelect.getValue(): "
					+ selected);
			MessageBox mb = new MessageBox(mainWIndow,
					"Error in selecting workspaces", MessageBox.Icon.INFO,
					"Please select the workspace to be deleted.",
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}

		@SuppressWarnings("unchecked")
		final Set<Long> toBeDeletedId = (Set<Long>) selected;
		if (toBeDeletedId.size() == 0) {
			MessageBox mb = new MessageBox(mainWIndow, "No workspace selected",
					MessageBox.Icon.INFO,
					"No worspace selected to be be deleted.",
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}

		MessageBox mb = new MessageBox(mainWIndow, "Deleting workspace",
				MessageBox.Icon.INFO,
				"The selected workspace will be deleted. Proceed?",
				new MessageBox.ButtonConfig(ButtonType.OK, "Ok"),
				new MessageBox.ButtonConfig(ButtonType.CANCEL, "Cancel"));
		mb.show(new MessageBox.EventListener() {

			private static final long serialVersionUID = 5532787363755213673L;

			@Override
			public void buttonClicked(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					delete(toBeDeletedId);
					Application app = getApplication();
					Window mainWindow = app.getMainWindow();
					mainWindow.removeWindow(DeleteWorkspaceDialog.this);
				}
			}

		});

	}

	private void delete(Set<Long> toBeDelectedId) {
		Long currentWorkspace = uMainToolBar.getCurrentWorkspace();
		if (toBeDelectedId.contains(currentWorkspace)) {
			Workspace c = FacadeFactory.getFacade().find(Workspace.class,
					currentWorkspace);
			String extraMessage = "You choose to delete the current workspace called '"
					+ c.getName()
					+ "', please enter a name for the new workspace.";
			createNewWorkspace(extraMessage);
		}
		for (Long id : toBeDelectedId) {
			DataSetOperations.deleteWorkspace(id);
		}

	}

	// TODO this is the same as the one in UMainToolBar. maybe better be
	// refactored.
	private void createNewWorkspace(String extraMessage) {
		Application app = getApplication();
		final Window mainWindow = app.getMainWindow();

		final Window newWorkspace = new Window("Create New Workspace");

		newWorkspace.setModal(true);
		newWorkspace.setDraggable(false);
		newWorkspace.setResizable(false);
		newWorkspace.setWidth("300px");

		FormLayout workspaceForm = new FormLayout();

		final TextField name = new TextField();
		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = -6393819962372106745L;

			@Override
			public void buttonClick(ClickEvent event) {

				Workspace workspace = new Workspace();

				workspace.setOwner(SessionHandler.get().getId());
				workspace.setName(name.getValue().toString());
				FacadeFactory.getFacade().store(workspace);

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("owner", SessionHandler.get().getId());

				List<AbstractPojo> activeWorkspace = FacadeFactory
						.getFacade()
						.list("Select p from ActiveWorkspace as p where p.owner=:owner",
								param);
				FacadeFactory.getFacade().delete(
						(ActiveWorkspace) activeWorkspace.get(0));

				/* Setting active workspace */
				ActiveWorkspace active = new ActiveWorkspace();
				active.setOwner(SessionHandler.get().getId());
				active.setWorkspace(workspace.getId());
				FacadeFactory.getFacade().store(active);

				mainWindow.removeWindow(newWorkspace);
				try {
					MessageBox mb = new MessageBox(
							mainWindow,
							"New Workspace",
							MessageBox.Icon.INFO,
							"New Workspace is created and set as Active Workspace",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));

					mb.show();
					mainWindow.setContent(new UMainLayout());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		name.setCaption("Enter Name");

		workspaceForm.setMargin(true);
		workspaceForm.setImmediate(true);
		workspaceForm.setSpacing(true);
		workspaceForm.addComponent(new Label(extraMessage));
		workspaceForm.addComponent(name);
		workspaceForm.addComponent(submit);

		newWorkspace.addComponent(workspaceForm);
		mainWindow.addWindow(newWorkspace);
	}
}
