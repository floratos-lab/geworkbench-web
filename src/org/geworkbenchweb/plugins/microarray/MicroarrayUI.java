package org.geworkbenchweb.plugins.microarray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.plugins.Analysis;
import org.geworkbenchweb.plugins.PluginRegistry;
import org.geworkbenchweb.plugins.anova.AnovaUI;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractComponentContainer;
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

public class MicroarrayUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
	private final Long dataId;

	public MicroarrayUI(Long dataSetId) {

		setSpacing(true);
		
		this.dataId = dataSetId;
		Label analysisLabel = new Label("Analyses Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(analysisLabel);
		
		/**
		 * ARACNE
		 */
		Button aracne 	= 	new Button("ARACNe", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("Aracne", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);
			}
		});
		final GridLayout aracneLayout = new GridLayout();
		aracneLayout.setColumns(2);
		aracneLayout.setRows(2);
		aracneLayout.setSizeFull();
		aracneLayout.setImmediate(true);
		aracneLayout.setColumnExpandRatio(1, 1.0f);
		final Label aracneText = new Label(
				"<p align= \"justify\">ARACNe (Algorithm for the Reconstruction of Accurate Cellular Networks) " +
				"(Basso 2005, Margolin 2006a, 2006b) is an information-theoretic algorithm used " +
				"to identify transcriptional interactions between gene products using microarray " +
				"gene expression profile data.</p>");
		aracneText.setContentMode(Label.CONTENT_XHTML);
		
		final FancyCssLayout cssLayout = new FancyCssLayout();
		cssLayout.setSlideEnabled(true);
		cssLayout.setWidth("95%");
		cssLayout.addStyleName("lay");
		
		final Button aracneButton 		= 	new Button();
		final Button aracneCancelButton = 	new Button();
		
		aracneButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				cssLayout.removeAllComponents();
				aracneLayout.removeComponent(aracneButton);
				aracneLayout.addComponent(aracneCancelButton, 1, 0);
				cssLayout.addComponent(aracneText);
				aracneLayout.addComponent(cssLayout, 0, 1, 1, 1);
			}
		});
		aracneCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				cssLayout.removeAllComponents();
				aracneLayout.removeComponent(aracneCancelButton);
				aracneLayout.addComponent(aracneButton, 1, 0);
				aracneLayout.removeComponent(cssLayout);
			}
		});
	
		aracneButton.setStyleName(BaseTheme.BUTTON_LINK);
		aracneButton.setIcon(ICON);
		aracneCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		aracneCancelButton.setIcon(CancelIcon);
		addComponent(aracneLayout);
		aracneLayout.setSpacing(true);
		aracneLayout.addComponent(aracne);
	    aracneLayout.addComponent(aracneButton);
	    
	    /** 
	     * ANOVA 
	     */
		// TODO convert other analysis plug-ins other ANOVA later
		for(final Analysis analysis : PluginRegistry.getInstance().getAnalysisList()) {
		
			final AbstractComponentContainer analysisUI = PluginRegistry.getInstance().getUI(analysis);
			buildOneItem(analysis, analysisUI);

		} // TODO this loops for now only contains ANOVA
		
	    /** 
	     * Hierarchial Clustering 
	     */
	    final GridLayout hcLayout = new GridLayout();
	    hcLayout.setColumns(2);
	    hcLayout.setRows(2);
	    hcLayout.setSizeFull();
	    hcLayout.setImmediate(true);
	    hcLayout.setColumnExpandRatio(1, 1.0f);
		Button hc		=	new Button("Hierarchical Clustering", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("HierarchicalClustering", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);	
			}
		});
		final FancyCssLayout hcCssLayout = new FancyCssLayout();
		hcCssLayout.setWidth("95%");
		hcCssLayout.setSlideEnabled(true);
		hcCssLayout.addStyleName("lay");
		final Label hcText = new Label(
				"<p align= \"justify\">Hierarchical clustering is a method to group arrays and/or markers together based on similarity " +
				"on their expression profiles. geWorkbench implements its own code for agglomerative hierarchical " +
				"clustering. Starting from individual points (the leaves of the tree), nearest neighbors are found " +
				"for individual points, and then for groups of points, at each step building up a branched " +
				"structure that converges toward a root that contains all points. The resulting graph tends to " +
				"group similar items together. Results of hierarchical clustering are displayed in the Dendrogram " +
				"component.</p>");
		hcText.setContentMode(Label.CONTENT_XHTML);
		
		final Button hcButton 		= 	new Button();
		final Button hcCancelButton = 	new Button();
		
		hcButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				hcCssLayout.removeAllComponents();
				hcLayout.removeComponent(hcButton);
				hcLayout.addComponent(hcCancelButton, 1, 0);
				hcCssLayout.addComponent(hcText);
				hcLayout.addComponent(hcCssLayout, 0, 1, 1, 1);
			}
		});
		hcCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				hcCssLayout.removeAllComponents();
				hcLayout.removeComponent(hcCancelButton);
				hcLayout.addComponent(hcButton, 1, 0);
				hcLayout.removeComponent(hcCssLayout);
			}
		});
		
		hcButton.setStyleName(BaseTheme.BUTTON_LINK);
		hcButton.setIcon(ICON);
		hcCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		hcCancelButton.setIcon(CancelIcon);
		addComponent(hcLayout);
		hcLayout.setSpacing(true);
		hcLayout.addComponent(hc);
	    hcLayout.addComponent(hcButton);
		
	    /**
	     * MARINa
	     */
		Button marina 	= 	new Button("MARINa", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("Marina", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);	
			}
		});
	    final GridLayout marinaLayout = new GridLayout();
	    marinaLayout.setColumns(2);
	    marinaLayout.setRows(2);
	    marinaLayout.setSizeFull();
	    marinaLayout.setImmediate(true);
	    marinaLayout.setColumnExpandRatio(1, 1.0f);

		final FancyCssLayout marinaCssLayout = new FancyCssLayout();
		marinaCssLayout.setWidth("95%");
		marinaCssLayout.setSlideEnabled(true);
		marinaCssLayout.addStyleName("lay");
		
		final Label marinaText = new Label(
				"<p align = \"justify\">MARINa Analysis</p>");
		marinaText.setContentMode(Label.CONTENT_XHTML);
		
		final Button marinaButton = new Button();
		final Button marinaCancelButton = new Button();
		marinaButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				marinaCssLayout.removeAllComponents();
				marinaLayout.removeComponent(marinaButton);
				marinaLayout.addComponent(marinaCancelButton, 1, 0);
				marinaCssLayout.addComponent(marinaText);
				marinaLayout.addComponent(marinaCssLayout, 0, 1, 1, 1);
			}
		});
		marinaCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				marinaCssLayout.removeAllComponents();
				marinaLayout.removeComponent(marinaCancelButton);
				marinaLayout.addComponent(marinaButton, 1, 0);
				marinaLayout.removeComponent(marinaCssLayout);
			}
		});
		
		marinaButton.setStyleName(BaseTheme.BUTTON_LINK);
		marinaButton.setIcon(ICON);
		marinaCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		marinaCancelButton.setIcon(CancelIcon);
		addComponent(marinaLayout);
		marinaLayout.setSpacing(true);
		marinaLayout.addComponent(marina);
	    marinaLayout.addComponent(marinaButton);
		
	    /**
	     * CNKB
	     */
		Button cnkb 	= 	new Button("CNKB", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("CNKB", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);	
			}
		});
	    final GridLayout cnkbLayout = new GridLayout();
	    cnkbLayout.setColumns(2);
	    cnkbLayout.setRows(2);
	    cnkbLayout.setSizeFull();
	    cnkbLayout.setImmediate(true);
	    cnkbLayout.setColumnExpandRatio(1, 1.0f);

		final FancyCssLayout cnkbCssLayout = new FancyCssLayout();
		cnkbCssLayout.setWidth("95%");
		cnkbCssLayout.setSlideEnabled(true);
		cnkbCssLayout.addStyleName("lay");
		
		final Label cnkbText = new Label(
				"<p align = \"justify\">The Cellular Network Knowledge Base (CNKB) is a repository of molecular interactions, " +
				"including ones both computationally and experimentally derived. Sources for interactions " +
				"include both publicly available databases such as BioGRID and HPRD, as well as reverse-engineered " +
				"cellular regulatory interactomes developed in the lab of Dr. Andrea Califano at Columbia University.</p>");
		cnkbText.setContentMode(Label.CONTENT_XHTML);
		
		final Button cnkbButton = new Button();
		final Button cnkbCancelButton = new Button();
		cnkbButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				cnkbCssLayout.removeAllComponents();
				cnkbLayout.removeComponent(cnkbButton);
				cnkbLayout.addComponent(cnkbCancelButton, 1, 0);
				cnkbCssLayout.addComponent(cnkbText);
				cnkbLayout.addComponent(cnkbCssLayout, 0, 1, 1, 1);
			}
		});
		cnkbCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				cnkbCssLayout.removeAllComponents();
				cnkbLayout.removeComponent(cnkbCancelButton);
				cnkbLayout.addComponent(cnkbButton, 1, 0);
				cnkbLayout.removeComponent(cnkbCssLayout);
			}
		});
		
		cnkbButton.setStyleName(BaseTheme.BUTTON_LINK);
		cnkbButton.setIcon(ICON);
		cnkbCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		cnkbCancelButton.setIcon(CancelIcon);
		addComponent(cnkbLayout);
		cnkbLayout.setSpacing(true);
		cnkbLayout.addComponent(cnkb);
		cnkbLayout.addComponent(cnkbButton);
		
		/*
		 * Differential Expression (TTest)
		 */
		Button ttest 	= 	new Button("Differential Expression (T-Test)", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("TTest", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);	
			}
		});
	    final GridLayout ttestLayout = new GridLayout();
	    ttestLayout.setColumns(2);
	    ttestLayout.setRows(2);
	    ttestLayout.setSizeFull();
	    ttestLayout.setImmediate(true);
	    ttestLayout.setColumnExpandRatio(1, 1.0f);

		final FancyCssLayout ttestCssLayout = new FancyCssLayout();
		ttestCssLayout.setWidth("95%");
		ttestCssLayout.setSlideEnabled(true);
		ttestCssLayout.addStyleName("lay");
		
		final Label ttestText = new Label(
				"<p align = \"justify\">A t-Test analysis can be used to identify markers with statistically " +
				"significant differential expression between two sets of microarrays.</p>");
		ttestText.setContentMode(Label.CONTENT_XHTML);
		
		final Button ttestButton = new Button();
		final Button ttestCancelButton = new Button();
		ttestButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				ttestCssLayout.removeAllComponents();
				ttestLayout.removeComponent(ttestButton);
				ttestLayout.addComponent(ttestCancelButton, 1, 0);
				ttestCssLayout.addComponent(ttestText);
				ttestLayout.addComponent(ttestCssLayout, 0, 1, 1, 1);
			}
		});
		ttestCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				ttestCssLayout.removeAllComponents();
				ttestLayout.removeComponent(ttestCancelButton);
				ttestLayout.addComponent(ttestButton, 1, 0);
				ttestLayout.removeComponent(ttestCssLayout);
			}
		});
		
		ttestButton.setStyleName(BaseTheme.BUTTON_LINK);
		ttestButton.setIcon(ICON);
		ttestCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		ttestCancelButton.setIcon(CancelIcon);
		addComponent(ttestLayout);
		ttestLayout.setSpacing(true);
		ttestLayout.addComponent(ttest);
		ttestLayout.addComponent(ttestButton);
		
		aracne.setStyleName(Reindeer.BUTTON_LINK);
		marina.setStyleName(Reindeer.BUTTON_LINK);
		cnkb.setStyleName(Reindeer.BUTTON_LINK);
		hc.setStyleName(Reindeer.BUTTON_LINK);
		ttest.setStyleName(Reindeer.BUTTON_LINK);
		
		Label vis = new Label("Visualizations Available");
		vis.setStyleName(Reindeer.LABEL_H2);
		vis.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(vis);
		
		/**
		 *  Tabular microarray viewer
		 */
		final GridLayout tableLayout 		=	new GridLayout();
		final Button tableButton 			= 	new Button();
		final Button tableCancelButton 		= 	new Button();	
		final FancyCssLayout tableCssLayout = 	new FancyCssLayout();
		
		tableLayout.setColumns(2);
		tableLayout.setRows(2);
		tableLayout.setSizeFull();
		tableLayout.setImmediate(true);
		tableLayout.setColumnExpandRatio(1, 1.0f);

		tableCssLayout.setWidth("95%");
		tableCssLayout.setSlideEnabled(true);
		tableCssLayout.addStyleName("lay");
		
		Button table 	= 	new Button("Tabular Microarray Viewer", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("TabularView", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);
			}
		});
		
		final Label tableText = new Label(
				"<p align = \"justify\">Presents the numerical values of the expression measurements in a table format. " +
				"One row is created per individual marker/probe and one column per microarray.</p>");
		tableText.setContentMode(Label.CONTENT_XHTML);
		
		tableButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				tableCssLayout.removeAllComponents();
				tableLayout.removeComponent(tableButton);
				tableLayout.addComponent(tableCancelButton, 1, 0);
				tableCssLayout.addComponent(tableText);
				tableLayout.addComponent(tableCssLayout, 0, 1, 1, 1);
			}
		});
		tableCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				tableCssLayout.removeAllComponents();
				tableLayout.removeComponent(tableCancelButton);
				tableLayout.addComponent(tableButton, 1, 0);
				tableLayout.removeComponent(tableCssLayout);
			}
		});
		
		tableButton.setStyleName(BaseTheme.BUTTON_LINK);
		tableButton.setIcon(ICON);
		tableCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		tableCancelButton.setIcon(CancelIcon);
		addComponent(tableLayout);
		tableLayout.setSpacing(true);
		tableLayout.addComponent(table);
		tableLayout.addComponent(tableButton);
		
		/**
		 * HeatMap
		 */
		Button heatMap 	= 	new Button("Heat Map");
		Label heatText 	=	new Label(
				"<p align = \"justify\">Heat map for microarray expression data, organized by phenotypic or gene groupings.</p>");
		heatText.setContentMode(Label.CONTENT_XHTML);
		
		table.setStyleName(Reindeer.BUTTON_LINK);
		heatMap.setStyleName(Reindeer.BUTTON_LINK);
    }

	private static Log log = LogFactory.getLog(MicroarrayUI.class);
	
	// FIXME
	// the entire implementation of the action after you click on an analysis name to bring up the parameter does not make any sense
	// I will re-write it gradually while maintaining so not to break the observable behavior. 
	@SuppressWarnings("deprecation")
	private void showAnalysisParameterPanel(Analysis analysis,
			AbstractComponentContainer analysisUI, Long dataSetId) {

		Component layoutToBeUpdated = this.getParent().getParent(); // pluginLayout

		if (!(analysisUI instanceof AnovaUI)) {
			log.warn(analysisUI.getClass() + " not supported yet.");
			return; // TODO all analysis UIs need to implement necessary method,
					// e.g. setDataSetId
		}
		AnovaUI ui = (AnovaUI) analysisUI;
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
	// TODO this is copied from ToolsUI, but exactly the same, especially that this needs to trigger the actual analysis.
	// the version in ToolsUI should not have the misleading link looking
	private void buildOneItem(final Analysis analysis,
			final AbstractComponentContainer analysisUI) {

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
