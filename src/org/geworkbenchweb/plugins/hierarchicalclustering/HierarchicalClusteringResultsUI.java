package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.util.Range;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.plugins.hierarchicalclustering.SubsetCommand.SetType;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Dendrogram;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

/* This is started from the class with the same name under the .results. package. */
public class HierarchicalClusteringResultsUI extends VerticalSplitPanel implements Visualizer {


	private static final long serialVersionUID = 8018658107854483097L;
	
	final private Long datasetId;

	@SuppressWarnings({ "unchecked" })
	public HierarchicalClusteringResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLocked(true);
		setSplitPosition(25, com.vaadin.terminal.Sizeable.UNITS_PIXELS); //((float) 2.3);

		MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");

		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		}
		if(! (object instanceof CSHierClusterDataSet)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			setFirstComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			return;
		}
		// TODO the above cases could happen for either corrupted/missing file or pending node. we need to differentiate and update (remove cache) in the second case

		CSHierClusterDataSet dataSet 	= 	(CSHierClusterDataSet) object;

		HierCluster markerCluster 		= 	dataSet.getCluster(0);
		HierCluster arrayCluster 		= 	dataSet.getCluster(1);

		reorderedMarker = new ArrayList<DSGeneMarker>();
		StringBuffer markerString 	= 	new StringBuffer();
		if(markerCluster != null) {
			convertToString(markerString, markerCluster, true);
		}
		String markerClusterString = markerString.toString();

		reorderedMicroarray = new ArrayList<DSMicroarray>();
		StringBuffer arrayString 	= 	new StringBuffer();
		if(arrayCluster != null) {
			convertToString(arrayString, arrayCluster, false);
		}
		String arrayClusterString = arrayString.toString();

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) dataSet.getDataSetView();
		int geneNo = microarraySet.markers().size();
		int chipNo = microarraySet.items().size();

		String[] markerNames = new String[geneNo];
		String[] arrayNames = new String[chipNo];
		int[] colors = new int[chipNo*geneNo]; /* range [-255, 255] */
		int k = 0;

		updateRange(microarraySet);
		
		if(reorderedMarker.size()==0) {
			reorderedMarker = microarraySet.markers();
		}
		if(reorderedMicroarray.size()==0) {
			reorderedMicroarray = microarraySet.items();
		}
		
		int j = 0;
		for (DSMicroarray a : reorderedMicroarray) {
			arrayNames[j++] = a.getLabel();
		}
		int i = 0;
		for (DSGeneMarker marker : reorderedMarker) {

			markerNames[i++] = marker.getLabel();
			for (DSMicroarray a : reorderedMicroarray) {
				double value = a.getMarkerValue(marker)
						.getValue();
				colors[k++] = getMarkerValueColor(value, marker, 1.0f);
			}
		}
		reorderedMarker = null;
		reorderedMicroarray = null;
		
		final Dendrogram dendrogram = new Dendrogram(chipNo, geneNo, arrayClusterString, markerClusterString,
				arrayNames, markerNames, colors);
		dendrogram.setSizeUndefined();

		dendrogram.setImmediate(true);
		//dendrogram.setSizeFull(); // FIXME why not

		toolBar.addItem("", new ThemeResource("../custom/icons/Zoom-In-icon.png"), 
				new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				dendrogram.zoomIn();
			}
		});
		toolBar.addItem("", new ThemeResource("../custom/icons/Zoom-Out-icon.png"),
				new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				dendrogram.zoomOut();
			}
		});

		MenuBar.MenuItem resetI		=	toolBar.addItem("Reset", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				dendrogram.reset();
			}
		});
		
		ResultSet data 			= 	FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long parentId = data.getParent();
		MenuBar.MenuItem saveM		=	toolBar.addItem("Save Markers", 
				new SubsetCommand("Add Markers to Set", this, SetType.MARKER, parentId, dendrogram));
		
		MenuBar.MenuItem saveP 		=	toolBar.addItem("Save Phenotypes",
				new SubsetCommand("Add Phenotypes to Set", this, SetType.MICROARRAY, parentId, dendrogram));
		
		MenuBar.MenuItem export 	= 	toolBar.addItem("Export Image", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				dendrogram.exportImage();
			}
		});

		resetI.setStyleName("plugin");
		saveM.setStyleName("plugin");
		saveP.setStyleName("plugin");
		export.setStyleName("plugin");

		setFirstComponent(toolBar);

		setSecondComponent(dendrogram);
	}

	private transient List<DSGeneMarker> reorderedMarker;
	private transient List<DSMicroarray> reorderedMicroarray;
	
	/**
	 * 
	 * Recursively convert Cluster to string.
	 * result stored in a StringBuffer buffer, so buffer should be set to empty before starting from the root
	 * @param hierCluster
	 * @return
	 */
	private void convertToString(final StringBuffer buffer, Cluster hierCluster, boolean isMarker) {

		buffer.append("(");

		if (!hierCluster.isLeaf()) {
			Cluster[] child = hierCluster.getChildrenNodes();
			convertToString(buffer, child[0], isMarker);
			convertToString(buffer, child[1], isMarker);
		} else if(isMarker) {
			MarkerHierCluster markerCluster = (MarkerHierCluster)hierCluster;
			reorderedMarker.add(markerCluster.getMarkerInfo());
		} else { // if is microarray
			MicroarrayHierCluster microarrayCluster = (MicroarrayHierCluster)hierCluster;
			reorderedMicroarray.add(microarrayCluster.getMicroarray());
		}
		buffer.append(")");
	}

	/* adapted from geWorkbench desktop version */
	/* this is necessary to handle the mutability of value range of DSGeneMarker. very dangerous and confusing. */
	private void updateRange(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		DSMicroarraySet microarraySet = view.getMicroarraySet();
		for (DSGeneMarker marker : microarraySet.getMarkers()) {
			((DSRangeMarker) marker).reset(marker.getSerial());
		}
		if (view.items().size() == 1) {
			DSMicroarray ma = view.items().get(0);
			Range range = new org.geworkbench.bison.util.Range();
			for (DSGeneMarker marker : microarraySet.getMarkers()) {
				DSMutableMarkerValue mValue = (DSMutableMarkerValue) ma
						.getMarkerValue(marker.getSerial());
				double value = mValue.getValue();
				range.min = Math.min(range.min, value);
				range.max = Math.max(range.max, value);
				range.norm.add(value);
			}
			for (DSGeneMarker marker : microarraySet.getMarkers()) {
				Range markerRange = ((DSRangeMarker) marker)
						.getRange();
				markerRange.min = range.min;
				markerRange.max = range.max;
				markerRange.norm = range.norm;
			}
		} else {
			for (DSGeneMarker marker : microarraySet.getMarkers()) {
				DSItemList<DSMicroarray> items = view.items();
				for (int i=0; i<items.size(); i++) {
					DSMicroarray ma = items.get(i);
					DSMutableMarkerValue mValue = (DSMutableMarkerValue) ma
							.getMarkerValue(marker.getSerial());
					((DSRangeMarker) marker).updateRange(mValue);
				}
			}
		}
	}
	
	// TODO important question: why is the original lock necessary?
	/** return value, range  [-255, 255] */
	private static int getMarkerValueColor(double value, DSGeneMarker mInfo,
			float intensity) {

		intensity = 2 / intensity;

		org.geworkbench.bison.util.Range range = ((DSRangeMarker) mInfo)
				.getRange();
		double mean = range.norm.getMean();
		double foldChange = (value - mean) / (range.norm.getSigma() + 0.00001);

		int colVal = (int) ((foldChange / intensity) * 255);
		if (colVal < -255) colVal = -255;
		if (colVal > 255) colVal = 255;

		return colVal;
	}

	@Override
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
