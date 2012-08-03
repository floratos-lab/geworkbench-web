package org.geworkbenchweb.analysis.markus.ui;

import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class UMarkusDataTab  extends VerticalLayout {
	private static final long serialVersionUID = -6059350836959131057L;

	public UMarkusDataTab(DSProteinStructure prtSet, String action) {

		setSizeFull();
		setStyleName(Reindeer.LAYOUT_WHITE);
		
		HorizontalSplitPanel dataSplitPanel 	= 	new HorizontalSplitPanel();
		Panel historyPanel						= 	new Panel();
		final Panel dataPanel					= 	new Panel();
		final Form paramForm 					= 	new Form();
		final ComboBox analysisBox				= 	new ComboBox();
		final Panel paramPanel					= 	new Panel();
		
		paramPanel.setImmediate(true);	
		
		paramForm.setImmediate(true);
		paramForm.addField("analysis", analysisBox);
		
		dataPanel.setImmediate(true);
		dataPanel.setSizeFull();
		dataPanel.setStyleName(Reindeer.PANEL_LIGHT);
		dataPanel.addComponent(paramForm);
	
		analysisBox.setWidth("60%");
		analysisBox.setNullSelectionAllowed(false);
		analysisBox.setCaption("Select Analyis Type");
		analysisBox.addItem("MarkUs Parameters");
		analysisBox.setInputPrompt("Choose Analysis from the list");
		analysisBox.addListener(new changeListener(prtSet, paramPanel, dataPanel));
	
		/* Data history Tab */
		historyPanel.setStyleName(Reindeer.PANEL_LIGHT);
		historyPanel.setSizeFull();
		historyPanel.addComponent(new Label("Name of the DataSet : " + prtSet.getLabel()));
		historyPanel.addComponent(new Label("--------------------------------------------------"));
		historyPanel.addComponent(new Label("Data set history"));
			
		dataSplitPanel.setImmediate(true);
		dataSplitPanel.setSplitPosition(70);
		dataSplitPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
		dataSplitPanel.setFirstComponent(dataPanel);
		dataSplitPanel.setSecondComponent(historyPanel);
		
		addComponent(dataSplitPanel);
		
	}

	private class changeListener implements Property.ValueChangeListener {
	    	
		private static final long serialVersionUID = 1L;
		private Panel paramPanel, dataPanel;
		private DSProteinStructure dataSet;

		public changeListener(DSProteinStructure dataSet, Panel paramPanel, Panel dataPanel){
			this.dataSet = dataSet;
			this.paramPanel = paramPanel;
			this.dataPanel = dataPanel;
		}

		public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
			try {
				if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("MarkUs")) {	
					paramPanel.removeAllComponents();
					paramPanel.setCaption("MarkUs");

					UMarkusParamForm markusParamForm = new UMarkusParamForm(dataSet);
					paramPanel.addComponent(markusParamForm);
					dataPanel.addComponent(paramPanel);
				}
			}catch (Exception e){
				dataPanel.removeComponent(paramPanel);
			}
		}
	}

}
