/**
 * 
 */
package org.geworkbenchweb.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.Icon;

/**
 * @author zji
 * 
 */
public class RenameHandler implements Handler {

	private static Log log = LogFactory.getLog(RenameHandler.class);

	private static final long serialVersionUID = 7610842227721909245L;

	private final Action[] actions = new Action[] { new Action("Rename") };

	private final NavigationTree navigationTree;

	RenameHandler(final NavigationTree navigationTree) {
		this.navigationTree = navigationTree;
		navigationTree.addActionHandler(this);
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (sender != navigationTree) {
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
		if (sender != navigationTree) {
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
		final DataSet data = FacadeFactory.getFacade().find(DataSet.class,
				itemId);
		final ResultSet result;
		if (data == null) {
			result = FacadeFactory.getFacade().find(ResultSet.class, itemId);
			if (result == null) {
				log.warn("itemId not supported for renaming: "+itemId);
				return;
			}
		} else {
			result = null;
		}

		final Window dialog = new Window();
		dialog.setModal(true);
		dialog.setClosable(true);
		dialog.setWidth("300px");
		dialog.setHeight("150px");
		dialog.setResizable(false);
		dialog.setCaption("Rename of dataset");
		dialog.setImmediate(true);

		final Item item = navigationTree.getItem(itemId);
		String oldName = item.getItemProperty("Name").getValue().toString();
		if (oldName.contains("Pending")) {
			MessageBox mb = new MessageBox(navigationTree.getWindow(),
					"Pending node cannot be renamed.", Icon.ERROR,
					"You cannot rename a pending node.",
					new MessageBox.ButtonConfig(MessageBox.ButtonType.OK, "OK"));
			mb.show();
			return;
		}
		final TextField newName = new TextField();
		newName.setInputPrompt(oldName);
		newName.setImmediate(true);

		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (newName.getValue() == null)
					return;

				if (data != null) {
					data.setName(newName.toString());
					FacadeFactory.getFacade().store(data);
				} else {
					result.setName(newName.toString());
					FacadeFactory.getFacade().store(result);
				}

				item.getItemProperty("Name").setValue(newName);

				navigationTree.getApplication().getMainWindow()
						.removeWindow(dialog);
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		dialog.addComponent(newName);
		dialog.addComponent(submit);
		navigationTree.getApplication().getMainWindow().addWindow(dialog);

	}
}
