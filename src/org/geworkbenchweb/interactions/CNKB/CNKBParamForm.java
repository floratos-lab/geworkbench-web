package org.geworkbenchweb.interactions.CNKB;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.analysis.hierarchicalclustering.HierarchicalClusteringAnalysis;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TwinColSelect;

/**
 * Parameter panel for CNKB
 * @author Nikhil
 * 
 */
public class CNKBParamForm extends Form {

	private static final long serialVersionUID = -1221913812891134388L;
	
	private DSMicroarraySet dataSet;
	
	private static final String[] interactomes = new String[] { "BCi (66193 interactions)", "BIND (45454 interactions)",
    "Geneways (26931 interactions)", "HGi (672786 interactions)" };
	
	private static final String[] interactions = new String[] { "Modular-TF", "Protein-DNA",
         "Protein-Protein" };
	
	public CNKBParamForm(DSMicroarraySet maSet) {
		
		this.dataSet = maSet;
	
		final String[] params 	=	new String[3];
		
		ComboBox interactomeBox 		= 	new ComboBox();
		TwinColSelect interactionTypes 	= 	new TwinColSelect();
		OptionGroup columnDispPref		= 	new OptionGroup("Column Display Preferences");
		OptionGroup networkGenPref		= 	new OptionGroup("Network Generation Preferences");
		
		interactomeBox.setCaption("Interactome");
		for (int j = 0; j < interactomes.length; j++) {
            interactomeBox.addItem(interactomes[j]);
        }
		interactomeBox.select(interactomeBox.getItemIds().iterator().next());
		interactomeBox.setWidth("50%");
		interactomeBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					
		

				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});
		
		for (int i = 0; i < interactions.length; i++) {
            interactionTypes.addItem(interactions[i]);
        }
		
		columnDispPref.setMultiSelect(true);
		columnDispPref.setNullSelectionAllowed(false);
		columnDispPref.setImmediate(true);
		
		
		interactionTypes.setWidth("50%");
		interactionTypes.setRows(3);
		interactionTypes.setNullSelectionAllowed(false);
		interactionTypes.setMultiSelect(true);
		interactionTypes.setImmediate(true);
		interactionTypes.setLeftColumnCaption("Available Interaction Types");
		interactionTypes.setRightColumnCaption("Selected Interaction Types");
		interactionTypes.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					
		

				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});
		
		final Button submitButton 	= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
				
					new HierarchicalClusteringAnalysis(dataSet, params);
						
				} catch (Exception e) {	
					
					System.out.println(e);

				}		
			}
		});
		submitButton.addStyleName("wide default");

		addField("interactomeBox", interactomeBox);
		addField("interactionTypes", interactionTypes);
		addField("submitAnalysis", submitButton);
	}

}
