package org.geworkbenchweb.layout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.MarkUsResultDataSet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.analysis.CNKB.ui.UCNKBTab;
import org.geworkbenchweb.analysis.hierarchicalclustering.ui.UClustergramTab;
import org.geworkbenchweb.analysis.markus.ui.UMarkusDataTab;
import org.geworkbenchweb.visualizations.Cytoscape;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class UVisualPlugin extends TabSheet implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID 				= 	1L;

	private static final String DATA_OPERATIONS 			= 	"Data Operations";

	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";

	private static final String MARKER_HEADER 				= 	"Marker";

	private static final String HEAT_MAP 					= 	"Heat Map";

	private Table dataTable;

	private DSMicroarraySet maSet;

	private UDataTab dataOp;

	private UHeatMap heatMap;

	private Cytoscape cy;
	
	private UClustergramTab dendrogramTab;
	
	private Object dataSet;

	public UVisualPlugin(Object dataSet, String dataType, String action) {

		this.dataSet = dataSet;

		addListener(this);
		setSizeFull();
		setStyleName(Reindeer.TABSHEET_SMALL);

		if(dataType.contentEquals("Expression File")) {

			maSet 			= 	(DSMicroarraySet) dataSet;
			dataOp			= 	new UDataTab(maSet, action);
			heatMap			=	new UHeatMap(maSet);
			dataTable 		= 	new Table();


			dataOp.setCaption(DATA_OPERATIONS);
			addTab(dataOp); 

			dataTable.setSizeFull();
			dataTable.setImmediate(true);
			dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
			dataTable.setContainerDataSource(tabularView(maSet));
			dataTable.setColumnWidth(MARKER_HEADER, 150);

			addTab(dataTable);

			heatMap.setCaption(HEAT_MAP);
			heatMap.setStyleName(Reindeer.LAYOUT_WHITE);

			addTab(heatMap);

		} else if(dataType.equalsIgnoreCase("CNKB")) {

			@SuppressWarnings("unchecked")
			Vector<CellularNetWorkElementInformation> hits 	=	(Vector<CellularNetWorkElementInformation>) dataSet;
			UCNKBTab cnkbTab 								= 	new UCNKBTab(hits);

			addTab(cnkbTab, "CNKB Results", null);		

			/* Preparing data for cytoscape */
			ArrayList<String> nodes = new ArrayList<String>();
			ArrayList<String> edges = new ArrayList<String>();

			for(CellularNetWorkElementInformation cellular: hits) {

				try {


					InteractionDetail[] interactions = cellular.getInteractionDetails();

					if(interactions.length != 0) {

						for(InteractionDetail interaction: interactions) {

							
							String edge = cellular.getdSGeneMarker().getGeneName() 
									+ ","
									+ interaction.getdSGeneName();

							String node1 = 	cellular.getdSGeneMarker().getGeneName()
									+ ","
									+ cellular.getdSGeneMarker().getGeneName();

							String node2 =	interaction.getdSGeneName()
									+ ","
									+ interaction.getdSGeneName();

							if(edges.isEmpty()) {

								edges.add(edge);

							}else if(!edges.contains(edge)) {

								edges.add(edge);
							}

							if(node1 == node2) {

								if(!nodes.contains(node1 + ",1")){
									
									nodes.add(node1+",1");
									
									if(nodes.contains(node1+",0")) {
										nodes.remove(node1 + ",0");
									}
								
								}else if(!nodes.contains(node1 + ",0")) {
									
									nodes.add(node1+",0");
								}
								
							}else if(nodes.isEmpty()) {


								nodes.add(node1 + ",1");
								nodes.add(node2 + ",0");

							} else { 

								if(!nodes.contains(node1 + ",1")) {

									nodes.add(node1 + ",1");
									if(nodes.contains(node1+",0")) {
										nodes.remove(node1 + ",0");
									}

								}

								if(!nodes.contains(node2 + ",1")) {	

									if(!nodes.contains(node2 + ",0")) {

										nodes.add(node2 + ",0");

									}
								}
							}
						}
					}
				}catch (Exception e) {

				}
			}	

			cy = new Cytoscape();

			cy.setImmediate(true);
			cy.setSizeFull();
			cy.setCaption("Cytoscape");

			String[] nodeArray = new String[nodes.size()];
			String[] edgeArray = new String[edges.size()];

			nodeArray = nodes.toArray(nodeArray);
			edgeArray = edges.toArray(edgeArray);

			cy.setNodes(nodeArray);
			cy.setEdges(edgeArray);
			cy.setNetwork("false");

			addTab(cy);

		} else if(dataType.equalsIgnoreCase("Hierarchical Clustering")){

			CSHierClusterDataSet results 	=  	(CSHierClusterDataSet) dataSet;
			dendrogramTab 					= 	new UClustergramTab(results);

			addTab(dendrogramTab, "Dendrogram", null);		

		} else if(dataType.equalsIgnoreCase("ARACne")) {

			AdjacencyMatrixDataSet adjMatrix = (AdjacencyMatrixDataSet) dataSet; 

			/* Preparing data for cytoscape */
			ArrayList<String> nodes = new ArrayList<String>();
			ArrayList<String> edges = new ArrayList<String>();

			for(int i=0; i<adjMatrix.getMatrix().getEdges().size(); i++) {

				String edge 	= 	adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel() 
						+ "," 
						+ adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel(); 


				String node1 	= adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel() 
						+ "," 
						+ adjMatrix.getMatrix().getEdges().get(i).node1.marker.getGeneName(); 

				String node2 	= adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel()
						+ ","
						+ adjMatrix.getMatrix().getEdges().get(i).node2.marker.getGeneName(); 


				if(edges.isEmpty()) {

					edges.add(edge);

				}else if(!edges.contains(edge)) {

					edges.add(edge);
				}

				if(nodes.isEmpty()) {

					nodes.add(node1);
					nodes.add(node2);

				} else { 

					if(!nodes.contains(node1)) {

						nodes.add(node1);

					}

					if(!nodes.contains(node2)) {

						nodes.add(node2);

					}
				}

			}

			cy = new Cytoscape();
			cy.setCaption("Cytoscape");
			cy.setImmediate(true);
			cy.setSizeFull();

			String[] nodeArray = new String[nodes.size()];
			String[] edgeArray = new String[edges.size()];

			nodeArray = nodes.toArray(nodeArray);
			edgeArray = edges.toArray(edgeArray);

			cy.setNodes(nodeArray);
			cy.setEdges(edgeArray);

			addTab(cy);

		}else if(dataType.equals("PDB File")) {

			DSProteinStructure prtset = (DSProteinStructure) dataSet;
			UMarkusDataTab datatab = new UMarkusDataTab(prtset, action);
			datatab.setCaption(DATA_OPERATIONS);
			addTab(datatab); 

		}else if (dataType.equals("MarkUs")){
			MarkUsResultDataSet resultset = (MarkUsResultDataSet)dataSet;
			String results = resultset.getResult();

			VerticalLayout layout = new VerticalLayout();
			Embedded browser = new Embedded("", new ExternalResource(MARKUS_RESULT_URL+results));
			browser.setType(Embedded.TYPE_BROWSER);
			browser.setSizeFull();
			layout.addComponent(browser);
			layout.setWidth("100%");
			layout.setHeight("100%");
			
			addTab(layout);
		}
	}

	private static final String MARKUS_RESULT_URL = "http://bhapp.c2b2.columbia.edu/MarkUs/cgi-bin/browse.pl?pdb_id=";

	public static IndexedContainer tabularView(DSMicroarraySet maSet) {

		String[] colHeaders 			= 	new String[(maSet.size())+1];
		IndexedContainer dataIn 		= 	new IndexedContainer();

		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	dataIn.addItem(j);

			for(int k=0;k<=maSet.size();k++) {

				if(k == 0) {

					colHeaders[k] 		= 	MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getMarkers().get(j).getLabel());

				} else {

					colHeaders[k] 		= 	maSet.get(k-1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class, null);
					item.getItemProperty(colHeaders[k]).setValue((float) maSet.getValue(j, k-1));

				}
			}
		}

		return dataIn;

	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {

		UMenuBar menu = new UMenuBar();

		try {
			TabSheet tabsheet = event.getTabSheet();
			Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
			if (tab.getCaption().equalsIgnoreCase("Tabular Microarray Viewer")) {

				final MenuBar.MenuItem save = menu.addItem("Save As", null);
				save.addItem("Excel Sheet", exportTable);
				save.addItem("CSV File", exportTable);

			}else if(tab.getCaption().equalsIgnoreCase("Data Operations")) {


			}else if(tab.getCaption().equalsIgnoreCase("Heat Map")) {

				//final MenuBar.MenuItem save = menu.addItem("Save As", null);
				//save.addItem("SVG", null);

			}else if(tab.getCaption().equalsIgnoreCase("Cytoscape")) {

				final MenuBar.MenuItem save = menu.addItem("Save Network As", null);
				save.addItem("SVG", exportCytoscapeSVG);
				save.addItem("PNG", exportCytoscapePNG);

			}else if(tab.getCaption().equalsIgnoreCase("CNKB Results")) {

				final MenuBar.MenuItem save 	= 	menu.addItem("Save", null);
				final MenuBar.MenuItem graph 	= 	save.addItem("Throttle Graph As", null);

				graph.addItem("SVG", exportGraph);

				menu.addItem("Print Graph", exportGraph);
				save.addSeparator();

				final MenuBar.MenuItem table = save.addItem("TableData As", null);
				table.addItem("Excel Sheet", cnkbTableExport);
				table.addItem("CSV File", cnkbTableExport);

			}else if(tab.getCaption().equalsIgnoreCase("Dendrogram")) {
				
				@SuppressWarnings("unused")
				final MenuBar.MenuItem save 	= 	menu.addItem("Reset Dendrogram", resetDendrogram);

			}


		}catch (Exception e) {

			//TODO

		}

	}
	
	private Command resetDendrogram = new Command() {

		private static final long serialVersionUID = 1L;

		@Override
		public void menuSelected(MenuItem selectedItem) {
			
			removeComponent(dendrogramTab);

			CSHierClusterDataSet results 	=  	(CSHierClusterDataSet) dataSet;
			dendrogramTab 					= 	new UClustergramTab(results);

			addTab(dendrogramTab, "Dendrogram", null);	

		}

	};
	
	private Command exportCytoscapeSVG = new Command() {

		private static final long serialVersionUID = 1L;

		@Override
		public void menuSelected(MenuItem selectedItem) {

			cy.setNetworkSVG("true");

		}

	};

	private Command exportCytoscapePNG = new Command() {

		private static final long serialVersionUID = 1L;

		@Override
		public void menuSelected(MenuItem selectedItem) {

			cy.setNetwork("true");

		}

	};

	private Command exportTable = new Command() {

		private static final long serialVersionUID = 1L;

		public void menuSelected(MenuItem selectedItem) {

			if(selectedItem.getText().equalsIgnoreCase("Excel Sheet")) {

				ExcelExport excelExport = new ExcelExport(dataTable);
				excelExport.excludeCollapsedColumns();
				excelExport.export();

			}else {

				CsvExport csvExport = new CsvExport(dataTable);
				csvExport.excludeCollapsedColumns();
				csvExport.setExportFileName("MicroarrayTableData.csv");
				csvExport.export();

			}
		}
	};

	private Command cnkbTableExport = new Command() {

		private static final long serialVersionUID = 1L;

		public void menuSelected(MenuItem selectedItem) {

			if(selectedItem.getText().equalsIgnoreCase("Excel Sheet")) {

				UCNKBTab.exportInteractionTable("excel");

			}else {

				UCNKBTab.exportInteractionTable("csv");

			}
		}
	};

	private Command exportGraph = new Command() {

		private static final long serialVersionUID = 1L;

		public void menuSelected(MenuItem selectedItem) {

			if(selectedItem.getText().equalsIgnoreCase("SVG")) {

				UCNKBTab.plot.addListener(new InvientCharts.ChartSVGAvailableListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void svgAvailable(
							ChartSVGAvailableEvent chartSVGAvailableEvent) {

						exportSVG(chartSVGAvailableEvent.getSVG());
					}
				});

			}else {

				UCNKBTab.plot.print();

			}

		}
	};

	@SuppressWarnings("unused")
	private Command exportHeatMap = new Command() {

		private static final long serialVersionUID = 1L;

		@Override
		public void menuSelected(MenuItem selectedItem) {

			UHeatMap.exportHeat();

		}

	};

	public void exportSVG(String image) {

		BufferedWriter fos = null;
		File tempFile = null;
		try {
			tempFile 	= 	File.createTempFile("tmp", ".svg");
			fos 		= 	new BufferedWriter(new FileWriter(tempFile));

			fos.write(image);
			fos.flush();  

		}
		catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		@SuppressWarnings("unused")
		String downloadFileName = "ThrottleGraph.svg";
		@SuppressWarnings("unused")
		String contentType = "image/svg+xml";
		
			//TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(getApplication(), downloadFileName, contentType, tempFile);
			//Root..open(resource, "_self");
		

	}

}
