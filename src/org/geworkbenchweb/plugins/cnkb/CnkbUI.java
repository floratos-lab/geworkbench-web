package org.geworkbenchweb.plugins.cnkb;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.components.interactions.cellularnetwork.VersionDescriptor;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbenchweb.plugins.cnkb.CNKBInteractions;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Parameter panel for CNKB
 * @author Nikhil
 * 
 */
public class CnkbUI extends VerticalLayout {

	private static final long serialVersionUID = -1221913812891134388L;
	
	private DSMicroarraySet dataSet;
	
	private ResultSet resultSet;
	
	private int timeout = 3000;
	
	private List<String> contextList = new ArrayList<String>();
	
	private List<VersionDescriptor> versionList = new ArrayList<VersionDescriptor>();
	
	User user = SessionHandler.get();
	
	private String[] params;
	
	private long dataSetId;
	
	public CnkbUI(DSMicroarraySet maSet, final long dataSetId) {
		this.dataSetId = dataSetId;
		this.dataSet = maSet;
		this.setSpacing(true);
	}
	
	public void attach() {
		
		super.attach();
		
		loadApplicationProperty();
		
		params 	=	new String[3];
		
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
					resultSet = new ResultSet();
					java.sql.Date date 	=	new java.sql.Date(System.currentTimeMillis());
					resultSet.setDateField(date);
					String dataSetName	=	"CNKB - " + new java.util.Date(); 
					resultSet.setName(dataSetName);
					resultSet.setType("CNKB");
					resultSet.setParent(dataSetId);
					resultSet.setOwner(user.getId());	
					FacadeFactory.getFacade().store(resultSet);	
			
					//NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
					//GeworkbenchRoot.getBlackboard().fire(resultEvent);
					
					new CNKBThread().start();
						
				} catch (Exception e) {	
					e.printStackTrace();
				}		
			}
		});
		
		markerSetBox.setCaption("Select Marker Set");
		markerSetBox.setInputPrompt("Select Marker Set from the list");
		markerSetBox.setWidth("50%");
		markerSetBox.setImmediate(true);
		markerSetBox.setNullSelectionAllowed(false);
		
		List<?> subSets		= 	SubSetOperations.getMarkerSets(dataSetId);
		
		for(int m=0; m<(subSets).size(); m++){
			
			markerSetBox.addItem(((SubSet) subSets.get(m)).getId());
			markerSetBox.setItemCaption(((SubSet) subSets.get(m)).getId(), ((SubSet) subSets.get(m)).getName());
		
		}
		markerSetBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				
				params[2] =  String.valueOf(valueChangeEvent.getProperty().getValue());
				addComponent(interactomeBox);
				
			}
		});
		
		interactomeDes.setStyleName(Reindeer.LABEL_SMALL);
		interactomeDes.setImmediate(true);
		
		versionBox.setCaption("Select Version");
		versionBox.setInputPrompt("Select Version number from the list");
		versionBox.setWidth("50%");   
		versionBox.setImmediate(true);
		versionBox.setNullSelectionAllowed(false);
		versionBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				try {
					params[1] = valueChangeEvent.getProperty().getValue().toString();
					addComponent(submitButton);
				}catch (Exception e) {
					
					//TODO
					
				}
			}
		});
		
		interactomeBox.setCaption("Select Interactome");
		interactomeBox.setWidth("50%");
		interactomeBox.setInputPrompt("Select Interactome from the list");
		interactomeBox.setImmediate(true);
		interactomeBox.setNullSelectionAllowed(false);
		
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
					addComponent(interactomeDes);
					addComponent(versionBox);
					
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
		
		addComponent(markerSetBox);
		
	}

	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		
		String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";
		ResultSetlUtil.setUrl(interactionsServletUrl);
		ResultSetlUtil.setTimeout(timeout);
		
	}
	
	public class CNKBThread extends Thread {	
		@Override
		public void run() {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CNKBInteractions cnkb = new CNKBInteractions();
			Vector<CellularNetWorkElementInformation> hits = cnkb.CNKB(dataSet, params, dataSetId);
			resultSet.setData(ObjectConversion.convertToByte(hits));
			FacadeFactory.getFacade().store(resultSet);
			System.out.println(super.getName());
		}
	}
}
