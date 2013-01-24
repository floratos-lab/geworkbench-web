package org.geworkbenchweb.layout;

import org.apache.commons.collections.map.MultiKeyMap;
import org.geworkbenchweb.plugins.DataTypeUI;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * ViusalPluginView sets the visualplugin to the main layout.
 * @author Nikhil Reddy
 */
public class VisualPluginView extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	private Panel right;
	
	private VerticalLayout left;

	private HorizontalLayout controls;

	private Label title = new Label("", Label.CONTENT_XHTML);

	/** Contians 
	 * key1 - plugin 
	 * key2 - dataSetId of plugin 
	 * value - layout of the plugin 
	 * */
	private MultiKeyMap pluginCache = new MultiKeyMap();
	
	public VisualPluginView() {
		setImmediate(true);
	}
	
	// TODO phase out this, especially the 'non-visualizer' part first
	public void setVisualPlugin(VisualPlugin plugin) {
		
		if (plugin.checkForVisualizer() == false) {
			setContent((ComponentContainer) getLayoutFor(plugin),
					plugin.getName(), plugin.getDescription());
		} else {
			this.removeAllComponents();
			if (plugin.getName().contains("HierarchicalClusteringResults")) {
				setSizeUndefined();
			} else {
				setSizeFull();
			}
			addComponent(getLayoutFor(plugin));
		}
	}

	/**
	 *  Set the content of this panel. Generic version.
	 * 
	 * Eventually this may not be necessary if we only handle one type, namely DataTypeUI
	 * */
	@SuppressWarnings("deprecation")
	public void setContent(ComponentContainer content, String titleText, String description) {
		removeAllComponents();
		setSizeFull();
		HorizontalLayout pluginLayout = new HorizontalLayout();
		pluginLayout.removeAllComponents();
		pluginLayout.setImmediate(true);
		pluginLayout.setWidth("100%");
		pluginLayout.setSpacing(true);
		pluginLayout.setMargin(true);
		pluginLayout.setStyleName("sample-view");
		
		left = new VerticalLayout();
		left.setWidth("100%");
		left.setSpacing(true);
		left.setMargin(false);
		
		VerticalLayout rightLayout = new VerticalLayout();
		right = new Panel(rightLayout);
		rightLayout.setMargin(true, false, false, false);
		right.setStyleName(Panel.STYLE_LIGHT);
		right.addStyleName("feature-info");
		right.setWidth("319px");

		controls = new HorizontalLayout();
		controls.setWidth("100%");
		controls.setStyleName("feature-controls");

		title.setStyleName("title");
		controls.addComponent(title);
		controls.setExpandRatio(title, 1);
		
		pluginLayout.addComponent(left);
		pluginLayout.setExpandRatio(left, 1);
		pluginLayout.addComponent(right);
		
		left.addComponent(controls);
		title.setValue("<span>" + titleText + "</span>");
		//left.addComponent(getLayoutFor(plugin));
		left.addComponent(content);
		right.setCaption("Description");
		if (description != null && description != "") {
			final Label l = new Label(
					"<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span>"
							+ description + "</div></div>", Label.CONTENT_XHTML);
			right.addComponent(l);
		}
		addComponent(pluginLayout);
	}
	
	public void setDataUI(DataTypeUI plugin) {
		setContent(plugin, plugin.getTitle(), plugin.getDescription());
	}

	private Component getLayoutFor(VisualPlugin f) {
		Component ex = null;
		if(f.checkForVisualizer()) {
			ex 	= (Component) pluginCache.get(f, f.getDataSetId());
			if (ex == null) {
				Long data = f.getDataSetId();
				ex = f.getLayout(data);
				pluginCache.put(f, f.getDataSetId(), ex);
			}
		} else {
			Long data = f.getDataSetId();
			ex = f.getLayout(data);
		}
		return ex;
	}
}
