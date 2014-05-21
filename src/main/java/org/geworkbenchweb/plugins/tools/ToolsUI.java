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
		final String pluginDescription = "LINCS Query Interface";
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
