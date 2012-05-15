package org.geworkbenchweb.layout;

import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class USetsTabSheet extends TabSheet{

	private static final long serialVersionUID = 1L;
	
	private DSMicroarraySet maSet;

	private TreeTable markerSets;

	private TreeTable arraySets;
	
	private static final String MARKER_HEADER 	= 	"Marker";
	
	private VerticalLayout l1;
	
	private VerticalLayout l2;
	
	private static USetsTabSheet setsTabSheetInstance;
	
	public static USetsTabSheet getSetsTabSheetObject() {
		if (setsTabSheetInstance == null) {
			setsTabSheetInstance= new USetsTabSheet();
		}
		
		return setsTabSheetInstance;
	}

	public USetsTabSheet() {
		
		setStyleName(Reindeer.TABSHEET_SMALL);
		setSizeFull();
		
		l1 = new VerticalLayout();
		l2 = new VerticalLayout();

		l1.setSizeFull();
		l1.setImmediate(true);
		l1.setStyleName(Reindeer.LAYOUT_WHITE);
		l2.setSizeFull();
		l2.setImmediate(true);
		l2.setStyleName(Reindeer.LAYOUT_WHITE);

		addTab(l1, "Marker Sets");
		addTab(l2, "Array Sets");
	
	}
	
	public void populateTabSheet(DSMicroarraySet dataSet) {
		
			this.maSet = dataSet;
			
			this.l1.removeAllComponents();
			this.l2.removeAllComponents();

			markerSets = new TreeTable();
			markerSets.setImmediate(true);
			markerSets.setSelectable(true);
			markerSets.addListener(new Property.ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {

					try {
						Item itemSelected = markerSets.getItem(event.getProperty().getValue());

						if(SubSetOperations.checkForDataSet(itemSelected.toString())) {

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
							UVisualPlugin.resetTableContainer(dataIn);
						}
					}catch (Exception e) {

						UVisualPlugin.resetOriginalView(maSet);

					}
				}
			});

			arraySets = new TreeTable();
			arraySets.setImmediate(true);
			arraySets.setSelectable(true);
			arraySets.addListener(new Property.ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(ValueChangeEvent event) {

					try {
						Item itemSelected = arraySets.getItem(event.getProperty().getValue());

						if(SubSetOperations.checkForDataSet(itemSelected.toString())) {

							String positions 		=	 getArrayData(itemSelected.toString(), maSet);
							String[] temp 			=   (positions.substring(1, positions.length()-1)).split(",");

							String[] colHeaders 			= 	new String[(temp.length)+1];
							IndexedContainer dataIn 		= 	new IndexedContainer();

							for(int j=0; j<maSet.getMarkers().size();j++) {

								Item item 					= 	dataIn.addItem(j);

								for(int i=0; i<=temp.length; i++) {

									if(i == 0) {

										colHeaders[i] 		= 	MARKER_HEADER;
										dataIn.addContainerProperty(colHeaders[i], String.class, null);
										item.getItemProperty(colHeaders[i]).setValue(maSet.getMarkers().get(j));

									} else {

										colHeaders[i] 		= 	maSet.get(Integer.parseInt(temp[i-1].trim())).toString();
										dataIn.addContainerProperty(colHeaders[i], Float.class, null);
										item.getItemProperty(colHeaders[i]).setValue(maSet.getValue(j, Integer.parseInt(temp[i-1].trim())));

									}
								}
							}

							UVisualPlugin.resetTableContainer(dataIn);
						}
					}catch (Exception e) {

						UVisualPlugin.resetOriginalView(maSet);
					}
				}
			});

			markerSetContainer(SubSetOperations.getMarkerSets(DataSetOperations.getDataSetID(maSet.getDataSetName())), maSet);
			arraySetContainer(SubSetOperations.getArraySets(DataSetOperations.getDataSetID(maSet.getDataSetName())), maSet);
		
			l1.addComponent(markerSets);
			l2.addComponent(arraySets);
		
	}
	
	private void arraySetContainer(List<?> list, DSMicroarraySet maSet) {

		arraySets.removeAllItems();
		arraySets.setSizeFull();
		arraySets.addContainerProperty("Name", String.class, "");
		arraySets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		
		if(!list.isEmpty()) {

			for(int i=0; i<list.size(); i++ ) {

				String name 		= 	((SubSet) list.get(i)).getName();
				String positions 	= 	(((SubSet) list.get(i)).getPositions()).trim();
				Object item 		= 	arraySets.addItem(new Object[] { name }, null);

				String[] temp =  (positions.substring(1, positions.length()-1)).split(",");

				for(int j = 0; j<temp.length; j++) {

					Object subItem = arraySets.addItem(new Object[] { maSet.get(Integer.parseInt(temp[j].trim())).getLabel() }, null);
					arraySets.setChildrenAllowed(subItem, false);
					arraySets.setParent(subItem, item);

				}

			}
		
		}

	}


	private void markerSetContainer(List<?> list, DSMicroarraySet maSet) {

		markerSets.removeAllItems();
		markerSets.setSizeFull();
		markerSets.addContainerProperty("Name", String.class, "");
		markerSets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		
		if(list.size() != 0) {	

			for(int i=0; i<list.size(); i++ ) {

				String name 		= 	((SubSet) list.get(i)).getName();
				String positions 	= 	(((SubSet) list.get(i)).getPositions()).trim();
				Object item 		= 	markerSets.addItem(new Object[] { name }, null);

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
			
		}

	}
	public void removeData() {
		
		l1.removeAllComponents();
		l2.removeAllComponents();
		
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

	/**
	 * Create Dataset for selected markerSet 
	 */
	public String getArrayData(String setName, DSMicroarraySet parentSet) {

		@SuppressWarnings("rawtypes")
		List subSet 		= 	SubSetOperations.getArraySet(setName);
		String positions 	= 	(((SubSet) subSet.get(0)).getPositions()).trim();
		
		return positions;
	}
}
