package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Dendrogram;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/* This is based on the older version under ...results. package after fixing many problems. */
public class HierarchicalClusteringResultsUI extends VerticalSplitPanel {


	private static final long serialVersionUID = 8018658107854483097L;

	private Dendrogram dendrogram;

	private int geneNo;

	private int chipNo;

	private String[] markerNames; 

	private String[] arrayNames	;	

	private int[] colors; /* range [-255, 255] */ 

	private int geneHeight 	= 	5;

	private int geneWidth 	= 	10;

	@SuppressWarnings({ "unchecked" })
	public HierarchicalClusteringResultsUI(Long dataSetId) {

		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLocked(true);
		setSplitPosition(25, com.vaadin.terminal.Sizeable.UNITS_PIXELS); //((float) 2.3);

		MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");

		final ResultSet data 			= 	FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		CSHierClusterDataSet dataSet 	= 	(CSHierClusterDataSet) ObjectConversion.toObject(UserDirUtils.getResultSet(dataSetId));

		DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) dataSet.getDataSetView();
		MarkerHierCluster currentMarkerCluster 			= 	(MarkerHierCluster)dataSet.getCluster(0);
		MicroarrayHierCluster currentArrayCluster 			= 	(MicroarrayHierCluster)dataSet.getCluster(1);

		Cluster[] leafMarkers = null;
		if (currentMarkerCluster != null) {
			java.util.List<Cluster> leaves = currentMarkerCluster.getLeafChildren();
			leafMarkers = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
			leaves.toArray(leafMarkers);
		}

		Cluster[] leafArrays = null;
		if (currentArrayCluster != null) {
			java.util.List<Cluster> leaves = currentArrayCluster.getLeafChildren();
			leafArrays = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
			leaves.toArray(leafArrays);
		}


		if (currentMarkerCluster == null) {
			geneNo = microarraySet.markers().size();
		} else {
			geneNo = leafMarkers.length;
		}

		if (currentArrayCluster == null) {
			chipNo = microarraySet.items().size();
		} else {
			chipNo = leafArrays.length;
		}

		markerNames 	= 	new String[geneNo];
		arrayNames		= 	new String[chipNo];
		colors 			= 	new int[chipNo*geneNo];
		int k = 0;

		for (int i = 0; i < geneNo; i++) {
			DSGeneMarker stats = null;

			if (leafMarkers != null) {
				stats = ((MarkerHierCluster) leafMarkers[i]).getMarkerInfo();
			} else {
				stats = microarraySet.markers().get(i);
			}

			markerNames[i] = stats.getLabel();
			for (int j = 0; j < chipNo; j++) {
				DSMicroarray mArray = null;
				if (leafArrays != null) {
					mArray = ((MicroarrayHierCluster) leafArrays[j])
							.getMicroarray();
				} else {
					mArray = microarraySet.get(j);
				}

				if(i == 0) {
					arrayNames[j] = mArray.getLabel();
				}

				DSMarkerValue marker 	= 	mArray.getMarkerValue(stats);
				colors[k] 				= 	getMarkerValueColor(marker.getValue(), stats, 1.0f);
				k++;
			}
		}

		HierCluster markerCluster 		= 	dataSet.getCluster(0);
		HierCluster arrayCluster 		= 	dataSet.getCluster(1);

		StringBuffer markerString 	= 	new StringBuffer();
		if(markerCluster != null) {
			convertToString(markerString, markerCluster);
		}
		final String markerClusterString = markerString.toString();

		StringBuffer arrayString 	= 	new StringBuffer();
		if(arrayCluster != null) {
			convertToString(arrayString, arrayCluster);
		}
		final String arrayClusterString = arrayString.toString();

		dendrogram = new Dendrogram(chipNo, geneNo, arrayClusterString, markerClusterString);
		dendrogram.setSizeUndefined();

		/**
		 * default gene height and width for the dendrogram
		 */
		dendrogram.setGeneHeight(geneHeight);
		dendrogram.setGeneWidth(geneWidth);

		toolBar.addItem("", new ThemeResource("../custom/icons/Zoom-In-icon.png"), 
				new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				removeComponent(dendrogram);
				geneHeight 	= 	geneHeight*2;
				geneWidth 	=	geneWidth*2;
				dendrogram.setGeneHeight(geneHeight);
				dendrogram.setGeneWidth(geneWidth);
				setSecondComponent(dendrogram);
			}
		});
		toolBar.addItem("", new ThemeResource("../custom/icons/Zoom-Out-icon.png"),
				new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				if(geneHeight != 1) {
					removeComponent(dendrogram);
					geneHeight 	= 	geneHeight/2;
					geneWidth 	=	geneWidth/2;
					dendrogram.setGeneHeight(geneHeight);
					dendrogram.setGeneWidth(geneWidth);
					setSecondComponent(dendrogram);
				}
			}
		});

		MenuBar.MenuItem resetI		=	toolBar.addItem("Reset", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				removeComponent(dendrogram);
				geneHeight 	= 	5;
				geneWidth 	=	10;
				dendrogram = new Dendrogram(chipNo, geneNo, arrayClusterString, markerClusterString);
				dendrogram.setGeneHeight(geneHeight);
				dendrogram.setGeneWidth(geneWidth);

				dendrogram.setColors(colors);
				dendrogram.setMarkerLabels(markerNames);
				dendrogram.setArrayLabels(arrayNames);
				dendrogram.setImmediate(true);
				//dendrogram.setSizeFull();
				setSecondComponent(dendrogram);			}
		});
		
		MenuBar.MenuItem saveM		=	toolBar.addItem("Save Markers", new Command(){

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window nameWindow = new Window();
				nameWindow.setModal(true);
				nameWindow.setClosable(true);
				((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
				nameWindow.setWidth("300px");
				nameWindow.setHeight("150px");
				nameWindow.setResizable(false);
				nameWindow.setCaption("Add Markers to Set");
				nameWindow.setImmediate(true);

				final TextField setName = new TextField();
				setName.setInputPrompt("Please enter set name");
				setName.setImmediate(true);

				Button submit = new Button("Submit", new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {
							if(setName.getValue() != null) {
								ArrayList<String> markers = new ArrayList<String>();
								String[] temp 	= 	dendrogram.getMarkerLabels();
								for(int i=0; i<temp.length; i++) {
									String label = temp[i].trim();
									markers.add(label);
								}
								String subSetName = (String) setName.getValue() + " ["+markers.size()+ "]";
								SubSetOperations.storeData(markers, "marker", subSetName , data.getParent());
								getApplication().getMainWindow().removeWindow(nameWindow);
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				nameWindow.addComponent(setName);
				nameWindow.addComponent(submit);
				getApplication().getMainWindow().addWindow(nameWindow);
			}
		});
		
		MenuBar.MenuItem saveP 		=	toolBar.addItem("Save Phenotypes", new Command(){

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window nameWindow = new Window();
				nameWindow.setModal(true);
				nameWindow.setClosable(true);
				((AbstractOrderedLayout) nameWindow.getLayout()).setSpacing(true);
				nameWindow.setWidth("300px");
				nameWindow.setHeight("150px");
				nameWindow.setResizable(false);
				nameWindow.setCaption("Add Phenotypes to Set");
				nameWindow.setImmediate(true);

				final TextField setName = new TextField();
				setName.setInputPrompt("Please enter set name");
				setName.setImmediate(true);

				Button submit = new Button("Submit", new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						try {
							if(setName.getValue() != null) {
								ArrayList<String> arrays = new ArrayList<String>();
								String[] temp 	= 	dendrogram.getArrayLabels();
								for(int i=0; i<temp.length; i++) {
									arrays.add(temp[i].trim());
								}

								String subSetName =  (String) setName.getValue() + " [" + arrays.size() + "]";
								SubSetOperations.storeArraySetInCurrentContext(arrays, subSetName, data.getParent());
								getApplication().getMainWindow().removeWindow(nameWindow);
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				submit.setClickShortcut(KeyCode.ENTER);
				nameWindow.addComponent(setName);
				nameWindow.addComponent(submit);
				getApplication().getMainWindow().addWindow(nameWindow);
			}
		});
		
		MenuBar.MenuItem export 	= 	toolBar.addItem("Export HTML", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				dendrogram.setSVGFlag("true");
			}
		});

		resetI.setStyleName("plugin");
		saveM.setStyleName("plugin");
		saveP.setStyleName("plugin");
		export.setStyleName("plugin");

		setFirstComponent(toolBar);

		dendrogram.setColors(colors);
		dendrogram.setMarkerLabels(markerNames);
		dendrogram.setArrayLabels(arrayNames);
		dendrogram.setImmediate(true);
		//dendrogram.setSizeFull(); // FIXME why not
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
