package org.geworkbenchweb.plugins.hierarchicalclustering;

import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
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
		final int geneNo = microarraySet.markers().size();
		final int chipNo = microarraySet.items().size();

		final String[] markerNames = new String[geneNo];
		final String[] arrayNames = new String[chipNo];
		final int[] colors = new int[chipNo*geneNo]; /* range [-255, 255] */
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
		final String markerClusterString = markerString.toString();

		StringBuffer arrayString 	= 	new StringBuffer();
		if(arrayCluster != null) {
			convertToString(arrayString, arrayCluster);
		}
		final String arrayClusterString = arrayString.toString();

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
								String[] temp 	= 	new String[0]; // TODO selected markers
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
								String[] temp 	= 	new String[0]; //TODO selected arrays
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
		
		MenuBar.MenuItem export 	= 	toolBar.addItem("Export Image", new Command(){

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO 
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
