package org.geworkbenchweb.analysis.hierarchicalclustering;

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

public class HierarchicalClusteringParamForm extends Form {
	
	private static final long serialVersionUID = 988711785863720384L;
	
	private String clustMethod;
	private String clustDimension;
	private String clustMetric;
	
	public HierarchicalClusteringParamForm() {
		
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

					clustDimension = valueChangeEvent.getProperty().getValue().toString();

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

					clustMetric = valueChangeEvent.getProperty().getValue().toString();

				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});
		
		final Button submitAnalysis = new Button("Analyze", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {


				} catch (Exception e) {	

				}		
			}
		});
		addField("clusterMethod", clusterMethod);
		addField("clusterMethod", clusterDim);
		addField("clusterMethod", clusterMetric);
		addField("submit", submitAnalysis);
	}
}
