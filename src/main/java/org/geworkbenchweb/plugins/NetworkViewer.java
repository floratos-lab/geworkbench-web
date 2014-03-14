package org.geworkbenchweb.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Cytoscape;

import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/* this used to be AracneResultsUI, but is in fact used by both ARACNe result and CNKB result. */
public class NetworkViewer extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(NetworkViewer.class);

	private static final String LIMIT_CYTOSCAPE_OBJECTS = "limit.cytoscape.objects";
	private static final int DEFAULT_LIMIT_CYTOSCAPE_OBJECTS = 5000;
	private static int limit_num = 0;
	
	// TODO final is remove due to jdk compile problem, need to fix the real problem
	private AdjacencyMatrix adjMatrix; 	

	final private Long datasetId;
	
	public NetworkViewer(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) {
			adjMatrix = null;
			return;
		}

		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			adjMatrix = null;
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			adjMatrix = null;
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			adjMatrix = null;
			return;
		}
		if(! (object instanceof AdjacencyMatrix)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			addComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			adjMatrix = null;
			return;
		}
		adjMatrix = (AdjacencyMatrix) object;

		setImmediate(true);
		setSizeFull();		 
		getLimitCytoscapeObjectsNum();
	}
	
	@Override
	public void attach() {
		this.removeAllComponents();
		
		if(adjMatrix==null) return;
		
		int edgeNumber = adjMatrix.getConnectionNo();
		int nodeNumber = adjMatrix.getNodeNumber();
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
		} else {
			viewAsCytoscape();
		}
	}
	
	static private String getExportName(AdjacencyMatrix.Node node) {
		if (node.type == NodeType.MARKER) {
			return node.marker.getLabel();
		} else {
			return node.stringId;
		}
	}

	private void viewAsText()
	{
		TextArea area = new TextArea();
		area.setSizeFull();
	
		area.setWordwrap(false);
		 
		area.setImmediate(true);
		int edgeNumber = adjMatrix.getConnectionNo();
		int nodeNumber = adjMatrix.getNodeNumber();
		StringBuffer sb = new StringBuffer("This network has "
				+ nodeNumber + " nodes and " + edgeNumber
				+ " edges: \n\n");
        
       
		for (AdjacencyMatrix.Node node1 : adjMatrix.getNodes()) {
			
			sb.append(getExportName(node1) + "\t");

			for (AdjacencyMatrix.Edge edge : adjMatrix
					.getEdges(node1)) {
				sb.append(getExportName(edge.node2)
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

       	Command exportCommand = new Command() {

			private static final long serialVersionUID = -5284315483966959132L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				downloadNetwork();
			}
       	};
       	toolBar.addItem("Export", exportCommand); /* ignore return value */

       	layoutNames.setStyleName("plugin");
		
		this.setSpacing(true);
		this.addComponent(toolBar);
		this.addComponent(cy);
		this.setExpandRatio(cy, 1);
	}
	
	private void downloadNetwork() {
		final Application app = getApplication();
		final File file = new File("network_" + System.currentTimeMillis()
				+ ".adj");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			for (AdjacencyMatrix.Node node1 : adjMatrix.getNodes()) {
				pw.write(getExportName(node1) + "\t");

				for (AdjacencyMatrix.Edge edge : adjMatrix.getEdges(node1)) {
					pw.write(getExportName(edge.node2) + "\t" + edge.info.value
							+ "\t");
				}
				pw.write("\n");
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file, app);
		app.getMainWindow().open(resource);
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
	public Long getDatasetId() {
		return datasetId;
	}

}
