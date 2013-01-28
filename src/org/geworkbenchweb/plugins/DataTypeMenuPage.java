package org.geworkbenchweb.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.VisualPluginView;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * UI for a data type.
 * 
 */
public class DataTypeMenuPage extends VerticalLayout {

	private static final long serialVersionUID = 1540643922901498218L;

	protected final Long dataId; // this should be private once the visualization part of MicroarrayUI is also cleaned up.
	
	private final String title;
	
	public String getTitle() { return title; }

	public DataTypeMenuPage(String description, String title, Class<? extends DSDataSet<?>> dataType, Long dataId) {
		this.dataId = dataId;
		
		setDescription(description);

		setSpacing(true);
		
		Label analysisLabel = new Label("Analyses Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(analysisLabel);

		// loop through all analysis plug-ins
		for(final Analysis analysis : GeworkbenchRoot.getPluginRegistry().getAnalysisList(dataType)) {
		
			final AnalysisUI analysisUI = GeworkbenchRoot.getPluginRegistry().getUI(analysis);
			buildOneItem(analysis, analysisUI);

		}
		
		// TODO visualization should be implemented similarly to the analysis part
		
		this.title = title;
    }

	private static Log log = LogFactory.getLog(DataTypeMenuPage.class);
	
	private void showAnalysisParameterPanel(Analysis analysis,
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
		Component component = this.getParent().getParent().getParent(); // VisualPluginView
		if(component instanceof VisualPluginView) {
			VisualPluginView visualPluginView = (VisualPluginView)component;
			visualPluginView.setContent(analysisUI, analysis.getName(), analysis.getDescription());
		} else {
			log.error(component+" is not VisualPluginView");
		}
	}

	// this is copied from ToolsUI. probably we should refactor to have only one
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

	// TODO once ToolsUI is totally cleaned up, this should be private
	// this needs to trigger the actual analysis.
	// in the version in ToolsUI, the link-looking analysis names have not action
	protected void buildOneItem(final Analysis analysis,
			final AnalysisUI analysisUI) {

		final ItemLayout itemLayout = new ItemLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();

		Button toolButton = new Button(analysis.getName(),
				new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						showAnalysisParameterPanel(analysis, analysisUI, dataId);
					}
				});
		toolButton.setStyleName(Reindeer.BUTTON_LINK);

		final String itemDescription = analysis.getDescription();
		infoButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				itemLayout.removeComponent(infoButton);
				itemLayout.addComponent(cancelButton, 1, 0);
				itemLayout.addDescription(itemDescription);
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

		addComponent(itemLayout);
	}

}
