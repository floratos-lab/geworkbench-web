package org.geworkbenchweb.plugins.tools;

import org.geworkbenchweb.plugins.Analysis;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.plugins.DataTypeUI;

import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

/* FIXME This is a temporary solution. the visualPlugin in UMainLayout and many other stuff need to fixed first. */
public class ToolsUI extends DataTypeMenuPage implements DataTypeUI {

	private static final long serialVersionUID = 1L;
	
	public ToolsUI() {
		super("The list of all the available tools.", "Tools", null, null);
		
		// first part: analysis. taken care of in DataTypeMenuPage
		
		// second part: visualizations. eventually this should be covered by DataTypeMenuPage as well
		Label vis = new Label("Visualizations Available");
		vis.setStyleName(Reindeer.LABEL_H2);
		vis.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(vis);
		
		// tabular view
		buildOneItem(new Analysis("Tabular Microarray Viewer", "Presents the numerical values of the expression measurements in a table format. " +
				"One row is created per individual marker/probe and one column per microarray."), null);
		// anova result
		buildOneItem(new Analysis("ANOVA Result Viewer", "Show ANOVA result as a table"), null);
		// aracne result
		buildOneItem(new Analysis("Cytoscape", "Show network in cytoscape web, or in text view."), null);
		// cnkb result
		buildOneItem(new Analysis("CNKB Result View", "Show CNKB Result including throttle plot."), null);
		// hierarchical result
		buildOneItem(new Analysis("Dendrogram plus heat map", "Show result from hierarchical clustering."), null);
		// marina result
		buildOneItem(new Analysis("MARINa result viewer", "Show result of MARINa analysis"), null);
		// markus result
		buildOneItem(new Analysis("MARKUS result viewer", "Show result MARKUS result in embbed browser"), null);
	}
}
