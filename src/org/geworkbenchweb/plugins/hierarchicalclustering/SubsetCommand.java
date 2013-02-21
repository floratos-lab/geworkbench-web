/**
 * 
 */
package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.util.ArrayList;

import org.geworkbenchweb.utils.SubSetOperations;

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
// TODO no real selection was implemented yet
public class SubsetCommand implements Command {

	private static final long serialVersionUID = 1348332795447686854L;
	
	public enum SetType {MARKER, MICROARRAY};

	final private String caption;
	final private Component parent;
	final private SetType setType;
	final private Long parentId;
	
	SubsetCommand(final String caption, final Component parent, SetType setType, Long parentId) {
		this.caption = caption;
		this.parent = parent;
		this.setType = setType;
		this.parentId = parentId;
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
						ArrayList<String> items = new ArrayList<String>();
						String[] temp 	= 	new String[0]; // TODO placeholder for the actually selected markers/arrays
						for(int i=0; i<temp.length; i++) {
							String label = temp[i].trim();
							items.add(label);
						}
						String subSetName = (String) setName.getValue() + " ["+items.size()+ "]";
						// TODO why are marker and arrays treated differently? 
						if(setType == SetType.MARKER) {
							SubSetOperations.storeData(items, "marker", subSetName , parentId);
						} else { // MICRORRAY
							SubSetOperations.storeArraySetInCurrentContext(items, subSetName, parentId);
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
