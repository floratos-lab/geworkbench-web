package org.geworkbenchweb.plugins.aracne;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;

public class AracneUI extends GridLayout {

	private static final long serialVersionUID = 1L;
	
	private final Long dataSetId;
	
	HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 

	public AracneUI(Long dataId) {

		this.dataSetId = dataId;
		
		setColumns(4);
		setRows(10);
		setSpacing(true);
		setImmediate(true);

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
		
		/**
		 * Params default values
		 */
		params.put(AracneParameters.MODE, "Complete");
		params.put(AracneParameters.ALGORITHM, "Adaptive Partitioning");
		params.put(AracneParameters.KERNEL_WIDTH, "Inferred");
		params.put(AracneParameters.WIDTH_VALUE, "0.01");
		params.put(AracneParameters.TOL_TYPE, "Apply");
		params.put(AracneParameters.TOL_VALUE, "0.1");
		params.put(AracneParameters.T_TYPE, "Mutual Info");
		params.put(AracneParameters.T_VALUE, "0.01");
		params.put(AracneParameters.CORRECTION, "No Correction");
		params.put(AracneParameters.DPI_LIST, "Do Not Apply");
		params.put(AracneParameters.BOOTS_NUM, "1");
		params.put(AracneParameters.MERGEPS, "No");
		
		markerSetBox.setCaption("Hub Marker(s) From Sets");
		markerSetBox.setNullSelectionAllowed(false);
		markerSetBox.setInputPrompt("Select Marker Set");
		markerSetBox.setImmediate(true);

		List<?> subSets		= 	SubSetOperations.getMarkerSets(dataSetId);

		for(int m=0; m<(subSets).size(); m++){
			markerSetBox.addItem(((SubSet) subSets.get(m)).getId());
			markerSetBox.setItemCaption(((SubSet) subSets.get(m)).getId(), ((SubSet) subSets.get(m)).getName());
		}
		markerSetBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.MARKER_SET, String.valueOf(valueChangeEvent.getProperty().getValue()));
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
				params.remove(AracneParameters.MODE);
				params.put(AracneParameters.MODE, valueChangeEvent.getProperty().getValue().toString());
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
				params.remove(AracneParameters.ALGORITHM);
				params.put(AracneParameters.ALGORITHM, valueChangeEvent.getProperty().getValue().toString());
			}
		});

		widthValue.setCaption(" ");
		widthValue.setValue("0.1");
		widthValue.setEnabled(false);
		widthValue.setNullSettingAllowed(false);
		widthValue.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.WIDTH_VALUE);
				params.put(AracneParameters.WIDTH_VALUE, valueChangeEvent.getProperty().getValue().toString());
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
				params.remove(AracneParameters.KERNEL_WIDTH);
				params.put(AracneParameters.KERNEL_WIDTH, valueChangeEvent.getProperty().getValue().toString());
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
				params.remove(AracneParameters.CORRECTION);
				params.put(AracneParameters.CORRECTION, valueChangeEvent.getProperty().getValue().toString());
			}

		});

		threshold.setCaption(" ");
		threshold.setValue("0.01");
		threshold.setNullSettingAllowed(false);
		threshold.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.T_VALUE);
				params.put(AracneParameters.T_VALUE, valueChangeEvent.getProperty().getValue().toString());
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
				params.put(AracneParameters.T_TYPE, valueChangeEvent.getProperty().getValue().toString());
			}
		});

		tolerance.setCaption(" ");
		tolerance.setValue("0.1");
		tolerance.setNullSettingAllowed(false);
		tolerance.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.TOL_VALUE, valueChangeEvent.getProperty().getValue().toString());
			}

		});

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
				params.put(AracneParameters.TOL_TYPE, valueChangeEvent.getProperty().getValue().toString());
			}
		});

		dpiSetBox.setCaption(" ");
		dpiSetBox.setNullSelectionAllowed(false);
		dpiSetBox.setInputPrompt("Select Marker Set");
		dpiSetBox.setImmediate(true);
		dpiSetBox.setEnabled(false);

		for(int m=0; m<(subSets).size(); m++){
			dpiSetBox.addItem(((SubSet) subSets.get(m)).getId());
			dpiSetBox.setItemCaption(((SubSet) subSets.get(m)).getId(), ((SubSet) subSets.get(m)).getName());
		}
		
		dpiSetBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.put(AracneParameters.DPI_SET, String.valueOf(valueChangeEvent.getProperty().getValue()));
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
				params.remove(AracneParameters.DPI_LIST);
				params.put(AracneParameters.DPI_LIST, valueChangeEvent.getProperty().getValue().toString());
			}
		});

		bootStrapNumber.setCaption("Bootstrap Number");
		bootStrapNumber.setValue("1");
		bootStrapNumber.setNullSettingAllowed(false);
		bootStrapNumber.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				params.remove(AracneParameters.BOOTS_NUM);
				params.put(AracneParameters.BOOTS_NUM, valueChangeEvent.getProperty().getValue().toString());
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
				params.remove(AracneParameters.MERGEPS);
				params.put(AracneParameters.MERGEPS, valueChangeEvent.getProperty().getValue().toString());
			}
		});

		final Button submitButton 	= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				try {

					List<DataSet> data = DataSetOperations.getDataSet(dataSetId);
					DSMicroarraySet maSet = (DSMicroarraySet) ObjectConversion.toObject(data.get(0).getData());
					
					ResultSet resultSet = 	new ResultSet();
					java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
					resultSet.setDateField(date);
					String dataSetName = "Aracne - Pending" ;
					resultSet.setName(dataSetName);
					resultSet.setType("AracneResults");
					resultSet.setParent(dataSetId);
					resultSet.setOwner(SessionHandler.get().getId());	
					FacadeFactory.getFacade().store(resultSet);	
					
					NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
					GeworkbenchRoot.getBlackboard().fire(resultEvent);

					AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(maSet, resultSet, params);
					GeworkbenchRoot.getBlackboard().fire(analysisEvent);	
					
				} catch (Exception e) {	
				}		
			}
		});

		addComponent(markerSetBox, 0, 0);
		addComponent(modeBox, 0, 1);
		addComponent(algoBox, 0, 2);
		addComponent(kernelWidth, 0, 3);
		addComponent(widthValue, 1, 3);
		addComponent(thresholdType, 0, 4);
		addComponent(threshold, 1, 4);
		addComponent(correction, 2, 4);
		addComponent(dpiTolerance, 0, 5);
		addComponent(tolerance, 1, 5);
		addComponent(dpiTargetList, 0, 6);
		addComponent(dpiSetBox, 1, 6);
		addComponent(bootStrapNumber, 0, 7);
		addComponent(mergeProbeSets, 0, 8);
		addComponent(submitButton, 0, 9);

	}

}