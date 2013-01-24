package org.geworkbenchweb.plugins.microarray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.plugins.DataTypeUI;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/* TODO this is UI for one particular data type, namely microarray dataset.
 * The design should be general enough so we have code of similar structure 
 * for different data types, either as inputs or results
 * 
 */
public class MicroarrayUI extends DataTypeMenuPage implements DataTypeUI {

	private static final long serialVersionUID = 1L;
	
	public MicroarrayUI(Long dataSetId) {
		super("Microarray Description", "Microarray Data", DSMicroarraySet.class, dataSetId);
		
		// TODO visualization eventually should be covered by DataTypeMenuPage as well
		
		// for now, there is only one visualization - tabular view
		// but the design should be flexible enough to support multiple visualizations
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
		
		table.setStyleName(Reindeer.BUTTON_LINK);
		log.debug("constrcutor returns");
    }

	private static Log log = LogFactory.getLog(MicroarrayUI.class);

	private final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	private final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

}
