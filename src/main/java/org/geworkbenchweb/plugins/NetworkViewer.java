package org.geworkbenchweb.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.Network;
import org.geworkbenchweb.pojos.NetworkEdges;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.visualizations.Cytoscape;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

/* this used to be AracneResultsUI, but is in fact used by both ARACNe result and CNKB result. */
public class NetworkViewer extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(NetworkViewer.class);

	private static final String LIMIT_CYTOSCAPE_OBJECTS = "limit.cytoscape.objects";
	private static final int DEFAULT_LIMIT_CYTOSCAPE_OBJECTS = 2000;
	private static int limit_num = 0;
	
	private final Network networkResult;

	final private Long datasetId;
	private String messageLabel = null;
	
	public NetworkViewer(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) {
			networkResult = null;
			return;
		}

		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		if(id==null) { // pending node
			messageLabel = "Pending computation - ID "+ dataSetId;
			networkResult = null;
			return;
		}
		networkResult = FacadeFactory.getFacade().find(Network.class, id);
		if(networkResult==null) {
			messageLabel = "Error in retreiving the network information. Id="+id;
		}

		setImmediate(true);
		setSizeFull();		 
		getLimitCytoscapeObjectsNum();
	}
	
	public NetworkViewer(Network network) {
		datasetId = null;
		networkResult = network;
		setImmediate(true);
		setSizeFull();		 
		getLimitCytoscapeObjectsNum();
	}

	private static enum DisplayOption {UNDECIDED, TEXT, CYTOSCAPE};
	private volatile DisplayOption option = DisplayOption.UNDECIDED;
	
	@Override
	public void detach() {
		super.detach();
		option = DisplayOption.UNDECIDED;
	}
	
	@Override
	public void attach() {
		this.removeAllComponents();
		
		if(networkResult==null) {
			addComponent(new Label(messageLabel));
			return;
		}

		int edgeNumber = networkResult.getEdgeNumber();
		int nodeNumber = networkResult.getNodeNumber();
		if(edgeNumber + nodeNumber <= limit_num) {
			option=DisplayOption.CYTOSCAPE;
		}
		
		if(option==DisplayOption.CYTOSCAPE) {
			viewAsCytoscape();
		} else if(option==DisplayOption.TEXT) {
			viewAsText();
		} else if (option==DisplayOption.UNDECIDED) {
			String theMessage = "This network has "
					+ nodeNumber
					+ " nodes and "
					+ edgeNumber
					+ " edges, which may be too large to display in Cytoscape. \nAn alternate, text view is available instead.";

			MessageBox mb = new MessageBox(getWindow(), "Warning", null,
					theMessage, new MessageBox.ButtonConfig(
							MessageBox.ButtonType.CUSTOM1, "View as text"),
					new MessageBox.ButtonConfig(MessageBox.ButtonType.CUSTOM2,
							"View in Cytoscape", "150px"));
			mb.show(new MessageBox.EventListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClicked(ButtonType buttonType) {
					if (buttonType == ButtonType.CUSTOM1)
						option=DisplayOption.TEXT;
					else
						option=DisplayOption.CYTOSCAPE;
					attach();
				}
			});
			option = null; // prevent message box is shown again before the option is ready
		}
	}
	
	private void viewAsText()
	{
		Label area = new Label();
		
		MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");
		Command adjCommand = new Command() {

				private static final long serialVersionUID = -5284315483966959132L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					downloadNetwork();
				}
	       	};
	     Command sifCommand = new Command() {

				private static final long serialVersionUID = -5284315483966959132L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					downloadNetworkAsSIF();
				}
	       	};
		 MenuItem exportMenu = toolBar.addItem("Export", null,null);
	     exportMenu.addItem("Save as SIF", null,sifCommand);
		 exportMenu.addItem("Save as ADJ", null,adjCommand);
		
		
		int edgeNumber = networkResult.getEdgeNumber();
		int nodeNumber = networkResult.getNodeNumber();
		StringBuffer sb = new StringBuffer("This network has "
				+ nodeNumber + " nodes and " + edgeNumber
				+ " edges: \n\n");

		sb.append(networkResult.toString());
		area.setValue(sb.toString());
		area.setReadOnly(true);
		area.setContentMode(Label.CONTENT_PREFORMATTED);
		
		Panel panel = new Panel();
		panel.setSizeFull();
        panel.setStyleName(Reindeer.PANEL_LIGHT);
		
        panel.getContent().setSizeUndefined();
        
		addComponent(toolBar);
		
		panel.addComponent(area);
		 
		addComponent(panel);
		setExpandRatio(panel, 1);
	}

	final Cytoscape cy = new Cytoscape();

	private void viewAsCytoscape()
	{
		 
		/* Preparing data for cytoscape */
		ArrayList<String> nodes = new ArrayList<String>();
		ArrayList<String> edges = new ArrayList<String>();

		String[] node1s = networkResult.getNode1();
		NetworkEdges[] eds = networkResult.getEdges();
		for (int index=0; index<node1s.length; index++) {

			String[] node2s = eds[index].getNode2s();
			/* ignored weight here*/
			/* double[] weights = eds[index].getWeights(); */ 
			
			String id1 = node1s[index];
			for(int j=0; j< node2s.length; j++) {
				String id2 = node2s[j];
				String edge = id1 + "," + id2;

				String node1 = id1 + "," + id1 + ",0"; /* meant to be probeset ID, gene symbol*/
				String node2 = id2 + "," + id2 + ",0";

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
		}

		cy.setImmediate(true);
		cy.setSizeFull();

		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
		edgeArray = edges.toArray(edgeArray);

		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);
		cy.setColor(null);
	 
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
		layoutNames.addItem("concentric", layoutCommand);
       	layoutNames.addItem("grid", layoutCommand);
       	layoutNames.addItem("circle", layoutCommand);
       	layoutNames.addItem("breadthfirst", layoutCommand);
       	layoutNames.addItem("cose", layoutCommand);

       	Command adjCommand = new Command() {

			private static final long serialVersionUID = -5284315483966959132L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				downloadNetwork();
			}
       	};
       	Command sifCommand = new Command() {

			private static final long serialVersionUID = -5284315483966959132L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				downloadNetworkAsSIF();
			}
       	};
       	MenuItem exportMenu = toolBar.addItem("Export", null);
       	exportMenu.addItem("Save as ADJ", adjCommand);
       	exportMenu.addItem("Save as SIF", sifCommand);
       	MenuItem displayMenuItem = toolBar.addItem("Display", null);
       	Command ttestCommand = new Command() {

			private static final long serialVersionUID = 3164110221403962033L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				displayTTestresult();
			}};
       	Command resetCommand = new Command() {

			private static final long serialVersionUID = -6800631047305236790L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				resetDisplay();
			}};
		displayMenuItem.addItem("t-test", ttestCommand);
		displayMenuItem.addItem("reset", resetCommand);

       	layoutNames.setStyleName("plugin");
		
		this.setSpacing(true);
		this.addComponent(toolBar);
		this.addComponent(cy);
		this.setExpandRatio(cy, 1);
	}
	
	private void resetDisplay() {
		MessageBox mb = new MessageBox(getWindow(), "Under development", MessageBox.Icon.INFO,
				"This feature is not available yet.", new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
		mb.show();
	}

	private void displayTTestresult() {
		new ChooseTTestResultDialog().display(this);
	}

	private void downloadNetwork() {
		final Application app = getApplication();
		String dir = GeworkbenchRoot.getBackendDataDirectory()
				+ System.getProperty("file.separator")
				+ SessionHandler.get().getUsername()
				+ System.getProperty("file.separator") + "export";
		if (!new File(dir).exists())
			new File(dir).mkdirs();
		
		final File file = new File(dir, "network_" + System.currentTimeMillis()
				+ ".adj");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			pw.print(networkResult.toString());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file, app);
		app.getMainWindow().open(resource);
	}
	
	private void downloadNetworkAsSIF() {
		final Application app = getApplication();
		String dir = GeworkbenchRoot.getBackendDataDirectory()
				+ System.getProperty("file.separator")
				+ SessionHandler.get().getUsername()
				+ System.getProperty("file.separator") + "export";
		if (!new File(dir).exists())
			new File(dir).mkdirs();
		
		final File file = new File(dir, "network_" + System.currentTimeMillis()
				+ ".sif");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			pw.print(networkResult.toSIF());
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

	public void displayWithTTestResult(String choice) {
		// TODO
		System.out.println("Choice is made: " + choice);
		int nodeCount = cy.getNodes().length; // TODO this does not match networkResult.getNodeNumber();
		String colors[] = new String[nodeCount];
		for (int i = 0; i < nodeCount; i++)
			colors[i] = String.format("#%06X", (int) (Math.random() * 16777215));
		cy.setColor(colors);
	}

}
