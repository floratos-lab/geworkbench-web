package org.geworkbenchweb.layout;

import org.geworkbenchweb.dataset.UDataSetUpload;
import org.vaadin.peter.multibutton.MultiButton;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

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
			setHeight("600px");
			setWidth("800px");
			setCaption("Workspace Manager");
			setClosable(true);
			setDraggable(false);
			setImmediate(true);
			setScrollable(false);
			setResizable(false);
			
			HorizontalSplitPanel workPanel = new HorizontalSplitPanel();
	
			workPanel.setSizeFull();
			workPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
			workPanel.setSplitPosition(25);
			workPanel.setMargin(false);
			
			ListSelect workspaces 			= 	new ListSelect();
			VerticalLayout workSpaceLayout 	=	new VerticalLayout();
			
			workspaces.setCaption("Select Workspace");
			workspaces.setMultiSelect(false);
			workspaces.setNullSelectionAllowed(false);
			workspaces.addItem("Demo Workspace");	
			
			workSpaceLayout.setSizeFull();
			workSpaceLayout.setMargin(true);
			workSpaceLayout.addComponent(workspaces);
			workSpaceLayout.setComponentAlignment(workspaces, Alignment.TOP_LEFT);
			workPanel.setFirstComponent(workSpaceLayout);
			
			setContent(workPanel);
		}
	}
	
}
