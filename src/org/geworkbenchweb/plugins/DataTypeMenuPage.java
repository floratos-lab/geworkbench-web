package org.geworkbenchweb.plugins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
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
	
	// FIXME
	// the entire implementation of the action after you click on an analysis name to bring up the parameter does not make any sense
	// I will re-write it gradually while maintaining so not to break the observable behavior. 
	@SuppressWarnings("deprecation")
	private void showAnalysisParameterPanel(Analysis analysis,
			AnalysisUI analysisUI, Long dataSetId) {

		Component layoutToBeUpdated = this.getParent().getParent(); // pluginLayout

		if (!(analysisUI instanceof AnalysisUI)) {
			log.warn(analysisUI.getClass() + " not supported yet.");
			return; // TODO all analysis UIs need to implement necessary method,
					// e.g. setDataSetId
		}
		AnalysisUI ui = (AnalysisUI) analysisUI;
		ui.setDataSetId(dataId);

		HorizontalLayout pluginLayout = (HorizontalLayout) layoutToBeUpdated;
		pluginLayout.removeAllComponents();

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

		Label title = new Label("<span>" + analysis.getName() + "</span>", Label.CONTENT_XHTML);
		title.setStyleName("title");
		controls.addComponent(title);
		controls.setExpandRatio(title, 1);

		pluginLayout.addComponent(left);
		pluginLayout.setExpandRatio(left, 1);
		pluginLayout.addComponent(right);

		left.addComponent(controls);
		left.addComponent(ui);
		right.setCaption("Description");
		String desc = analysis.getDescription();
		if (desc != null && desc != "") {
			final Label l = new Label(
					"<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span>"
							+ desc + "</div></div>", Label.CONTENT_XHTML);
			right.addComponent(l);
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

	// build one item and add to the UI
	// this is copied from ToolsUI, but not exactly the same, especially that this needs to trigger the actual analysis.
	// in the version in ToolsUI, the link-looking analysis names have not action
	private void buildOneItem(final Analysis analysis,
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
