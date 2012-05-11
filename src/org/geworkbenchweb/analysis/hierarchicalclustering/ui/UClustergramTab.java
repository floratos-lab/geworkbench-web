package org.geworkbenchweb.analysis.hierarchicalclustering.ui;

import java.awt.Color;
import java.lang.reflect.Array;
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
import org.geworkbenchweb.analysis.hierarchicalclustering.ClusterNode;
import org.geworkbenchweb.visualizations.Clustergram;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class UClustergramTab extends VerticalLayout{

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
    
    /**
     * The String is passed to the client side
     * Marker String
     */
    private static StringBuffer markerString = new StringBuffer(); 
    
    /**
     * The string is passed to the client side
     * Array String
     */
    private static StringBuffer arrayString = new StringBuffer(); 
    
    User user = SessionHandler.get();

    
    
    private transient Object lock = new Object();
	
	@SuppressWarnings({ "unchecked", "unused" })
	public UClustergramTab(CSHierClusterDataSet dataSet) {
		
		setStyleName(Reindeer.LAYOUT_WHITE);
        microarraySet = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) dataSet.getDataSetView();
        
        currentMarkerCluster = (MarkerHierCluster)dataSet.getCluster(0);
    	currentArrayCluster = (MicroarrayHierCluster)dataSet.getCluster(1);
    	
    	
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
    	 
        int geneNo = 0;

		if (currentMarkerCluster == null) {
			
			geneNo = microarraySet.markers().size();
			
		
		} else {
		
			geneNo = leafMarkers.length;
		
		}

		int chipNo = 0;

		if (currentArrayCluster == null) {
			
			chipNo = microarraySet.items().size();
			
			
		} else {
			
			chipNo = leafArrays.length;
		
		}
		
		String[] markerNames 	= 	new String[geneNo];
		String[] arrayNames		= 	new String[chipNo];
		String[] colors 		= 	new String[chipNo*geneNo];
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
				
				DSMarkerValue marker = mArray.getMarkerValue(stats);
				
				Color color = getMarkerValueColor(marker, stats, (float) intensity);
				String rgb = Integer.toHexString(color.getRGB());
				rgb = rgb.substring(2, rgb.length());
				colors[k] = rgb;
				k++;
			}
		}
		
		HierCluster markerCluster 		= 	dataSet.getCluster(0);
		HierCluster arrayCluster 		= 	dataSet.getCluster(1);
		
		Clustergram dendrogram = new Clustergram();
		
		setHeight(((geneNo*5) + 300) + "px");
        setWidth(((chipNo*20) + 500) + "px");
		dendrogram.setColors(colors);
		dendrogram.setArrayNumber(chipNo);
		dendrogram.setMarkerNumber(geneNo);
		dendrogram.setMarkerLabels(markerNames);
		dendrogram.setArrayLabels(arrayNames);
		dendrogram.setSizeFull();
		
		
		if(markerCluster != null) {
				
			ClusterNode clusterNode 	= 	convertMarkerCluster(markerCluster);
			dendrogram.setMarkerCluster(markerString.toString());
			
			//since this is the member variable I have to reset it. Have to find a way to make it non-member variable
			markerString.delete(0, markerString.length());
			
		}
			
		if(arrayCluster != null) {
			ClusterNode clusterNode 	= convertArrayCluster(arrayCluster);
			dendrogram.setArrayCluster(arrayString.toString());
			
			//since this is the member variable I have to reset it. Have to find a way to make it non-member variable
			arrayString.delete(0, arrayString.length());
		}
		addComponent(dendrogram);
	}

	private static ClusterNode convertMarkerCluster(Cluster hierCluster) {
	
		markerString.append("(");
		ClusterNode cluster = null;
		
		if(hierCluster.isLeaf()) {
			
			markerString.append(")");
		
		} else {	
			
			Cluster[] child 	= 	hierCluster.getChildrenNodes();
			ClusterNode c1 		= 	convertMarkerCluster(child[0]);
			ClusterNode c2 		= 	convertMarkerCluster(child[1]);
			cluster 			= 	new ClusterNode(c1, c2);
			
			markerString.append(")");
		
		}
		
		return cluster;
	
	}
	
	private static ClusterNode convertArrayCluster(Cluster hierCluster) {

		arrayString.append("(");
		ClusterNode cluster = null;
		
		if(hierCluster.isLeaf()) {
			
			arrayString.append(")");
		
		} else {	
			
			Cluster[] child 	= 	hierCluster.getChildrenNodes();
			ClusterNode c1 		= 	convertArrayCluster(child[0]);	
			ClusterNode c2 		= 	convertArrayCluster(child[1]);
			cluster 			= 	new ClusterNode(c1, c2);
			
			arrayString.append(")");
		
		}
		
		return cluster;
	
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
