package org.geworkbenchweb.plugins.aracne.results;

import java.util.ArrayList;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Cytoscape;
import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.TextArea;

public class AracneResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(AracneResultsUI.class);

	private static final String LIMIT_CYTOSCAPE_OBJECTS = "limit.cytoscape.objects";
	private static final int DEFAULT_LIMIT_CYTOSCAPE_OBJECTS = 5000;
	private static int limit_num = 0;
	
 
	private AdjacencyMatrixDataSet adjMatrixDataSet = null;

	final private Long datasetId;
	
	public AracneResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;

		adjMatrixDataSet = (AdjacencyMatrixDataSet) ObjectConversion
				.toObject(UserDirUtils.getResultSet(dataSetId));

		setImmediate(true);
		setSizeFull();		 
		getLimitCytoscapeObjectsNum();
	}

	@Override
	public void attach() {	 
		super.attach(); 
		removeAllComponents();
		
		int edgeNumber = adjMatrixDataSet.getMatrix().getConnectionNo();
		int nodeNumber = adjMatrixDataSet.getMatrix().getNodeNumber();
		if ((edgeNumber + nodeNumber) > limit_num) {
			String theMessage = "This network has "
					+ nodeNumber
					+ " nodes and "
					+ edgeNumber
					+ " edges, which may be too large to display in Cytoscape. \nAn alternate, text view is available instead.";

			MessageBox mb = new MessageBox(getWindow(), "Warning", null,
					theMessage, new MessageBox.ButtonConfig(
							MessageBox.ButtonType.CUSTOM1, "View as text"),
					new MessageBox.ButtonConfig(MessageBox.ButtonType.CUSTOM2,
							"View in Cytoscape"));
			mb.show(new MessageBox.EventListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClicked(ButtonType buttonType) {
					if (buttonType == ButtonType.CUSTOM1)  
						viewAsText();					    
					else 
						viewAsCytoscape();
				}
			});
		} else
		{
			viewAsCytoscape();
		}
	}
	
	
	private void viewAsText()
	{
		TextArea area = new TextArea();
		area.setSizeFull();
	
		area.setWordwrap(false);
		 
		area.setImmediate(true);
		AdjacencyMatrix  adjMatrix = adjMatrixDataSet.getMatrix();
		int edgeNumber = adjMatrix.getConnectionNo();
		int nodeNumber = adjMatrix.getNodeNumber();
		StringBuffer sb = new StringBuffer("This network has "
				+ nodeNumber + " nodes and " + edgeNumber
				+ " edges: \n\n");
        
       
		for (AdjacencyMatrix.Node node1 : adjMatrix.getNodes()) {
			
			sb.append(adjMatrixDataSet.getExportName(node1) + "\t");

			for (AdjacencyMatrix.Edge edge : adjMatrix
					.getEdges(node1)) {
				sb.append(adjMatrixDataSet.getExportName(edge.node2)
						+ "\t" + edge.info.value + "\t");
			}
			sb.append("\n");
			
			 
		}
		area.setValue(sb.toString());
		 
		 
		addComponent(area);
		 
		
	}

	private void viewAsCytoscape()
	{
		 
		/* Preparing data for cytoscape */
		ArrayList<String> nodes = new ArrayList<String>();
		ArrayList<String> edges = new ArrayList<String>();

		AdjacencyMatrix  adjMatrix = adjMatrixDataSet.getMatrix();
		for (int i = 0; i < adjMatrix.getEdges()
				.size(); i++) {
			String id1;
			String label1;
			String id2;
			String label2;
			if (adjMatrix.getEdges().get(i).node1.type
					.equals(NodeType.MARKER)) {
				id1 = adjMatrix.getEdges().get(i).node1.marker
						.getLabel();
				label1 = adjMatrix.getEdges()
						.get(i).node1.marker.getGeneName();
				if (label1.equals("---"))
					label1 = id1;
				id2 = adjMatrix.getEdges().get(i).node2.marker
						.getLabel();
				label2 = adjMatrix.getEdges()
						.get(i).node2.marker.getGeneName();
				if (label2.equals("---"))
					label2 = id2;

			} else {
				id1 = adjMatrix.getEdges().get(i).node1
						.getStringId();
				label1 = id1;
				id2 = adjMatrix.getEdges().get(i).node2
						.getStringId();
				label2 = id2;
			}

			String edge = id1 + "," + id2;

			String node1 = id1 + "," + label1 + ",0";
			String node2 = id2 + "," + label2 + ",0";

			if (edges.isEmpty()) {
				edges.add(edge);
			} else if (!edges.contains(edge)) {
				edges.add(edge);
			}

			if (nodes.isEmpty()) {
				nodes.add(node1);
				nodes.add(node2);
			} else {
				if (!nodes.contains(node1)) {
					nodes.add(node1);
				}
				if (!nodes.contains(node2)) {
					nodes.add(node2);
				}
			}
		}

		Cytoscape cy = new Cytoscape();
		cy.setImmediate(true);
		cy.setSizeFull();

		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
		edgeArray = edges.toArray(edgeArray);

		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);		 
	 
		addComponent(cy);
	}
	
	private void getLimitCytoscapeObjectsNum() {
		if (limit_num == 0) {
			try {
				limit_num = Integer.parseInt(GeworkbenchRoot.getAppProperties()
						.getProperty(LIMIT_CYTOSCAPE_OBJECTS));
			} catch (NumberFormatException e) {
				log.warn("limit cytoscape objects value is not set properly.");
			}
			if (limit_num == 0)
				limit_num = DEFAULT_LIMIT_CYTOSCAPE_OBJECTS;

		}

	}

	@Override
	public PluginEntry getPluginEntry() {
		return new PluginEntry("Cytoscape", "Show network in cytoscape web, or in text view.");
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

}
