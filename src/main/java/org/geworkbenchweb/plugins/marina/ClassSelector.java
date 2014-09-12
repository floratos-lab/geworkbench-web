package org.geworkbenchweb.plugins.marina;

import java.util.List;

import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;

 
public class ClassSelector extends FormLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6214265705833965798L;
	
 
	private final String ARRAYCONTEXT = "ArrayContext";
	 
	 
	private ComboBox arrayContextCB;
	private Long dataSetId;
	private long userId;
	private String parentName = null;	 
	private boolean isArrayContextSetByApp = false;
	
	private ListSelect class1ArraySelect = new ListSelect();
	private ListSelect class2ArraySelect = new ListSelect();
	private TextField tf1 = new TextField();
	private TextField tf2 = new TextField();
	private HorizontalLayout h1 = new HorizontalLayout();
	private HorizontalLayout h2 = new HorizontalLayout();
	private MarinaUI marinaUI = null;
	
	
	public ClassSelector(Long dataSetId, Long userId, String parentName, MarinaUI parent) {
 
		this.parentName = parentName;
		this.dataSetId = dataSetId;
		this.userId = userId;
	  
		marinaUI = parent;
		
		
		arrayContextCB =  new ComboBox("Array Context");
		arrayContextCB.setWidth("140px");
		arrayContextCB.setImmediate(true);	
		arrayContextCB.setNullSelectionAllowed(false);
		arrayContextCB.setDescription("The context of microarray sets.");
	 
		
		class1ArraySelect = new ListSelect();
		class1ArraySelect.setMultiSelect(true);
		class1ArraySelect.setRows(4);
		class1ArraySelect.setColumns(11);
		class1ArraySelect.setImmediate(true);
		h1.setDescription("Case microarray set.");
		
		class2ArraySelect = new ListSelect();
		class2ArraySelect.setMultiSelect(true);
		class2ArraySelect.setRows(4);
		class2ArraySelect.setColumns(11);
		class2ArraySelect.setImmediate(true);
		h2.setDescription("Control microarray set.");
		
		tf1.setEnabled(false);
		tf2.setEnabled(false);
		
		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;
			public void valueChange(ValueChangeEvent event) {						 
			 
				Object val = arrayContextCB.getValue();
				if (val != null){
					Context context = (Context)val;							 
					List<SubSet> arraySubSets = SubSetOperations.getSubSetsForContext(context);
					 
					class1ArraySelect.removeAllItems();
					class2ArraySelect.removeAllItems();
					marinaUI.arraymap.clear();
					for (int m = 0; m < (arraySubSets).size(); m++) {	
						SubSet arraySubSet = (SubSet)arraySubSets.get(m);
						class1ArraySelect.addItem(arraySubSet.getId());
						class1ArraySelect.setItemCaption(
								 arraySubSet.getId(),
								 arraySubSet.getName());	
						class2ArraySelect.addItem(arraySubSet.getId());
						class2ArraySelect.setItemCaption(
								arraySubSet.getId(),
								arraySubSet.getName());	
						
						
						List<String> pos = arraySubSet.getPositions();
						if (pos == null || pos.isEmpty()) continue;
						StringBuilder builder = new StringBuilder();
						
						for(int i=0; i<pos.size(); i++) {
							builder.append(pos.get(i)+",");
						}
						
						String positions = builder.toString();
						marinaUI.arraymap.put(arraySubSet.getId().toString(), positions.substring(0, positions.length()-1));		
						
						
					}
					
					tf1.setEnabled(false);
					tf2.setEnabled(false);
					
					tf1.setValue("");
					tf2.setValue("");
					
					if (!isArrayContextSetByApp)					 
						saveArrayContextPreference();
					isArrayContextSetByApp = false;
					
				}
				
				
			}
		});
		
		
		
 

		class1ArraySelect.addListener( new Property.ValueChangeListener(){
			private static final long serialVersionUID = -3667564667049184754L;
			public void valueChange(ValueChangeEvent event) {
				String class1Arrays = getClassArrays(getClass1ArraySet());
				marinaUI.bean.setClass1(class1Arrays);				
				tf1.setValue(class1Arrays);
				if (class1Arrays == null || class1Arrays.trim().length() == 0 ) {
					tf1.setEnabled(false);
					marinaUI.submitButton.setEnabled(false);
				}else{
					tf1.setEnabled(true);
					if (marinaUI.isNetworkNameEnabled())
						marinaUI.submitButton.setEnabled(true);
				}
			}
		});
		class2ArraySelect.addListener( new Property.ValueChangeListener(){
			private static final long serialVersionUID = -5177825730266428335L;
			public void valueChange(ValueChangeEvent event) {
				String class2Arrays = getClassArrays(getClass2ArraySet());
				marinaUI.bean.setClass2(class2Arrays);	
				tf2.setValue(class2Arrays);
				if (class2Arrays == null || class2Arrays.trim().length() == 0){
					tf2.setEnabled(false);
				}else{
					tf2.setEnabled(true);
				}
			}
		});

		h1.setSpacing(true);
		h1.setCaption("Case");
		h1.addComponent(class1ArraySelect);
		h1.addComponent(tf1);
		h2.setSpacing(true);
		h2.setCaption("Control");
		h2.addComponent(class2ArraySelect);
		h2.addComponent(tf2);
		
		 
	}
	
	
	public void setData(Long dataSetId, Long userId)
	{		
	 
		this.dataSetId = dataSetId;
		this.userId = userId;
		
		Context selectedArrayContext = null;
		Preference pref = PreferenceOperations.getData(dataSetId, parentName + "." + ARRAYCONTEXT , userId);
		if (pref != null)
		{	
			selectedArrayContext = (Context)ObjectConversion.toObject(pref.getValue());
		}
		if (selectedArrayContext == null)
			selectedArrayContext = SubSetOperations.getCurrentArrayContext(dataSetId);
		List<Context> contexts = SubSetOperations.getArrayContexts(dataSetId);		 
		arrayContextCB.removeAllItems();
		for (Context c : contexts){
			arrayContextCB.addItem(c);	
			if (selectedArrayContext!=null && c.getId().longValue()==selectedArrayContext.getId().longValue()) 
			{
				isArrayContextSetByApp = true;
				arrayContextCB.setValue(c);
			}
		}	
	 
	
	}
	
	
	public  String[] getClass1ArraySet() {
		String[] selectList = null;
		String selectStr = class1ArraySelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}
	
	
	
	public  String[] getClass2ArraySet() {
		String[] selectList = null;
		String selectStr = class2ArraySelect.getValue().toString();
		if (!selectStr.equals("[]"))
		{
			selectList = selectStr.substring(1, selectStr.length()-1).split(",");			 
			
		}
			
		return selectList;
	}	
	
	
	public  String getClassArrays(String[] selectList) {	 
		StringBuilder builder = new StringBuilder();		
		if (selectList != null && selectList.length > 0)
		{
			builder.append(marinaUI.arraymap.get(selectList[0].trim()));
		    for (int i=1; i<selectList.length; i++)		 
			    builder.append("," + marinaUI.arraymap.get(selectList[i].trim()));
		} 
		return builder.toString();
	}	
	
    
    private void saveArrayContextPreference()
    {

		Context markerContext = (Context)arrayContextCB.getValue();
		Preference p = PreferenceOperations.getData(dataSetId,
				parentName + "." + ARRAYCONTEXT, userId);
				 
		if (p != null)
			PreferenceOperations.setValue(
					markerContext, p);
		else
			PreferenceOperations.storeData(
					markerContext,
					Context.class.getName(),
					parentName + "." + ARRAYCONTEXT,
					dataSetId, userId);
    }
    
    
    public ComboBox getArrayContextCB()
    {
    	return arrayContextCB;
    	
    }
    
    public HorizontalLayout getH1()
    {
    	return h1;
    }
    
    public HorizontalLayout getH2()
    {
    	return h2;
    }
    
    public TextField getTf1()
    {
    	return tf1;
    }
}
