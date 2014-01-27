package org.geworkbenchweb.utils;

import java.util.List;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
 
public class MarkerSelector extends VerticalLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6214265705833965798L;
	
 
	private final String MARKERCONTEXT = "MarkerContext";
	
	private ComboBox markerContextCB;  
	private ListSelect markerSetSelect;
	 
	private Long dataSetId;
	private long userId;
	private String parentName = null;	 
	private boolean isMarkerContextSetByApp = false;
	
	public MarkerSelector(Long dataSetId, Long userId, String parentName) {
 
		this.parentName = parentName;
		this.dataSetId = dataSetId;
		this.userId = userId;
	 
		setSpacing(true);
		setImmediate(true);		
	 
		markerContextCB =  new ComboBox("Marker Context");	
		markerContextCB.setWidth("160px");
		markerContextCB.setImmediate(true);	
		markerContextCB.setNullSelectionAllowed(false);		 
		
		markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(4);
		markerSetSelect.setColumns(15);	 
		markerSetSelect.setImmediate(true);
		
	 
	
		markerContextCB.addValueChangeListener(new Property.ValueChangeListener() {
		  
			private static final long serialVersionUID = -1701293764682250834L;

			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = markerContextCB.getValue();
				if (val != null){					 						 
					Context context = (Context)val;							 
					List<SubSet> markerSubSets = SubSetOperations.getSubSetsForContext(context);
					markerSetSelect.removeAllItems();
				//	markerSetSelect.addItem("");
				//	markerSetSelect.setItemCaption("", "All Markers");
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
		 
		addComponent(markerContextCB);	 
	    addComponent(markerSetSelect);		 
		 
		 
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
	 
	
	}
	
	
	public  String[] getSelectedMarkerSet() {
		String[] selectList = null;
		String selectStr = markerSetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}	 
	
	public ListSelect getMarkerSetSelect()
	    {
	    	return this.markerSetSelect;
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
}
