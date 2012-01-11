package org.geworkbenchweb.layout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchApplication;
import org.geworkbenchweb.analysis.HierClusterTestResult;
import org.geworkbenchweb.analysis.HierarchicalClustering;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

public class DataTab extends VerticalLayout {

	private static final long serialVersionUID = -1888971408170241086L;
    private static DSMicroarraySet dataSet; 	
    private static HierClusterTestResult results;
    private static String analysisType;
    User user 		= 	SessionHandler.get();
	public DataTab(DSMicroarraySet maSet) {
		
		setSizeFull();
		
		dataSet  =   maSet;
		
		final Form operationsForm 		= 	new Form();
		final String[] operations 		= 	new String[] { "Analyze Data", "Normalize Data"};
		final Panel formPanel 			= 	new Panel();
		final ComboBox typeCombo 		= 	new ComboBox("Type");
        ComboBox operationCombo 		= 	new ComboBox();
        
		
		formPanel.setStyleName("bubble");
		formPanel.setCaption("Data Operations Panel");
		formPanel.setWidth("50%");
		
		operationCombo.setRequired(true);
		operationCombo.setImmediate(true);
		operationCombo.setInputPrompt("Please select Data Operation");
        operationCombo.addStyleName("select-button");
        operationCombo.setCaption("Select Operation");
        
        for (int i = 0; i < operations.length; i++) {
            operationCombo.addItem(operations[i]);
        }
        operationCombo.setWidth("70%");
        operationsForm.addField("operation", operationCombo);
        
        typeCombo.addStyleName("select-button");
        typeCombo.setEnabled(false);
        
        Button submitButton = new Button("Next", new Button.ClickListener() {
          
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
                
				try {
					if (analysisType == "Hierarchical Clustering" ) {
						
						analysisType = null;
						removeAllComponents();
						addComponent(formPanel);
						setComponentAlignment(formPanel, Alignment.TOP_LEFT);

						Panel parameterPanel 	= 	new Panel();
						final Form paramForm 	= 	new Form();
						ComboBox clusterMethod 	= 	new ComboBox();
						ComboBox clusterDim 	= 	new ComboBox();
						ComboBox clusterMetric  = 	new ComboBox();

						parameterPanel.setStyleName("bubble");
						parameterPanel.setCaption("Heirarchical Clustering Parameter Panel");
						parameterPanel.setWidth("50%");

						clusterMethod.addStyleName("select-button");
						clusterMethod.setCaption("Clustering Method");
						clusterMethod.setInputPrompt("Please select Clustering Method");
						clusterMethod.setRequired(true);
						clusterMethod.addItem("Single Linkage");
						clusterMethod.addItem("Average Linkage");
						clusterMethod.addItem("total linkage");
						clusterMethod.setWidth("70%");
						paramForm.addField("clusterMethod", clusterMethod);

						clusterDim.addStyleName("select-button");
						clusterDim.setCaption("Clustering Dimension");
						clusterDim.setInputPrompt("Please select Clustering Dimension");
						clusterDim.setRequired(true);
						clusterDim.addItem("Marker");
						clusterDim.addItem("Microarray");
						clusterDim.addItem("Both");
						clusterDim.setWidth("70%");
						paramForm.addField("clusterDim", clusterDim);

						clusterMetric.addStyleName("select-button");
						clusterMetric.setCaption("Clustering Metric");
						clusterMetric.setRequired(true);
						clusterMetric.setInputPrompt("Please select Clustering Metric");
						clusterMetric.addItem("Eucledian Distance");
						clusterMetric.addItem("Pearson's Correlation");
						clusterMetric.addItem("Spearman's Rank Correlation");
						clusterMetric.setWidth("70%");
						paramForm.addField("clusterMetric", clusterMetric);

						Button submitAnalysis = new Button("Submit", new Button.ClickListener() {

							private static final long serialVersionUID = 1L;

							public void buttonClick(ClickEvent event) {
								try {

									final Refresher refresher = new Refresher();
									refresher.addListener(new AnalysisListener());
									getApplication().getMainWindow().addComponent(refresher);

									new AnalysisProcess().start();

								} catch (Exception e) {	

								}		
							}
						});

						paramForm.addField("submitAnalysis", submitAnalysis);
						parameterPanel.addComponent(paramForm);
						addComponent(parameterPanel);
						setComponentAlignment(parameterPanel, Alignment.TOP_LEFT);
					} else {
						
						removeAllComponents();
						addComponent(formPanel);
						getApplication().getMainWindow().showNotification("Selected Operation is not Implemented",  
								Notification.TYPE_ERROR_MESSAGE );
						
					}

				} catch (Exception e) {

				}
			}
            
        });
        
        
        
        operationCombo.addListener(new Property.ValueChangeListener() {
        	
        	private static final long serialVersionUID = 1L;
			
        	public void valueChange(Property.ValueChangeEvent valueChangeEvent) { 	
                
        		String selectedOperation = valueChangeEvent.getProperty().getValue().toString();
        		
        		typeCombo.removeAllItems();
        		typeCombo.setEnabled(true);
        		typeCombo.setRequired(true);
       
        		if (selectedOperation.equals("Analyze Data")) {
                    
        			typeCombo.setCaption("Analysis Type");
        			typeCombo.setInputPrompt("Select Analysis Type");
                    typeCombo.addItem("Hierarchical Clustering");
                
        		} else if (selectedOperation.equals("Normalize Data")) {
                	
        			typeCombo.setCaption("Normalization Type");
        			typeCombo.setInputPrompt("Select Normalization Type");
                	typeCombo.addItem("Housekeeping Gene Normalizer");
                	typeCombo.addItem("Log2 Normalizer");
                	typeCombo.addItem("Mean-Variance Normalizer");
                
        		}
                
        		typeCombo.addListener(new Property.ValueChangeListener() {
                	
        			private static final long serialVersionUID = 1L;

        			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                		
        				try {
        					
                			analysisType = valueChangeEvent.getProperty().getValue().toString();
                		
                		}catch(NullPointerException e) {
                			
                			System.out.println("let us worry about this later");
                			
        				}
                	}
                });
        		
        		typeCombo.requestRepaint();
            
        	}
        });
        
        
        
        typeCombo.setWidth("70%");
        operationsForm.addField("type", typeCombo);
        operationsForm.addField("submitButton", submitButton);
        
        typeCombo.addListener(new Property.ValueChangeListener() {
        	
        	private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) { 	
            
			}
        });
        
        formPanel.addComponent(operationsForm);
		addComponent(formPanel);
		setComponentAlignment(formPanel, Alignment.TOP_LEFT);
	
	}
	
	public class AnalysisListener implements RefreshListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void refresh(Refresher source) {
			
			if (results != null) {
		        
		        source.setEnabled(false);
		        GeworkbenchApplication app = new GeworkbenchApplication();
		        app.initView(getApplication().getMainWindow());
			    
            }
			
		}
		
	}
	
	private byte[] convertToByte(Object object) {

		byte[] byteData = null;
		ByteArrayOutputStream bos 	= 	new ByteArrayOutputStream();

		try {

			ObjectOutputStream oos 	= 	new ObjectOutputStream(bos); 

			oos.writeObject(object);
			oos.flush(); 
			oos.close(); 
			bos.close();
			byteData 				= 	bos.toByteArray();

		} catch (IOException ex) {

			System.out.println("Exception with in convertToByte");

		}

		return byteData;

	}
	
	
	public class AnalysisProcess extends Thread {
		@Override
		public void run() {
			
			HierarchicalClustering hS 		 = 	new HierarchicalClustering();
			results 						 = 	hS.doHierClusterAnalysis(dataSet);
		
			if(results != null) {
				
				
				ResultSet resultSet = 	new ResultSet();
				java.util.Date date= new java.util.Date();
				resultSet.setName("HC - " + date);
				resultSet.setType(analysisType);
				resultSet.setParent(dataSet.getDataSetName());
				resultSet.setOwner(user.getId());	
				resultSet.setData(convertToByte(results));
				FacadeFactory.getFacade().store(resultSet);	
			
			}
		}
	}
}


