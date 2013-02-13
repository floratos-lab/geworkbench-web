package org.geworkbenchweb.plugins.hierarchicalclustering.results;

import java.awt.Color;
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
import org.geworkbenchweb.visualizations.Clustergram;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

public class HierarchicalClusteringResultsUI extends VerticalSplitPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * The underlying micorarray set used in the hierarchical clustering
	 * analysis.
	 */
	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = null;

	/**
	 * The current marker cluster being rendered in the marker Dendrogram
	 */
	private MarkerHierCluster currentMarkerCluster = null;

	/**
	 * The leaf marker clusters in <code>currentMarkerCluster</code>.
	 */
	private Cluster[] leafMarkers = null;

	/**
	 * The current array cluster being rendered in the marker Dendrogram
	 */
	private MicroarrayHierCluster currentArrayCluster = null;

	/**
	 * The leaf microarrays clusters in <code>currentArrayCluster</code>.
	 */
	private Cluster[] leafArrays = null;

	private double intensity = 1.0;

	User user = SessionHandler.get();

	private transient Object lock = new Object();

	private Clustergram dendrogram;

	private int geneNo;

	private int chipNo;

	private String[] markerNames; 

	private String[] arrayNames	;	

	private String[] colors; 

	private String mString;

	private String aString;

	private int geneHeight 	= 	5;

	private int geneWidth 	= 	10;

	private MenuBar toolBar;

	@SuppressWarnings({ "unchecked" })
	public HierarchicalClusteringResultsUI(Long dataSetId) {

		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.SPLITPANEL_SMALL);
		setLocked(true);
		setSplitPosition(25, com.vaadin.terminal.Sizeable.UNITS_PIXELS); //((float) 2.3);

		toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");

		final ResultSet data 			= 	FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		CSHierClusterDataSet dataSet 	= 	(CSHierClusterDataSet) ObjectConversion.toObject(UserDirUtils.getResultSet(dataSetId));
		microarraySet	 				= 	(DSMicroarraySetView<DSGeneMarker, DSMicroarray>) dataSet.getDataSetView();
		currentMarkerCluster 			= 	(MarkerHierCluster)dataSet.getCluster(0);
		currentArrayCluster 			= 	(MicroarrayHierCluster)dataSet.getCluster(1);

		if (currentMarkerCluster != null) {
			java.util.List<Cluster> leaves = currentMarkerCluster.getLeafChildren();
			leafMarkers = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
			leaves.toArray(leafMarkers);
		}

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
		colors 			= 	new String[chipNo*geneNo];
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
				Color color 			= 	getMarkerValueColor(marker, stats, (float) intensity);
				String rgb 				= 	Integer.toHexString(color.getRGB());
				rgb 					= 	rgb.substring(2, rgb.length());
				colors[k] 				= 	rgb;
				k++;
			}
		}

		HierCluster markerCluster 		= 	dataSet.getCluster(0);
		HierCluster arrayCluster 		= 	dataSet.getCluster(1);

		if(markerCluster != null) {
			StringBuffer markerString 	= 	new StringBuffer();
			convertToString(markerString, markerCluster);
			mString = markerString.toString();
		}

		if(arrayCluster != null) {
			StringBuffer arrayString 	= 	new StringBuffer();
			convertToString(arrayString, arrayCluster);
			aString = arrayString.toString();
		}

		dendrogram = new Clustergram();
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
				dendrogram = new Clustergram();
				dendrogram.setGeneHeight(geneHeight);
				dendrogram.setGeneWidth(geneWidth);
				addDendrogram();
			}
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
		addDendrogram();
	}

	public void addDendrogram() {

		/*if(geneHeight == 1) {
			out.setEnabled(false);
		} else {
			out.setEnabled(true);
		}
		if(geneHeight == 80) {
			in.setEnabled(false);
		} else {
			in.setEnabled(true);
		}*/
		dendrogram.setMarkerCluster(mString);
		dendrogram.setArrayCluster(aString);
		dendrogram.setColors(colors);
		dendrogram.setArrayNumber(chipNo);
		dendrogram.setMarkerNumber(geneNo);
		dendrogram.setMarkerLabels(markerNames);
		dendrogram.setArrayLabels(arrayNames);
		dendrogram.setImmediate(true);
		//dendrogram.setSizeFull();
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

	public Color getMarkerValueColor(DSMarkerValue mv, DSGeneMarker mInfo, float intensity) {

		//      intensity *= 2;
		intensity = 2 / intensity; 
		double value = mv.getValue();
		if (lock == null)
			lock = new Object();
		synchronized (lock) {

			org.geworkbench.bison.util.Range range = ((DSRangeMarker) mInfo).getRange();
			double mean = range.norm.getMean(); //(range.max + range.min) / 2.0;
			double foldChange = (value - mean) / (range.norm.getSigma() + 0.00001); //Math.log(change) / Math.log(2.0);
			if (foldChange < -intensity) {
				foldChange = -intensity;
			}
			if (foldChange > intensity) {
				foldChange = intensity;
			}

			double colVal = foldChange / intensity;
			if (foldChange > 0) {
				return new Color(1.0F, (float) (1 - colVal), (float) (1 - colVal));
			} else {
				return new Color((float) (1 + colVal), (float) (1 + colVal), 1.0F);
			}
		}
	}
}
