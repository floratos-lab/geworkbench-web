/**
 * 
 */
package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.layout.SetViewLayout;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.visualizations.Dendrogram;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Tree;
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
	private static Log log = LogFactory.getLog(SubsetCommand.class);
	
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

		final TextField setName = new TextField();
		setName.setInputPrompt("Please enter set name");
		setName.setImmediate(true);

		if(parent==null) return; // parent should never be null
		final Window mainWindow = parent.getApplication().getMainWindow();
		
		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				UMainLayout mainLayout = null;
				ComponentContainer content = mainWindow.getContent();
				if(content instanceof UMainLayout) {
					mainLayout = (UMainLayout)content;
				} else {
					log.error("unable to get UMainLayout");
					return;
				}
				SetViewLayout setViewLayout = mainLayout.getSetViewLayout();
				
				String newSetName = (String) setName.getValue();

				try {
					Long subSetId = 0L;
					ArrayList<String> items = null;
					String parentSet = null;
					Tree tree = null;

					if(setName.getValue() != null) {
						// TODO why are marker and arrays treated differently? 
						// TODO use List instead of ArrayList when possible
						if(setType == SetType.MARKER) {
							items = (ArrayList<String>) dendrogram.getSelectedMarkerLabels();
							subSetId = SubSetOperations.storeMarkerSetInCurrentContext(items, newSetName, parentId);
							parentSet = "MarkerSets";
							if(setViewLayout!=null) {
								tree = setViewLayout.getMarkerSetTree();
							}
						} else { // MICRORRAY
							items = (ArrayList<String>)dendrogram.getSelectedArrayLabels();
							subSetId = SubSetOperations.storeArraySetInCurrentContext(items, newSetName, parentId);
							parentSet = "arraySets";
							if(setViewLayout!=null) {
								tree = setViewLayout.getArraySetTree();
							}
						}
						mainWindow.removeWindow(nameWindow);
					}
					
					if(tree!=null) { /* set view instead of workspace view */
						tree.addItem(subSetId);
						tree.getContainerProperty(subSetId, SetViewLayout.SET_DISPLAY_NAME).setValue(newSetName + " [" + items.size() + "]");
						tree.setParent(subSetId, parentSet);
						tree.setChildrenAllowed(subSetId, true);
						for(int j=0; j<items.size(); j++) {
							String itemLabel = items.get(j); 
							tree.addItem(itemLabel+subSetId);
							tree.getContainerProperty(itemLabel+subSetId, SetViewLayout.SET_DISPLAY_NAME).setValue(itemLabel);
							tree.setParent(itemLabel+subSetId, subSetId);
							tree.setChildrenAllowed(itemLabel+subSetId, false);
						}
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
