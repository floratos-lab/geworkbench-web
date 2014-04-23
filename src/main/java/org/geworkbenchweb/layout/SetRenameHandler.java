/**
 * 
 */
package org.geworkbenchweb.layout;

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

/**
 * @author zji
 * 
 */
public class SetRenameHandler implements Handler {

	private static final long serialVersionUID = -7195634300959844209L;

	private static Log log = LogFactory.getLog(SetRenameHandler.class);

	private final Action[] actions = new Action[] { new Action("Rename") };

	private final Tree setTree;

	SetRenameHandler(final Tree setTree) {
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
		if (!(target instanceof Long)) {
			log.debug("unexpected target type: " + target);
			return null;
		}
		return actions;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (sender != setTree) {
			log.warn("unexpected sender: " + sender);
			return;
		}
		if (!(target instanceof Long)) {
			log.error("unexpected target of action " + target);
			return;
		}
		Long itemId = (Long) target;
		rename(itemId);
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
				item.getItemProperty(SetViewLayout.SET_DISPLAY_NAME).setValue(newName+"["+size+"]");

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
