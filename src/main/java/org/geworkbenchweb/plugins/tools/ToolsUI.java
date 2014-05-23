package org.geworkbenchweb.plugins.tools;

import org.geworkbenchweb.layout.VisualPluginView;
import org.geworkbenchweb.plugins.DataTypeMenuPage;
import org.geworkbenchweb.plugins.ItemLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * List of all plug-ins regardless of data type. 
*/
public class ToolsUI extends DataTypeMenuPage {

	private static final long serialVersionUID = 1L;
	
	public ToolsUI(final VisualPluginView pluginView) {
		super("The list of all the available tools.", "Tools", null, null);
		
		Label analysisLabel = new Label("Other Tools");
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(analysisLabel);

		VerticalLayout group = new VerticalLayout();
		group.setMargin(true);
		
		final ItemLayout itemLayout = new ItemLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();

		final String pluginName = "LINCS";
		final String pluginDescription = "The LINCS tool provides for query and display of data generated by the "
+"Columbia LINCS Technology U01 and Computation U01 Centers, both under "
+"the direction of Dr. Andrea Califano. It provides experimental drug-drug "
+"synergy results, as well as computationally-derived similarity scores "
+"for drug mode of action.  The NIH LINCS (Library of Integrated "
+"Network-based Cellular Signatures) Program supports a "
+"cellular-network-based investigation into biology at a number of centers "
+"across the country. It catalogs changes to gene expression and other "
+"cellular processes resulting from perturbation experiments.<br/>"
+"Data is preliminary, please see "
+"<a href='http://wiki.c2b2.columbia.edu/workbench/index.php/LINCS_Query#Currently_Available_Data' target='_blank'>"
+"here</a> "
+"for currently available data.";
		Button toolButton = new Button(pluginName,
				new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						pluginView.showLincs();
					}
				});
		toolButton.setStyleName(Reindeer.BUTTON_LINK);

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
		
		addComponent(group);
	}
}
