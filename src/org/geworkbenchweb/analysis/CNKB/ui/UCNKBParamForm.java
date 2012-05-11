package org.geworkbenchweb.analysis.CNKB.ui;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.components.interactions.cellularnetwork.VersionDescriptor;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbenchweb.analysis.CNKB.CNKBInteractions;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

/**
 * Parameter panel for CNKB
 * @author Nikhil
 * 
 */
public class UCNKBParamForm extends Form {

	private static final long serialVersionUID = -1221913812891134388L;
	
	private DSMicroarraySet dataSet;
	
	private int timeout = 3000;
	
	private List<String> contextList = new ArrayList<String>();
	
	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();
	
	User user = SessionHandler.get();
	
	@SuppressWarnings("rawtypes")
	public UCNKBParamForm(DSMicroarraySet maSet) {
		
		this.dataSet = maSet;
		
		loadApplicationProperty();
		
		final String[] params 	=	new String[3];
		
		final String dataSetName =	maSet.getDataSetName();
		
		final InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		
		try {
			
			contextList = interactionsConnection.getInteractomeNames();
			
		} catch (ConnectException e1) {
			//TODO
			e1.printStackTrace();
		} catch (SocketTimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		final ComboBox interactomeBox 		= 	new ComboBox();
		final ComboBox versionBox			= 	new ComboBox();
		final Label interactomeDes			= 	new Label();
		final ComboBox markerSetBox			= 	new ComboBox();
		final Button submitButton 			= 	new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
				
					new CNKBInteractions(dataSet, params);
						
				} catch (Exception e) {	
					
					System.out.println(e);

				}		
			}
		});
		
		markerSetBox.setCaption("Select Marker Set");
		markerSetBox.setWidth("50%");
		markerSetBox.setImmediate(true);
		
		List data 		=	DataSetOperations.getDataSet(dataSetName);
		List subSets	= 	SubSetOperations.getMarkerSets(((DataSet) data.get(0)).getId());
		
		for(int m=0; m<(subSets).size(); m++){
			
			markerSetBox.addItem(((SubSet) subSets.get(m)).getName());
		
		}
		markerSetBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params[2] = getMarkerData(valueChangeEvent.getProperty().getValue().toString(), dataSet);
				addField("interactomeBox", interactomeBox);
				
			}
		});
		
		interactomeDes.setStyleName(Reindeer.LABEL_SMALL);
		interactomeDes.setImmediate(true);
		
		versionBox.setCaption("Select Version");
		versionBox.setWidth("50%");   
		versionBox.setImmediate(true);
		versionBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				try {
					params[1] = valueChangeEvent.getProperty().getValue().toString();
					addField("submitAnalysis", submitButton);
				}catch (Exception e) {
					
					//TODO
					
				}
			}
		});
		
		interactomeBox.setCaption("Select Interactome");
		interactomeBox.setWidth("50%");
		interactomeBox.setInputPrompt("Select Interactome from the list");
		interactomeBox.setImmediate(true);
		
		for(int j=0; j<contextList.size(); j++) {
			interactomeBox.addItem(contextList.get(j));
		}
		
		interactomeBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

				try {
					params[0] = valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim();
					interactomeDes.setValue(interactionsConnection.getInteractomeDescription(valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim()));
					versionBox.removeAllItems();
					versionList = interactionsConnection.getVersionDescriptor(valueChangeEvent.getProperty().getValue().toString().split(" \\(")[0].trim());
					for(int k=0; k<versionList.size(); k++) {
						versionBox.addItem(versionList.get(k).getVersion());
					}
					getLayout().addComponent(interactomeDes);
					addField("version", versionBox);
					
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		addField("markerSets", markerSetBox);
		
	}

	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		
		String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
		ResultSetlUtil.setUrl(interactionsServletUrl);
		ResultSetlUtil.setTimeout(timeout);
		
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
