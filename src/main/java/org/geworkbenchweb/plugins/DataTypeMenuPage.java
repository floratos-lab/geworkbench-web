package org.geworkbenchweb.plugins;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.VisualPluginView;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
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
	
	public String getTitle() { return title; }

	public DataTypeMenuPage(String description, String title, Class<?> dataType, Long dataId) {
		this.dataId = dataId;
		this.title = title;
		
		setDescription(description);

		setSpacing(true);
		
		// first part: analysis
		Label analysisLabel = new Label("Analyses Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(analysisLabel);

		VerticalLayout analysisGroup = new VerticalLayout();
		analysisGroup.setMargin(true);
		// loop through all analysis plug-ins
		for(final PluginEntry analysis : GeworkbenchRoot.getPluginRegistry().getAnalysisList(dataType)) {
		
			final AnalysisUI analysisUI = GeworkbenchRoot.getPluginRegistry().getUI(analysis);
			buildOneItem(analysisGroup, analysis, analysisUI);

		}
		addComponent(analysisGroup);
		
		// second part: visualizations
		Class<? extends Visualizer>[] visualizers = GeworkbenchRoot.getPluginRegistry().getVisualizers(dataType);
		if(visualizers.length==0) return;
		
		Label vis = new Label("Visualizations Available");
		vis.setStyleName(Reindeer.LABEL_H2);
		vis.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(vis);
		
		VerticalLayout visualizerGroup = new VerticalLayout();
		visualizerGroup.setMargin(true);
		// loop through all visualizer plug-ins
		for(final Class<? extends Visualizer> visualizerClass : visualizers) {

			try {
				final Visualizer visualizer = visualizerClass.getConstructor(
						Long.class).newInstance((Long)null); // create a placeholder visualizer because the visualizers do not have set-dataset-Id method
				buildOneItem(visualizerGroup, null,
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
		addComponent(visualizerGroup);
    }

	private static Log log = LogFactory.getLog(DataTypeMenuPage.class);
	
	private VisualPluginView visualPluginView = null;
	
	/* this method should be called before this instance is added to VisualPluginView because of the complicated multiple layers of layout */
	public void setVisualPluginView(VisualPluginView v) {
		// this should be the same as this.getParent().getParent().getParent().getParent();
		visualPluginView = v;
	}
	
	private void showAnalysisParameterPanel(PluginEntry analysis,
			AnalysisUI analysisUI, Long dataSetId) {

		if (analysisUI==null) {
			log.debug("no analysis UI is chosen");
			return; // do thing. reusing this makes it easier to implement Tools list
		}
		if (dataSetId==null) {
			log.debug("no data set is chosen");
			return; // do thing. reusing this makes it easier to implement Tools list
		}
		
		analysisUI.setDataSetId(dataId);
		if(visualPluginView instanceof VisualPluginView) {
			visualPluginView.setContent(analysisUI, analysis.getName(), analysis.getDescription());
		} else {
			log.error(visualPluginView+" is not VisualPluginView");
		}
	}

	private void showVisualizer(Visualizer visualizer) {
		if(visualizer.getDatasetId()==null) return; //no-op
		
		if(visualPluginView instanceof VisualPluginView) {
			visualPluginView.setContent(visualizer);
		} else {
			log.error("pluginView is "+visualPluginView.getClass());
		}
	}
	
	private static class ItemLayout extends GridLayout {

		private static final long serialVersionUID = -2801145303701009347L;
		
		private final FancyCssLayout cssLayout = new FancyCssLayout();
		
		private ItemLayout() {
			setColumns(2);
			setRows(2);
			setSizeFull();
			setImmediate(true);
			setColumnExpandRatio(1, 1.0f);

			cssLayout.setSlideEnabled(true);
			cssLayout.setWidth("95%");
			cssLayout.addStyleName("lay");
		}
	
		private void addDescription(String itemDescription) {
			Label tableText = new Label(
					"<p align = \"justify\">"+itemDescription+"</p>");
			tableText.setContentMode(Label.CONTENT_XHTML);
			cssLayout.addComponent(tableText);
			addComponent(cssLayout, 0, 1, 1, 1);
		}
		
		private void clearDescription() {
			cssLayout.removeAllComponents();
			removeComponent(cssLayout);
		}
	}

	private final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	private final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

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
		Button toolButton = new Button(pluginName,
				new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						if(container instanceof AnalysisUI) {
							showAnalysisParameterPanel(analysis, (AnalysisUI)container, dataId);
						} else if (container instanceof Visualizer) {
							// a new instance (with actual data) is created because currently visualizer does not have set-dataset-ID method
							Visualizer newVisualizer;
							try {
								newVisualizer = (Visualizer)container.getClass().getDeclaredConstructor(Long.class).newInstance(dataId);
								showVisualizer(newVisualizer);
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
						} else {
							log.error("unkown view type "+container.getClass());
						}
					}
				});
		toolButton.setStyleName(Reindeer.BUTTON_LINK);
		if(dataId==null) {
			toolButton.addStyleName("nolink");
		}

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

}
