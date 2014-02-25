/**
 * 
 */
package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.util.ArrayList;

import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.visualizations.Dendrogram;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * 
 * Command to create subset. Used by hierarchical clustering result UI.
 * 
 * @author zji
 * 
 */
public class SubsetCommand implements Command {

	private static final long serialVersionUID = 1348332795447686854L;
	
	public enum SetType {MARKER, MICROARRAY};

	final private String caption;
	final private Component parent;
	final private SetType setType;
	final private Long parentId;
	
	final private Dendrogram dendrogram;
	
	SubsetCommand(final String caption, final Component parent, SetType setType, Long parentId, final Dendrogram dendrogram) {
		this.caption = caption;
		this.parent = parent;
		this.setType = setType;
		this.parentId = parentId;
		this.dendrogram = dendrogram;
	}
	
	@SuppressWarnings("deprecation") // for getLayout()
	@Override
	public void menuSelected(MenuItem selectedItem) {
		final Window nameWindow = new Window();
		nameWindow.setModal(true);
		nameWindow.setClosable(true);
		((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
		nameWindow.setWidth("300px");
		nameWindow.setHeight("150px");
		nameWindow.setResizable(false);
		nameWindow.setCaption(caption);
		nameWindow.setImmediate(true);

		final TextField setName = new TextField();
		setName.setInputPrompt("Please enter set name");
		setName.setImmediate(true);

		if(parent==null) return; // parent should never be null
		final Window mainWindow = parent.getApplication().getMainWindow();
		
		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if(setName.getValue() != null) {
						// TODO why are marker and arrays treated differently? 
						// TODO use List instead of ArrayList when possible
						if(setType == SetType.MARKER) {
							ArrayList<String> items = (ArrayList<String>) dendrogram.getSelectedMarkerLabels();
							SubSetOperations.storeMarkerSetInCurrentContext(items, (String) setName.getValue(), parentId);
						} else { // MICRORRAY
							ArrayList<String> items = (ArrayList<String>)dendrogram.getSelectedArrayLabels();
							SubSetOperations.storeArraySetInCurrentContext(items, (String) setName.getValue(), parentId);
						}
						mainWindow.removeWindow(nameWindow);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		nameWindow.addComponent(setName);
		nameWindow.addComponent(submit);
		mainWindow.addWindow(nameWindow);
	}

}
