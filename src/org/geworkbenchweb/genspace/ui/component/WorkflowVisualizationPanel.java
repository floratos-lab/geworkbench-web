package org.geworkbenchweb.genspace.ui.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbenchweb.genspace.rating.WorkflowVisualizationPopup;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class WorkflowVisualizationPanel extends Panel {
	private static final long serialVersionUID = -3135103378675075343L;
	
	private Panel scrollPanel;
	
	private VerticalLayout layout;
	
	private Resource resource;
	
	private String resourcePath = "img/arrow.png";
	
	private HashMap<Integer, WorkflowWrapper> wkflwCache;
	
	private WorkflowVisualizationPopup popup;
	
	private GenSpaceLogin login;

	public WorkflowVisualizationPanel() {
		//layout = (VerticalLayout) getContent();
		layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		this.setScrollable(true);
		this.setHeight("500px");
		this.addComponent(layout);
				
		this.resource = new ThemeResource(resourcePath);
	}
	
	public void setGenSpaceLogin(GenSpaceLogin login) {
		this.login = login;
	}

	public void render(List<WorkflowWrapper> workflows) {
		//removeAllComponents();
		
		wkflwCache = new HashMap<Integer, WorkflowWrapper>();
		this.layout.removeAllComponents();

		Iterator<WorkflowTool> wIT;
		HorizontalLayout flowLayout;
		Label flowLabel;
		for (WorkflowWrapper w : workflows) {
			wkflwCache.put(w.getId(), w);
			
			wIT = w.getTools().iterator();
			
			flowLayout = new HorizontalLayout();
			flowLayout.setData(w.getId());
//			System.out.println("DEBUG workflowid: " + w.getId());
			
			while(wIT.hasNext()) {
				flowLabel = new Label(wIT.next().getTool().getName());
				flowLayout.addComponent(flowLabel);
				if (wIT.hasNext()) {
					flowLayout.addComponent(new Embedded(null, this.resource));
				}
			}
						
			flowLayout.addListener(new LayoutEvents.LayoutClickListener() {
				public void layoutClick(LayoutEvents.LayoutClickEvent evt) {
//					System.out.println("DEBUG layoutclickevt: " + ((HorizontalLayout)evt.getComponent()).getData());
					int wkflwID = (Integer)(((HorizontalLayout)evt.getComponent()).getData());
					popup = new WorkflowVisualizationPopup(login, wkflwCache.get(wkflwID).getDelegate());
					popup.setWidth("350px");
					popup.setHeight("100px");
					popup.setCaption("Workflow Actions");
					
					getApplication().getMainWindow().addWindow(popup);
				}
			});
			
			this.layout.addComponent(flowLayout);
		}
	}
}