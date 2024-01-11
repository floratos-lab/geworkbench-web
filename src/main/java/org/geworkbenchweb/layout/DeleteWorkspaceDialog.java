package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.HashSet;
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

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.steinwedel.messagebox.MessageBox;

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

		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(workspaceSelect);
		Button deleteButton = new Button("Delete");
		deleteButton.addClickListener(this);
		layout.addComponent(deleteButton);
		this.setContent(layout);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		final Object selected = workspaceSelect.getValue();

		final UI mainWindow = UI.getCurrent();

		if (!(selected instanceof Set)) { /* not expected case */
			log.error("wrong type returned by ListSelect.getValue(): "
					+ selected);
			MessageBox.createInfo().withCaption("Error in selecting workspaces")
					.withMessage("Please select the workspace to be deleted.").withOkButton().open();
			return;
		}

		@SuppressWarnings("unchecked")
		final Set<Long> toBeDeletedId = (Set<Long>) selected;
		if (toBeDeletedId.size() == 0) {
			MessageBox.createInfo().withCaption("No workspace selected")
					.withMessage("No workspace selected to be deleted.").withOkButton().open();
			return;
		}

		/*
		 * If the current workspace is selected to be deleted, give the user the chance
		 * to cancel.
		 */
		Long currentWorkspace = uMainToolBar.getCurrentWorkspace();
		if (toBeDeletedId.contains(currentWorkspace)) {
			MessageBox.createQuestion().withCaption("Current workspace selected")
					.withMessage("You are deleting the currently open workspace. Do you want to continue?")
					.withYesButton(() -> {
						confirmDeletingWorkspace(toBeDeletedId);
					}).withNoButton(() -> {
						/* at this point, the "delete workspace" dialog should be closed. */
						mainWindow.removeWindow(DeleteWorkspaceDialog.this);
					}).open();
			return;
		}
		confirmDeletingWorkspace(toBeDeletedId);
	}

	/**
	 * Confirm to delete workspace after the user confirms to delete the current
	 * workspace,
	 * or the current workspace is not selected to be deleted.
	 */
	private void confirmDeletingWorkspace(final Set<Long> toBeDeletedId) {
		MessageBox.createInfo().withCaption("Deleting workspace")
				.withMessage("\"You are deleting one or more workspaces. Proceed?").withOkButton(() -> {
					delete(toBeDeletedId);
					UI.getCurrent().removeWindow(DeleteWorkspaceDialog.this);
				}).withCancelButton().open();
	}

	private transient Long newCurrentWorkspace = 0L;

	private void delete(Set<Long> toBeDelectedId) {

		final String defaultWorkspaceName = "Default Workspace";
		Long currentWorkspace = uMainToolBar.getCurrentWorkspace();
		int totalNumber = workspaceSelect.size(); /* the totally number of workspace of this user */
		if (toBeDelectedId.size() == totalNumber) {
			Long userId = SessionHandler.get().getId();
			/* Step 1: if there is workspace named "Default Workspace", rename them first */
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", userId);
			param.put("name", defaultWorkspaceName);
			List<Workspace> workspaces = FacadeFactory
					.getFacade()
					.list("Select p from Workspace as p where p.owner=:owner and p.name=:name",
							param);
			int counter = 1;
			for (Workspace w : workspaces) {
				w.setName(defaultWorkspaceName + "." + counter);
				FacadeFactory.getFacade().store(w);
				counter++;
			}
			/* Step 2: create a new workspace called "Default Workspace" */
			Workspace newDefaultWorksapce = new Workspace();
			newDefaultWorksapce.setOwner(userId);
			newDefaultWorksapce.setName(defaultWorkspaceName);
			FacadeFactory.getFacade().store(newDefaultWorksapce);
			/* Step 3: set the new default workspace as the current */
			switchWorkspace(newDefaultWorksapce.getId());
			/* Step 4: delete all the selected workspaces */
			for (Long id : toBeDelectedId) {
				DataSetOperations.deleteWorkspace(id);
			}
		} else if (toBeDelectedId.contains(currentWorkspace)) {
			/* Step 1: find the workspaces that are not selected */
			Set<Long> notSelected = new HashSet<Long>();
			List<Workspace> spaces = WorkspaceUtils.getAvailableWorkspaces();
			for (Workspace w : spaces) {
				Long id = w.getId();
				if (!toBeDelectedId.contains(id)) {
					notSelected.add(id);
				}
			}
			/* Step 2: choose a new current workspace */
			if (notSelected.size() == 1) { /* it must be least one */
				/* set the only unselected as the current */
				switchWorkspace(notSelected.iterator().next());
				/*
				 * Step 3: otherwise, just delete all selected (because the current workspace
				 * will not be changed
				 */
				for (Long id : toBeDelectedId) {
					DataSetOperations.deleteWorkspace(id);
				}
			} else {
				/* Step 3: actual delete must be done after setting new current workspace */
				Workspace c = FacadeFactory.getFacade().find(Workspace.class,
						currentWorkspace);
				chooseNewCurrent(notSelected, toBeDelectedId, currentWorkspace, c.getName());
				return;
			}
		} else {
			for (Long id : toBeDelectedId) {
				DataSetOperations.deleteWorkspace(id);
			}
		}
	}

	private static void switchWorkspace(Long wsid) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("owner", SessionHandler.get().getId());
		List<AbstractPojo> activeWorkspace = FacadeFactory
				.getFacade()
				.list("Select p from ActiveWorkspace as p where p.owner=:owner",
						param);
		FacadeFactory.getFacade().delete(
				(ActiveWorkspace) activeWorkspace.get(0));

		ActiveWorkspace active = new ActiveWorkspace();
		active.setOwner(SessionHandler.get().getId());
		active.setWorkspace(wsid);
		FacadeFactory.getFacade().store(active);

		/* switch GUI to the newly selected workspace */
		// FIXME no more com.vaadin.Application in vaadin 7
	}

	private void chooseNewCurrent(final Set<Long> notSelected,
			final Set<Long> toBeDelectedId,
			final Long currentWorkspaceId, final String currentWorkspaceName) {
		final ListSelect workspaceRemained = new ListSelect(
				"Choose from remained workspaces");
		workspaceRemained.setNullSelectionAllowed(false);
		workspaceRemained.setMultiSelect(false);
		workspaceRemained.setImmediate(true);
		workspaceRemained.setWidth("250px");
		workspaceRemained.setHeight("100px");

		java.sql.Timestamp t = new java.sql.Timestamp(0);
		newCurrentWorkspace = 0L;
		for (Long id : notSelected) {
			Workspace w = FacadeFactory.getFacade().find(Workspace.class, id);
			workspaceRemained.addItem(w);
			workspaceRemained.setItemCaption(w, w.getName());
			if (w.getTimestamp().after(t)) { /* remember the latest one in case the user does not select */
				t = w.getTimestamp();
				newCurrentWorkspace = w.getId();
			}
		}

		final Window chooseDialog = new Window("Choose New Current Workspace");
		chooseDialog.setModal(true);
		chooseDialog.setDraggable(false);
		chooseDialog.setResizable(false);
		chooseDialog.setWidth("300px");

		chooseDialog.addCloseListener(new CloseListener() {

			private static final long serialVersionUID = -7856367679710164681L;

			@Override
			public void windowClose(CloseEvent e) {
				switchWorkspace(newCurrentWorkspace);
				for (Long id : toBeDelectedId) {
					DataSetOperations.deleteWorkspace(id);
				}
			}

		});

		chooseDialog.setContent(workspaceRemained);

		FormLayout workspaceForm = new FormLayout();

		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = -6393819962372106745L;

			@Override
			public void buttonClick(ClickEvent event) {

				Object selected = workspaceRemained.getValue();
				if (!(selected instanceof Workspace)) {
					log.error("Selected item is not a workspace: " + selected);
					return;
				}
				Workspace ncws = (Workspace) selected;
				newCurrentWorkspace = ncws.getId();

				UI.getCurrent().removeWindow(chooseDialog);
			}
		});

		workspaceForm.setMargin(true);
		workspaceForm.setImmediate(true);
		workspaceForm.setSpacing(true);
		workspaceForm.addComponent(new Label(
				"Please choose the workspace to switch to after the current workspace '"
						+ currentWorkspaceName
						+ "' is deleted."));
		workspaceForm.addComponent(submit);

		chooseDialog.setContent(workspaceForm);
		UI.getCurrent().addWindow(chooseDialog);
	}
}
