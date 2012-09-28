package org.geworkbenchweb.layout;

import org.geworkbenchweb.layout.ActiveLink.LinkActivatedEvent;
import org.geworkbenchweb.layout.ActiveLink.LinkActivatedListener;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.apache.commons.collections.map.MultiKeyMap;

@SuppressWarnings("serial")
public class VisualPluginView extends HorizontalLayout {

	private static final String MSG_SHOW_TUT = "View Tutorial";

	private Panel right;
	private VerticalLayout left;

	private HorizontalLayout controls;

	private Label title = new Label("", Label.CONTENT_XHTML);

	private ActiveLink showTut;

	private VisualPlugin currentFeature;

	private Window tutWindow;

	/** Contians 
	 * key1 - plugin 
	 * key2 - dataSetId of plugin 
	 * value - layout of the plugin 
	 * */
	private MultiKeyMap pluginCache = new MultiKeyMap();

	@SuppressWarnings("deprecation")
	public VisualPluginView() {

		setWidth("100%");
		setMargin(true);
		setSpacing(true);
		setStyleName("sample-view");
		
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

		showTut = new ActiveLink();
		showTut
		.setDescription("Right / middle / ctrl / shift -click for browser window/tab");
		showTut.addListener(new LinkActivatedListener() {

			public void linkActivated(LinkActivatedEvent event) {
				if (!event.isLinkOpened()) {
					showTutorial(currentFeature.getTutorial());
				}
			}
		});
		showTut.setCaption(MSG_SHOW_TUT);
		showTut.addStyleName("showcode");
		showTut.setTargetBorder(Link.TARGET_BORDER_NONE);
		//controls.addComponent(showTut);

	}
	
	public void setVisualPlugin(VisualPlugin plugin) {
		
		if(plugin.checkForVisualizer() == false) {
			this.removeAllComponents();
			this.setMargin(true);
			this.setSpacing(true);
			this.setStyleName("sample-view");
			
			right.removeAllComponents();
			left.removeAllComponents();
			
			addComponent(left);
			setExpandRatio(left, 1);
			addComponent(right);
			
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
		} else {
			this.removeAllComponents();
			this.setSizeFull();
			this.setImmediate(true);
			this.removeStyleName("sample-view");
			setMargin(false);
			setSpacing(false);
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
	
	public void showTutorial(CustomLayout tutorial) {
		if (tutWindow == null) {
			tutWindow = new Window("Tutorial");
			//tutWindow.getContent().setSizeUndefined();
			tutWindow.setWidth("70%");
			tutWindow.setHeight("60%");
			tutWindow.setPositionX(100);
			tutWindow.setPositionY(100);
			tutWindow.setScrollable(true);
		}
		tutWindow.removeAllComponents();
		tutWindow.setContent(tutorial);

		if (tutWindow.getParent() == null) {
			getWindow().addWindow(tutWindow);
		}
	}
}
