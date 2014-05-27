package org.geworkbenchweb.genspace.ui;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.collections.map.MultiKeyMap;
import org.geworkbenchweb.genspace.chat.ChatReceiver;
import org.geworkbenchweb.genspace.ui.component.ActivityFeedWindow;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;
import org.vaadin.artur.icepush.ICEPush;

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
public class GenSpacePluginView extends HorizontalLayout {

	private static final long serialVersionUID = 1L;

	/** Contians 
	 * key1 - plugin 
	 * key2 - dataSetId of plugin 
	 * value - layout of the plugin 
	 * */
	private MultiKeyMap pluginCache = new MultiKeyMap();
	private VerticalLayout afLayout;
	private String afCaption = "Activity Feeder";
	private VerticalLayout right;
	private HorizontalLayout pluginLayout;
	private VerticalLayout left;  
	private ActivityFeedWindow af;
	private ChatReceiver chatHandler;
	Panel right1;
	private ICEPush pusher = new ICEPush();

	public GenSpacePluginView() {
		setImmediate(true);
	}
	
	public void setContent(Component content) {
		removeAllComponents();
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
	public void setContent(ComponentContainer content, String titleText, String description, GenSpaceLogin_1 genSpaceLogin) {
		removeAllComponents();
		setSizeFull();
		pluginLayout = new HorizontalLayout();
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
		right = new VerticalLayout();
		right.setWidth("319px");
		rightLayout.setMargin(true, false, false, false);
		right1 = new Panel();
		right1.setStyleName(Panel.STYLE_LIGHT);
		right1.addStyleName("feature-info");
		right.addComponent(right1);
		
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
		left.addComponent(content);
		left.setExpandRatio(content, 1);
		right1.setCaption("Description");
		if (description != null && description != "") {
			final Label l = new Label(
					"<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span>"
							+ description + "</div></div>", Label.CONTENT_XHTML);
			right1.addComponent(l);
		}
		if(genSpaceLogin.getGenSpaceServerFactory().getUser() != null){
			this.setAf(genSpaceLogin);
		}
		pluginLayout.setSizeFull();
		this.addComponent(pluginLayout);
	}
	
	
	public void setContentWithoutListner(ComponentContainer content, String titleText, String description, GenSpaceLogin_1 genSpaceLogin) {
		removeAllComponents();
		setSizeFull();
		pluginLayout = new HorizontalLayout();
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
		right = new VerticalLayout();
		right.setWidth("319px");

		rightLayout.setMargin(true, false, false, false);
		right1 = new Panel();
		right1.setStyleName(Panel.STYLE_LIGHT);
		right1.addStyleName("feature-info");
		right.addComponent(right1);
		
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
		left.addComponent(content);
		left.setExpandRatio(content, 1);
		right1.setCaption("Description");
		if (description != null && description != "") {
			final Label l = new Label(
					"<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span>"
							+ description + "</div></div>", Label.CONTENT_XHTML);
			right1.addComponent(l);
		}
		pluginLayout.setSizeFull();
		this.addComponent(pluginLayout);
	}
	
	
	public void setChat(GenSpaceLogin_1 genspaceLogin){
		chatHandler  = genspaceLogin.getChatHandler();
		
		if (chatHandler.rf != null || right.getComponentIndex(chatHandler.rf) != -1){
			clearChat();
		}
		chatHandler.updateRoster();
		chatHandler.createRosterFrame();
		chatHandler.rf.addStyleName("feature-info");
		right.addComponent(chatHandler.rf);
	}
	
	public void setAf(GenSpaceLogin_1 genspaceLogin){
		if (af != null || right.getComponentIndex(af) != -1){
			clearAf();
		}
		af = new ActivityFeedWindow(genspaceLogin);
		af.addStyleName("feature-info");
		GenSpaceWindow.getGenSpaceBlackboard().addListener(af);
		GenSpaceWindow.getGenSpaceBlackboard().addListener(af);
		right.addComponent(af);
	}
	public void clearChat(){
		right.removeComponent(chatHandler.rf);
	}
	public void clearAf(){
		right.removeComponent(af);
		GenSpaceWindow.getGenSpaceBlackboard().removeListener(af);
		GenSpaceWindow.getGenSpaceBlackboard().removeListener(af);
	}

}
