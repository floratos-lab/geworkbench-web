package org.geworkbenchweb.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.VisualPluginView;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * UI for a data type, a menu-like page.
 * 
 */
public class DataTypeMenuPage extends VerticalLayout {

	private static final long serialVersionUID = 1540643922901498218L;

	private final Long dataId;

	private final String title;

	public String getTitle() {
		return title;
	}

	public DataTypeMenuPage(String description, String title, Class<?> dataType, Long dataId) {
		this.dataId = dataId;
		this.title = title;

		setDescription(description);

		setSpacing(true);
		setMargin(true);

		// first part: analysis
		Label analysisLabel = new Label("Analyses Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(ContentMode.PREFORMATTED);
		addComponent(analysisLabel);

		VerticalLayout analysisGroup = new VerticalLayout();
		analysisGroup.setMargin(true);
		// loop through all analysis plug-ins
		List<PluginEntry> analysisList = GeworkbenchRoot.getPluginRegistry().getAnalysisList(dataType);
		Collections.sort(analysisList);
		for (final PluginEntry analysis : analysisList) {

			final Class<? extends AnalysisUI> analysisUI = GeworkbenchRoot.getPluginRegistry().getUI(analysis);
			buildOneItem(analysisGroup, analysis, analysisUI);

		}
		addComponent(analysisGroup);

		// second part: visualizations
		Class<? extends Visualizer>[] visualizers = GeworkbenchRoot.getPluginRegistry().getVisualizers(dataType);
		if (visualizers.length == 0)
			return;
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
		addComponent(vis);

		VerticalLayout visualizerGroup = new VerticalLayout();
		visualizerGroup.setMargin(true);
		// loop through all visualizer plug-ins
		for (final Class<? extends Visualizer> visualizerClass : visualizers) {

			try {
				buildOneItem(visualizerGroup, null, visualizerClass);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		addComponent(visualizerGroup);
	}

	private static Log log = LogFactory.getLog(DataTypeMenuPage.class);

	private VisualPluginView visualPluginView = null;

	/*
	 * this method should be called before this instance is added to
	 * VisualPluginView because of the complicated multiple layers of layout
	 */
	public void setVisualPluginView(VisualPluginView v) {
		// this should be the same as
		// this.getParent().getParent().getParent().getParent();
		visualPluginView = v;
	}

	private void showAnalysisParameterPanel(PluginEntry analysis,
			AnalysisUI analysisUI) {

		if (analysisUI == null) {
			log.debug("no analysis UI is chosen");
			return; // do thing. reusing this makes it easier to implement Tools list
		}

		if (visualPluginView instanceof VisualPluginView) {
			visualPluginView.setContent(analysisUI, analysis.getName(), analysis.getDescription());
		} else {
			log.error(visualPluginView + " is not VisualPluginView");
		}
	}

	private void showVisualizer(Visualizer visualizer) {
		if (visualizer.getDatasetId() == null)
			return; // no-op

		if (visualPluginView instanceof VisualPluginView) {
			visualPluginView.setContent(visualizer);
		} else {
			log.error("pluginView is " + visualPluginView.getClass());
		}
	}

	protected final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	protected final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

	private void buildOneItem(VerticalLayout group,
			final PluginEntry analysis,
			final Class<?> container) { // ComponentContainer class

		final ItemLayout itemLayout = new ItemLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();

		final String pluginName, pluginDescription;
		if (analysis != null) {
			pluginName = analysis.getName();
			pluginDescription = analysis.getDescription();
		} else {
			Class<Visualizer> v = (Class<Visualizer>) container;
			PluginEntry plugin = GeworkbenchRoot.getPluginRegistry()
					.getVisualizerPluginEntry(v);
			pluginName = plugin.getName();
			pluginDescription = plugin.getDescription();
		}
		Button toolButton = new Button(pluginName,
				new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {
							if (AnalysisUI.class.isAssignableFrom(container)) {
								AnalysisUI parameterPanel = (AnalysisUI) container.getDeclaredConstructor(Long.class)
										.newInstance(dataId);
								showAnalysisParameterPanel(analysis, parameterPanel);
							} else if (Visualizer.class.isAssignableFrom(container)) {
								// a new instance (with actual data) is created because currently visualizer
								// does not have set-dataset-ID method
								Visualizer newVisualizer = (Visualizer) container.getDeclaredConstructor(Long.class)
										.newInstance(dataId);
								showVisualizer(newVisualizer);
							} else {
								log.error("unknown view type " + container);
							}
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
				});
		toolButton.setStyleName(Reindeer.BUTTON_LINK);
		if (dataId == null) {
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

		group.addComponent(itemLayout);
	}

}
