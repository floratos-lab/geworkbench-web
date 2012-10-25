package org.geworkbenchweb.layout;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.collections.map.MultiKeyMap;

public class VisualPluginView extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	private Panel right;
	
	private VerticalLayout left;

	private HorizontalLayout controls;

	private Label title = new Label("", Label.CONTENT_XHTML);

	@SuppressWarnings("unused")
	private VisualPlugin currentFeature;

	/** Contians 
	 * key1 - plugin 
	 * key2 - dataSetId of plugin 
	 * value - layout of the plugin 
	 * */
	private MultiKeyMap pluginCache = new MultiKeyMap();
	
	public VisualPluginView() {
		setImmediate(true);
	}
	
	@SuppressWarnings("deprecation")
	public void setVisualPlugin(VisualPlugin plugin) {
		
		if(plugin.checkForVisualizer() == false) {
			this.removeAllComponents();
			this.setSizeFull();
			HorizontalLayout pluginLayout = new HorizontalLayout();
			pluginLayout.removeAllComponents();
			pluginLayout.setWidth("100%");
			pluginLayout.setImmediate(true);
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
			
			currentFeature = plugin;

			left.addComponent(controls);
			title.setValue("<span>" + plugin.getName() + "</span>");
			left.addComponent(getLayoutFor(plugin));
			right.setCaption("Description");
			String desc = plugin.getDescription();
			if (desc != null && desc != "") {
				final Label l = new Label(
						"<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span>"
								+ desc + "</div></div>", Label.CONTENT_XHTML);
				right.addComponent(l);
			}
			addComponent(pluginLayout);
		} else {
			this.removeAllComponents();
			if(plugin.getName().contains("HierarchicalClusteringResults")) {
				setSizeUndefined();
			} else {
				setSizeFull();
			}
			addComponent(getLayoutFor(plugin));
		}
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
