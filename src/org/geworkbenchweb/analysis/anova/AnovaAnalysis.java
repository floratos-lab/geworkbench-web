package org.geworkbenchweb.analysis.anova;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.geworkbenchweb.analysis.anova.ui.UAnovaParamForm;
import org.geworkbenchweb.analysis.markus.MarkusAnalysis;
 
import org.geworkbenchweb.analysis.markus.ui.UMarkusParamForm;
 
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;

/**
 * 
 * This class submits ARACne Analysis from web application
 * @author Nikhil Reddy
 *
 */
public class AnovaAnalysis {
	
	private static Log log = LogFactory.getLog(AnovaAnalysis.class);
	private User user = SessionHandler.get();
	private DSMicroarraySet dataSet = null;
	private UAnovaParamForm  paramForm= null;
 
	public AnovaAnalysis(DSMicroarraySet dataSet, UAnovaParamForm paramForm) {
		this.dataSet = dataSet;
		this.paramForm	 = paramForm;
	}
	
	 
	
	/*  public ResultSet execute(){ 
		
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet);
		
		String positions  	=  	params.get(0);
		String[] temp 		=   (positions.substring(1, positions.length()-1)).split(",");
		
		ArrayList<String> hubGeneList = new ArrayList<String>();
		
		for(int i=0; i<temp.length; i++) {
			
			hubGeneList.add(dataSet.getMarkers().get(Integer.parseInt(temp[i].trim())).getGeneName());
			
		}
		
		p.setSubnet(new Vector<String>(hubGeneList));
		if(params.get(5).equalsIgnoreCase("Mutual Info")) {
			
			p.setThreshold(Double.valueOf(params.get(6).toString()));
			
		} else {
			
			p.setPvalue(Double.valueOf(params.get(6).toString()));
			
		}
		if(params.get(8).equalsIgnoreCase("Apply")) {
		
			p.setEps(Double.valueOf(params.get(9).toString()));
		
		}
		
		
		
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
			java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
			resultSet.setDateField(date);
			String dataSetName 	=	"ARACne - " + new java.util.Date();
			resultSet.setName(dataSetName);
			resultSet.setType("ARACne");
			resultSet.setParent(dataSet.getDataSetName());
			resultSet.setOwner(SessionHandler.get().getId());	
			resultSet.setData(ObjectConversion.convertToByte(dSet));
			FacadeFactory.getFacade().store(resultSet);	
			
			NodeAddEvent resultEvent = new NodeAddEvent(dataSetName, "result");
			GeworkbenchRoot.getBlackboard().fire(resultEvent);
			
		}
	}   */
	
	 
}
