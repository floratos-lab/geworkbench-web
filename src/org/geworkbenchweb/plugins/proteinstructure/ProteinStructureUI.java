package org.geworkbenchweb.plugins.proteinstructure;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.PluginEvent;
import org.geworkbenchweb.plugins.DataTypeUI;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

public class ProteinStructureUI extends VerticalLayout implements DataTypeUI {

	private static final long serialVersionUID = 1L;
	
	private final Long dataId;
	
	public ProteinStructureUI(Long dataSetId) {
		
		setDescription("PDB File");
		
		setSizeFull();
		
		this.dataId = dataSetId;
		
		ThemeResource ICON = new ThemeResource(
	            "../custom/icons/icon_info.gif");

		ThemeResource CancelIcon = new ThemeResource(
	            "../runo/icons/16/cancel.png");
		
		Label analysisLabel = new Label("Analysis Available\n\n");
		analysisLabel.setContentMode(Label.CONTENT_PREFORMATTED);
		analysisLabel.setStyleName(Reindeer.LABEL_H2);
		addComponent(analysisLabel);
		
		Button marcus 	= 	new Button("MarkUs", new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				PluginEvent loadPlugin = new PluginEvent("MarkUs", dataId);
				GeworkbenchRoot.getBlackboard().fire(loadPlugin);
			}
		});
		marcus.setStyleName(Reindeer.BUTTON_LINK);
		final Label markusText = new Label(
				"<p align=\"justify\">MarkUs is a web server to assist the assessment of the biochemical function " +
				"for a given protein structure. MarkUs identifies related protein structures " +
				"and sequences, detects protein cavities, and calculates the surface electrostatic " +
				"potentials and amino acid conservation profile.</p>");
		markusText.setContentMode(Label.CONTENT_XHTML);
		
		final GridLayout marcusLayout 			=	new GridLayout();
		final FancyCssLayout marcusCssLayout 	= 	new FancyCssLayout();
		
		marcusLayout.setColumns(2);
		marcusLayout.setRows(2);
		marcusLayout.setSizeFull();
		marcusLayout.setImmediate(true);
		marcusLayout.setColumnExpandRatio(1, 1.0f);
		
		marcusCssLayout.setWidth("95%");
		marcusCssLayout.setSlideEnabled(true);
		marcusCssLayout.addStyleName("lay");
		
		final Button marcusButton 		= 	new Button();
		final Button marcusCancelButton = 	new Button();
		
		marcusButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				marcusCssLayout.removeAllComponents();
				marcusLayout.removeComponent(marcusButton);
				marcusLayout.addComponent(marcusCancelButton, 1, 0);
				marcusCssLayout.addComponent(markusText);
				marcusLayout.addComponent(marcusCssLayout, 0, 1, 1, 1);
			}
		});
		marcusCancelButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				marcusCssLayout.removeAllComponents();
				marcusLayout.removeComponent(marcusCancelButton);
				marcusLayout.addComponent(marcusButton, 1, 0);
				marcusLayout.removeComponent(marcusCssLayout);
			}
		});
	
		marcusButton.setStyleName(BaseTheme.BUTTON_LINK);
		marcusButton.setIcon(ICON);
		marcusCancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		marcusCancelButton.setIcon(CancelIcon);
		addComponent(marcusLayout);
		marcusLayout.setSpacing(true);
		marcusLayout.addComponent(marcus);
		marcusLayout.addComponent(marcusButton);
		
		setCaption("Protein Strucure Data");
	}
}
