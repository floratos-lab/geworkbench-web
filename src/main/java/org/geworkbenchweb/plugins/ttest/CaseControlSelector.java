package org.geworkbenchweb.plugins.ttest;

import java.util.List;
 
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.ObjectConversion;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
 
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout; 
import com.vaadin.ui.ListSelect; 
 
public class CaseControlSelector extends GridLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6214265705833965798L;
	
	private final String ARRAYCONTEXT = "ArrayContext";
 
	private ComboBox arrayContextCB;
	private ListSelect caseArraySetSelect;
	private ListSelect controlArraySetSelect;
	
	
	
	private Long dataSetId;
	private long userId;
	private String parentName = null;
	private boolean isArrayContextSetByApp = false;
	 
	
	public CaseControlSelector(Long dataSetId,  String parentName) {
 
		this.parentName = parentName;
		this.dataSetId = dataSetId;	 
		setColumns(2);
		setRows(3);
		setSpacing(true);
		setImmediate(true);	
		
	 
		
		arrayContextCB =  new ComboBox("Array Context");
		arrayContextCB.setWidth("160px");
		arrayContextCB.setImmediate(true);	
		arrayContextCB.setNullSelectionAllowed(false);
		
		caseArraySetSelect = new ListSelect("Select Case Sets:");
		caseArraySetSelect.setMultiSelect(true);
		caseArraySetSelect.setRows(5);
		caseArraySetSelect.setColumns(15);
		caseArraySetSelect.setImmediate(true);
		
		controlArraySetSelect = new ListSelect("Select Control Sets:");
		controlArraySetSelect.setMultiSelect(true);
		controlArraySetSelect.setRows(5);
		controlArraySetSelect.setColumns(15);
		controlArraySetSelect.setImmediate(true);
	 
	 
		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = arrayContextCB.getValue();
				if (val != null){
					Context context = (Context)val;							 
					List<SubSet> arraySubSets = SubSetOperations.getSubSetsForContext(context);
					 
					caseArraySetSelect.removeAllItems();
					controlArraySetSelect.removeAllItems();
					for (int m = 0; m < (arraySubSets).size(); m++) {					 
						caseArraySetSelect.addItem(((SubSet) arraySubSets.get(m)).getId());
						caseArraySetSelect.setItemCaption(
								((SubSet) arraySubSets.get(m)).getId(),
								((SubSet) arraySubSets.get(m)).getName());	
						controlArraySetSelect.addItem(((SubSet) arraySubSets.get(m)).getId());
						controlArraySetSelect.setItemCaption(
								((SubSet) arraySubSets.get(m)).getId(),
								((SubSet) arraySubSets.get(m)).getName());
					 
					}
					
					if (!isArrayContextSetByApp)					 
						saveArrayContextPreference();
					isArrayContextSetByApp = false;
					
				}
				
				
			}
		});

	
	 
	 
		 
		 
		addComponent(arrayContextCB, 0, 0);
	    addComponent(caseArraySetSelect, 0 , 1);
		addComponent(controlArraySetSelect, 1, 1);
		 
		 
		 
	}
	
	
	public void setData(Long dataSetId, Long userId)
	{		
	 
		this.dataSetId = dataSetId;
		this.userId = userId;
		 
		Context selectedtArrayContext = null;
		Preference pref = PreferenceOperations.getData(dataSetId, parentName + "." + ARRAYCONTEXT , userId);
		if (pref != null)
		{	
			selectedtArrayContext = (Context)ObjectConversion.toObject(pref.getValue());
		}
		if (selectedtArrayContext == null)
			selectedtArrayContext = SubSetOperations.getCurrentArrayContext(dataSetId);
	    List<Context> contexts = SubSetOperations.getArrayContexts(dataSetId);		 
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
	
	
	public  String[] getSelectedCaseSet() {
		String[] selectList = null;
		String selectStr = caseArraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}
	
	public  String[] getSelectedControlSet() {		 
		String[] selectList = null;
		String selectStr = controlArraySetSelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
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
