package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.AnalysisSubmission;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.DataHistory;
import org.geworkbenchweb.pojos.HierarchicalClusteringResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.MarkerArraySelector;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * UI for Hierarchical Clustering analysis.
 *
 */
public class HierarchicalClusteringUI extends VerticalLayout implements AnalysisUI {

	private static final long serialVersionUID = 988711785863720384L;

	private static Log log = LogFactory.getLog(HierarchicalClusteringUI.class);

	private String clustMethod = "Single Linkage";

	private String clustDim = "Marker";

	private String clustMetric = "Euclidean Distance";

	private MarkerArraySelector markerArraySelector;

	private Long dataSetId;
	private Long userId;

	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();

	public HierarchicalClusteringUI(Long dataId) {

		this.dataSetId = dataId;
		User user = SessionHandler.get();
		if (user != null)
			userId = user.getId();

		setImmediate(true);
		setMargin(true);
		setSpacing(true);

		markerArraySelector = new MarkerArraySelector(dataSetId, userId, "HierarchicalClusteringUI");
		addComponent(markerArraySelector);

		ComboBox clusterMethod = new ComboBox();
		ComboBox clusterDim = new ComboBox();
		ComboBox clusterMetric = new ComboBox();

		clusterMethod.setCaption("Clustering Method");
		clusterMethod.addItem("Single Linkage");
		clusterMethod.addItem("Average Linkage");
		clusterMethod.addItem("Total Linkage");
		clusterMethod.setNullSelectionAllowed(false);
		clusterMethod.select(clusterMethod.getItemIds().iterator().next());
		clusterMethod.setWidth("50%");
		clusterMethod.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				try {
					clustMethod = valueChangeEvent.getProperty().getValue().toString();
				} catch (NullPointerException e) {
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
		clusterDim.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					clustDim = valueChangeEvent.getProperty().getValue().toString();
				} catch (NullPointerException e) {
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
		clusterMetric.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					clustMetric = valueChangeEvent.getProperty().getValue().toString();
				} catch (NullPointerException e) {
					System.out.println("let us worry about this later");
				}
			}
		});

		final Button submitButton = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {

					ResultSet resultSet = new ResultSet();
					java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
					resultSet.setTimestamp(timestamp);
					String dataSetName = "Hierarchical Clustering - Pending";
					resultSet.setName(dataSetName);
					resultSet.setType(getResultType().getName());
					resultSet.setParent(dataSetId);
					resultSet.setOwner(SessionHandler.get().getId());
					FacadeFactory.getFacade().store(resultSet);

					params.put(HierarchicalClusteringParams.MARKER_SET, markerArraySelector.getSelectedMarkerSet());
					params.put(HierarchicalClusteringParams.MICROARRAY_SET, markerArraySelector.getSelectedArraySet());
					params.put(HierarchicalClusteringParams.CLUSTER_METHOD, parseMethod(clustMethod));
					params.put(HierarchicalClusteringParams.CLUSTER_METRIC, parseDistanceMetric(clustMetric));
					params.put(HierarchicalClusteringParams.CLUSTER_DIMENSION, parseDimension(clustDim));

					generateHistoryString(resultSet.getId());

					GeworkbenchRoot app = (GeworkbenchRoot) UI.getCurrent();
					app.addNode(resultSet);

					Component content = UI.getCurrent().getContent();
					if (content instanceof UMainLayout) {
						new AnalysisSubmission((UMainLayout)content).submit(params, HierarchicalClusteringUI.this, resultSet);
					} else {
						log.error("THIS SHOULD NEVER HAPPEN.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		addComponent(clusterMethod);
		addComponent(clusterDim);
		addComponent(clusterMetric);
		addComponent(submitButton);

		setDataSetId(dataId);
	}

	private static int parseDistanceMetric(String d) {

		if (d == null) {
			return 0;
		}
		if (d.equals("Euclidean Distance")) {
			return 0;
		} else if (d.equals("Pearson's Correlation")) {
			return 1;
		} else if (d.equals("Spearman's Rank Correlation")) {
			return 2;
		} else {
			return 0;
		}
	}

	private static int parseMethod(String method) {
		if (method == null) {
			return 0;
		}
		if (method.equals("Single Linkage")) {
			return 0;
		} else if (method.equals("Average Linkage")) {
			return 1;
		} else if (method.equals("Total Linkage")) {
			return 2;
		} else {
			return 0;
		}
	}

	private static int parseDimension(String dim) {
		if (dim == null) {
			return 0;
		}
		if (dim.equals("Marker")) {
			return 0;
		} else if (dim.equals("Microarray")) {
			return 1;
		} else if (dim.equals("Both")) {
			return 2;
		} else {
			return 0;
		}
	}

	private void generateHistoryString(Long resultSetId) {
		StringBuilder builder = new StringBuilder();

		builder.append("Hierarchical Clustering Parameters : \n");
		builder.append("Clustering Method - " + clustMethod + "\n");
		builder.append("Clustering Dimension - " + clustDim + "\n");
		builder.append("Clustering Metric - " + clustMetric + "\n");

		builder.append(markerArraySelector.generateHistoryString());

		DataHistory his = new DataHistory();
		his.setParent(resultSetId);
		his.setData(builder.toString());
		FacadeFactory.getFacade().store(his);
	}

	@Override
	public void setDataSetId(Long dataSetId) {
		User user = SessionHandler.get();
		if (user != null) {
			userId = user.getId();
		}

		this.dataSetId = dataSetId;
		markerArraySelector.setData(dataSetId, userId);
	}

	@Override
	public Class<?> getResultType() {
		return HierarchicalClusteringResult.class;
	}

	@Override
	public String execute(Long resultId,
			HashMap<Serializable, Serializable> parameters, Long userId)
			throws Exception {
		HierarchicalClusteringComputation analysis = new HierarchicalClusteringComputation(
				dataSetId, params, userId);
		HierarchicalClusteringResult result = analysis.execute();
		FacadeFactory.getFacade().store(result);

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
		resultSet.setDataId(result.getId());
		FacadeFactory.getFacade().store(resultSet);

		return "Hierarchical Clustering Result";
	}

}
