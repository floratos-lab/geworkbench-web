package org.geworkbenchweb.analysis.aracne;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbenchweb.layout.UAccordionPanel;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

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
	
	final Parameter p = new Parameter();
	
	public AracneAnalysisWeb(DSMicroarraySet dataSet, ArrayList<String> params) {
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
		
		String positions  	=  	params.get(0);
		String[] temp 		=   (positions.substring(1, positions.length()-1)).split(",");
		
		ArrayList<String> hubGeneList = new ArrayList<String>();
		
		for(int i=0; i<temp.length; i++) {
			
			hubGeneList.add(dataSet.getMarkers().get(Integer.parseInt(temp[i].trim())).getGeneName());
			
		}
		
		p.setSubnet(new Vector<String>(hubGeneList));
		p.setPvalue(0.01);
		
		if(params.get(1).equalsIgnoreCase("Complete")) {
			p.setMode(Parameter.MODE.COMPLETE);
		}else if(params.get(1).equalsIgnoreCase("Discovery")) {
			p.setMode(Parameter.MODE.DISCOVERY);
		}else if(params.get(1).equalsIgnoreCase("Preprocessing")) {
			p.setMode(Parameter.MODE.PREPROCESSING);
		}
		
		if(params.get(2).equalsIgnoreCase("Adaptive Partitioning")) {
			p.setAlgorithm(Parameter.ALGORITHM.ADAPTIVE_PARTITIONING);
		}else {
			p.setAlgorithm(Parameter.ALGORITHM.FIXED_BANDWIDTH);
		}
		
		int  bs 	= 	Integer.valueOf(params.get(12));
		double  pt 	= 	Double.valueOf(params.get(6)); 
		
		AracneComputation aracneComputation = new AracneComputation(mSetView, p, bs , pt);
		
		WeightedGraph weightedGraph = aracneComputation.execute();
		
		
		if (weightedGraph.getEdges().size() > 0) {
			
			AdjacencyMatrixDataSet dSet = new AdjacencyMatrixDataSet(
					this.convert(weightedGraph, p, mSetView.getMicroarraySet(), false),
					0, "Adjacency Matrix", "ARACNE Set", mSetView
							.getMicroarraySet());
			
			ResultSet resultSet = 	new ResultSet();
			java.util.Date date= new java.util.Date();
			resultSet.setName("ARACne - " + date);
			resultSet.setType("ARACne");
			resultSet.setParent(dataSet.getDataSetName());
			resultSet.setOwner(SessionHandler.get().getId());	
			resultSet.setData(convertToByte(dSet));
			FacadeFactory.getFacade().store(resultSet);	
			
			UAccordionPanel.resetDataContainer();
			
		}
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
		AdjacencyMatrix matrix = new AdjacencyMatrix(null, mSet);

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
	
	private byte[] convertToByte(Object object) {

		byte[] byteData = null;
		ByteArrayOutputStream bos 	= 	new ByteArrayOutputStream();

		try {

			ObjectOutputStream oos 	= 	new ObjectOutputStream(bos); 

			oos.writeObject(object);
			oos.flush(); 
			oos.close(); 
			bos.close();
			byteData 				= 	bos.toByteArray();

		} catch (IOException ex) {

			System.out.println("Exception with in convertToByte");

		}

		return byteData;

	}

}
