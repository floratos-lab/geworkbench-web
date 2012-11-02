package org.geworkbenchweb.plugins.aracne;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import edu.columbia.c2b2.aracne.Parameter;
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;

/**
 * 
 * This class submits ARACne Analysis from web application
 * @author Nikhil Reddy
 *
 */
public class AracneAnalysisWeb {
	
	private DSMicroarraySet dataSet = null;
	
	final Parameter p = new Parameter();
	
	HashMap<Serializable, Serializable> params =  new HashMap<Serializable, Serializable>();
	
	public AracneAnalysisWeb(DSMicroarraySet dataSet, HashMap<Serializable, Serializable> params) {
		this.params = params;
		this.dataSet = dataSet;
	}
		
	public AdjacencyMatrixDataSet execute() {
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
		
		Long subSetId  				=  		Long.parseLong((String) params.get(AracneParameters.MARKER_SET));
		ArrayList<String> hubGeneList 	= 	new ArrayList<String>();
		ArrayList<String> markers		= 	getMarkerData(subSetId);
		
		for(int i=0; i<dataSet.getMarkers().size(); i++) {
			if(markers.contains(dataSet.getMarkers().get(i).getLabel())) {
				hubGeneList.add(dataSet.getMarkers().get(i).getGeneName());	
			}
		}
		
		p.setSubnet(new Vector<String>(hubGeneList));
		if(((String) params.get(AracneParameters.T_TYPE)).equalsIgnoreCase("Mutual Info")) {
			p.setThreshold(Double.valueOf((String) params.get(AracneParameters.T_VALUE)));
		} else {
			p.setPvalue(Double.valueOf(params.get(AracneParameters.T_VALUE).toString()));	
		}
		
		if(((String) params.get(AracneParameters.TOL_TYPE)).equalsIgnoreCase("Apply")) {
			p.setEps(Double.valueOf((String) params.get(AracneParameters.TOL_VALUE)));
		}
		
		if(((String) params.get(AracneParameters.MODE)).equalsIgnoreCase("Complete")) {
			p.setMode(Parameter.MODE.COMPLETE);
		}else if(((String) params.get(AracneParameters.MODE)).equalsIgnoreCase("Discovery")) {
			p.setMode(Parameter.MODE.DISCOVERY);
		}else if(((String) params.get(AracneParameters.MODE)).equalsIgnoreCase("Preprocessing")) {
			p.setMode(Parameter.MODE.PREPROCESSING);
		}
		
		if(((String) params.get(AracneParameters.ALGORITHM)).equalsIgnoreCase("Adaptive Partitioning")) {
			p.setAlgorithm(Parameter.ALGORITHM.ADAPTIVE_PARTITIONING);
		}else {
			p.setAlgorithm(Parameter.ALGORITHM.FIXED_BANDWIDTH);
		}
		
		int  bs 	= 	Integer.valueOf((String) params.get(AracneParameters.BOOTS_NUM));
		double  pt 	= 	Double.valueOf((String) params.get(AracneParameters.T_VALUE)); 
		
		AracneComputation aracneComputation = new AracneComputation(mSetView, p, bs , pt);
		
		WeightedGraph weightedGraph = aracneComputation.execute();
		
		AdjacencyMatrixDataSet dSet = null;
		if (weightedGraph.getEdges().size() > 0) {
			dSet = new AdjacencyMatrixDataSet(
					this.convert(weightedGraph, p, mSetView.getMicroarraySet(), false),
					0, "Adjacency Matrix", "ARACNE Set", mSetView
							.getMicroarraySet());
		}
		return dSet;
	}

	/**
	 * Convert the result from aracne-java to an AdjacencyMatrix object.
	 * @param graph
	 * @param p 
	 * @param mSet
	 * @return
	 */
	private AdjacencyMatrix convert(WeightedGraph graph, Parameter p,
			DSMicroarraySet mSet, boolean prune) {
		AdjacencyMatrix matrix = new AdjacencyMatrix(null);

		Vector<String> subnet = p.getSubnet();
		
		@SuppressWarnings("unused")
		int nEdge = 0;
		for (GraphEdge graphEdge : graph.getEdges()) {
			DSGeneMarker marker1 = mSet.getMarkers().get(graphEdge.getNode1());
			DSGeneMarker marker2 = mSet.getMarkers().get(graphEdge.getNode2());
			
			if (!subnet.contains(marker1.getLabel())) {
				DSGeneMarker m = marker1;
				marker1 = marker2;
				marker2 = m;
			}

			AdjacencyMatrix.Node node1, node2;
			if (!prune) {
				node1 = new AdjacencyMatrix.Node(marker1);
				node2 = new AdjacencyMatrix.Node(marker2);
				matrix.add(node1, node2, graphEdge.getWeight(), null);
			} else {
				node1 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
						marker1.getGeneName());
				node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
						marker2.getGeneName());
				matrix.add(node1, node2, graphEdge.getWeight());
				
			}
			nEdge++;
		}
	
		return matrix;
	}
	
	/**
	 * Create Dataset for selected markerSet 
	 */
	public ArrayList<String> getMarkerData(Long subSetId) {

		@SuppressWarnings("rawtypes")
		List subSet 			= 	SubSetOperations.getMarkerSet(subSetId);
		ArrayList<String> positions 	= 	(((SubSet) subSet.get(0)).getPositions());
		
		return positions;
	}

}
