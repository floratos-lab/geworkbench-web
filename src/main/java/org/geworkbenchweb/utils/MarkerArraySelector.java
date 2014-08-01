package org.geworkbenchweb.utils;

import java.util.List;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
 
public class MarkerArraySelector extends GridLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6214265705833965798L;
	
	private final String ARRAYCONTEXT = "ArrayContext";
	private final String MARKERCONTEXT = "MarkerContext";
	
	private ComboBox markerContextCB;  
	private ListSelect markerSetSelect;
	private ComboBox arrayContextCB;
	private ListSelect arraySetSelect;
	
	private Long dataSetId;
	private long userId;
	private String parentName = null;
	private boolean isArrayContextSetByApp = false;
	private boolean isMarkerContextSetByApp = false;
	
	public MarkerArraySelector(Long dataSetId, Long userId, String parentName) {
 
		this.parentName = parentName;
		this.dataSetId = dataSetId;
		this.userId = userId;
		setColumns(2);
		setRows(3);
		setSpacing(true);
		setImmediate(true);		
		Label spaceLabel = new Label("                       ");

		markerContextCB =  new ComboBox("Marker Context");	
		markerContextCB.setWidth("160px");
		markerContextCB.setImmediate(true);	
		markerContextCB.setNullSelectionAllowed(false);		 
		
		markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(15);
		markerSetSelect.setImmediate(true);
		
		arrayContextCB =  new ComboBox("Array Context");
		arrayContextCB.setWidth("160px");
		arrayContextCB.setImmediate(true);	
		arrayContextCB.setNullSelectionAllowed(false);
		
		arraySetSelect = new ListSelect("Select Array Sets:");
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(15);
		arraySetSelect.setImmediate(true);
	 
	
		markerContextCB.addListener(new Property.ValueChangeListener() {
		  
			private static final long serialVersionUID = -1701293764682250834L;

			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = markerContextCB.getValue();
				if (val != null){					 						 
					Context context = (Context)val;							 
					List<SubSet> markerSubSets = SubSetOperations.getSubSetsForContext(context);
					markerSetSelect.removeAllItems();
					markerSetSelect.addItem("All Markers");
					markerSetSelect.setItemCaption("All Markers", "All Markers");
					for (int m = 0; m < (markerSubSets).size(); m++) {					 
						markerSetSelect.addItem(((SubSet) markerSubSets.get(m)).getId());
						markerSetSelect.setItemCaption(
								((SubSet) markerSubSets.get(m)).getId(),
								((SubSet) markerSubSets.get(m)).getName());
						 
					}
					if (!isMarkerContextSetByApp)					 
						saveMarkerContextPreference();
					isMarkerContextSetByApp = false;
				 
				}
			}
		});

	 
		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = arrayContextCB.getValue();
				if (val != null){
					Context context = (Context)val;							 
					List<SubSet> arraySubSets = SubSetOperations.getSubSetsForContext(context);
					arraySetSelect.removeAllItems();
					arraySetSelect.addItem("All Arrays");
					arraySetSelect.setItemCaption("All Arrays", "All Arrays");
					for (int m = 0; m < (arraySubSets).size(); m++) {					 
						arraySetSelect.addItem(((SubSet) arraySubSets.get(m)).getId());
						arraySetSelect.setItemCaption(
								((SubSet) arraySubSets.get(m)).getId(),
								((SubSet) arraySubSets.get(m)).getName());
						 
					}
					if (!isArrayContextSetByApp)					 
						saveArrayContextPreference();
					isArrayContextSetByApp = false;
					
				}
				
				
			}
		});

	
	 
	 
		 
		addComponent(markerContextCB, 0, 0);
		addComponent(arrayContextCB, 1, 0);
	    addComponent(markerSetSelect, 0 , 1);
		addComponent(arraySetSelect, 1, 1);
		addComponent(spaceLabel, 0, 2);	
		 
	}
	
	
	public void setData(Long dataSetId, Long userId)
	{		
	 
		this.dataSetId = dataSetId;
		this.userId = userId;
		
		Context selectedMarkerContext = null;
		Preference pref = PreferenceOperations.getData(dataSetId, parentName + "." + MARKERCONTEXT , userId);
		if (pref != null)
		{	
			selectedMarkerContext = (Context)ObjectConversion.toObject(pref.getValue());
		}
		if (selectedMarkerContext == null)
			selectedMarkerContext = SubSetOperations.getCurrentMarkerContext(dataSetId);
		List<Context> contexts = SubSetOperations.getMarkerContexts(dataSetId);		 
		markerContextCB.removeAllItems();
		for (Context c : contexts){
			markerContextCB.addItem(c);	
			if (selectedMarkerContext!=null && c.getId().longValue()==selectedMarkerContext.getId().longValue()) 
			{
				isMarkerContextSetByApp = true;
				markerContextCB.setValue(c);
			}
		}	
	
		
		Context selectedtArrayContext = null;
		pref = PreferenceOperations.getData(dataSetId, parentName + "." + ARRAYCONTEXT , userId);
		if (pref != null)
		{	
			selectedtArrayContext = (Context)ObjectConversion.toObject(pref.getValue());
		}
		if (selectedtArrayContext == null)
			selectedtArrayContext = SubSetOperations.getCurrentArrayContext(dataSetId);
	    contexts = SubSetOperations.getArrayContexts(dataSetId);		 
		arrayContextCB.removeAllItems();
		for (Context c : contexts){
			arrayContextCB.addItem(c);	
			if (selectedtArrayContext!=null && c.getId().longValue()==selectedtArrayContext.getId().longValue()) 
			{
				isArrayContextSetByApp = true;
				arrayContextCB.setValue(c);
			}
		}	
	
	}
	
	
	public  String[] getSelectedMarkerSet() {
		String[] selectList = null;
		String selectStr = markerSetSelect.getValue().toString();
		if (selectStr.equals("[]") || selectStr.contains("All Markers"))
		    return null;
		else
			selectList = selectStr.substring(1, selectStr.length()-1).split(","); 
			
		return selectList;
	}
	
	public  String[] getSelectedArraySet() {		 
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (selectStr.equals("[]") || selectStr.contains("All Arrays"))
		   return null;
		else		 
		   selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
		 
			
		return selectList;
	}
	
	public  String[] getSelectedArraySetNames() {		 
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (selectStr.equals("[]") || selectStr.contains("All Arrays"))
		   return null;
		else
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
		    for(int i=0; i<selectList.length; i++ )
		    	selectList[i] = arraySetSelect.getItemCaption(Long.parseLong(selectList[i].trim()));
		}		
			
		return selectList;
	}
	
	
	 public ListSelect getMarkerSetSelect()
	    {
	    	return this.markerSetSelect;
	    }
	
    public ListSelect getArraySetSelect()
    {
    	return this.arraySetSelect;
    }
    
    
    private void saveArrayContextPreference()
    {

		Context arrayContext = (Context)arrayContextCB.getValue();
		Preference p = PreferenceOperations.getData(dataSetId,
				parentName + "." + ARRAYCONTEXT, userId);
				 
		if (p != null)
			PreferenceOperations.setValue(
					arrayContext, p);
		else
			PreferenceOperations.storeData(
					arrayContext,
					Context.class.getName(),
					parentName + "." + ARRAYCONTEXT,
					dataSetId, userId);
    }
    
    private void saveMarkerContextPreference()
    {

		Context markerContext = (Context)markerContextCB.getValue();
		Preference p = PreferenceOperations.getData(dataSetId,
				parentName + "." + MARKERCONTEXT, userId);
				 
		if (p != null)
			PreferenceOperations.setValue(
					markerContext, p);
		else
			PreferenceOperations.storeData(
					markerContext,
					Context.class.getName(),
					parentName + "." + MARKERCONTEXT,
					dataSetId, userId);
    }
    
    public String generateHistoryString(){
    	StringBuilder builder = new StringBuilder();
		int numMarker = 0, numArray = 0;
		Long masetId = null;
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		if(dataset != null) masetId = dataset.getDataId();
		
		String[] m = getSelectedMarkerSet();
		StringBuilder markerBuilder = new StringBuilder();
		if(m==null) {
			String[] markers = DataSetOperations.getStringLabels("markerLabels", masetId);
			if(markers != null){				 
				numMarker = markers.length;
			}
		} else { 
			for(String setName : m) {
				List<String> markers = SubSetOperations.getMarkerData(Long.parseLong(setName.trim()));
				for(String markerName : markers)
					markerBuilder.append("\t").append(markerName).append("\n");
				numMarker += markers.size();
			}
		}
		builder.append("Markers used (" + numMarker + ") - \n" );
		
		if (m==null)
			builder.append("All Markers \n");
		else
		    builder.append(markerBuilder.toString());
		
		m = getSelectedArraySet();
		StringBuilder arrayBuilder = new StringBuilder();
		if(m==null) {
			String[] arrays = DataSetOperations.getStringLabels("arrayLabels", masetId);
			if(arrays != null){				 
				numArray = arrays.length;
			}
		} else {
			for(String setName : m) {
				List<String> arrays = SubSetOperations.getArrayData(Long.parseLong(setName.trim()));
				for(String arrayName : arrays)
					arrayBuilder.append("\t").append(arrayName).append("\n");
				numArray += arrays.size();
			}
		}
		builder.append("Phenotypes used (" + numArray + ") - \n" );
		if (m==null)
			builder.append("All Arrays\n");
		else			
		    builder.append(arrayBuilder.toString());
		
		return builder.toString();
    }
    
    
    
}
