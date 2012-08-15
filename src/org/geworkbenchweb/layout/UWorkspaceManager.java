package org.geworkbenchweb.layout;

import org.geworkbenchweb.dataset.UDataSetUpload;
import org.vaadin.peter.multibutton.MultiButton;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

/**
 * UWorkspaceManager handles all the workspace details.
 * @author np2417
 */
public class UWorkspaceManager extends MultiButton {
	
	private static final long serialVersionUID = -6898992321189378943L;
	
	public UWorkspaceManager() {
		
		setCaption("WorkSpace Manager");
		setSizeFull(); 
		setPopupButtonPixelWidth(50); 
		setPopupButtonEnabled(true); 
		
		Button createWorkspace 	= 	new Button("Create New Workspace");
		Button importWorkspace 	= 	new Button("Import Workspace");
		Button createProject 	= 	new Button("Create Project");
		Button uploadDataset 	= 	new Button("Upload Dataset", new Button.ClickListener() {
			
			private static final long serialVersionUID = -6393819962372106745L;

			@Override
			public void buttonClick(ClickEvent event) {
				UDataSetUpload dataWindow = new UDataSetUpload();
				getApplication().getMainWindow().addWindow(dataWindow);
			}
		});
		Button switchWorkspace 	= 	new Button("Switch Workspace");
		
		addButton(uploadDataset);
		addButton(createProject);
		addButton(createWorkspace);
		addButton(importWorkspace);
		addButton(switchWorkspace);
		
		this.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L; 

			public void buttonClick(ClickEvent event) {

				WorkspaceWindow window = new WorkspaceWindow();
				getApplication().getMainWindow().addWindow(window);
			}
		});
	}
	
	/**
	 * Workspace Manager Window with all the options
	 */
	private class WorkspaceWindow extends Window {
	
		private static final long serialVersionUID = 867444216969708459L;

		private WorkspaceWindow(){
			
			center();
			setModal(true);
			setHeight("400px");
			setWidth("600px");
			setCaption("Workspace Manager");
			setClosable(true);
			setDraggable(true);
			
		}
	}
	
}
