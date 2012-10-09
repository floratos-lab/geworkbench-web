package org.geworkbenchweb.plugins.microarray;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.PluginEvent;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

public class MicroarrayUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
	private final Long dataId;

	public MicroarrayUI(Long dataSetId) {

		setSpacing(true);
		
		this.dataId = dataSetId;
		Label analysisLabel = new Label("Analysis Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		
		ThemeResource ICON = new ThemeResource(
	            "../custom/icons/icon_info.gif");

		ThemeResource CancelIcon = new ThemeResource(
	            "../runo/icons/16/cancel.png");
		
		addComponent(analysisLabel);
		
		//ARACNE
		Button aracne 	= 	new Button("ARACne", new Button.ClickListener() {
			
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
	    final GridLayout anovaLayout = new GridLayout();
	    anovaLayout.setColumns(2);
	    anovaLayout.setRows(2);
	    anovaLayout.setSizeFull();
	    anovaLayout.setImmediate(true);
	    anovaLayout.setColumnExpandRatio(1, 1.0f);
		Button anova 	= 	new Button("Anova", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("Anova", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);	
			}
		});
		final FancyCssLayout anovaCssLayout = new FancyCssLayout();
		anovaCssLayout.setWidth("95%");
		anovaCssLayout.setSlideEnabled(true);
		anovaCssLayout.addStyleName("lay");
		
		final Label anovaText = new Label(
				"<p align= \"justify\">The geWorkbench ANOVA component implements a one-way analysis of variance calculation " +
				"derived from TIGR's MeV (MultiExperiment Viewer) (Saeed, 2003). At least three groups of " +
				"arrays must be specified by defining and activating them in the Arrays/Phenotypes component.</p>");
		anovaText.setContentMode(Label.CONTENT_XHTML);
		
		final Button anovaButton = new Button();
		final Button anovaCancelButton = new Button();
			
		anovaButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				anovaCssLayout.removeAllComponents();
				anovaLayout.removeComponent(anovaButton);
				anovaLayout.addComponent(anovaCancelButton, 1, 0);
				anovaCssLayout.addComponent(anovaText);
				anovaLayout.addComponent(anovaCssLayout, 0, 1, 1, 1);
			}
		});
		anovaCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				anovaCssLayout.removeAllComponents();
				anovaLayout.removeComponent(anovaCancelButton);
				anovaLayout.addComponent(anovaButton, 1, 0);
				anovaLayout.removeComponent(anovaCssLayout);
				            
			}
		});
		
		anovaButton.setStyleName(BaseTheme.BUTTON_LINK);
		anovaButton.setIcon(ICON);
		anovaCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		anovaCancelButton.setIcon(CancelIcon);
		addComponent(anovaLayout);
		anovaLayout.setSpacing(true);
		anovaLayout.addComponent(anova);
	    anovaLayout.addComponent(anovaButton);
		
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
				"<p align = \"justify\">MARINa Analysis</p>");
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
		
		
		aracne.setStyleName(Reindeer.BUTTON_LINK);
		anova.setStyleName(Reindeer.BUTTON_LINK);
		marina.setStyleName(Reindeer.BUTTON_LINK);
		cnkb.setStyleName(Reindeer.BUTTON_LINK);
		hc.setStyleName(Reindeer.BUTTON_LINK);
	
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
		
		
		
		/*addComponent(table);
		addComponent(tableText);
		addComponent(heatMap);
		addComponent(heatText);*/
    }
}
