package org.geworkbenchweb.plugins.tools;

import java.util.List;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Analysis;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/* FIXME This is a temporary quick solution. the visualPlugin in UMainLayout and many other stuff need to fixed first. */
//most of the code is copied from MicroarrayUI.java
//FIXME eventually we need a registry for all the 'visual plugins' or some other better name
//FIXME visual plugins (or another better name) need some common interface(s)
// FIXME MicroarrayUI and similar classes should be fixed like this class as well
public class ToolsUI extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	
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

	// FIXME when I tested, this constructor seemed to be called twice
	public ToolsUI(Long dummy) {

		setSpacing(true);
		
		List<Analysis> analysisList = GeworkbenchRoot.getPluginRegistry().getAnalysisList();
		
		// first part: analysis
		Label analysisLabel = new Label("Analysis Available");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(analysisLabel);
		
		// TODO convert other analysis plugins
		for(Analysis a : analysisList) {
			buildOneItem(a.getName(), a.getDescription());
		}
		
		// hierarchical clustering
		buildOneItem("Hierarchical Clustering", "Hierarchical clustering is a method to group arrays and/or markers together based on similarity " +
				"on their expression profiles. geWorkbench implements its own code for agglomerative hierarchical " +
				"clustering. Starting from individual points (the leaves of the tree), nearest neighbors are found " +
				"for individual points, and then for groups of points, at each step building up a branched " +
				"structure that converges toward a root that contains all points. The resulting graph tends to " +
				"group similar items together. Results of hierarchical clustering are displayed in the Dendrogram " +
				"component.");
		// MARINa
		buildOneItem("MARINa", "MARINa Analysis");
		//MarkUs
		buildOneItem("MarkUs", "MarkUs is a web server to assist the assessment of the biochemical function " +
				"for a given protein structure. MarkUs identifies related protein structures " +
				"and sequences, detects protein cavities, and calculates the surface electrostatic " +
				"potentials and amino acid conservation profile.");
		// t-test
		buildOneItem("Differential Expression (T-Test)", "A t-Test analysis can be used to identify markers with statistically " +
				"significant differential expression between two sets of microarrays.");
		
		// second part: visualizations
		Label vis = new Label("Visualizations Available");
		vis.setStyleName(Reindeer.LABEL_H2);
		vis.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(vis);
		
		// tabular view
		buildOneItem("Tabular Microarray Viewer", "Presents the numerical values of the expression measurements in a table format. " +
				"One row is created per individual marker/probe and one column per microarray.");
		// anova result
		buildOneItem("ANOVA Result Viewer", "Show ANOVA result as a table");
		// aracne result
		buildOneItem("Cytoscape", "Show network in cytoscape web, or in text view.");
		// cnkb result
		buildOneItem("CNKB Result View", "Show CNKB Result including throttle plot.");
		// hierarchical result
		buildOneItem("Dendrogram plus heat map", "Show result from hierarchical clustering.");
		// marina result
		buildOneItem("MARINa result viewer", "Show result of MARINa analysis");
		// markus result
		buildOneItem("MARKUS result viewer", "Show result MARKUS result in embbed browser");
	}

	private final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	private final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

	// build one item and add to the UI
	private void buildOneItem (String itemName, final String itemDescription) {

		final ItemLayout itemLayout 		=	new ItemLayout();
		final Button infoButton 			= 	new Button();
		final Button cancelButton 		= 	new Button();	
		
		Button toolNameText 	= 	new Button(itemName); // FIXME, not a button, a text label really
		toolNameText.setStyleName(Reindeer.BUTTON_LINK);
		
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
		itemLayout.addComponent(toolNameText);
		itemLayout.addComponent(infoButton);

		addComponent(itemLayout);
	}
}
