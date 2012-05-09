package org.geworkbenchweb.analysis.aracne.ui;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.analysis.aracne.AracneAnalysisWeb;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

/**
 * Builds the Parameter Form for ARACne Analysis
 * @author Nikhil Reddy
 */
public class UAracneParamForm extends Form {

	private static final long serialVersionUID = 1L;
	
	public UAracneParamForm(final DSMicroarraySet maSet) {
		
		String dataSetName = maSet.getDataSetName();
		
		GridLayout formLayout			= 	new GridLayout(4, 10);
		formLayout.setSpacing(true);
		formLayout.setImmediate(true);
		
		setLayout(formLayout);
		
		final ArrayList<String> params 	=	new ArrayList<String>();
		final ComboBox markerSetBox		= 	new ComboBox();
		final ComboBox modeBox			=	new ComboBox();
		final ComboBox algoBox			=	new ComboBox();
		final ComboBox kernelWidth		= 	new ComboBox();
		final ComboBox thresholdType	= 	new ComboBox();
		final ComboBox dpiTolerance		= 	new ComboBox();
		final ComboBox dpiTargetList	=	new ComboBox();	
		final TextField widthValue		= 	new TextField();
		final TextField threshold		= 	new TextField();
		final ComboBox correction		=	new ComboBox();
		final TextField tolerance		= 	new TextField();
		final ComboBox dpiSetBox		=	new ComboBox();
		final TextField bootStrapNumber	= 	new TextField();
		final ComboBox  mergeProbeSets	=	new ComboBox();
		
		/* Default values */
		params.add(0," ");
		params.add(1, "Complete");
		params.add(2, "Adaptive Partitioning");
		params.add(3, "Inferred");
		params.add(4, "0.1");
		params.add(5, "P-Value");
		params.add(6, "0.01");
		params.add(7, "No Correction");
		params.add(8, "Apply");
		params.add(9, "0.01");
		params.add(10, "Do Not Apply");
		params.add(11, " ");
		params.add(12, "1");
		params.add(13, "No");
		
		markerSetBox.setCaption("Hub Marker(s) From Sets");
		markerSetBox.setNullSelectionAllowed(false);
		markerSetBox.setInputPrompt("Select Marker Set");
		markerSetBox.setImmediate(true);
		
		List<?> data 		=	DataSetOperations.getDataSet(dataSetName);
		List<?> subSets		= 	SubSetOperations.getMarkerSets(((DataSet) data.get(0)).getId());
		
		for(int m=0; m<(subSets).size(); m++){
			
			markerSetBox.addItem(((SubSet) subSets.get(m)).getName());
		
		}
		markerSetBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(0);
				params.add(0, getMarkerData(valueChangeEvent.getProperty().getValue().toString(), maSet));
				
			}
		});
		
		modeBox.setCaption("Select Mode");
		modeBox.setNullSelectionAllowed(false);
		modeBox.setImmediate(true);
		modeBox.addItem("Complete");
		modeBox.addItem("Discovery");
		modeBox.addItem("Preprocessing");
		modeBox.select("Complete");
		modeBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(1);
				params.add(1, valueChangeEvent.getProperty().getValue().toString());
				
			}
		});
		
		algoBox.setCaption("Select Algorithm");
		algoBox.setImmediate(true);
		algoBox.setNullSelectionAllowed(false);
		algoBox.addItem("Adaptive Partitioning");
		algoBox.addItem("Fixed Bandwidth");
		algoBox.select("Adaptive Partitioning");
		algoBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Fixed Bandwidth")) {
					kernelWidth.setEnabled(true);
				} else if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Adaptive Partitioning")) {
					kernelWidth.setEnabled(false);
				}
				params.remove(2);
				params.add(2, valueChangeEvent.getProperty().getValue().toString());
				
			}
		});
		
		widthValue.setCaption(" ");
		widthValue.setValue(0.1);
		widthValue.setEnabled(false);
		widthValue.setNullSettingAllowed(false);
		widthValue.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(4);
				params.add(4, valueChangeEvent.getProperty().getValue().toString());
				
			}
		});
		
		kernelWidth.setCaption("Kernel Width");
		kernelWidth.setImmediate(true);
		kernelWidth.setNullSelectionAllowed(false);
		kernelWidth.addItem("Inferred");
		kernelWidth.addItem("Specify");
		kernelWidth.select("Inferred");
		kernelWidth.setEnabled(false);
		kernelWidth.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Specify")) {
					widthValue.setEnabled(true);
				} else {
					widthValue.setEnabled(false);
				}
				params.remove(3);
				params.add(3, valueChangeEvent.getProperty().getValue().toString());
				
			}
		});
		
		
		
		correction.setCaption(" ");
		correction.setNullSelectionAllowed(false);
		correction.addItem("No Correction");
		correction.addItem("Bonferroni Correction");
		correction.select("No Correction");
		correction.setEnabled(true);
		correction.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(7);
				params.add(7, valueChangeEvent.getProperty().getValue().toString());
			}
				
		});
		
		threshold.setCaption(" ");
		threshold.setValue(0.01);
		threshold.setNullSettingAllowed(false);
		threshold.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(6);
				params.add(6, valueChangeEvent.getProperty().getValue().toString());
			}
				
		});
		
		thresholdType.setCaption("Threshold Type");
		thresholdType.setImmediate(true);
		thresholdType.setNullSelectionAllowed(false);
		thresholdType.addItem("P-Value");
		thresholdType.addItem("Mutual Info");
		thresholdType.select("P-Value");
		thresholdType.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("P-Value")) {
					correction.setEnabled(true);
				} else {
					correction.setEnabled(false);
				}
				params.remove(5);
				params.add(5, valueChangeEvent.getProperty().getValue().toString());
				
			}
		});
		
		tolerance.setCaption(" ");
		tolerance.setValue(0.1);
		tolerance.setNullSettingAllowed(false);
		
		dpiTolerance.setCaption("DPI Tolerance");
		dpiTolerance.setImmediate(true);
		dpiTolerance.setNullSelectionAllowed(false);
		dpiTolerance.addItem("Apply");
		dpiTolerance.addItem("Do Not Apply");
		dpiTolerance.select("Apply");
		dpiTolerance.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("Apply")) {
					tolerance.setEnabled(true);
				} else {
					tolerance.setEnabled(false);
				}
				
			}
		});
		
		dpiSetBox.setCaption(" ");
		dpiSetBox.setNullSelectionAllowed(false);
		dpiSetBox.setInputPrompt("Select Marker Set");
		dpiSetBox.setImmediate(true);
		dpiSetBox.setEnabled(false);
		
		for(int m=0; m<(subSets).size(); m++){
			
			dpiSetBox.addItem(((SubSet) subSets.get(m)).getName());
		
		}
		dpiSetBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(11);
				params.add(11, getMarkerData(valueChangeEvent.getProperty().getValue().toString(), maSet));
				
			}
		});
		
		dpiTargetList.setCaption("DPI TargetList");
		dpiTargetList.setImmediate(true);
		dpiTargetList.setNullSelectionAllowed(false);
		dpiTargetList.addItem("From Sets");
		dpiTargetList.addItem("Do Not Apply");
		dpiTargetList.select("Do Not Apply");
		dpiTargetList.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				if(valueChangeEvent.getProperty().getValue().toString().equalsIgnoreCase("From Sets")) {
					dpiSetBox.setEnabled(true);
				} else {
					dpiSetBox.setEnabled(false);
				}
				params.remove(10);
				params.add(10, valueChangeEvent.getProperty().getValue().toString());
			}
		});
		
		bootStrapNumber.setCaption("Bootstrap Number");
		bootStrapNumber.setValue(1);
		bootStrapNumber.setNullSettingAllowed(false);
		bootStrapNumber.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(12);
				params.add(12, valueChangeEvent.getProperty().getValue().toString());
			}
				
		});
		
		mergeProbeSets.setCaption("Merge multiple probesets");
		mergeProbeSets.addItem("Yes");
		mergeProbeSets.addItem("No");
		mergeProbeSets.select("No");
		mergeProbeSets.setNullSelectionAllowed(false);
		mergeProbeSets.setImmediate(true);
		mergeProbeSets.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(13);
				params.add(13, valueChangeEvent.getProperty().getValue().toString());
			}
				
		});
		
		final Button submitButton 	= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {

					new AracneAnalysisWeb(maSet, params);

				} catch (Exception e) {	

					System.out.println(e);

				}		
			}
		});
		
		formLayout.addComponent(markerSetBox, 0, 0);
		formLayout.addComponent(modeBox, 0, 1);
		formLayout.addComponent(algoBox, 0, 2);
		formLayout.addComponent(kernelWidth, 0, 3);
		formLayout.addComponent(widthValue, 1, 3);
		formLayout.addComponent(thresholdType, 0, 4);
		formLayout.addComponent(threshold, 1, 4);
		formLayout.addComponent(correction, 2, 4);
		formLayout.addComponent(dpiTolerance, 0, 5);
		formLayout.addComponent(tolerance, 1, 5);
		formLayout.addComponent(dpiTargetList, 0, 6);
		formLayout.addComponent(dpiSetBox, 1, 6);
		formLayout.addComponent(bootStrapNumber, 0, 7);
		formLayout.addComponent(mergeProbeSets, 0, 8);
		formLayout.addComponent(submitButton, 0, 9);
		
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
