package org.geworkbenchweb.plugins.hierarchicalclustering;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbenchweb.plugins.hierarchicalclustering.SubsetCommand.SetType;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Dendrogram;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

/* This is started from the class with the same name under the .results. package. */
public class HierarchicalClusteringResultsUI extends VerticalSplitPanel {


	private static final long serialVersionUID = 8018658107854483097L;

	@SuppressWarnings({ "unchecked" })
	public HierarchicalClusteringResultsUI(Long dataSetId) {

		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLocked(true);
		setSplitPosition(25, com.vaadin.terminal.Sizeable.UNITS_PIXELS); //((float) 2.3);

		MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");

		CSHierClusterDataSet dataSet 	= 	(CSHierClusterDataSet) ObjectConversion.toObject(UserDirUtils.getResultSet(dataSetId));

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) dataSet.getDataSetView();
		int geneNo = microarraySet.markers().size();
		int chipNo = microarraySet.items().size();

		String[] markerNames = new String[geneNo];
		String[] arrayNames = new String[chipNo];
		int[] colors = new int[chipNo*geneNo]; /* range [-255, 255] */
		int k = 0;

		for (int j = 0; j < chipNo; j++) {
			arrayNames[j] = microarraySet.get(j).getLabel();
		}
		for (int i = 0; i < geneNo; i++) {
			DSGeneMarker marker = microarraySet.markers().get(i);

			markerNames[i] = marker.getLabel();
			for (int j = 0; j < chipNo; j++) {
				double value = microarraySet.get(j).getMarkerValue(marker)
						.getValue();
				colors[k++] = getMarkerValueColor(value, marker, 1.0f);
			}
		}

		HierCluster markerCluster 		= 	dataSet.getCluster(0);
		HierCluster arrayCluster 		= 	dataSet.getCluster(1);

		StringBuffer markerString 	= 	new StringBuffer();
		if(markerCluster != null) {
			convertToString(markerString, markerCluster);
		}
		String markerClusterString = markerString.toString();

		StringBuffer arrayString 	= 	new StringBuffer();
		if(arrayCluster != null) {
			convertToString(arrayString, arrayCluster);
		}
		String arrayClusterString = arrayString.toString();

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

	/**
	 * 
	 * Recursively convert Cluster to string.
	 * result stored in a StringBuffer buffer, so buffer should be set to empty before starting from the root
	 * @param hierCluster
	 * @return
	 */
	static private void convertToString(final StringBuffer buffer, Cluster hierCluster) {

		buffer.append("(");

		if (!hierCluster.isLeaf()) {
			Cluster[] child = hierCluster.getChildrenNodes();
			convertToString(buffer, child[0]);
			convertToString(buffer, child[1]);
		}
		buffer.append(")");
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
}
