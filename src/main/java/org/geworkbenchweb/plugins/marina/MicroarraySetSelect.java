package org.geworkbenchweb.plugins.marina;

import java.util.List;

import org.geworkbenchweb.pojos.SubSet;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;

public class MicroarraySetSelect extends HorizontalLayout {

	private static final long serialVersionUID = -562495103003506549L;

	private final ListSelect arraySetSelect;

	private final TextField textField = new TextField();

	private final MarinaUI marinaUI;
	
	private final boolean required;

	private final static ThemeResource infoIcon = new ThemeResource(
			"../custom/icons/icon_info.gif");

	void reset(List<SubSet> arraySubSets) {
		arraySetSelect.removeAllItems();
		marinaUI.arraymap.clear();
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
			marinaUI.arraymap.put(arraySubSet.getId().toString(),
					positions.substring(0, positions.length() - 1));

		}

		textField.setEnabled(false);
		textField.setValue("");
	}

	public MicroarraySetSelect(Long dataSetId, Long userId, String parentName,
			MarinaUI parent, String caption, String description, boolean requiredForComputation) {
		this.required = requiredForComputation;

		marinaUI = parent;

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
				marinaUI.bean.setClass1(class1Arrays);
				textField.setValue(class1Arrays);
				if (class1Arrays == null || class1Arrays.trim().length() == 0) {
					textField.setEnabled(false);
					if(required) marinaUI.submitButton.setEnabled(false);
				} else {
					textField.setEnabled(true);
					if (required && marinaUI.isNetworkNameEnabled())
						marinaUI.submitButton.setEnabled(true);
				}
			}
		});

		this.setSpacing(true);
		this.setCaption("\u2605  "+caption);
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
			builder.append(marinaUI.arraymap.get(selectList[0].trim()));
			for (int i = 1; i < selectList.length; i++)
				builder.append(","
						+ marinaUI.arraymap.get(selectList[i].trim()));
		}
		return builder.toString();
	}

	public boolean isTextFieldEnabled() {
		return textField.isEnabled();
	}
}
