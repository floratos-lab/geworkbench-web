package org.geworkbenchweb.layout;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.collections.map.MultiKeyMap;
import org.geworkbenchweb.genspace.ui.GenspaceLayout;
import org.geworkbenchweb.plugins.tools.ToolsUI;

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
	
	public void setContent(Component content) {
		removeAllComponents();
		setSizeFull();
		addComponent(content);
	}
	
	public void setContentUsingCache(Class<? extends Component> resultUiClass, Long dataSetId) {
		Object object = pluginCache.get(resultUiClass, dataSetId);
		if(object!=null) {
			setContent((Component)object);
		} else {
			setContentUpdatingCache(resultUiClass, dataSetId);
		}
	}
	
	public void setContentUpdatingCache(Class<? extends Component> resultUiClass, Long dataSetId) {
		try {
			Component resultUI = resultUiClass.getDeclaredConstructor(Long.class).newInstance(dataSetId);
			pluginCache.put(resultUiClass, dataSetId, resultUI);
			setContent(resultUI);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
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
		
		left.setSizeFull();
		left.addComponent(controls);
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.addComponent(content);
		left.addComponent(panel);
		left.setExpandRatio(panel, 1);
		right.setCaption("Description");
		if (description != null && description != "") {
			final Label l = new Label(
					"<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span>"
							+ description + "</div></div>", Label.CONTENT_XHTML);
			right.addComponent(l);
		}
		pluginLayout.setSizeFull();
		this.addComponent(pluginLayout);
	}
	
	public void showToolList() {
		ToolsUI toolList = new ToolsUI();
		setContent(toolList, toolList.getTitle(), toolList.getDescription());
	}

	public void showWeclomeScreen() {
		removeAllComponents();
		setWidth("100%");
		QuickIntro quickIntro = new QuickIntro();
		addComponent(quickIntro);
	}
		
	public void showGenSpace(GenspaceLayout layout) {
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
		
		HorizontalLayout controls = new HorizontalLayout();
		controls.setWidth("100%");
		controls.setStyleName("feature-controls");

		Label title = new Label("<span>genSpace</span>", Label.CONTENT_XHTML);
		title.setStyleName("title");
		controls.addComponent(title);
		controls.setExpandRatio(title, 1);
		
		pluginLayout.addComponent(left);
		pluginLayout.setExpandRatio(left, 1);
		
		left.setSizeFull();
		left.addComponent(controls);
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.addComponent(layout);
		left.addComponent(panel);
		left.setExpandRatio(panel, 1);
		pluginLayout.setSizeFull();
		this.addComponent(pluginLayout);
	}
}
