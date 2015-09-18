/**
 * 
 */
package org.geworkbenchweb.plugins;

import java.util.Arrays;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/**
 * @author zji
 *
 */
public class ChooseTTestResultDialog extends Window {

	private static final long serialVersionUID = -4712749272144439069L;

	private NetworkViewer networkViewer;

	public void display(NetworkViewer networkViewer) {
		this.networkViewer = networkViewer;
		Window mainWindow = networkViewer.getApplication().getMainWindow();
		if (mainWindow == null) {
			MessageBox mb = new MessageBox(getWindow(), "No main window", MessageBox.Icon.ERROR,
					"Unexpected case of no main window.", new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
			return;
		}
		mainWindow.addWindow(this);
	}

	public ChooseTTestResultDialog() {

		this.setModal(true);
		this.setClosable(true);
		((AbstractOrderedLayout) this.getContent()).setSpacing(true);
		this.setWidth("250px");
		this.setHeight("200px");
		this.setResizable(false);
		this.setCaption("Select t-test result");
		this.setImmediate(true);

		String[] nodes = { "RESULT 1", "RESULT 2", "RESULT 3" };
		final ListSelect tTestResult = new ListSelect("t-test result node:", Arrays.asList(nodes));
		tTestResult.setImmediate(true);
		tTestResult.setNullSelectionAllowed(false);
		tTestResult.setRows(5);
		tTestResult.setColumns(15);

		Button submit = new Button("Display", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				Window mainWindow = ChooseTTestResultDialog.this.getApplication().getMainWindow();
				mainWindow.removeWindow(ChooseTTestResultDialog.this);
				Object choice = tTestResult.getValue();
				if (networkViewer != null && choice != null) {
					networkViewer.displayWithTTestResult(choice.toString());
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		this.addComponent(tTestResult);
		this.addComponent(submit);
	}
};
