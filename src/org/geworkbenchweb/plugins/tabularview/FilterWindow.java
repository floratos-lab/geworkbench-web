package org.geworkbenchweb.plugins.tabularview;

import java.util.List;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.TableView;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;


public class FilterWindow extends Window {
	 
	private static final long serialVersionUID = 5480097015206241444L;
	private TabularViewUI parent;
	
    public FilterWindow(TabularViewUI parentUi,  final TableView table,  final TabularViewPreferences tabViewPreferences, final long dataSetId, final long userId)
    {
    	
    	this.parent  = parentUi;
    	final GridLayout gridLayout1 = new GridLayout(2, 4);			
		
		gridLayout1.setSpacing(true);
		gridLayout1.setImmediate(true);
		
		 setModal(true);
		 setClosable(true);				 ;
		 setWidth("500px");
		 setHeight("320px");
		 setResizable(false);
		 setCaption("Filter Setting");
		 setImmediate(true);
		
		Label spaceLabel = new Label("                       ");

		final ComboBox markerContextCB =  new ComboBox("Marker Context");	
		markerContextCB.setWidth("160px");
		markerContextCB.setImmediate(true);	
		markerContextCB.setNullSelectionAllowed(false);
		markerContextCB.addItem("default");	
		markerContextCB.setValue("default");
		
		final ListSelect markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(15);
		markerSetSelect.setImmediate(true);
		
		final ComboBox arrayContextCB =  new ComboBox("Array Context");
		arrayContextCB.setWidth("160px");
		arrayContextCB.setImmediate(true);	
		arrayContextCB.setNullSelectionAllowed(false);
		
		final ListSelect arraySetSelect = new ListSelect("Select Array Sets:");
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(15);
		arraySetSelect.setImmediate(true);
	 
	
		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = arrayContextCB.getValue();
				if (val != null){
					Context context = (Context)val;							 
					List<SubSet> arraySubSets = SubSetOperations.getArraySetsForContext(context);
					arraySetSelect.removeAllItems();
					arraySetSelect.addItem("All Arrays");
					for (int m = 0; m < (arraySubSets).size(); m++) {					 
						arraySetSelect.addItem(((SubSet) arraySubSets.get(m)).getId());
						arraySetSelect.setItemCaption(
								((SubSet) arraySubSets.get(m)).getId(),
								((SubSet) arraySubSets.get(m)).getName());
						 
					}
				}
			}
		});

		
		
		Context selectedtContext = null;
		if (tabViewPreferences.getMarkerFilter() != null)
			selectedtContext = tabViewPreferences.getArrayFilter().getContext();
		if (selectedtContext == null)
			selectedtContext = SubSetOperations.getCurrentContext(dataSetId);
		List<Context> contexts = SubSetOperations.getAllContexts(dataSetId);		 
		for (Context c : contexts){
			arrayContextCB.addItem(c);	
			if (selectedtContext!=null && c.getId().longValue()==selectedtContext.getId().longValue()) 
				arrayContextCB.setValue(c);
		}
		
		arrayContextCB.setValue(selectedtContext);
		
		
		
	
		
		List<?> markerSubSets = SubSetOperations.getMarkerSets(dataSetId);

		markerSetSelect.removeAllItems();
		markerSetSelect.addItem("All Markers");
		for (int m = 0; m < (markerSubSets).size(); m++) {
			markerSetSelect.addItem(((SubSet) markerSubSets.get(m)).getId());
			markerSetSelect.setItemCaption(
					((SubSet) markerSubSets.get(m)).getId(),
					((SubSet) markerSubSets.get(m)).getName());
		}				
		
		String[] selectedMarkerSet = null;
		if (tabViewPreferences.getMarkerFilter() != null)
			selectedMarkerSet= tabViewPreferences.getMarkerFilter().getSelectedSet();
		if (selectedMarkerSet != null && selectedMarkerSet.length > 0)
		{
			int startIndex =0;
			if (selectedMarkerSet[0].equalsIgnoreCase("All Markers"))
			{
				startIndex = startIndex +1;
				markerSetSelect.select("All Markers");
			}
			for(int i=startIndex; i<selectedMarkerSet.length; i++)
			{
				markerSetSelect.select(new Long(selectedMarkerSet[i].trim()));
			}
		}
	 
		
	 
		 
		String[] selectedArraySet = null;
		if (tabViewPreferences.getArrayFilter() != null)
			selectedArraySet= tabViewPreferences.getArrayFilter().getSelectedSet();
		if (selectedArraySet != null && selectedArraySet.length > 0)
		{
			int startIndex =0;
			if (selectedArraySet[0].equalsIgnoreCase("All Arrays"))
			{
				startIndex = startIndex +1;
				arraySetSelect.select("All Arrays");
			}
			for(int i=startIndex; i<selectedArraySet.length; i++)
				arraySetSelect.select(new Long(selectedArraySet[i].trim()));
		}
	 
		
		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = -4799561372701936132L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {							 
					 String value = markerSetSelect.getValue().toString();						   
					 FilterInfo markerFilter = new FilterInfo(null, getSelectedSet(value));
					 
					 
					 Preference p = PreferenceOperations.getData(dataSetId, Constants.MARKER_FILTER_CONTROL, userId);
					 if (p != null)
						PreferenceOperations.setValue(markerFilter, p);
					 else
						PreferenceOperations.storeData(markerFilter, FilterInfo.class.getName(), Constants.MARKER_FILTER_CONTROL, dataSetId, userId);
					 
					 value = arraySetSelect.getValue().toString();	
					 FilterInfo arrayFilter = new FilterInfo((Context)arrayContextCB.getValue(), getSelectedSet(value));
					 p = PreferenceOperations.getData(dataSetId, Constants.ARRAY_FILTER_CONTROL, userId);
					 if (p != null)
						PreferenceOperations.setValue(arrayFilter, p);
					 else
						PreferenceOperations.storeData(arrayFilter, FilterInfo.class.getName(), Constants.ARRAY_FILTER_CONTROL, dataSetId, userId);					 
					 table.setContainerDataSource(parent.tabularView(1, Constants.DEFAULT_PAGE_SIZE, dataSetId, tabViewPreferences));
					
					 parent.setPaginationBar();
					 getApplication().getMainWindow().removeWindow(getFilterWindow());
					 
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		 
		submit.setClickShortcut(KeyCode.ENTER);
		gridLayout1.addComponent(markerContextCB, 0, 0);
		gridLayout1.addComponent(arrayContextCB, 1, 0);
		gridLayout1.addComponent(markerSetSelect, 0 , 1);
		gridLayout1.addComponent(arraySetSelect, 1, 1);
		gridLayout1.addComponent(spaceLabel, 0, 2);	
		gridLayout1.addComponent(submit, 0, 3);				 
		addComponent(gridLayout1);
		 
    }
    
    
    
    private String[] getSelectedSet(String selectedList)
	{
		String[] selectedSet = null;
		
		 
			if (!selectedList.equals("[]"))					 
				selectedSet = selectedList.substring(1,
						selectedList.length() - 1).split(",");	        
		 
		
		return selectedSet;
	}
    
    
    private FilterWindow getFilterWindow()
    {
    	return this;
    }
    
  
}
