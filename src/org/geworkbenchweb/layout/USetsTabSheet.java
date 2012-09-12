package org.geworkbenchweb.layout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.util.Util;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.Ostermiller.util.ExcelCSVParser;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

/**
 * USetsTabSheet loads the Marker and Phenotype sets for the dataSet selected
 * @author np2417
 */
public class USetsTabSheet extends TabSheet {

	private static final long serialVersionUID = 1L;

	private DSMicroarraySet maSet;

	private TreeTable markerSets;

	private TreeTable arraySets;

	private VerticalLayout l1;

	private VerticalLayout l2;

	private UploadField uploadField = null;

	public USetsTabSheet() {

		setSizeFull();

		l1 	= 	new VerticalLayout();
		l2 	= 	new VerticalLayout();

		l1.setSizeFull();
		l1.setImmediate(true);
		l1.setStyleName(Reindeer.LAYOUT_WHITE);
		l2.setSizeFull();
		l2.setImmediate(true);
		l2.setStyleName(Reindeer.LAYOUT_WHITE);

		addTab(l1, "Marker Sets");
		addTab(l2, "Array Sets");

	}

	public void populateTabSheet(DSMicroarraySet dataSet, final Long dataSetId) {

		l1.removeAllComponents();
		l2.removeAllComponents();
		
		if(dataSet == null) {
			System.out.println("Some how dataset supplied is empty. Check the code !!");
		}else {
			
			this.maSet = dataSet;
			
			markerSets = new TreeTable();
			markerSets.setImmediate(true);
			markerSets.setSelectable(true);
			markerSets.addActionHandler(new Action.Handler() {

				private static final long serialVersionUID = 1L;

				public Action[] getActions(Object target, Object sender) {
					return null;
				}
				
				public void handleAction(Action action, Object sender, Object target) {	
				}
			});
			
			markerSets.addListener(new ItemClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void itemClick(ItemClickEvent event) {
					
				}
				
			});

			arraySets = new TreeTable();
			arraySets.setImmediate(true);
			arraySets.setSelectable(true);
			arraySets.addActionHandler(new Action.Handler() {

				private static final long serialVersionUID = 1L;

				public Action[] getActions(Object target, Object sender) {
					return null;
				}
				
				public void handleAction(Action action, Object sender, Object target) {	
				}
			});
			arraySets.addListener(new ItemClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void itemClick(ItemClickEvent event) {
					
				}
				
			});
			
			markerSetContainer(SubSetOperations.getMarkerSets(dataSetId), maSet);
			arraySetContainer(SubSetOperations.getArraySets(dataSetId), maSet);

			l1.addComponent(markerSets);
			l2.addComponent(arraySets);

	        uploadField = new UploadField(){
				private static final long serialVersionUID = 3738084401913970304L;
	            protected void updateDisplay() {
	        		byte[] bytes = (byte[]) getValue();
	        		String filename = getLastFileName();
		            if (filename.endsWith(".csv")||filename.endsWith(".CSV")){
		            	parseCSV(filename, bytes, dataSetId);
	        		}else{
	        			getWindow().showNotification("File Format Error", filename + " is not a CSV file", Notification.TYPE_WARNING_MESSAGE);
	        		}
	            }
	        };
	        uploadField.setStorageMode(StorageMode.MEMORY);
	        uploadField.setFieldType(FieldType.BYTE_ARRAY);
	        l2.addComponent(uploadField);
			l2.setExpandRatio(arraySets, 0.9f);
			
		}

	}
	
	private void parseCSV(String filename, byte[] bytes, Long dataSetId){
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		if (filename.toLowerCase().endsWith(".csv")) {
			filename = filename.substring(0, filename.length() - 4);
		}
		// Ensure loaded set has unique name
		Set<String> nameSet = new HashSet<String>();
		for (Object id : arraySets.getItemIds()){
			nameSet.add(arraySets.getItem(id).toString());
		}

		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		try {
			ExcelCSVParser parser = new ExcelCSVParser(inputStream);
			String[][] data = parser.getAllValues();
			for (int i = 0; i < data.length; i++) {
				String[] line = data[i];
				if (line.length > 0) {
					String setname = (line.length > 1 && line[1].trim().length() > 0)?
									  line[1].trim() : filename;
					List<String> selectedNames = map.get(setname);
					if (selectedNames == null){
						selectedNames = new ArrayList<String>();
						map.put(setname, selectedNames);
					}
					selectedNames.add(line[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Lost cause
				}
			}
		}

		int missing = 0;
		for (String setname : map.keySet()){
			List<String> selectedNames = map.get(setname);
			setname = Util.getUniqueName(setname, nameSet);
			nameSet.add(setname);
            int setsize=0;
            ArrayList<String> arrayList = new ArrayList<String>();
			for(DSMicroarray array: maSet) {
				if(selectedNames.contains(array.getLabel())) {
					arrayList.add(array.getLabel());
					setsize++;
				}
			}
			if(setsize != selectedNames.size())
				missing += selectedNames.size() - setsize;
		
			if (setsize > 0) {
				if( SubSetOperations.storeData(arrayList, "Microarray", setname, dataSetId ) == true ) {
					populateTabSheet(maSet, dataSetId);
				}
			}
		}
		if(missing > 0) {
			if (missing == 1)
				getWindow().showNotification("Array Not Found", missing + " array listed in the CSV file is not present in the dataset.  Skipped.", Notification.TYPE_WARNING_MESSAGE);
			else 
				getWindow().showNotification("Arrays Not Found", missing + " arrays listed in the CSV file are not present in the dataset.  Skipped.", Notification.TYPE_WARNING_MESSAGE);
		}
	}

	private void arraySetContainer(List<?> list, DSMicroarraySet maSet) {

		arraySets.removeAllItems();
		arraySets.setSizeFull();
		arraySets.addContainerProperty("Name", String.class, "");
		arraySets.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		
		if(!list.isEmpty()) {
			for(int i=0; i<list.size(); i++ ) {
				String name 		= 	((SubSet) list.get(i)).getName();
				Long subSetId		=	((SubSet) list.get(i)).getId();
				ArrayList<String> arrays 	= 	(((SubSet) list.get(i)).getPositions());
			
				arraySets.addItem(subSetId);
				arraySets.getContainerProperty(subSetId, "Name").setValue(name);

				for(int j = 0; j<arrays.size(); j++) {
					
					String child	=	arrays.get(j);
					String childId  =	child + " " + i;
					arraySets.addItem(childId);
					arraySets.getContainerProperty(childId, "Name").setValue(child);
					arraySets.setChildrenAllowed(childId, false);
					arraySets.setParent(childId, subSetId);
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
				Long subSetId		=	((SubSet) list.get(i)).getId();
				ArrayList<String> markers 	= 	(((SubSet) list.get(i)).getPositions());
			
				markerSets.addItem(subSetId);
				markerSets.getContainerProperty(subSetId, "Name").setValue(name);
				
				for(int j = 0; j<markers.size(); j++) {
					String child = markers.get(j);
					String childId  =	child + " " + i;
					markerSets.addItem(childId);
					markerSets.getContainerProperty(childId, "Name").setValue(child);
					markerSets.setChildrenAllowed(childId, false);
					markerSets.setParent(childId, subSetId);
				}
			}
		}
	}
	
	public void removeData() {
		l1.removeAllComponents();
		l2.removeAllComponents();
	}
	
}
