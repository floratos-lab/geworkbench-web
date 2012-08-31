package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbenchweb.analysis.CNKB.ui.UCNKBParamForm;
import org.geworkbenchweb.analysis.aracne.ui.UAracneParamForm;
import org.geworkbenchweb.analysis.anova.ui.UAnovaParamForm;
import org.geworkbenchweb.analysis.hierarchicalclustering.ui.UHierarchicalClusteringParamForm;
import org.geworkbenchweb.analysis.marina.ui.UMarinaParamForm;
import org.geworkbenchweb.analysis.markus.ui.UMarkusParamForm;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Have to refactor this code so that this handles only basic data operations.
 * Each analysis, normalization should have their own param panel.
 * @author Nikhil Reddy
 *
 */

public class UDataTab extends VerticalLayout {

	private static final long serialVersionUID 		= 	-1888971408170241086L;

	private HorizontalSplitPanel dataSplitPanel;
	
	private Panel historyPanel;
	
	private Panel dataPanel;
	
	private String[] dataProp;
	
	public UDataTab(DSDataSet<? extends DSBioObject> inputDataSet, String[] dataProperties) {
				
		this.dataProp = dataProperties;
		
		setSizeFull();
		setStyleName(Reindeer.LAYOUT_WHITE);
		
		dataSplitPanel 		= 	new HorizontalSplitPanel();
		historyPanel		= 	new Panel();
		dataPanel			= 	new Panel();
		
		dataPanel.setImmediate(true);
		dataPanel.setSizeFull();
		dataPanel.setStyleName(Reindeer.PANEL_LIGHT);
		
		historyPanel.setImmediate(true);
		historyPanel.setStyleName(Reindeer.PANEL_LIGHT);
		historyPanel.setSizeFull();
		
		if(inputDataSet != null) {
			if(inputDataSet instanceof DSMicroarraySet) {
				
				DSMicroarraySet maSet = (DSMicroarraySet) inputDataSet;
				buildMicroarraySetPanel(maSet);
			 
			}else{
				
				DSProteinStructure pdbSet = (DSProteinStructure) inputDataSet;
				buildPDBPanel(pdbSet);
			
			}
		}
		
		dataSplitPanel.setImmediate(true);
		dataSplitPanel.setSplitPosition(70);
		dataSplitPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
		dataSplitPanel.setFirstComponent(dataPanel);
		dataSplitPanel.setSecondComponent(historyPanel);
		
		addComponent(dataSplitPanel);
		
	}
	
	/**
	 * Builds Panel for MicroarraySet Operations
	 */
	private void buildMicroarraySetPanel(DSMicroarraySet maSet) {
		
		final DSMicroarraySet dataSet = maSet;
		
		final ComboBox operationsBox			=   new ComboBox();
		final ComboBox analysisBox				= 	new ComboBox();
		final ComboBox interactionsBox			= 	new ComboBox();
		final Form paramForm 					= 	new Form();
		final Panel paramPanel					= 	new Panel();
		
		paramPanel.setImmediate(true);	
		
		paramForm.setImmediate(true);
		paramForm.addField("operations", operationsBox);
		
		analysisBox.setWidth("60%");
		analysisBox.setNullSelectionAllowed(false);
		analysisBox.setCaption("Select Analyis Type");
		
		analysisBox.addItem("Anova");
		analysisBox.addItem("ARACne");
		analysisBox.addItem("Hierarchical Clustering");
		analysisBox.addItem("MARINa");
		
		analysisBox.setInputPrompt("Choose Analysis from the list");
		analysisBox.addListener(new Property.ValueChangeListener() {
	    	
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				try {
					if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Hierarchical Clustering")) {	
						
						paramPanel.removeAllComponents();
						paramPanel.setCaption("Hierarchical Clustering Parameters");
						
						UHierarchicalClusteringParamForm hsParamForm = new UHierarchicalClusteringParamForm(dataSet, Long.parseLong(dataProp[1].substring(0, dataProp[1].length() - 1)));
						paramPanel.addComponent(hsParamForm);
						dataPanel.addComponent(paramPanel);

					}else if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("ARACne")) {
						
						paramPanel.removeAllComponents();
						paramPanel.setCaption("ARACne Parameters");
						
						UAracneParamForm aracneParamForm = new UAracneParamForm(dataSet, Long.parseLong(dataProp[1].substring(0, dataProp[1].length() - 1)));
						paramPanel.addComponent(aracneParamForm);
						dataPanel.addComponent(paramPanel);
						
					}else if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Anova")) {
						
						paramPanel.removeAllComponents();
						paramPanel.setCaption("Anova Parameters");
						
						UAnovaParamForm aracneParamForm = new UAnovaParamForm(dataSet, Long.parseLong(dataProp[1].substring(0, dataProp[1].length() - 1)));
						paramPanel.addComponent(aracneParamForm);
						dataPanel.addComponent(paramPanel);
						
					}else if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("MARINa")) {
						
						paramPanel.removeAllComponents();
						paramPanel.setCaption("MARINa Parameters");
						
						UMarinaParamForm marinaParamForm = new UMarinaParamForm(dataSet, Long.parseLong(dataProp[1].substring(0, dataProp[1].length() - 1)));
						paramPanel.addComponent(marinaParamForm);
						dataPanel.addComponent(paramPanel);

					}
						
				}catch (Exception e){
					dataPanel.removeComponent(paramPanel);
				}
			}
		});
		
		interactionsBox.setWidth("60%");
		interactionsBox.setCaption("Select Interactions Database");
		interactionsBox.setNullSelectionAllowed(false);
		interactionsBox.addItem("CNKB");
		interactionsBox.setInputPrompt("Choose Interaction Database from the list");
		interactionsBox.addListener(new Property.ValueChangeListener() {
	    	
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				try {
					if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("CNKB")) {
						paramPanel.removeAllComponents();
						paramPanel.setCaption("CNKB Parameters");
						paramPanel.addComponent(new UCNKBParamForm(dataSet, Long.parseLong(dataProp[1].substring(0, dataProp[1].length() - 1))));
						
						dataPanel.addComponent(paramPanel);
					}
				}catch (Exception e){
					
					dataPanel.removeComponent(paramPanel);
					
				}
			}
		});
		
		operationsBox.setWidth("60%");
		operationsBox.setNullSelectionAllowed(false);
		operationsBox.setCaption("Select Data Operation");
		operationsBox.addItem("Analyze Data");
		operationsBox.addItem("Get Interactions");
		operationsBox.setInputPrompt("Choose Data Operation from the list");
		
		operationsBox.addListener(new Property.ValueChangeListener() {
	    	
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				try {
					dataPanel.removeComponent(paramPanel);
				}catch (Exception e){
					//TODO
				}
				
				try {
					if(valueChangeEvent.getProperty().getValue().toString() == "Analyze Data") {

						interactionsBox.unselect(interactionsBox.getValue());
						interactionsBox.setVisible(false);
						analysisBox.setVisible(true);
						paramForm.addField("analysis", analysisBox);

					}else {

						analysisBox.unselect(analysisBox.getValue());
						analysisBox.setVisible(false);
						interactionsBox.setVisible(true);						
						paramForm.addField("interactions", interactionsBox);

					}
				}catch(NullPointerException e) {
					System.out.println("let us worry about this later");
				}
			}
		});
		
		if(dataProp[2] != null) {
			if(dataProp[2] == "Analyze Data") {
				
				operationsBox.select("Analyze Data");
				paramForm.addField("analysis", analysisBox);
				
			}else if(dataProp[2] == "Get Interactions") {
				
				operationsBox.select("Get Interactions");
				paramForm.addField("interactions", interactionsBox);
			}
			
			operationsBox.setEnabled(false);
		
		}
		
		/* Data history Tab */
		historyPanel.addComponent(new Label("Name of the DataSet : " + dataSet.getLabel()));
		historyPanel.addComponent(new Label("Number of Markers : " + dataSet.getMarkers().size()));
		historyPanel.addComponent(new Label("Number of Arrays : " + dataSet.size()));
		historyPanel.addComponent(new Label("--------------------------------------------------"));
		historyPanel.addComponent(new Label("Data set history"));
	
		dataPanel.addComponent(paramForm);
		
	}
	
	/**
	 * Builds Panel for protien structure analysis
	 * @param pdbSet 
	 * @return
	 */
	private void buildPDBPanel(DSProteinStructure pdbSet) {
		
		final DSProteinStructure dataSet 		= 	pdbSet;
		final ComboBox analysisBox				= 	new ComboBox();
		final Panel paramPanel					= 	new Panel();
		final Form paramForm 					= 	new Form();
		
		paramPanel.setImmediate(true);	
		
		paramForm.setImmediate(true);
		paramForm.addField("analysis", analysisBox);
		
		analysisBox.setWidth("60%");
		analysisBox.setNullSelectionAllowed(false);
		analysisBox.setCaption("Select Analyis Type");
		analysisBox.addItem("MarkUs");
		analysisBox.setInputPrompt("Choose Analysis from the list");
		analysisBox.addListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					if(event.getProperty().getValue().toString().equalsIgnoreCase("MarkUs")) {	
						paramPanel.removeAllComponents();
						paramPanel.setCaption("MarkUs Parameters");

						UMarkusParamForm markusParamForm = new UMarkusParamForm(dataSet, Long.parseLong(dataProp[1].substring(0, dataProp[1].length() - 1)));
						paramPanel.addComponent(markusParamForm);
						dataPanel.addComponent(paramPanel);
					}
				}catch (Exception e){
					dataPanel.removeComponent(paramPanel);
				}
				
			}
		
		});
		
		/* Data history Tab */
		historyPanel.addComponent(new Label("Name of the DataSet : " + pdbSet.getLabel()));
		historyPanel.addComponent(new Label("--------------------------------------------------"));
		historyPanel.addComponent(new Label("Data set history"));
		
		dataPanel.addComponent(paramForm);
		
	}
}


