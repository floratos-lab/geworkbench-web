package org.geworkbenchweb.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.util.Util;
import org.geworkbenchweb.pojos.SubSet;

import com.Ostermiller.util.ExcelCSVParser;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class CSVUtil {

	public static void loadArraySet(String filename, byte[] bytes, long datasetId, Tree tree){

		loadSubSet("Array", filename, bytes, datasetId, tree, null);
	}
	public static void loadMarkerSet(String filename, byte[] bytes, long datasetId, Tree tree, String markerType){

		loadSubSet("Marker", filename, bytes, datasetId, tree, markerType);
	}
	
	public static void loadSubSet(String setType, String filename, byte[] bytes, long datasetId, Tree tree, String markerType) {

		filename = getUniqueSetName(setType, filename, datasetId);
		
		ArrayList<String> selectedNames = getSelectedNames(bytes);
		ArrayList<String> panel = getPanel(setType, markerType, datasetId, selectedNames);

		int missing = selectedNames.size() - panel.size();
		showWarning(setType, missing, tree.getApplication().getMainWindow());

		storeSubSet(setType, panel, filename, datasetId, tree);
	}

	private static String getUniqueSetName(String setType, String filename, Long datasetId){

		if (filename.toLowerCase().endsWith(".csv")) {
			filename = filename.substring(0, filename.length() - 4);
		}
		// Ensure loaded file has unique name
		Set<String> nameSet = new HashSet<String>();
		List<?> subsets = null;
		if (setType.equals("Array"))
			subsets = SubSetOperations.getArraySetsForCurrentContext(datasetId);
		else
			subsets = SubSetOperations.getMarkerSets(datasetId);
		for (Object arrayset : subsets) {
			nameSet.add(((SubSet)arrayset).getName());
		}
		filename = Util.getUniqueName(filename, nameSet);
		return filename;
	}

	private static ArrayList<String> getSelectedNames(byte[] bytes){

		ArrayList<String> selectedNames = new ArrayList<String>();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		try {
			ExcelCSVParser parser = new ExcelCSVParser(inputStream);
			String[][] data = parser.getAllValues();
			for (int i = 0; i < data.length; i++) {
				String[] line = data[i];
				if (line.length > 0) {
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
		return selectedNames;
	}

	private static ArrayList<String> getPanel(String setType, String markerType, Long datasetId, ArrayList<String> selectedNames){
		DSMicroarraySet dataSet;
		try {
			dataSet = (DSMicroarraySet) UserDirUtils.deserializeDataSet(datasetId, DSMicroarraySet.class);
		} catch (Exception e) {
			e.printStackTrace();
			dataSet = null;
		}

		ArrayList<String> panel = new ArrayList<String>();
		if(dataSet==null) return panel;
		if (setType.equals("Array")){
			for (DSMicroarray array: dataSet) {
				if(selectedNames.contains(array.getLabel())) 
					panel.add(array.getLabel());
			}
		}else if (markerType.equals("Marker ID")){
			for (DSGeneMarker marker : dataSet.getMarkers()){
				if (selectedNames.contains(marker.getLabel()))
					panel.add(marker.getLabel());
			}
		}else if (markerType.equals("Gene Symbol")){
			for(DSGeneMarker marker: dataSet.getMarkers()) {
				String geneName = marker.getGeneName();
				if(selectedNames.contains(geneName))
					panel.add(marker.getLabel());
				else if (geneName.contains(" /// ")){
					for (String gname : geneName.split(" /// ")){
						if (selectedNames.contains(gname)){
							panel.add(marker.getLabel());
							break;
						}
					}
				} 
			}
		}
		return panel;
	}

	private static void showWarning(String setType, int missing, Window pWindow){
		if(missing > 0) {
			if (missing == 1){
				MessageBox mb = new MessageBox(
						pWindow,
						setType+" Not Found",
						MessageBox.Icon.WARN,
						missing + " "+setType.toLowerCase()+" listed in the CSV file is not present in the dataset.  Skipped.",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
			}else{
				MessageBox mb = new MessageBox(
						pWindow,
						setType+" Not Found",
						MessageBox.Icon.WARN,
						missing + " "+setType.toLowerCase()+"s listed in the CSV file are not present in the dataset.  Skipped.",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
			}
		}
	}

	private static void storeSubSet(String setType, ArrayList<String> panel, String setname, Long datasetId, Tree tree){
		Long subSetId = 0l;
		String parentSet = "";
		if (setType.equals("Array")){
			subSetId = SubSetOperations.storeArraySetInCurrentContext(panel, setname, datasetId);
			parentSet = "arraySets";
		}else{
			subSetId = SubSetOperations.storeMarkerSetInCurrentContext(panel, setname, datasetId);
			parentSet = "MarkerSets";
		}

		tree.addItem(subSetId);
		tree.getContainerProperty(subSetId, "setName").setValue(setname + " [" + panel.size() + "]");
		tree.setParent(subSetId, parentSet);
		tree.setChildrenAllowed(subSetId, true);
		for(int j=0; j<panel.size(); j++) {
			tree.addItem(panel.get(j)+subSetId);
			tree.getContainerProperty(panel.get(j)+subSetId, "setName").setValue(panel.get(j));
			tree.setParent(panel.get(j)+subSetId, subSetId);
			tree.setChildrenAllowed(panel.get(j)+subSetId, false);
		}
	}
	
	public static void saveSetToFile(String savefname, SubSet subSet){
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(savefname));
			for (String name : subSet.getPositions()){
				bw.write(name);
				bw.newLine();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{ 
				if (bw!=null) bw.close(); 
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}