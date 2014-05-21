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
	
	private static final String clickDescription = "Click for more";
	
	private Resource resource;
	
	private String resourcePath = "img/arrow.png";
	
	private HashMap<Integer, WorkflowWrapper> wkflwCache;
	
	private WorkflowVisualizationPopup popup;
	
	private Panel resultPanel = new Panel("Workflow Actions");
	
	private GenSpaceLogin_1 login;
	

	private boolean isWindow = true;

	public WorkflowVisualizationPanel() {
		this.setScrollable(true);		
		this.resource = new ThemeResource(resourcePath);
	}
	
	public void setIsWindow(boolean isWindow) {
		this.isWindow = isWindow;
	}
	
	public void setGenSpaceLogin(GenSpaceLogin_1 login2) {
		this.login = login2;
	}

	public void render(List<WorkflowWrapper> workflows) {
	
		wkflwCache = new HashMap<Integer, WorkflowWrapper>();
		this.removeAllComponents();

		Iterator<WorkflowTool> wIT;
		HorizontalLayout flowLayout;
		Label flowLabel;
		for (WorkflowWrapper w : workflows) {
			wkflwCache.put(w.getId(), w);
			
			wIT = w.getTools().iterator();
			
			flowLayout = new HorizontalLayout();
			flowLayout.setData(w.getId());

			Tool tmpTool;
			while(wIT.hasNext()) {
				tmpTool = wIT.next().getTool();
				
				flowLabel = new Label(tmpTool.getName());
				flowLabel.setData(tmpTool);
				flowLabel.setDescription(clickDescription);
				
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
					
					if (isWindow) {
						getApplication().getMainWindow().addWindow(popup);
					} else {
						resultPanel.removeAllComponents();
						popup.detachAndAttachListener(getApplication());
						resultPanel.addComponent(popup.getVLayout());
						addComponent(resultPanel);
					}
					popup.attachAllPushers();
				}
			});
			
			this.addComponent(flowLayout);
		}
	}
}