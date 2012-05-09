package org.geworkbenchweb.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.analysis.CNKB.ui.UCNKBTab;
import org.geworkbenchweb.analysis.hierarchicalclustering.ui.UClustergramTab;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.visualizations.Cytoscape;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

public class UVisualPlugin extends TabSheet implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID 				= 	1L;

	private Table dataTable;

	private static final String DATA_OPERATIONS 			= 	"Data Operations";

	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";

	private static final String MARKER_HEADER 				= 	"Marker";

	private DSMicroarraySet maSet;

	private TreeTable markerSets;

	private TreeTable arraySets;

	public UVisualPlugin(Object dataSet, String dataType, String action) {

		addListener(this);
		setSizeFull();
		setStyleName(Reindeer.TABSHEET_SMALL);
		
		if(dataType.contentEquals("Expression File")) {

			HorizontalSplitPanel viewPanel 	= 	new HorizontalSplitPanel(); 
			VerticalSplitPanel 	setLayout	=	new VerticalSplitPanel();
			maSet 							= 	(DSMicroarraySet) dataSet;
			UDataTab dataOp					= 	new UDataTab(maSet, action);
			dataTable 						= 	new Table();

			
			dataOp.setCaption(DATA_OPERATIONS);
			dataOp.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			addTab(dataOp); 

			setLayout.setImmediate(true);
			setLayout.setSizeFull();
			setLayout.setSplitPosition(50);
			setLayout.setLocked(true);
			
			markerSets = new TreeTable();
			markerSets.setImmediate(true);
			markerSets.addStyleName("borderless");
			markerSets.setSelectable(true);
			markerSets.addListener(new Property.ValueChangeListener() {
			     
				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {

					Item itemSelected = markerSets.getItem(event.getProperty().getValue());
					
					if(SubSetOperations.checkForDataSet(itemSelected.toString())) {
						
						dataTable.removeAllItems();
						String positions 			=	 getMarkerData(itemSelected.toString(), maSet);
						String[] temp 				=   (positions.substring(1, positions.length()-1)).split(",");
						String[] colHeaders 		= 	new String[(maSet.size())+1];
						IndexedContainer dataIn 	= 	new IndexedContainer();

						for(int j=0; j<temp.length; j++) {

							Item item 				= 	dataIn.addItem(j);

							for(int k=0;k<=maSet.size();k++) {

								if(k == 0) {

									colHeaders[k] 	= 	MARKER_HEADER;
									dataIn.addContainerProperty(colHeaders[k], String.class, null);
									item.getItemProperty(colHeaders[k]).setValue(maSet.getMarkers().get(Integer.parseInt(temp[j].trim())));


								} else {

									colHeaders[k] 	= 	maSet.get(k-1).toString();
									dataIn.addContainerProperty(colHeaders[k], Float.class, null);
									item.getItemProperty(colHeaders[k]).setValue(maSet.getValue(Integer.parseInt(temp[j].trim()), k-1));

								}
							}
						}
						dataTable.setContainerDataSource(dataIn);
						dataTable.setColumnWidth(MARKER_HEADER, 150);
					}
	            }
	        });
			
			arraySets = new TreeTable();
			arraySets.setImmediate(true);
			arraySets.addStyleName("borderless");
			arraySets.setSelectable(true);
			arraySets.addListener(new Property.ValueChangeListener() {
	     
				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {

	            	System.out.println(event.getProperty().getValue().toString());
	            }
	        });
			
			markerSetContainer(SubSetOperations.getMarkerSets(DataSetOperations.getDataSetID(maSet.getDataSetName())), maSet);
			arraySetContainer(SubSetOperations.getArraySets(DataSetOperations.getDataSetID(maSet.getDataSetName())), maSet);

			setLayout.setFirstComponent(markerSets);
			setLayout.setSecondComponent(arraySets);
			
			dataTable.setStyleName("small striped");
			dataTable.setSizeFull();
			dataTable.setImmediate(true);

			viewPanel.setSplitPosition(250, Sizeable.UNITS_PIXELS);
			viewPanel.setSizeFull();
			viewPanel.setImmediate(true);
			viewPanel.setStyleName("small previews");
			viewPanel.setCaption(MICROARRAY_TABLE_CAPTION);
			viewPanel.setIcon(new ThemeResource("../runo/icons/16/document-web.png"));
			viewPanel.setFirstComponent(setLayout);
			viewPanel.setSecondComponent(dataTable);

			addTab(viewPanel);

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
					for(InteractionDetail interaction: interactions) {

						String edge = interaction.getdSGeneMarker1() 
								+ ","
								+ interaction.getdSGeneMarker2();

						String node1 = 	interaction.getdSGeneMarker1()
								+ ","
								+ interaction.getdSGeneName1();

						String node2 =	interaction.getdSGeneMarker2()
								+ ","
								+ interaction.getdSGeneName2();

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

				}catch (Exception e) {

					//TODO: Handle Null pointer exception
				}
			}	


			Cytoscape cy = new Cytoscape();

			cy.setImmediate(true);
			cy.setSizeFull();
			cy.setCaption("Cytoscape");

			String[] nodeArray = new String[nodes.size()];
			String[] edgeArray = new String[edges.size()];

			nodeArray = nodes.toArray(nodeArray);
			edgeArray = edges.toArray(edgeArray);

			cy.setNodes(nodeArray);
			cy.setEdges(edgeArray);

			addTab(cy);

		} else if(dataType.equalsIgnoreCase("Hierarchical Clustering")){

			CSHierClusterDataSet results 	=  	(CSHierClusterDataSet) dataSet;
			UClustergramTab dendrogramTab 	= 	new UClustergramTab(results);
			CSMicroarraySet	data			= 	(CSMicroarraySet) results.getParentDataSet();

			//Height and width of the visualization are calculted based on number of phenotypes and markers
			setHeight(((data.getMarkers().size()*5) + 600) + "px");
			setWidth(((data.size()*20) + 600) + "px");
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

			Cytoscape cy = new Cytoscape();
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

		}
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {

		if(event.getTabSheet().getSelectedTab().getCaption() == MICROARRAY_TABLE_CAPTION){

			dataTable.setContainerDataSource(tabularView(maSet));
			dataTable.setColumnWidth(MARKER_HEADER, 150);

		}
	} 

	public IndexedContainer tabularView(DSMicroarraySet maSet) {

		String[] colHeaders 			= 	new String[(maSet.size())+1];
		IndexedContainer dataIn 		= 	new IndexedContainer();

		for(int j=0; j<maSet.getMarkers().size();j++) {

			Item item 					= 	dataIn.addItem(j);

			for(int k=0;k<=maSet.size();k++) {

				if(k == 0) {

					colHeaders[k] 		= 	MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getMarkers().get(j));

				} else {

					colHeaders[k] 		= 	maSet.get(k-1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getValue(j, k-1));

				}
			}
		}

		return dataIn;

	}

	private void arraySetContainer(List<?> list, DSMicroarraySet maSet) {

		arraySets.removeAllItems();
		arraySets.setSizeFull();
		arraySets.addContainerProperty("Name", String.class, "");
		arraySets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		Object superItem = arraySets.addItem(new Object[] { "Array Sets" }, null);
		
		if(!list.isEmpty()) {

			for(int i=0; i<list.size(); i++ ) {

				String name 		= 	((SubSet) list.get(i)).getName();
				String positions 	= 	(((SubSet) list.get(i)).getPositions()).trim();
				Object item 		= 	arraySets.addItem(new Object[] { name }, null);

				arraySets.setParent(item, superItem);
				String[] temp =  (positions.substring(1, positions.length()-1)).split(",");

				for(int j = 0; j<temp.length; j++) {

					Object subItem = arraySets.addItem(new Object[] { maSet.get(Integer.parseInt(temp[j].trim())).getLabel() }, null);
					arraySets.setChildrenAllowed(subItem, false);
					arraySets.setParent(subItem, item);

				}

			}
			
			arraySets.setCollapsed(superItem, false);
		
		}

	}


	private void markerSetContainer(List<?> list, DSMicroarraySet maSet) {

		markerSets.removeAllItems();
		markerSets.setSizeFull();
		markerSets.addContainerProperty("Name", String.class, "");
		markerSets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		Object superItem = markerSets.addItem(new Object[] { "Marker Sets" }, null);
		
		if(list.size() != 0) {	

			for(int i=0; i<list.size(); i++ ) {

				String name 		= 	((SubSet) list.get(i)).getName();
				String positions 	= 	(((SubSet) list.get(i)).getPositions()).trim();
				Object item 		= 	markerSets.addItem(new Object[] { name }, null);

				markerSets.setParent(item, superItem);
				String[] temp =  (positions.substring(1, positions.length()-1)).split(",");

				for(int j = 0; j<temp.length; j++) {

					Object subItem = markerSets.addItem(new Object[] { maSet.getMarkers().get(Integer.parseInt(temp[j].trim())).getLabel() 
							+ " (" 
							+ maSet.getMarkers().get(Integer.parseInt(temp[j].trim())).getGeneName()
							+ ")" 
								}, null);
					markerSets.setChildrenAllowed(subItem, false);
					markerSets.setParent(subItem, item);
				}

			}
			markerSets.setCollapsed(superItem, false);
		}

	}
	
	/**
	 * Create Dataset for selected markerSet 
	 */
	public String getMarkerData(String setName, DSMicroarraySet parentSet) {

		@SuppressWarnings("rawtypes")
		List subSet 		= 	SubSetOperations.getMarkerSet(setName);
		String positions 	= 	(((SubSet) subSet.get(0)).getPositions()).trim();
		
		return positions;
	}

}
