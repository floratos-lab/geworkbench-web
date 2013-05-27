package org.geworkbenchweb.genspace.ui.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbenchweb.genspace.rating.WorkflowVisualizationPopup;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.event.LayoutEvents;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class WorkflowVisualizationPanel extends Panel {
	
	private static final long serialVersionUID = -3135103378675075343L;
	
	private Resource resource;
	
	private String resourcePath = "img/arrow.png";
	
	private HashMap<Integer, WorkflowWrapper> wkflwCache;
	
	private WorkflowVisualizationPopup popup;
	
	private GenSpaceLogin login;

	public WorkflowVisualizationPanel() {
		this.setScrollable(true);
				
		this.resource = new ThemeResource(resourcePath);
	}
	
	public void setGenSpaceLogin(GenSpaceLogin login) {
		this.login = login;
	}

	public void render(List<WorkflowWrapper> workflows) {
		//removeAllComponents();
		
		wkflwCache = new HashMap<Integer, WorkflowWrapper>();
		//this.layout.removeAllComponents();
		this.removeAllComponents();

		Iterator<WorkflowTool> wIT;
		HorizontalLayout flowLayout;
		Label flowLabel;
		for (WorkflowWrapper w : workflows) {
			wkflwCache.put(w.getId(), w);
			
			wIT = w.getTools().iterator();
			
			flowLayout = new HorizontalLayout();
			flowLayout.setData(w.getId());
//			System.out.println("DEBUG workflowid: " + w.getId());
			
			Tool tmpTool;
			while(wIT.hasNext()) {
				tmpTool = wIT.next().getTool();
				
				flowLabel = new Label(tmpTool.getName());
				flowLabel.setData(tmpTool);
				
				flowLayout.addComponent(flowLabel);
				if (wIT.hasNext()) {
					flowLayout.addComponent(new Embedded(null, this.resource));
				}
			}
						
			flowLayout.addListener(new LayoutEvents.LayoutClickListener() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void layoutClick(LayoutEvents.LayoutClickEvent evt) {
//					System.out.println("DEBUG layoutclickevt: " + ((HorizontalLayout)evt.getComponent()).getData());
					
					if (evt.getClickedComponent().getClass().getName() != "com.vaadin.ui.Label") {
						return ;
					}
					
					Label clickedLabel = (Label)(evt.getClickedComponent());
					Tool clickedTool = (Tool)clickedLabel.getData();
					int wkflwID = (Integer)(((HorizontalLayout)evt.getComponent()).getData());
					popup = new WorkflowVisualizationPopup(login, wkflwCache.get(wkflwID).getDelegate(), clickedTool);
					popup.setWidth("400px");
					popup.setHeight("350px");
					popup.setCaption("Workflow Actions");
					
					getApplication().getMainWindow().addWindow(popup);
				}
			});
			
			this.addComponent(flowLayout);
		}
	}
}