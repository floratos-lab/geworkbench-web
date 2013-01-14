package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Select;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * This class handles Hierarchical Clustering parameter form and submits analysis.
 * No computation should be there in this class.
 * @author Nikhil Reddy
 *
 */
public class HierarchicalClusteringUI extends VerticalLayout implements AnalysisUI {
	
	private static final long serialVersionUID = 988711785863720384L;

	private String clustMethod = "Single Linkage";
	
	private String clustDim = "Marker";
	
	private String clustMetric = "Euclidean Distance";
	
	private Long dataSetId;
	
	private ResultSet resultSet;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 
	
	public HierarchicalClusteringUI(Long dataId) {
		
		this.dataSetId = dataId;
		
		setImmediate(true);
		setSpacing(true);
		
		ComboBox clusterMethod 	= 	new ComboBox();
		ComboBox clusterDim 	= 	new ComboBox();
		ComboBox clusterMetric 	= 	new ComboBox();
		
		markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(10);
		markerSetSelect.setImmediate(true);
		markerSetSelect.addItem("");
		markerSetSelect.setItemCaption("", "All markers");
		

		arraySetSelect = new ListSelect("Select array sets:");
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(10);
		arraySetSelect.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);
		arraySetSelect.setImmediate(true);
		arraySetSelect.addItem("");
		arraySetSelect.setItemCaption("", "All microarrays");

		final GridLayout gridLayout1 = new GridLayout(2, 2);
		gridLayout1.setSpacing(true);
		gridLayout1.setImmediate(true);
		gridLayout1.addComponent(markerSetSelect, 0, 0);
		gridLayout1.addComponent(arraySetSelect, 1, 0);

		addComponent(gridLayout1);
		//... end of code copied from ANOVA component

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
		clusterMetric.addItem("Euclidean Distance");
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

					params.put(HierarchicalClusteringParams.MARKER_SET, getMarkerSet());
					params.put(HierarchicalClusteringParams.MICROARRAY_SET, getMicroarraySet());
					params.put(HierarchicalClusteringParams.CLUSTER_METHOD, parseMethod(clustMethod));
					params.put(HierarchicalClusteringParams.CLUSTER_METRIC, parseDistanceMetric(clustMetric));
					params.put(HierarchicalClusteringParams.CLUSTER_DIMENSION, parseDimension(clustDim));
					
					final DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(UserDirUtils.getDataSet(dataSetId));
					generateHistoryString(maSet);
					
					NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
					GeworkbenchRoot.getBlackboard().fire(resultEvent);

					AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(maSet, resultSet, params);
					GeworkbenchRoot.getBlackboard().fire(analysisEvent);	
					
				} catch (Exception e) {	
					e.printStackTrace();
				}		
			}
		});

		addComponent(clusterMethod);
		addComponent(clusterDim);
		addComponent(clusterMetric);
		addComponent(submitButton);
		
	}
	
	private ListSelect markerSetSelect;
	private ListSelect arraySetSelect;
	
	// I copied the mechanism from ANOVA component. not sure if this is the best way
	private Serializable getMicroarraySet() {
		String selectStr = arraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			return selectStr.substring(1, selectStr.length()-1).split(",");			 
		} else {
			return null;
		}
	}

	// I copied the mechanism from ANOVA component. not sure if this is the best way
	private Serializable getMarkerSet() {
		String selectStr = markerSetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			return selectStr.substring(1, selectStr.length()-1).split(",");			 
		} else {
			return null;
		}
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
	
	private void generateHistoryString(DSMicroarraySet maSet) {
		StringBuilder mark = new StringBuilder();
		
		mark.append("Hierarchical Clustering Parameters : \n");
		mark.append("Clustering Method - " + clustMethod + "\n");
		mark.append("Clustering Dimension - " + clustDim + "\n");
		mark.append("Clustering Metric - " + clustMetric + "\n");
		
		mark.append("Markers used - \n" );
		for(int i=0; i<maSet.getMarkers().size(); i++) {
			mark.append( "\t" + maSet.getMarkers().get(i).getLabel() + "\n");
		}
		
		mark.append("Phenotypes used - \n" );
		for(int i=0; i<maSet.size(); i++) {
			mark.append( "\t" + maSet.get(i).getLabel() + "\n");
		}
		
		DataHistory his = new DataHistory();
		his.setParent(resultSet.getId());
		his.setData(ObjectConversion.convertToByte(mark.toString()));
		FacadeFactory.getFacade().store(his);
	}

	@Override
	public void setDataSetId(Long dataId) {
		this.dataSetId = dataId;
		
		List<?> subMarkerSets = SubSetOperations.getMarkerSets(dataSetId);
		List<?> subArraySets = SubSetOperations.getArraySets(dataSetId);
		
		markerSetSelect.removeAllItems();
		markerSetSelect.addItem("");
		markerSetSelect.setItemCaption("", "All markers");

		arraySetSelect.removeAllItems();
		arraySetSelect.addItem("");
		arraySetSelect.setItemCaption("", "All microarrays");
		
		if (subMarkerSets != null)
			for (int m = 0; m < (subMarkerSets).size(); m++) {

				markerSetSelect.addItem(((SubSet) subMarkerSets.get(m)).getId());
				markerSetSelect.setItemCaption(((SubSet) subMarkerSets.get(m)).getId(), ((SubSet) subMarkerSets.get(m)).getName());

			}

		if (subArraySets != null)
			for (int m = 0; m < (subArraySets).size(); m++) {

				arraySetSelect.addItem(((SubSet) subArraySets.get(m)).getId().longValue());
				arraySetSelect.setItemCaption(((SubSet) subArraySets.get(m)).getId(), ((SubSet) subArraySets.get(m)).getName());
				
			}

	}
}



