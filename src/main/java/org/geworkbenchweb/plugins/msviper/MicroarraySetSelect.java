package org.geworkbenchweb.plugins.msviper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.SubSet;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;

public class MicroarraySetSelect extends HorizontalLayout {

	private static final long serialVersionUID = -562495103003506549L;

	private final ListSelect arraySetSelect;

	private final TextField textField = new TextField();

	private final MsViperUI viperUI;
	 
	private  Map<String, String> arrayGroupIdNameMap = null;
	
	private final String type;
 
	void reset(List<SubSet> arraySubSets) {
		arraySetSelect.removeAllItems();
		viperUI.arraymap.clear();
		arrayGroupIdNameMap = new HashMap<String, String>();
		for (int m = 0; m < (arraySubSets).size(); m++) {
			SubSet arraySubSet = (SubSet) arraySubSets.get(m);
			arraySetSelect.addItem(arraySubSet.getId());
			arraySetSelect.setItemCaption(arraySubSet.getId(),
					arraySubSet.getName());

			List<String> pos = arraySubSet.getPositions();
			if (pos == null || pos.isEmpty())
				continue;
			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < pos.size(); i++) {
				builder.append(pos.get(i) + ",");
			}

			String positions = builder.toString();
			viperUI.arraymap.put(arraySubSet.getId().toString(),
					positions.substring(0, positions.length() - 1));
			arrayGroupIdNameMap.put(arraySubSet.getId().toString(), arraySubSet.getName());

		}

		textField.setEnabled(false);
		textField.setValue("");
	}

	public MicroarraySetSelect(Long dataSetId, Long userId, String parentName,
			MsViperUI parent, String caption, String description, String selectType) {
		 
		viperUI = parent;
        type = selectType;
		arraySetSelect = new ListSelect();
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(4);
		arraySetSelect.setColumns(11);
		arraySetSelect.setImmediate(true);

		textField.setEnabled(false);

		arraySetSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -3667564667049184754L;

			public void valueChange(ValueChangeEvent event) {			 
				String class1Arrays = getClassArrays(getArraySet());
				if (type.equalsIgnoreCase(viperUI.CASE))					
				{
					viperUI.param.setClassCase(getClassArrayMap(getArraySet()));
					viperUI.param.setCaseGroup(getGroupNames(getArraySet()));
				}
				else
				{
					viperUI.param.setClassControl(getClassArrayMap(getArraySet()));
					viperUI.param.setControlGroup(getGroupNames(getArraySet()));
				}
				textField.setValue(getClassArrays(getArraySet()));
				if (class1Arrays == null || class1Arrays.trim().length() == 0) {
					textField.setEnabled(false);
					 
				} else {
					textField.setEnabled(true);
					if ( viperUI.isNetworkNameEnabled())
						viperUI.submitButton.setEnabled(true);
				}
			}
		});

		this.setSpacing(true);
		this.setCaption(caption+" \uFFFD");
		this.addComponent(arraySetSelect);
		this.addComponent(textField);

//		this.setIcon(infoIcon);
		this.setDescription(description);
	}

	public String[] getArraySet() {
		String[] selectList = null;
		String selectStr = arraySetSelect.getValue().toString();
		if (!selectStr.equals("[]")) {
			selectList = selectStr.substring(1, selectStr.length() - 1).split(
					",");

		}

		return selectList;
	}

	private String getClassArrays(String[] selectList) {
		StringBuilder builder = new StringBuilder();
		if (selectList != null && selectList.length > 0) {
			builder.append(viperUI.arraymap.get(selectList[0].trim()));
			for (int i = 1; i < selectList.length; i++)
				builder.append(","
						+ viperUI.arraymap.get(selectList[i].trim()));
		}
		return builder.toString();
	}
	
	private Map<String, String> getClassArrayMap(String[] selectList) {
		Map<String, String> selectedGroupMap = new HashMap<String, String>();
		if (selectList != null && selectList.length > 0) {
			 
			for (int i = 0; i < selectList.length; i++)
			{	
				String[] arrays = viperUI.arraymap.get(selectList[i].trim()).split(",");
				String groupName = arrayGroupIdNameMap.get(selectList[i].trim());
				for(int j=0; j<arrays.length; j++)
				{
					selectedGroupMap.put(arrays[j], groupName);
				}
				 
			
			}
		}
		return selectedGroupMap;
	}
	
	private String getGroupNames(String[] selectList) {
		 
		String s = "";
		if (selectList != null && selectList.length > 0) {
			 
			for (int i = 0; i < selectList.length; i++)
			{	
				if (i == 0)
				  s = arrayGroupIdNameMap.get(selectList[i].trim());
				else
				  s = s + "," + arrayGroupIdNameMap.get(selectList[i].trim());
				
			}
		}
		return s;
	}
	
	
	

	public boolean isTextFieldEnabled() {
		return textField.isEnabled();
	}
}
