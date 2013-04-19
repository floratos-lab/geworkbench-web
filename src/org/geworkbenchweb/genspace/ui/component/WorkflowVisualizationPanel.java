package org.geworkbenchweb.genspace.ui.component;

import java.util.Iterator;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;

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

	public WorkflowVisualizationPanel() {
		//layout = (VerticalLayout) getContent();
		layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		this.setScrollable(true);
		this.setHeight("500px");
		//setSizeFull();
		
		/*scrollPanel = new Panel();
		scrollPanel.setScrollable(true);
		scrollPanel.setContent(layout);
		
		this.addComponent(scrollPanel);*/
		this.addComponent(layout);
				
		this.resource = new ThemeResource(resourcePath);
	}

	public void render(List<WorkflowWrapper> workflows) {
		//removeAllComponents();
		this.layout.removeAllComponents();

		Iterator<WorkflowTool> wIT;
		HorizontalLayout flowLayout;
		Label flowLabel;
		for (WorkflowWrapper w : workflows) {
			wIT = w.getTools().iterator();
			
			flowLayout = new HorizontalLayout();
			
			while(wIT.hasNext()) {
				flowLabel = new Label(wIT.next().getTool().getName());
				flowLayout.addComponent(flowLabel);
				if (wIT.hasNext()) {
					flowLayout.addComponent(new Embedded(null, this.resource));
				}
			}
			
			this.layout.addComponent(flowLayout);
			
			/*Label label = new Label();
			String value = w.toString();
			label.setValue(value);
			addComponent(label);*/
		}
	}
}