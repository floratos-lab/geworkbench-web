package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * This class handles Hierarchical Clustering parameter form and submits analysis.
 * No computation should be there in this class.
 * @author Nikhil Reddy
 *
 */
public class HierarchicalClusteringUI extends VerticalLayout {
	
	private static final long serialVersionUID = 988711785863720384L;

	private String clustMethod = null;
	
	private String clustDim = null;
	
	private String clustMetric = null;
	
	private final Long dataSetId;
	
	private final DSMicroarraySet maSet;
	
	private ResultSet resultSet;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 
	
	public HierarchicalClusteringUI(Long dataId) {
		
		this.dataSetId = dataId;
		
		List<DataSet> data = DataSetOperations.getDataSet(dataSetId);
		maSet = (DSMicroarraySet) ObjectConversion.toObject(data.get(0).getData());
		
		setImmediate(true);
		setSpacing(true);
		
		ComboBox clusterMethod 	= 	new ComboBox();
		ComboBox clusterDim 	= 	new ComboBox();
		ComboBox clusterMetric 	= 	new ComboBox();

		clusterMethod.setCaption("Clustering Method");
		clusterMethod.addItem("Single Linkage");
		clusterMethod.addItem("Average Linkage");
		clusterMethod.addItem("Total linkage");
		clusterMethod.setNullSelectionAllowed(false);
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
		clusterDim.setNullSelectionAllowed(false);
		clusterDim.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {

					clustDim 	= 	valueChangeEvent.getProperty().getValue().toString();

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
		clusterMetric.setNullSelectionAllowed(false);
		clusterMetric.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {

					clustMetric 	= 	valueChangeEvent.getProperty().getValue().toString();
		
				}catch(NullPointerException e) {

					System.out.println("let us worry about this later");

				}
			}
		});
		
		final Button submitButton 	= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					
					resultSet = 	new ResultSet();
					java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
					resultSet.setDateField(date);
					String dataSetName = "Hierarchical Clustering - Pending" ;
					resultSet.setName(dataSetName);
					resultSet.setType("HierarchicalClusteringResults");
					resultSet.setParent(dataSetId);
					resultSet.setOwner(SessionHandler.get().getId());	
					FacadeFactory.getFacade().store(resultSet);	

					params.put(HierarchicalClusteringParams.CLUSTER_METHOD, parseMethod(clustMethod));
					params.put(HierarchicalClusteringParams.CLUSTER_METRIC, parseDistanceMetric(clustMetric));
					params.put(HierarchicalClusteringParams.CLUSTER_DIMENSION, parseDimension(clustDim));
					
					NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
					GeworkbenchRoot.getBlackboard().fire(resultEvent);

					AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(maSet, resultSet, params);
					GeworkbenchRoot.getBlackboard().fire(analysisEvent);	
					
				} catch (Exception e) {	
					
					System.out.println(e);

				}		
			}
		});

		addComponent(clusterMethod);
		addComponent(clusterDim);
		addComponent(clusterMetric);
		addComponent(submitButton);
		
	}
	
	
		private static int parseDistanceMetric(String d) {
			
			if(d == null) {
				return 0;
			}
			if(d.equals("Eucledian Distance")) {
				return 0;
			} else if(d.equals("Pearson's Correlation")) {
				return 1;
			} else if(d.equals("Spearman's Rank Correlation")) {
				return 2;
			} else {
				return 0;
			}
		}
		private static int parseMethod(String method) {
			if(method == null) {
				return 0;
			}
			if(method.equals("Single Linkage")) {
				return 0;
			} else if(method.equals("Average Linkage")) {
				return 1;
			} else if(method.equals("Total linkage")) {
				return 2;
			} else {
				return 0;
			}
		}
		private static int parseDimension(String dim) {
			if(dim == null) {
				return 0;
			}
			if(dim.equals("Marker")) {
				return 0;
			} else if(dim.equals("Microarray")) {
				return 1;
			} else if(dim.equals("Both")) {
				return 2;
			} else {
				return 0;
			}
		}
}



