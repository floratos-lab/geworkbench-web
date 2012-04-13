package org.geworkbenchweb.analysis.CNKB.ui;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbenchweb.analysis.CNKB.CNKBInteractions;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;

/**
 * Parameter panel for CNKB
 * @author Nikhil
 * 
 */
public class UCNKBParamForm extends Form {

	private static final long serialVersionUID = -1221913812891134388L;
	
	private DSMicroarraySet dataSet;
	
	private int timeout = 3000;
	
	private static final String[] interactomes = new String[] { "BCi (66193 interactions)", "BIND (45454 interactions)",
    "Geneways (26931 interactions)", "HGi (672786 interactions)" };
	
	//private static final String[] interactions = new String[] { "Modular-TF", "Protein-DNA", "Protein-Protein" };
	
	public UCNKBParamForm(DSMicroarraySet maSet) {
		
		loadApplicationProperty();
		
		this.dataSet = maSet;
	
		final String[] params 	=	new String[3];
		
		ComboBox interactomeBox 		= 	new ComboBox();
		final Label interactomeDes		= 	new Label("Human B-Cell Interactome");
		
		interactomeDes.addStyleName("tiny color");
		interactomeDes.setImmediate(true);
		
		interactomeBox.setCaption("Interactome");
		for (int j = 0; j < interactomes.length; j++) {
            interactomeBox.addItem(interactomes[j]);
        }
		interactomeBox.select(interactomeBox.getItemIds().iterator().next());
		interactomeBox.setWidth("50%");
		interactomeBox.setImmediate(true);
		interactomeBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("BCi (66193 interactions)")) {
						//TODO
					}else if(valueChangeEvent.getProperty().getValue().toString() .equalsIgnoreCase("BIND (45454 interactions)")) {
						interactomeDes.setValue("Biomoecular Interaction Network Database");
					}else if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Geneways (26931 interactions)")) {
						interactomeDes.setValue("Mined from various literature sources");
					}else {
						interactomeDes.setValue("Integrated Version of the HGi-TCGA, HGi-Phillips and HGi-Sun interactome");
					}
					
					interactomeDes.requestRepaint();
				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});
		
		
		
		final Button submitButton 	= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
				
					new CNKBInteractions(dataSet, params);
						
				} catch (Exception e) {	
					
					System.out.println(e);

				}		
			}
		});
		submitButton.addStyleName("wide default");

		addField("interactomeBox", interactomeBox);
		getLayout().addComponent(interactomeDes);
		
		Label note = new Label("NOTE: Using all markers in dataset for now.");
		note.addStyleName("tiny color");
		note.setImmediate(true);
		getLayout().addComponent(note);
		
		addField("submitAnalysis", submitButton);
	}

	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		
		String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
		ResultSetlUtil.setUrl(interactionsServletUrl);
		ResultSetlUtil.setTimeout(timeout);
		
	}
	
}
