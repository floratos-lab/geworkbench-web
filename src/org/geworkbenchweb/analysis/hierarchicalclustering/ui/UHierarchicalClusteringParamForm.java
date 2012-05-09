package org.geworkbenchweb.analysis.hierarchicalclustering.ui;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.analysis.hierarchicalclustering.HierarchicalClusteringAnalysis;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Button.ClickEvent;

/**
 * 
 * This class handles Hierarchical Clustering parameter form and submits analysis.
 * No computation should be there in this class.
 * @author Nikhil Reddy
 *
 */

public class UHierarchicalClusteringParamForm extends Form {
	
	private static final long serialVersionUID = 988711785863720384L;

	private String clustMethod;
	
	private String clustDim;
	
	private String clustMetric;
	
	private DSMicroarraySet dataSet;
	
	public UHierarchicalClusteringParamForm(DSMicroarraySet maSet) {
		
		this.dataSet = maSet;
		setImmediate(true);
		
		final String[] params = new String[3];
		
		ComboBox clusterMethod 	= 	new ComboBox();
		ComboBox clusterDim 	= 	new ComboBox();
		ComboBox clusterMetric 	= 	new ComboBox();

		clusterMethod.setCaption("Clustering Method");
		clusterMethod.addItem("Single Linkage");
		clusterMethod.addItem("Average Linkage");
		clusterMethod.addItem("Total linkage");
		clusterMethod.select(clusterMethod.getItemIds().iterator().next());
		clusterMethod.setWidth("50%");
		clusterMethod.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					
					clustMethod = valueChangeEvent.getProperty().getValue().toString();
					params[0] = clustMethod;

				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});

		clusterDim.setCaption("Clustering Dimension");
		clusterDim.setInputPrompt("Please select Clustering Dimension");
		clusterDim.addItem("Marker");
		clusterDim.addItem("Microarray");
		clusterDim.addItem("Both");
		clusterDim.select(clusterDim.getItemIds().iterator().next());
		clusterDim.setWidth("50%");
		clusterDim.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {

					clustDim 	= 	valueChangeEvent.getProperty().getValue().toString();
					params[1] 	=	clustDim;

				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});

		clusterMetric.setCaption("Clustering Metric");
		clusterMetric.setInputPrompt("Please select Clustering Metric");
		clusterMetric.addItem("Eucledian Distance");
		clusterMetric.addItem("Pearson's Correlation");
		clusterMetric.addItem("Spearman's Rank Correlation");
		clusterMetric.select(clusterMetric.getItemIds().iterator().next());
		clusterMetric.setWidth("50%");
		clusterMetric.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {

					clustMetric 	= 	valueChangeEvent.getProperty().getValue().toString();
					params[2]		=	clustMetric;	

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

		addField("clusterMethod", clusterMethod);
		addField("clusterMethod", clusterDim);
		addField("clusterMethod", clusterMetric);
		addField("submitAnalysis", submitButton);
		
	}
}



