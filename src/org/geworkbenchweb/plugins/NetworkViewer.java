package org.geworkbenchweb.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Cytoscape;

import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;
import de.steinwedel.messagebox.MessageBoxListener;

/* this used to be AracneResultsUI, but is in fact used by both ARACNe result and CNKB result. */
public class NetworkViewer extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(NetworkViewer.class);

	private static final String LIMIT_CYTOSCAPE_OBJECTS = "limit.cytoscape.objects";
	private static final int DEFAULT_LIMIT_CYTOSCAPE_OBJECTS = 5000;
	private static int limit_num = 0;
	
 
	private AdjacencyMatrixDataSet adjMatrixDataSet = null;

	final private Long datasetId;
	
	public NetworkViewer(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;

		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		}
		if(! (object instanceof AdjacencyMatrixDataSet)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			addComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			return;
		}
		adjMatrixDataSet = (AdjacencyMatrixDataSet) object;

		setImmediate(true);
		setSizeFull();		 
		getLimitCytoscapeObjectsNum();

		// the following code used to be attach - which is not really necessary and is not consistent with other plug-ins
		int edgeNumber = adjMatrixDataSet.getMatrix().getConnectionNo();
		int nodeNumber = adjMatrixDataSet.getMatrix().getNodeNumber();
		if ((edgeNumber + nodeNumber) > limit_num) {
			String theMessage = "This network has "
					+ nodeNumber
					+ " nodes and "
					+ edgeNumber
					+ " edges, which may be too large to display in Cytoscape. \nAn alternate, text view is available instead.";

			MessageBox mb = MessageBox.showPlain(Icon.WARN, "Warning",
					theMessage, 
					new MessageBoxListener() {

						@Override
						public void buttonClicked(ButtonId buttonId) {
							if (buttonId == ButtonId.CUSTOM_1)  
								viewAsText();					    
							else 
								viewAsCytoscape();
						}
					},
					ButtonId.CUSTOM_1,
					ButtonId.CUSTOM_2);
			mb.getButton(ButtonId.CUSTOM_1).setCaption("View as text");
			mb.getButton(ButtonId.CUSTOM_2).setCaption("View in Cytoscape");
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

		final Cytoscape cy = new Cytoscape();
		cy.setImmediate(true);
		cy.setSizeFull();

		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
		edgeArray = edges.toArray(edgeArray);

		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);		 
	 
       	Command layoutCommand = new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				cy.setLayout(selectedItem.getText());
				cy.requestRepaint();
			}
       		
       	};
		MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");
		MenuItem layoutNames = toolBar.addItem("Layout managers", null);
		layoutNames.addItem("ForceDirected", layoutCommand);
       	layoutNames.addItem("Circle", layoutCommand);
       	layoutNames.addItem("Radial", layoutCommand);
       	layoutNames.addItem("Tree", layoutCommand);
       	layoutNames.addItem("CompoundSpringEmbedder", layoutCommand);

       	layoutNames.setStyleName("plugin");
		
		this.setSpacing(true);
		this.addComponent(toolBar);
		this.addComponent(cy);
		this.setExpandRatio(cy, 1);
	}
	
	private void getLimitCytoscapeObjectsNum() {
		if (limit_num == 0) {
			try {
				limit_num = Integer.parseInt(GeworkbenchRoot.getAppProperty(LIMIT_CYTOSCAPE_OBJECTS));
			} catch (NumberFormatException e) {
				log.warn("limit cytoscape objects value is not set properly.");
			}
			if (limit_num == 0)
				limit_num = DEFAULT_LIMIT_CYTOSCAPE_OBJECTS;

		}

	}

	@Override
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

}
