package org.geworkbenchweb.layout;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbenchweb.analysis.HierClusterTestResult;
import org.geworkbenchweb.analysis.HierarchicalClustering;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class DataTab extends VerticalLayout {

	private static final long serialVersionUID = -1888971408170241086L;

	public DataTab(DSMicroarraySet maSet) {
		
		setSizeFull();
		
		final DSMicroarraySet dataSet 	= 	maSet;
		final Form operationsForm 		= 	new Form();
		final String[] operations 		= 	new String[] { "Analyze Data", "Normalize Data", "Filter Data"};
		final Panel formPanel 			= 	new Panel();
		final ComboBox typeCombo 		= 	new ComboBox("Type :");
        ComboBox operationCombo 		= 	new ComboBox();
		
		formPanel.setStyleName("bubble");
		formPanel.setCaption("Data Operations Panel");
		formPanel.setWidth("50%");
		
        operationCombo.addStyleName("select-button");
        operationCombo.setCaption("Select Operation : ");
        for (int i = 0; i < operations.length; i++) {
            operationCombo.addItem(operations[i]);
        }
        operationCombo.setWidth("250px");
        operationsForm.addField("operation", operationCombo);
        
        typeCombo.addStyleName("select-button");
        
        Button submitButton = new Button("Next", new Button.ClickListener() {
          
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
                
				try {
                	
					removeAllComponents();
                	addComponent(formPanel);
                	setComponentAlignment(formPanel, Alignment.TOP_CENTER);
                	
                	Panel parameterPanel 	= 	new Panel();
                	final Form paramForm 	= 	new Form();
                	ComboBox clusterMethod 	= 	new ComboBox();
                	ComboBox clusterDim 	= 	new ComboBox();
                	ComboBox clusterMetric  = 	new ComboBox();
                	
                	parameterPanel.setStyleName("bubble");
                	parameterPanel.setCaption("Heirarchical Clustering Parameter Panel");
                	parameterPanel.setWidth("50%");
                	
                	clusterMethod.addStyleName("select-button");
                	clusterMethod.setCaption("Clustering Method : ");
                    clusterMethod.addItem("Single Linkage");
                    clusterMethod.addItem("Average Linkage");
                    clusterMethod.addItem("total linkage");
                    clusterMethod.setWidth("250px");
                	paramForm.addField("clusterMethod", clusterMethod);
                	
                	clusterDim.addStyleName("select-button");
                	clusterDim.setCaption("Clustering Dimension : ");
                	clusterDim.addItem("Marker");
                	clusterDim.addItem("Microarray");
                	clusterDim.addItem("Both");
                	clusterDim.setWidth("250px");
                	paramForm.addField("clusterDim", clusterDim);
                	    	
                	clusterMetric.addStyleName("select-button");
                	clusterMetric.setCaption("Clustering Metric : ");
                	clusterMetric.addItem("Eucledian Distance");
                	clusterMetric.addItem("Pearson's Correlation");
                	clusterMetric.addItem("Spearman's Rank Correlation");
                	clusterMetric.setWidth("250px");
                	paramForm.addField("clusterMetric", clusterMetric);
                	
                	Button submitAnalysis = new Button("Submit", new Button.ClickListener() {
                        
            			private static final long serialVersionUID = 1L;

            			public void buttonClick(ClickEvent event) {
                            try {
                            	
                            	HierarchicalClustering hS 		= 	new HierarchicalClustering();
                            	HierClusterTestResult results 	= 	hS.doHierClusterAnalysis((DSMicroarray) dataSet);
                            	
                            	if(results.equals(null)) {
                            		
                            		System.out.println("Analysis Failed !!");
                            	
                            	} else {
                            		
                            		System.out.println("Clustering is done");
                            		System.out.println("Root Node : " + results.getRoot());
                            	
                            	}
                            	
                            } catch (Exception e) {	
                            	
                            }		
            			}
            		});
                	
                	paramForm.addField("submitAnalysis", submitAnalysis);
                	parameterPanel.addComponent(paramForm);
                	addComponent(parameterPanel);
                	setComponentAlignment(parameterPanel, Alignment.TOP_CENTER);
                	
                } catch (Exception e) {
                    
                }
            }
        });
        
        operationCombo.addListener(new Property.ValueChangeListener() {
        	
        	private static final long serialVersionUID = 1L;
			
        	public void valueChange(Property.ValueChangeEvent valueChangeEvent) { 	
                
        		String selectedOperation = valueChangeEvent.getProperty().getValue().toString();
                
        		if (selectedOperation.equals("Analyze Data")) {
                    
        			typeCombo.setCaption("Analysis Type:");
                    typeCombo.removeAllItems();
                    typeCombo.addItem("ARACne");
                    typeCombo.addItem("Hierarchical Clustering");
                    typeCombo.addItem("T-test Analysis");
                
        		} else if (selectedOperation.equals("Normalize Data")) {
                	
        			typeCombo.setCaption("Normalization Type:");
                	typeCombo.removeAllItems();
                	typeCombo.addItem("Housekeeping Gene Normalizer");
                	typeCombo.addItem("Log2 Normalizer");
                	typeCombo.addItem("Mean-Variance Normalizer");
                
        		}
                
        		typeCombo.requestRepaint();
            
        	}
        });
        
        typeCombo.setWidth("250px");
        operationsForm.addField("type", typeCombo);
        operationsForm.addField("submitButton", submitButton);
        
        typeCombo.addListener(new Property.ValueChangeListener() {
        	
        	private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) { 	
            
			}
        });
        
        formPanel.addComponent(operationsForm);
		addComponent(formPanel);
		setComponentAlignment(formPanel, Alignment.TOP_CENTER);
	
	}
}
