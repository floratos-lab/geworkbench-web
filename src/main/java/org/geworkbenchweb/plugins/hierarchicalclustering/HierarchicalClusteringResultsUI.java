package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.hierarchicalclustering.computation.HNode;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.MicroarraySet;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.plugins.hierarchicalclustering.SubsetCommand.SetType;
import org.geworkbenchweb.pojos.HierarchicalClusteringResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.visualizations.Dendrogram;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

public class HierarchicalClusteringResultsUI extends VerticalSplitPanel implements Visualizer {

	private static final long serialVersionUID = 8018658107854483097L;
	private static Log log = LogFactory.getLog(HierarchicalClusteringResultsUI.class);
	
	final private Long datasetId;

	public HierarchicalClusteringResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) {
			log.debug("dataSetId is null");
			return;
		}
		
		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLocked(true);
		setSplitPosition(25, com.vaadin.terminal.Sizeable.UNITS_PIXELS); //((float) 2.3);

		MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class,
				dataSetId);
		Long id = resultSet.getDataId();
		if (id == null) { // pending node
			addComponent(new Label("Pending computation - ID " + dataSetId));
			return;
		}
		HierarchicalClusteringResult result = FacadeFactory.getFacade().find(
				HierarchicalClusteringResult.class, id);

		HNode markerCluster = result.getMarkerCluster();
		HNode arrayCluster = result.getArrayCluster();
		int[] selectedMarkers = result.getSelectedMarkers();
		int[] selectedArrays = result.getSelectedArrays();

		List<Integer> reorderedMarker = new ArrayList<Integer>();
		StringBuffer markerString 	= 	new StringBuffer();
		if(markerCluster != null) {
			convertToString(markerString, markerCluster, reorderedMarker);
		} else {
			if(selectedMarkers!=null)
				for(int i=0; i<selectedMarkers.length; i++) reorderedMarker.add(i);
		}
		String markerClusterString = markerString.toString();

		List<Integer> reorderedMicroarray = new ArrayList<Integer>();
		StringBuffer arrayString 	= 	new StringBuffer();
		if(arrayCluster != null) {
			convertToString(arrayString, arrayCluster, reorderedMicroarray);
		} else {
			for(int i=0; i<selectedArrays.length; i++) reorderedMicroarray.add(i);
		}
		String arrayClusterString = arrayString.toString();

		Long parentDatasetId = resultSet.getParent();
		MicroarraySet microarrays = DataSetOperations.getMicroarraySet(parentDatasetId);
		
		if(selectedMarkers==null) {
			selectedMarkers = new int[microarrays.markerNumber];
			for(int i=0; i<selectedMarkers.length; i++) selectedMarkers[i] = i;
		}
		if(selectedArrays==null) {
			selectedArrays = new int[microarrays.arrayNumber];
			for(int i=0; i<selectedArrays.length; i++) selectedArrays[i] = i;
		}
		int geneNo = selectedMarkers.length;
		int chipNo = selectedArrays.length;

		String[] markerNames = new String[geneNo];
		String[] arrayNames = new String[chipNo];
		int[] colors = new int[chipNo*geneNo]; /* range [-255, 255] */
		int k = 0;

		float[][] values = microarrays.values;
		Range[] ranges = updateRange(selectedMarkers, selectedArrays, values);

		if(reorderedMarker.size()==0) {
			for(int i=0; i<microarrays.markerNumber; i++) reorderedMarker.add(i);
		}
		if(reorderedMicroarray.size()==0) {
			for(int i=0; i<microarrays.arrayNumber; i++) reorderedMicroarray.add(i);
		}

		for (int j=0; j<reorderedMicroarray.size(); j++) {
			int index = reorderedMicroarray.get(j); // index within the selected microarrays
			arrayNames[j] = microarrays.arrayLabels[selectedArrays[index]];
		}
		for (int i=0; i<reorderedMarker.size(); i++) {
			int index = reorderedMarker.get(i); // index within the selected markers
			int markerIndex = selectedMarkers[index];
			markerNames[i] = microarrays.markerLabels[markerIndex];
			for (int j=0; j<selectedArrays.length; j++) {
				int arrayIndex = selectedArrays[reorderedMicroarray.get(j)];
				double value = values[markerIndex][arrayIndex];
				colors[k++] = getMarkerValueColor(value, 1.0f, ranges[index]);
			}
		}
		
		final Dendrogram dendrogram = new Dendrogram(chipNo, geneNo, arrayClusterString, markerClusterString,
				arrayNames, markerNames, colors);
		dendrogram.setSizeUndefined();

		dendrogram.setImmediate(true);

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

		Long parentId = resultSet.getParent();
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
	} /* end of constructor */

	/**
	 * 
	 * Recursively convert Cluster to string.
	 * result stored in a StringBuffer buffer, so buffer should be set to empty before starting from the root
	 */
	private void convertToString(final StringBuffer buffer, HNode hierCluster, List<Integer> reorderedIndex) {

		buffer.append("(");

		if (!hierCluster.isLeafNode()) {
			convertToString(buffer, hierCluster.getRight(), reorderedIndex);
			convertToString(buffer, hierCluster.getLeft(), reorderedIndex);
		} else {
			// TODO item should be numeric instead of string is that is what we want
			int index = Integer.parseInt(hierCluster.getLeafItem());
			reorderedIndex.add(index);
		}
		buffer.append(")");
	}

	/* 'range' a given marker is calculated over the selected microarrays */
	private Range[] updateRange(final int[] selectedMarkers,
			final int[] selectedArrays, final float[][] values) {
		Range[] ranges = new Range[selectedMarkers.length];
		
		if (selectedArrays.length==1) {
			/* special case of only one array selected:
			 * calculate differently (over all markers, only once) and use it for all markers */
			int arrayIndex = selectedArrays[0];
			double sum = 0;
			double sumSquare = 0;
			for(int i=0; i<selectedMarkers.length; i++) {
				int markerIndex = selectedMarkers[i];
				double v = values[markerIndex][arrayIndex];
				sum += v;
				sumSquare += v*v;
			}
			int n = selectedMarkers.length;
			double mean = sum/n;
            double variance = (sumSquare - n * mean * mean) / (n - 1);
            double sigma = Math.sqrt(variance);
			for(int i=0; i<selectedMarkers.length; i++) {
				ranges[i] = new Range(mean, sigma);
			}
		} else {
			for (int i=0; i< selectedMarkers.length; i++) {
				int markerIndex = selectedMarkers[i];

				double sum = 0;
				double sumSquare = 0;

				for (int j=0; j<selectedArrays.length; j++) {
					int arrayIndex = selectedArrays[j];
					double v = values[markerIndex][arrayIndex];
					sum += v;
					sumSquare += v*v;
				}
				int n = selectedArrays.length;
				double mean = sum/n;
                double variance = (sumSquare - n * mean * mean) / (n - 1);
                double sigma = Math.sqrt(variance);
				ranges[i] = new Range(mean, sigma); 
			}
		}
		return ranges;
	}
	
	/* the name 'Range' is a left-over from earlier code of desktop version. just two statistics: mean and standard deviation*/
	private static class Range {
		final double mean;
		final double sigma;
		
		Range(double m, double s) {
			mean = m;
			sigma = s;
		}
	}
	
	/** return value, range  [-255, 255] */
	private static int getMarkerValueColor(double value, float intensity, Range range) {

		intensity = 2 / intensity;

		double mean = range.mean;
		double foldChange = (value - mean) / (range.sigma + 0.00001);

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
