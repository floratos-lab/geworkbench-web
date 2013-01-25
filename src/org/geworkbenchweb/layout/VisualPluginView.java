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

	/** Contians 
	 * key1 - plugin 
	 * key2 - dataSetId of plugin 
	 * value - layout of the plugin 
	 * */
	private MultiKeyMap pluginCache = new MultiKeyMap();
	
	public VisualPluginView() {
		setImmediate(true);
	}
	
	// TODO phase out this
	public void setVisualPlugin(VisualPlugin plugin) {
		// plugin.checkForVisualizer() always return true now
		this.removeAllComponents();
		if (plugin.getName().contains("HierarchicalClusteringResults")) {
			setSizeUndefined();
		} else {
			setSizeFull();
		}

		Component ex = (Component) pluginCache.get(plugin,
				plugin.getDataSetId());
		if (ex == null) {
			Long data = plugin.getDataSetId();
			ex = plugin.getLayout(data);
			pluginCache.put(plugin, plugin.getDataSetId(), ex);
		}
		addComponent(ex);
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
		
		VerticalLayout left = new VerticalLayout();
		left.setWidth("100%");
		left.setSpacing(true);
		left.setMargin(false);
		
		VerticalLayout rightLayout = new VerticalLayout();
		Panel right = new Panel(rightLayout);
		rightLayout.setMargin(true, false, false, false);
		right.setStyleName(Panel.STYLE_LIGHT);
		right.addStyleName("feature-info");
		right.setWidth("319px");

		HorizontalLayout controls = new HorizontalLayout();
		controls.setWidth("100%");
		controls.setStyleName("feature-controls");

		Label title = new Label("<span>" + titleText + "</span>", Label.CONTENT_XHTML);
		title.setStyleName("title");
		controls.addComponent(title);
		controls.setExpandRatio(title, 1);
		
		pluginLayout.addComponent(left);
		pluginLayout.setExpandRatio(left, 1);
		pluginLayout.addComponent(right);
		
		left.addComponent(controls);
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
}
