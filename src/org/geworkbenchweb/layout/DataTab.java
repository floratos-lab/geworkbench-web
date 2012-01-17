package org.geworkbenchweb.layout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.components.hierarchicalclustering.FastHierClustAnalysis;
import org.geworkbenchweb.GeworkbenchApplication;
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
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class DataTab extends VerticalLayout {

	private static final long serialVersionUID 		= 		-1888971408170241086L;
	User user 										= 		SessionHandler.get();
	
	private static DSMicroarraySet 	dataSet; 	
    private static HierCluster[] 	results;
    private static String			analysisType;
    private static String 			clustMetric;
    private static String 			clustMethod;
    private static String 			clustDim;
    
	public DataTab(DSMicroarraySet maSet, String action) {
		
		setSizeFull();
	
		dataSet  =   maSet;
		VerticalSplitPanel mainPanel		= 	new VerticalSplitPanel();
		HorizontalSplitPanel dataPanel 		= 	new HorizontalSplitPanel();
		final Panel paramPanel 				=	new Panel();
		Panel historyPanel					= 	new Panel();
		final Form paramForm				= 	new Form();
		ComboBox operationsBox				=   new ComboBox();
		final ComboBox analysisBox			= 	new ComboBox();
		final Panel paramDetails			= 	new Panel();
		
		paramForm.setImmediate(true);
		
		dataPanel.setImmediate(true);
		dataPanel.setSplitPosition(70);
		dataPanel.setStyleName("small previews");
		
		historyPanel.setCaption("DataSet History");
		historyPanel.setStyleName("bubble");
		historyPanel.setSizeFull();
		historyPanel.addComponent(new Label("Name of the DataSet : " + maSet.getLabel()));
		historyPanel.addComponent(new Label("Number of Markers : " + maSet.getMarkers().size()));
		historyPanel.addComponent(new Label("Number of Arrays : " + maSet.size()));
		historyPanel.addComponent(new Label("--------------------------------------------------"));
		historyPanel.addComponent(new Label("All the randon dataset history and what kind of analysis performed should be displayed here. " +
				"It should also accompany parameters used to perform the analysis."));
		
		paramPanel.setImmediate(true);
		paramPanel.setStyleName("bubble");
		paramPanel.setCaption("Parameter Panel");
		paramPanel.setSizeFull();		
		
		final Button submitAnalysis = new Button("Analyze", new Button.ClickListener() {

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
		analysisBox.setWidth("60%");
		analysisBox.setCaption("Select Analyis Type");
		analysisBox.addItem("Hierarchical Clustering");
		analysisBox.setInputPrompt("Choose Analysis from the list");
		analysisBox.addListener(new Property.ValueChangeListener() {
        	
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
        		
				try {	
					
					VerticalLayout dataLayout = new VerticalLayout();
					dataLayout.setSizeFull();
					dataLayout.removeAllComponents();
					analysisType = "Hierarchical Clustering";
					ComboBox clusterMethod 	= 	new ComboBox();
					ComboBox clusterDim 	= 	new ComboBox();
					ComboBox clusterMetric 	= 	new ComboBox();
					
					clusterMethod.setCaption("Clustering Method");
					clusterMethod.addItem("Single Linkage");
					clusterMethod.addItem("Average Linkage");
					clusterMethod.addItem("total linkage");
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
	        					
	                			clustDim = valueChangeEvent.getProperty().getValue().toString();
	                		
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
					
					paramDetails.setStyleName("bubble");
					paramDetails.removeAllComponents();

					Form dataForm = new Form();
					dataForm.setImmediate(true);
					
					dataForm.addField("cluster method", clusterMethod);
					dataForm.addField("cluster dimension", clusterDim);
					dataForm.addField("cluster metric", clusterMetric);
					dataForm.addField("submitAnalysis", submitAnalysis);
					
					dataLayout.addComponent(dataForm);
					dataLayout.setComponentAlignment(dataForm, Alignment.BOTTOM_CENTER);
					
					paramDetails.addComponent(dataLayout);
					paramPanel.addComponent(paramDetails);
					
        		}catch(NullPointerException e) {
        			
        			System.out.println("let us worry about this later");
        			
				}
        	}
        });
		
		operationsBox.setWidth("60%");
		operationsBox.setCaption("Select Data Operation");
		operationsBox.addItem("Analyze Data");
		operationsBox.addItem("Normalize Data");
		operationsBox.addItem("Filter Data");
		operationsBox.setInputPrompt("Choose Data Operation from the list");
		operationsBox.addListener(new Property.ValueChangeListener() {
        	
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
        		
				try {
					
        			paramForm.addField("analysis", analysisBox);
        			
        		
        		}catch(NullPointerException e) {
        			
        			System.out.println("let us worry about this later");
        			
				}
        	}
        });
		
		
		
		paramForm.addField("operation", operationsBox);
		
		
		if(action == null) {
			
			
			
		} else {
			operationsBox.select(1);
			operationsBox.setEnabled(false);
			paramForm.addField("analysis", analysisBox);
		}

		paramPanel.addComponent(paramForm);
		dataPanel.setFirstComponent(paramPanel);
		dataPanel.setSecondComponent(historyPanel);
		
		mainPanel.setSplitPosition(60);
		mainPanel.setStyleName("small previews");
		mainPanel.setFirstComponent(dataPanel);
		
		addComponent(mainPanel);
		
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
			try {
				
				clustMethod.isEmpty();
			
			} catch (Exception e) {
				
				clustMethod = "Single Linkage";
			
			}
			
			try {
				clustDim.isEmpty();
				
			} catch(Exception e) {
				
				clustDim = "Marker";
			
			}
			try {
				
				clustMetric.isEmpty();
				
			} catch (Exception e) {
				
				clustMetric = "Eucledian Distance";
			}
			
			FastHierClustAnalysis analysis 	= 	new FastHierClustAnalysis();
			results							=	analysis.analyze(dataSet, clustMethod, clustDim, clustMetric);
			
			if(results != null) {
			
				ResultSet resultSet = 	new ResultSet();
				java.util.Date date= new java.util.Date();
				resultSet.setName("HC - " + date);
				resultSet.setType(analysisType);
				analysisType = null;
				resultSet.setParent(dataSet.getDataSetName());
				resultSet.setOwner(user.getId());	
				resultSet.setData(convertToByte(results));
				FacadeFactory.getFacade().store(resultSet);	
				
			
			}
		}
	}
}


