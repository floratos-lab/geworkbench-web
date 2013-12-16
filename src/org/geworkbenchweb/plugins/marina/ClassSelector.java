package org.geworkbenchweb.plugins.marina;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geworkbench.util.Util;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.MicroarrayDataset;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.FieldType;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.Ostermiller.util.ExcelCSVParser;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
 
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
	 
		
		class1ArraySelect = new ListSelect();
		class1ArraySelect.setMultiSelect(true);
		class1ArraySelect.setRows(4);
		class1ArraySelect.setColumns(11);
		class1ArraySelect.setImmediate(true);
		
		class2ArraySelect = new ListSelect();
		class2ArraySelect.setMultiSelect(true);
		class2ArraySelect.setRows(4);
		class2ArraySelect.setColumns(11);
		class2ArraySelect.setImmediate(true);
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
						
						
						ArrayList<String> pos = arraySubSet.getPositions();
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
					if (marinaUI.form.getField("network").isEnabled())
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
		
		UploadField uploadField1 = new UploadField(){
			private static final long serialVersionUID = 3738084401913970304L;
            protected void updateDisplay() {
        		byte[] bytes = (byte[]) getValue();
        		String filename = getLastFileName();
	            if (filename.endsWith(".csv")||filename.endsWith(".CSV")){
	            	String newset = parseCSV(filename, bytes);
	            	Iterator<?> iterator = class1ArraySelect.getItemIds().iterator();
	            	while (iterator.hasNext())
	            	  class1ArraySelect.unselect(iterator.next());
	            	if (newset != null) class1ArraySelect.select(newset);
        		}else{
					MessageBox mb = new MessageBox(getWindow(),
							"File Format Error", MessageBox.Icon.WARN,
							filename + " is not a CSV file",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
        		}
            }
        };
        uploadField1.setStorageMode(StorageMode.MEMORY);
        uploadField1.setFieldType(FieldType.BYTE_ARRAY);

		UploadField uploadField2 = new UploadField(){
			private static final long serialVersionUID = 3738084401913970304L;
            protected void updateDisplay() {
        		byte[] bytes = (byte[]) getValue();
        		String filename = getLastFileName();
	            if (filename.endsWith(".csv")||filename.endsWith(".CSV")){
	            	Iterator<?> iterator = class2ArraySelect.getItemIds().iterator();
	            	while (iterator.hasNext())
	            	  class2ArraySelect.unselect(iterator.next());
	            	String newset = parseCSV(filename, bytes);
	            	if (newset != null) class2ArraySelect.select(newset);
        		}else{
					MessageBox mb = new MessageBox(getWindow(),
							"File Format Error", MessageBox.Icon.WARN,
							filename + " is not a CSV file",
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show();
        		}
            }
        };
        uploadField2.setStorageMode(StorageMode.MEMORY);
        uploadField2.setFieldType(FieldType.BYTE_ARRAY);

		h1.setSpacing(true);
		h1.setCaption("Case");
		h1.addComponent(class1ArraySelect);
		h1.addComponent(tf1);
		h1.addComponent(uploadField1);
		h2.setSpacing(true);
		h2.setCaption("Control");
		h2.addComponent(class2ArraySelect);
		h2.addComponent(tf2);
		h2.addComponent(uploadField2);
		
		 
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
    
    
    private String parseCSV(String filename, byte[] bytes){
		DataSet dataset = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long id = dataset.getDataId();
		MicroarrayDataset microarray = FacadeFactory.getFacade().find(
				MicroarrayDataset.class, id);
		String[] arrayLabels = microarray.getArrayLabels(); // TODO only arrayLabels needed, not the entire dataset

		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		if (filename.toLowerCase().endsWith(".csv")) {
			filename = filename.substring(0, filename.length() - 4);
		}
		// Ensure loaded set has unique name
		Set<String> nameSet = new HashSet<String>();
		nameSet.addAll(marinaUI.arraymap.keySet());

		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		try {
			ExcelCSVParser parser = new ExcelCSVParser(inputStream);
			String[][] data = parser.getAllValues();
			for (int i = 0; i < data.length; i++) {
				String[] line = data[i];
				if (line.length > 0) {
					String setname = (line.length > 1 && line[1].trim().length() > 0)?
									  line[1].trim() : filename;
					List<String> selectedNames = map.get(setname);
					if (selectedNames == null){
						selectedNames = new ArrayList<String>();
						map.put(setname, selectedNames);
					}
					selectedNames.add(line[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Lost cause
				}
			}
		}

		int missing = 0;
		String aNewSet = null;
		for (String setname : map.keySet()){
			List<String> selectedNames = map.get(setname);
			setname = Util.getUniqueName(setname, nameSet);
			nameSet.add(setname);
            int setsize=0;
            StringBuilder builder = new StringBuilder();
			for(String arrayLabel: arrayLabels) {
				if(selectedNames.contains(arrayLabel)) {
					builder.append(arrayLabel+",");
					setsize++;
				}
			}
			if(setsize != selectedNames.size())
				missing += selectedNames.size() - setsize;
		
			if (setsize > 0) {
				String positions = builder.toString();
				marinaUI.arraymap.put(setname, positions.substring(0, positions.length()-1));
				class1ArraySelect.addItem(setname);
				class2ArraySelect.addItem(setname);
				aNewSet = setname;
			}
		}
		if(missing > 0) {
			if (missing == 1){
				MessageBox mb = new MessageBox(
						getWindow(),
						"Array Not Found",
						MessageBox.Icon.WARN,
						missing + " array listed in the CSV file is not present in the dataset.  Skipped.",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
			}else{
				MessageBox mb = new MessageBox(
						getWindow(),
						"Array Not Found",
						MessageBox.Icon.WARN,
						missing + " arrays listed in the CSV file are not present in the dataset.  Skipped.",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
			}
		}
		return aNewSet;
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
