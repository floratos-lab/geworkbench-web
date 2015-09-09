package org.geworkbenchweb.plugins.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.VisualPluginView;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.plugins.ItemLayout;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.plugins.cnkb.CNKB2;
import org.geworkbenchweb.plugins.lincs.LINCS;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * List of all plug-ins regardless of data type. 
*/
public class ToolsUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
	/* For so-called 'stand-alone' tools only. 
	 * ATTENTION: They are not really stand-alone, just do not take any datasets as input. */
	static public ToolsUI createStandaloneInstance(final VisualPluginView pluginView) {
		ToolsUI inst = new ToolsUI("Standalone Tools", "The list of 'standalone' tools.", pluginView);
		inst.addStandaloneSection();
		return inst;
	}
	
	static public ToolsUI createInstance(final VisualPluginView pluginView) {
		ToolsUI inst = new ToolsUI("All Tools", "The list of all the available tools.", pluginView);

		inst.setSpacing(true);
		
		// first part: analysis
		Label analysisLabel = new Label("Analyses Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		inst.addComponent(analysisLabel);

		VerticalLayout analysisGroup = new VerticalLayout();
		analysisGroup.setMargin(true);
		// loop through all analysis plug-ins
		List<PluginEntry> analysisList = GeworkbenchRoot.getPluginRegistry().getAnalysisList(null);
		Collections.sort(analysisList);
		for(final PluginEntry analysis : analysisList) {
		
			final AnalysisUI analysisUI = GeworkbenchRoot.getPluginRegistry().getUI(analysis);
			inst.buildOneItem(analysisGroup, analysis, analysisUI);

		}
		inst.addComponent(analysisGroup);
		
		// second part: visualizations
		Class<? extends Visualizer>[] visualizers = GeworkbenchRoot.getPluginRegistry().getVisualizers(null);
		Arrays.sort(visualizers, new Comparator<Class<? extends Visualizer>>() {

			@Override
			public int compare(Class<? extends Visualizer> o1, Class<? extends Visualizer> o2) {
				PluginEntry v1 = GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(o1);
				PluginEntry v2 = GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(o2);

				return v1.getName().compareTo(v2.getName());
			}
			
		});
		
		Label vis = new Label("Visualizations Available");
		vis.setStyleName(Reindeer.LABEL_H2);
		vis.setContentMode(Label.CONTENT_PREFORMATTED);
		inst.addComponent(vis);
		
		VerticalLayout visualizerGroup = new VerticalLayout();
		visualizerGroup.setMargin(true);
		// loop through all visualizer plug-ins
		for(final Class<? extends Visualizer> visualizerClass : visualizers) {

			try {
				final Visualizer visualizer = visualizerClass.getConstructor(
						Long.class).newInstance((Long)null); // create a placeholder visualizer because the visualizers do not have set-dataset-Id method
				inst.buildOneItem(visualizerGroup, null,
						visualizer);
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
		inst.addComponent(visualizerGroup);
		
		inst.addStandaloneSection();
		
		return inst;
	}
	
	public String getTitle() { return title; }

	private void addStandaloneSection() {
		Label label2 = new Label("Stand-alone Tools");
		label2.setStyleName(Reindeer.LABEL_H2);
		label2.setContentMode(Label.CONTENT_PREFORMATTED);
		this.addComponent(label2);
		
		/* This should be designed in a more general way, but for now LINCS and CNKB are the only things here and they are not even similar. */
		VerticalLayout standaloneGroup = new VerticalLayout();
		standaloneGroup.setMargin(true);
		List<PluginEntry> plugins = GeworkbenchRoot.getPluginRegistry().getStandalonePlugins();
		for(PluginEntry entry : plugins) {
			Component content = null;
			if( "LINCS".equals(entry.getName()) ) {
				content = new LINCS();
			} else {
				content = new CNKB2(); // TODO this should be written in a more general way, but for now CNKB is the only thing.
			}
			Component item = buildStandaloneItem(entry, content, entry.getName(), entry.getDescription());
			standaloneGroup.addComponent(item );
		}
		this.addComponent(standaloneGroup);
	}
	
	private final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	private final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

	private ItemLayout buildStandaloneItem(final PluginEntry analysis, final Component content, final String title, final String description) {

		final ItemLayout itemLayout = new ItemLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();

		final String pluginName, pluginDescription;
		pluginName = analysis.getName();
		pluginDescription = analysis.getDescription();

		Button toolButton = new Button(pluginName, new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(content instanceof ComponentContainer && title!=null)
					pluginView.setContent((ComponentContainer)content, title, description);
				else
					pluginView.setContent(content);
			}
		});
		toolButton.setStyleName(Reindeer.BUTTON_LINK);

		infoButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				itemLayout.removeComponent(infoButton);
				itemLayout.addComponent(cancelButton, 1, 0);
				itemLayout.addDescription(pluginDescription);
			}
		});
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				itemLayout.removeComponent(cancelButton);
				itemLayout.addComponent(infoButton, 1, 0);
				itemLayout.clearDescription();
			}
		});

		infoButton.setStyleName(BaseTheme.BUTTON_LINK);
		infoButton.setIcon(ICON);
		cancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		cancelButton.setIcon(CancelIcon);
		itemLayout.setSpacing(true);
		itemLayout.addComponent(toolButton);
		itemLayout.addComponent(infoButton);

		return itemLayout;
	}

	// copied from DataTypeMenuPage and modified to break off the inheritance
	private void buildOneItem(VerticalLayout group,
			final PluginEntry analysis,
			final Object container) { //ComponentContainer

		final ItemLayout itemLayout = new ItemLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();

		final String pluginName, pluginDescription;
		if (analysis != null) {
			pluginName = analysis.getName();
			pluginDescription = analysis.getDescription();
		} else {
			Visualizer v = (Visualizer) container;
			PluginEntry plugin = GeworkbenchRoot.getPluginRegistry()
					.getVisualizerPluginEntry(v.getClass());
			pluginName = plugin.getName();
			pluginDescription = plugin.getDescription();
		}
		Button toolButton = new Button(pluginName);
		toolButton.setStyleName(Reindeer.BUTTON_LINK);
		toolButton.addStyleName("nolink");

		infoButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				itemLayout.removeComponent(infoButton);
				itemLayout.addComponent(cancelButton, 1, 0);
				itemLayout.addDescription(pluginDescription);
			}
		});
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				itemLayout.removeComponent(cancelButton);
				itemLayout.addComponent(infoButton, 1, 0);
				itemLayout.clearDescription();
			}
		});

		infoButton.setStyleName(BaseTheme.BUTTON_LINK);
		infoButton.setIcon(ICON);
		cancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		cancelButton.setIcon(CancelIcon);
		itemLayout.setSpacing(true);
		itemLayout.addComponent(toolButton);
		itemLayout.addComponent(infoButton);

		group.addComponent(itemLayout);
	}
	
	private ToolsUI(String title, String description, final VisualPluginView pluginView) {
		this.title = title;
		this.setDescription( description );
		this.pluginView = pluginView;
	}
	
	private final VisualPluginView pluginView;
	private final String title;
}
