package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.analysis.CNKB.ui.UCNKBParamForm;
import org.geworkbenchweb.analysis.aracne.ui.UAracneParamForm;
import org.geworkbenchweb.analysis.hierarchicalclustering.ui.UHierarchicalClusteringParamForm;
import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

/**
 * Have to refactor this code so that this handles only basic data operations.
 * Each analysis, normalization should have their own param panel.
 * @author Nikhil Reddy
 *
 */
public class UDataTab extends VerticalLayout {

	private static final long serialVersionUID 		= 		-1888971408170241086L;
	
	private DSMicroarraySet dataSet;
	
	public UDataTab(DSMicroarraySet maSet, String action) {
	
		this.dataSet = maSet;
		setSizeFull();
		
		HorizontalSplitPanel dataSplitPanel 	= 	new HorizontalSplitPanel();
		Panel historyPanel						= 	new Panel();
		final Panel dataPanel					= 	new Panel();
		final Form paramForm 					= 	new Form();
		final ComboBox operationsBox			=   new ComboBox();
		final ComboBox analysisBox				= 	new ComboBox();
		final ComboBox interactionsBox			= 	new ComboBox();
		final Panel paramPanel					= 	new Panel();
		
		paramPanel.setImmediate(true);	
		paramPanel.setStyleName("bubble");
		
		paramForm.setImmediate(true);
		paramForm.addField("operations", operationsBox);
		
		dataPanel.setImmediate(true);
		dataPanel.setSizeFull();
		dataPanel.setCaption("Parameter Panel");
		dataPanel.setStyleName("bubble");
		dataPanel.addComponent(paramForm);
	
		analysisBox.setWidth("60%");
		analysisBox.setNullSelectionAllowed(false);
		analysisBox.setCaption("Select Analyis Type");
		analysisBox.addItem("ARACne");
		analysisBox.addItem("Hierarchical Clustering");
		analysisBox.setInputPrompt("Choose Analysis from the list");
		analysisBox.addListener(new Property.ValueChangeListener() {
	    	
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				try {
					if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Hierarchical Clustering")) {	
						
						paramPanel.removeAllComponents();
						paramPanel.setCaption("Hierarchical Clustering Parameters");
						
						UHierarchicalClusteringParamForm hsParamForm = new UHierarchicalClusteringParamForm(dataSet);
						paramPanel.addComponent(hsParamForm);
						dataPanel.addComponent(paramPanel);

					}else if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("ARACne")) {
						
						paramPanel.removeAllComponents();
						paramPanel.setCaption("ARACne Parameters");
						
						UAracneParamForm aracneParamForm = new UAracneParamForm(dataSet);
						paramPanel.addComponent(aracneParamForm);
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
						paramPanel.addComponent(new UCNKBParamForm(dataSet));
						
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
		
		if(action != null) {
			if(action == "Analyze Data") {
				
				operationsBox.select("Analyze Data");
				paramForm.addField("analysis", analysisBox);
				
			}else if(action == "Get Interactions") {
				
				operationsBox.select("Get Interactions");
				paramForm.addField("interactions", interactionsBox);
			}
			
			operationsBox.setEnabled(false);
		
		}
		
		/* Data history Tab */
		historyPanel.setCaption("DataSet History");
		historyPanel.setStyleName("bubble");
		historyPanel.setSizeFull();
		historyPanel.addComponent(new Label("Name of the DataSet : " + maSet.getLabel()));
		historyPanel.addComponent(new Label("Number of Markers : " + maSet.getMarkers().size()));
		historyPanel.addComponent(new Label("Number of Arrays : " + maSet.size()));
		historyPanel.addComponent(new Label("--------------------------------------------------"));
		historyPanel.addComponent(new Label("All the randon dataset history and what kind of analysis performed should be displayed here. " +
				"It should also accompany parameters used to perform the analysis."));
		
				
		dataSplitPanel.setImmediate(true);
		dataSplitPanel.setSplitPosition(70);
		dataSplitPanel.setStyleName("small previews");
		dataSplitPanel.setFirstComponent(dataPanel);
		dataSplitPanel.setSecondComponent(historyPanel);
		
		addComponent(dataSplitPanel);
		
	}

}


