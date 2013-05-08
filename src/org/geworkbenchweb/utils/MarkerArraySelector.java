package org.geworkbenchweb.utils;

import java.util.List;
 
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;

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
	//private final String MARKERCONTEXT = "MarkerContext";
	
	private ComboBox markerContextCB;  
	private ListSelect markerSetSelect;
	private ComboBox arrayContextCB;
	private ListSelect arraySetSelect;
	
	private Long dataSetId;
	private long userId;
	private String parentName = null;
	private boolean isArrayContextSetByApp = false;
  
	
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
		markerContextCB.addItem("Default");	
		markerContextCB.setValue("Default");
		
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
					 						 
					 //todo 
				 
				}
			}
		});

	 
		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = arrayContextCB.getValue();
				if (val != null){
					Context context = (Context)val;							 
					List<SubSet> arraySubSets = SubSetOperations.getArraySetsForContext(context);
					arraySetSelect.removeAllItems();
					arraySetSelect.addItem("");
					arraySetSelect.setItemCaption("", "All Arrays");
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
		
		List<?> markerSubSets = SubSetOperations.getMarkerSets(dataSetId);

		markerSetSelect.removeAllItems();
		markerSetSelect.addItem("");
		markerSetSelect.setItemCaption("", "All markers");
		for (int m = 0; m < (markerSubSets).size(); m++) {
			markerSetSelect.addItem(((SubSet) markerSubSets.get(m)).getId());
			markerSetSelect.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}		 
		
		Context selectedtArrayContext = null;
		Preference pref = PreferenceOperations.getData(dataSetId, parentName + "." + ARRAYCONTEXT , userId);
		if (pref != null)
		{	
			selectedtArrayContext = (Context)ObjectConversion.toObject(pref.getValue());
		}
		if (selectedtArrayContext == null)
			selectedtArrayContext = SubSetOperations.getCurrentContext(dataSetId);
		List<Context> contexts = SubSetOperations.getAllContexts(dataSetId);		 
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
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}
	
	public  String[] getSelectedArraySet() {		 
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}
	
	public  String[] getSelectedArraySetNames() {		 
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
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
    
	
}
