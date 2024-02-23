package org.geworkbenchweb.plugins.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.VisualPluginView;
import org.geworkbenchweb.plugins.ItemLayout;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
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

	/*
	 * For so-called 'stand-alone' tools only.
	 * ATTENTION: They are not really stand-alone, just do not take any datasets as
	 * input.
	 */
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
		analysisLabel.setContentMode(ContentMode.PREFORMATTED);
		inst.addComponent(analysisLabel);

		VerticalLayout analysisGroup = new VerticalLayout();
		analysisGroup.setMargin(true);
		// loop through all analysis plug-ins
		List<PluginEntry> analysisList = GeworkbenchRoot.getPluginRegistry().getAnalysisList(null);
		Collections.sort(analysisList);
		for (final PluginEntry analysis : analysisList) {
			analysisGroup.addComponent(inst.buildToolItem(analysis, null));
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
		vis.setContentMode(ContentMode.PREFORMATTED);
		inst.addComponent(vis);

		VerticalLayout visualizerGroup = new VerticalLayout();
		visualizerGroup.setMargin(true);
		// loop through all visualizer plug-ins
		for (final Class<? extends Visualizer> visualizerClass : visualizers) {

			try {
				PluginEntry plugin = GeworkbenchRoot.getPluginRegistry()
						.getVisualizerPluginEntry(visualizerClass);
				visualizerGroup.addComponent(inst.buildToolItem(plugin, null));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		inst.addComponent(visualizerGroup);

		inst.addStandaloneSection();

		return inst;
	}

	public String getTitle() {
		return title;
	}

	private void addStandaloneSection() {
		Label label2 = new Label("Stand-alone Tools");
		label2.setStyleName(Reindeer.LABEL_H2);
		label2.setContentMode(ContentMode.PREFORMATTED);
		this.addComponent(label2);

		/*
		 * This should be designed in a more general way, but for now LINCS and CNKB are
		 * the only things here and they are not even similar.
		 */
		VerticalLayout standaloneGroup = new VerticalLayout();
		standaloneGroup.setMargin(true);
		Map<PluginEntry, Class<? extends Component>> plugins = GeworkbenchRoot.getPluginRegistry()
				.getStandalonePlugins();
		for (PluginEntry entry : new TreeSet<>(plugins.keySet())) {
			Component content;
			try {
				content = plugins.get(entry).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}
			Component item = buildToolItem(entry, content);
			standaloneGroup.addComponent(item);
		}
		this.addComponent(standaloneGroup);
	}

	private final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	private final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

	private ItemLayout buildToolItem(final PluginEntry analysis, final Component content) {

		final ItemLayout itemLayout = new ItemLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();

		final String pluginName, pluginDescription;
		pluginName = analysis.getName();
		pluginDescription = analysis.getDescription();

		Button toolButton = new Button(pluginName);
		toolButton.setStyleName(Reindeer.BUTTON_LINK);
		if (content != null) {
			toolButton.addClickListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					if (content instanceof ComponentContainer && title != null)
						pluginView.setContent((ComponentContainer) content, analysis.getName(),
								analysis.getDescription());
					else
						pluginView.setContent(content);
				}
			});
		} else {
			toolButton.addStyleName("nolink");
		}

		infoButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				itemLayout.removeComponent(infoButton);
				itemLayout.addComponent(cancelButton, 1, 0);
				itemLayout.addDescription(pluginDescription);
			}
		});
		cancelButton.addClickListener(new Button.ClickListener() {

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

	private ToolsUI(String title, String description, final VisualPluginView pluginView) {
		this.title = title;
		this.setDescription(description);
		this.pluginView = pluginView;
		setMargin(true);
	}

	private final VisualPluginView pluginView;
	private final String title;
}
