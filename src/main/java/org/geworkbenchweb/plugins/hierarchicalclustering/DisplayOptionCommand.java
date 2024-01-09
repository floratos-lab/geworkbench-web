package org.geworkbenchweb.plugins.hierarchicalclustering;

import org.geworkbenchweb.visualizations.Dendrogram;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class DisplayOptionCommand implements Command {

	private static final long serialVersionUID = 1348332795447686854L;

	public enum SetType {
		MARKER, MICROARRAY
	};

	final private String caption;
	final private Component parent;
	final private SetType setType;

	private static String arrayPos = "Bottom";
	private static String markerPos = "Right";
	private boolean choseArray = false, choseMarker = false;

	final private Dendrogram dendrogram;

	DisplayOptionCommand(final String caption, final Component parent, SetType setType, final Dendrogram dendrogram) {
		this.caption = caption;
		this.parent = parent;
		this.setType = setType;
		this.dendrogram = dendrogram;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		final Window nameWindow = new Window();
		nameWindow.setModal(true);
		nameWindow.setClosable(true);
		((AbstractOrderedLayout) nameWindow.getContent()).setSpacing(true);
		nameWindow.setWidth("300px");
		nameWindow.setHeight("150px");
		nameWindow.setResizable(false);
		nameWindow.setCaption(caption);
		nameWindow.setImmediate(true);

		choseArray = false;
		choseMarker = false;
		final OptionGroup displayOption = new OptionGroup("Display Option");
		if (setType == SetType.MARKER) {
			choseMarker = true;
			displayOption.addItem("Right");
			displayOption.addItem("Left");
			displayOption.select("Right");
		} else { // MICROARRAY
			choseArray = true;
			displayOption.addItem("Top");
			displayOption.addItem("Bottom");
			displayOption.select("Bottom");
		}

		if (parent == null)
			return; // parent should never be null
		final UI mainWindow = UI.getCurrent();

		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (choseArray)
					arrayPos = (String) displayOption.getValue();
				else if (choseMarker)
					markerPos = (String) displayOption.getValue();
				dendrogram.refresh(arrayPos, markerPos);
				mainWindow.removeWindow(nameWindow);
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(displayOption);
		layout.addComponent(submit);
		nameWindow.setContent(layout);
		mainWindow.addWindow(nameWindow);
	}

}
