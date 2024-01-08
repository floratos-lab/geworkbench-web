package org.geworkbenchweb.layout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geworkbench.util.Util;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.Ostermiller.util.ExcelCSVParser;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class OpenSetWindow extends Window {

	private static final long serialVersionUID = 3780041096719367174L;
	
	OpenSetWindow(final Long dataSetId, final SetViewLayout setViewLayout) {
		super("Open Set");

		this.center();
		this.setWidth("20%");
		this.setHeight("40%");

		VerticalLayout vlayout = (VerticalLayout) this.getContent();
		vlayout.setMargin(true);
		vlayout.setSpacing(true);

		final OptionGroup setGroup = new OptionGroup("Please choose a set type");
		setGroup.addItem("Marker Set");
		setGroup.addItem("Array Set");
		setGroup.setValue("Array Set");
		setGroup.setImmediate(true);
		vlayout.addComponent(setGroup);

		final OptionGroup markerGroup = new OptionGroup(
				"Markers are represented by");
		markerGroup.addItem("Marker ID");
		markerGroup.addItem("Gene Symbol");
		markerGroup.setValue("Marker ID");
		vlayout.addComponent(markerGroup);
		markerGroup.setVisible(false);

		setGroup.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 2481194620858021204L;

			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue().equals("Marker Set"))
					markerGroup.setVisible(true);
				else
					markerGroup.setVisible(false);
			}
		});

		UploadField openFile = new UploadField(StorageMode.MEMORY) {
			private static final long serialVersionUID = -212174451849906591L;

			protected void updateDisplay() {
				Window pWindow = OpenSetWindow.this.getParent();
				if (pWindow != null)
					pWindow.removeWindow(OpenSetWindow.this);
				String filename = getLastFileName();
				byte[] bytes = (byte[]) getValue();

				if (filename.endsWith(".csv") || filename.endsWith(".CSV")) {
					if (setGroup.getValue().equals("Array Set")) {
						loadSubSet("Array", filename, bytes, dataSetId, setViewLayout.getArraySetTree(), null);
					} else {
						loadSubSet("Marker", filename, bytes, dataSetId, setViewLayout.getMarkerSetTree(), (String) markerGroup.getValue());
					}
				} else {
					MessageBox mb = new MessageBox(pWindow,
							"File Format Error", MessageBox.Icon.WARN, filename
									+ " is not a CSV file",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
				}
			}
		};
		openFile.setButtonCaption("Open File");
		openFile.setFieldType(FieldType.BYTE_ARRAY);
		vlayout.addComponent(openFile);
	}

	private static void loadSubSet(String setType, String filename, byte[] bytes, long datasetId, Tree tree, String markerType) {

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
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, datasetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(MicroarrayDataset.class, id);
		
		ArrayList<String> panel = new ArrayList<String>();
		if(microarray==null) return panel;
		
		String[] arrayLabels = microarray.getArrayLabels();
		String[] markerLabels = microarray.getMarkerLabels();

		if (setType.equals("Array")){
			for (String arrayLabel : arrayLabels) {
				if(selectedNames.contains(arrayLabel)) 
					panel.add(arrayLabel);
			}
		}else if (markerType.equals("Marker ID")){
			for (String markerLabel : markerLabels){
				if (selectedNames.contains(markerLabel))
					panel.add(markerLabel);
			}
		}else if (markerType.equals("Gene Symbol")){
			Map<String, String> map = DataSetOperations.getAnnotationMap(datasetId);
			for(String markerLabel : markerLabels) {
				if(!map.containsKey(markerLabel)) continue;
				String geneName = map.get(markerLabel);
				if(selectedNames.contains(geneName))
					panel.add(markerLabel);
				else if (geneName.contains(" /// ")){
					for (String gname : geneName.split(" /// ")){
						if (selectedNames.contains(gname)){
							panel.add(markerLabel);
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
		tree.getContainerProperty(subSetId, SetViewLayout.SUBSET_NAME).setValue(setname);
		tree.getContainerProperty(subSetId, SetViewLayout.SET_DISPLAY_NAME).setValue(setname + " [" + panel.size() + "]");
		tree.setParent(subSetId, parentSet);
		tree.setChildrenAllowed(subSetId, true);
		for(int j=0; j<panel.size(); j++) {
			tree.addItem(panel.get(j)+subSetId);
			tree.getContainerProperty(panel.get(j)+subSetId, SetViewLayout.SET_DISPLAY_NAME).setValue(panel.get(j));
			tree.setParent(panel.get(j)+subSetId, subSetId);
			tree.setChildrenAllowed(panel.get(j)+subSetId, false);
		}
	}
}
