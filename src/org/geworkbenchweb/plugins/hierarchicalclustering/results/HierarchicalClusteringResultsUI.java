package org.geworkbenchweb.plugins.hierarchicalclustering.results;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.geworkbenchweb.visualizations.Clustergram;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;

public class HierarchicalClusteringResultsUI extends VerticalLayout {

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
	
	private Button in;
	
	private Button out;
	
	@SuppressWarnings({ "unchecked" })
	public HierarchicalClusteringResultsUI(Long dataSetId) {
		
		setImmediate(true);
		setStyleName(Reindeer.LAYOUT_WHITE);
	
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id", parameters);
		
		CSHierClusterDataSet dataSet 	= 	(CSHierClusterDataSet) ObjectConversion.toObject(data.get(0).getData());
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
			@SuppressWarnings("unused")
			ClusterNode clusterNode 	= 	convertMarkerCluster(markerCluster);
			mString = markerString.toString();
		}
			
		if(arrayCluster != null) {
			@SuppressWarnings("unused")
			ClusterNode clusterNode 	= convertArrayCluster(arrayCluster);
			aString = arrayString.toString();
		}
		
		dendrogram = new Clustergram();
		/**
		 * default gene height and width for the dendrogram
		 */
		dendrogram.setGeneHeight(geneHeight);
		dendrogram.setGeneWidth(geneWidth);
		
        setWidth("100%");
        setHeight("100%");
		
        HorizontalLayout controlLayout = new HorizontalLayout();
        
        
        in	= 	new Button("+", new Button.ClickListener() {
			
			private static final long serialVersionUID = -7814134578733193087L;

			@Override
			public void buttonClick(ClickEvent event) {
				removeComponent(dendrogram);
				geneHeight 	= 	geneHeight*2;
				geneWidth 	=	geneWidth*2;
				dendrogram = new Clustergram();
				dendrogram.setGeneHeight(geneHeight);
				dendrogram.setGeneWidth(geneWidth);
				addDendrogram();
			}
		});
        out 	= 	new Button("-", new Button.ClickListener() {
			
			private static final long serialVersionUID = -396773583444901979L;

			@Override
			public void buttonClick(ClickEvent event) {
				removeComponent(dendrogram);
				dendrogram = new Clustergram();
				geneHeight 	= 	geneHeight/2;
				geneWidth 	=	geneWidth/2;
				dendrogram.setGeneHeight(geneHeight);
				dendrogram.setGeneWidth(geneWidth);
				addDendrogram();
				
			}
		});
		Button reset = new Button("Reset", new Button.ClickListener() {
			
			private static final long serialVersionUID = -7814134578733193087L;

			@Override
			public void buttonClick(ClickEvent event) {
				removeComponent(dendrogram);
				geneHeight 	= 	5;
				geneWidth 	=	10;
				dendrogram = new Clustergram();
				dendrogram.setGeneHeight(geneHeight);
				dendrogram.setGeneWidth(geneWidth);
				addDendrogram();
			}
		});
		
		controlLayout.setSpacing(true);
		controlLayout.addComponent(in);
		controlLayout.addComponent(out);
		controlLayout.addComponent(reset);
		addComponent(controlLayout);
		addDendrogram();
	}
	
	public void addDendrogram() {
		
		if(geneHeight == 1) {
			out.setEnabled(false);
		} else {
			out.setEnabled(true);
		}
		if(geneHeight == 80) {
			in.setEnabled(false);
		} else {
			in.setEnabled(true);
		}
		dendrogram.setMarkerCluster(mString);
		dendrogram.setArrayCluster(aString);
		dendrogram.setColors(colors);
		dendrogram.setArrayNumber(chipNo);
		dendrogram.setMarkerNumber(geneNo);
		dendrogram.setMarkerLabels(markerNames);
		dendrogram.setArrayLabels(arrayNames);
		dendrogram.setImmediate(true);
		dendrogram.setSizeFull();
		
		this.addComponent(dendrogram);
		this.setExpandRatio(dendrogram, 1);
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
