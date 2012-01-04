package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;

public class VisualPlugin extends TabSheet implements TabSheet.SelectedTabChangeListener {

	private static final long serialVersionUID 				= 	1L;
	
	User user = SessionHandler.get();
	
	private Table dataTable;
	
	private DSMicroarraySet maSet;
	
	private static final String DATA_OPERATIONS 			= 	"Data Operations";
	
	private static final String MICROARRAY_TABLE_CAPTION 	= 	"Tabular Microarray Viewer";
	
	private static final String MARKER_HEADER 				= 	"Marker";
	
	public VisualPlugin(String dataSetName) {

		addListener(this);
		setSizeFull();
		dataTable 						= 	new Table();
		String dataPeru					= 	dataSetName;
		String query 					= 	"Select p from DataSet as p where p.name=:name and p.owner=:owner";
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		
		parameters.put("name", dataPeru);
		parameters.put("owner", user.getId());
		
		DataSet dataSet 				= 	FacadeFactory.getFacade().find(query, parameters);
		byte[] dataByte 				= 	dataSet.getData();
		maSet 							= 	(DSMicroarraySet) toObject(dataByte);
		DataTab dataOp					= 	new DataTab(maSet);
		
		dataOp.setCaption(DATA_OPERATIONS);
		dataTable.setStyleName("small striped");
		dataTable.setSizeFull();
		dataTable.setCaption(MICROARRAY_TABLE_CAPTION);
		
	    addTab(dataOp);
		addTab(dataTable);
		
	}
	
	@SuppressWarnings("deprecation")
	public static Object toObject(byte[] bytes){ 
		
		Object object = null; 
		
		try{ 
			
			object = new java.io.ObjectInputStream(new 
					java.io.ByteArrayInputStream(bytes)).readObject(); 
		
		}catch(java.io.IOException ioe){ 
			
			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					ioe.getMessage()); 
		
		}catch(java.lang.ClassNotFoundException cnfe){ 
			
			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					cnfe.getMessage()); 
		
		} 
		
		return object; 
	
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		
		if(event.getTabSheet().getSelectedTab().getCaption() == MICROARRAY_TABLE_CAPTION){
		
			dataTable.setContainerDataSource(tabularView(maSet));
			
		}
	} 
	
	public IndexedContainer tabularView(DSMicroarraySet maSet) {
	
		String[] colHeaders 			= 	new String[(maSet.size())+1];
		IndexedContainer dataIn 		= 	new IndexedContainer();

		for(int j=0; j<maSet.getMarkers().size();j++) {
			
			Item item 					= 	dataIn.addItem(j);
			
			for(int k=0;k<=maSet.size();k++) {
				
				if(k == 0) {
					
					colHeaders[k] 		= 	MARKER_HEADER;
					dataIn.addContainerProperty(colHeaders[k], String.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getMarkers().get(j));
				
				} else {
					
					colHeaders[k] 		= 	maSet.get(k-1).toString();
					dataIn.addContainerProperty(colHeaders[k], Float.class, null);
					item.getItemProperty(colHeaders[k]).setValue(maSet.getValue(j, k-1));
				
				}
			}
		}
		
		return dataIn;
	
	}
	
}
