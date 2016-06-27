package org.geworkbenchweb.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

public class SetTreeHandler implements Handler {

	private static final long serialVersionUID = -7195634300959844209L;

	private static Log log = LogFactory.getLog(SetTreeHandler.class);

	private final Action[] actions = new Action[] { new Action("Rename") };
	private final Action DELETE = new Action("Delete");

	private final Tree setTree;

	SetTreeHandler(final Tree setTree) {
		this.setTree = setTree;
		setTree.addActionHandler(this);
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (sender != setTree) {
			log.warn("unexpected sender: " + sender);
			return null;
		}
		if(target==null) return null;
		if (target instanceof Long) {
			return actions;
		} else if (target instanceof String ) {
			if(setTree.hasChildren(target)) {
				// System.out.println(setTree.getChildren(target).size());
				return null;
			}
			// only if it is string and has no child, it is the leaf node that we can delete
			return new Action[] { DELETE };
		} else {
			log.debug("unexpected target type: " + target+" "+target.getClass().getName());
			return null;
		}
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (sender != setTree) {
			log.warn("unexpected sender: " + sender);
			return;
		}
		if (target instanceof Long) {
			Long itemId = (Long) target;
			rename(itemId);
		} else if(target instanceof String && !setTree.hasChildren(target)) {
			Long subsetId = (Long) setTree.getParent(target);
			// target is where you right-click, not what we want to delete
			// instead, the selection is what we want to delete (because we want to support multi-selection
			@SuppressWarnings("unchecked")
			Set<String> selected = (Set<String>) setTree.getValue();
			List<String> namesToBeDeleted = new ArrayList<String>();
			for (String itemId : selected) {
				Item item = setTree.getItem(itemId);
				String name = (String) item.getItemProperty(SetViewLayout.ELEMENT_NAME).getValue();
				namesToBeDeleted.add(name);
				setTree.removeItem(itemId);
			}
			Item subsetItem = setTree.getItem(subsetId);
			Collection<?> children = setTree.getChildren(subsetId);
			if(children!=null && children.size()>0) {
				String subsetName = (String) subsetItem.getItemProperty(SetViewLayout.SUBSET_NAME).getValue();
				subsetItem.getItemProperty(SetViewLayout.SET_DISPLAY_NAME)
						.setValue(subsetName + " [" + setTree.getChildren(subsetId).size() + "]");
				// there is no JPA object for each item. They are a list in SubSet.
				SubSet subset = FacadeFactory.getFacade().find(SubSet.class, subsetId);
				subset.getPositions().removeAll(namesToBeDeleted);
				FacadeFactory.getFacade().store(subset);
			} else { // all children deleted, let's delete the set itself
				setTree.removeItem(subsetId);
				SubSet subset = FacadeFactory.getFacade().find(SubSet.class, subsetId);
				FacadeFactory.getFacade().delete(subset);
			}
		} else {
			log.error("unexpected target of action " + target+" "+target.getClass().getName());
			return;
		}
	}

	private void rename(final Long itemId) {
		final SubSet labelSet = FacadeFactory.getFacade().find(SubSet.class,
				itemId);
		if (labelSet == null) {
			log.warn("itemId not supported for renaming: "+itemId);
			return;
		}

		final Window dialog = new Window();
		dialog.setModal(true);
		dialog.setClosable(true);
		dialog.setWidth("300px");
		dialog.setHeight("150px");
		dialog.setResizable(false);
		dialog.setCaption("Rename a set");
		dialog.setImmediate(true);

		final Item item = setTree.getItem(itemId);
		final TextField newName = new TextField();
		newName.setValue(labelSet.getName());
		newName.setImmediate(true);

		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (newName.getValue() == null)
					return;

				labelSet.setName(newName.toString());
				FacadeFactory.getFacade().store(labelSet);

				int size = labelSet.getPositions().size();
				item.getItemProperty(SetViewLayout.SUBSET_NAME).setValue(newName);
				item.getItemProperty(SetViewLayout.SET_DISPLAY_NAME).setValue(newName+" ["+size+"]");

				setTree.getApplication().getMainWindow()
						.removeWindow(dialog);
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		dialog.addComponent(newName);
		dialog.addComponent(submit);
		setTree.getApplication().getMainWindow().addWindow(dialog);

	}
}
