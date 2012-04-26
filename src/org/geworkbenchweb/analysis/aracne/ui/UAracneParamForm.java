package org.geworkbenchweb.analysis.aracne.ui;

import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.analysis.CNKB.CNKBInteractions;
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

	private DSMicroarraySet dataSet;
	
	DataSetOperations dataOp  	= 	new DataSetOperations();
	
	SubSetOperations setOp		= 	new SubSetOperations();
	
	public UAracneParamForm(DSMicroarraySet maSet) {
		
		String dataSetName = maSet.getDataSetName();
		
		final String[] params 	=	new String[3];
		
		final ComboBox markerSetBox			= 	new ComboBox();
		markerSetBox.setCaption("Select Marker Set");
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
				
				
				
			}
		});
		
		final Button submitButton 			= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;
			final String[] params 	=	new String[3];

			public void buttonClick(ClickEvent event) {
				try {

					new CNKBInteractions(dataSet, params);

				} catch (Exception e) {	

					System.out.println(e);

				}		
			}
		});
		
		addField("submitButton", submitButton);
	}
	
}
