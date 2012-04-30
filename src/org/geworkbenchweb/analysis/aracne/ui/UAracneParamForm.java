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

/**
 * Builds the Parameter Form for ARACne Analysis
 * @author Nikhil Reddy
 */
public class UAracneParamForm extends Form {

	private static final long serialVersionUID = 1L;

	DataSetOperations dataOp  	= 	new DataSetOperations();
	
	SubSetOperations setOp		= 	new SubSetOperations();
	
	public UAracneParamForm(final DSMicroarraySet maSet) {
		
		String dataSetName = maSet.getDataSetName();
		
		final ArrayList<String> params 	=	new ArrayList<String>();
		final ComboBox markerSetBox		= 	new ComboBox();
		final ComboBox modeBox			=	new ComboBox();
		final ComboBox algoBox			=	new ComboBox();
		
		
		/* Default values */
		params.add(0," ");
		params.add(1, "Complete");
		params.add(2, "Adaptive Partitioning");
		
		markerSetBox.setCaption("Hub Marker(s) From Sets");
		markerSetBox.setInputPrompt("Select Marker Set");
		markerSetBox.setWidth("50%");
		markerSetBox.setImmediate(true);
		
		List<?> data 		=	dataOp.getDataSet(dataSetName);
		List<?> subSets	= 	setOp.getMarkerSets(((DataSet) data.get(0)).getId());
		
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
		modeBox.setWidth("50%");
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
		algoBox.setWidth("50%");
		algoBox.setImmediate(true);
		algoBox.addItem("Adaptive Partitioning");
		algoBox.addItem("Fixed Bandwidth");
		algoBox.select("Adaptive Partitioning");
		algoBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params.remove(2);
				params.add(2, valueChangeEvent.getProperty().getValue().toString());
				
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
		
		addField("markerSetBox", markerSetBox);
		addField("modeBox", modeBox);
		addField("algoBox", algoBox);
		addField("submitButton", submitButton);
	}
	
	/**
	 * Create Dataset for selected markerSet 
	 */
	public String getMarkerData(String setName, DSMicroarraySet parentSet) {

		@SuppressWarnings("rawtypes")
		List subSet 		= 	setOp.getMarkerSet(setName);
		String positions 	= 	(((SubSet) subSet.get(0)).getPositions()).trim();
		
		return positions;
	}
	
}
