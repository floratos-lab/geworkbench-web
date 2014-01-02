package org.geworkbenchweb.plugins.tabularview;

import java.util.List;

import org.geworkbenchweb.plugins.Tabular;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.Preference;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.PreferenceOperations;
import org.geworkbenchweb.utils.SubSetOperations;

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

	private GridLayout gridLayout1;
	private ComboBox markerContextCB;
	private ListSelect markerSetSelect;
	private ComboBox arrayContextCB;
	private ListSelect arraySetSelect;
	private Button submit;
	private long datasetId;

	public FilterWindow(final Tabular parent) {

		datasetId = parent.getDatasetId();
		gridLayout1 = new GridLayout(2, 4);
		gridLayout1.setSpacing(true);
		gridLayout1.setImmediate(true);

		setModal(true);
		setClosable(true);
		 
		setWidth("500px");
		setHeight("320px");
		setResizable(false);
		setCaption("Filter Setting");
		setImmediate(true);

		Label spaceLabel = new Label("                       ");

		markerContextCB = new ComboBox("Marker Context");
		markerContextCB.setWidth("160px");
		markerContextCB.setImmediate(true);
		markerContextCB.setNullSelectionAllowed(false);

		markerSetSelect = new ListSelect("Select Marker Sets:");
		markerSetSelect.setMultiSelect(true);
		markerSetSelect.setRows(5);
		markerSetSelect.setColumns(15);
		markerSetSelect.setImmediate(true);

		arrayContextCB = new ComboBox("Array Context");
		arrayContextCB.setWidth("160px");
		arrayContextCB.setImmediate(true);
		arrayContextCB.setNullSelectionAllowed(false);

		arraySetSelect = new ListSelect("Select Array Sets:");
		arraySetSelect.setMultiSelect(true);
		arraySetSelect.setRows(5);
		arraySetSelect.setColumns(15);
		arraySetSelect.setImmediate(true);

		markerContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;

			public void valueChange(ValueChangeEvent event) {

				Object val = markerContextCB.getValue();
				if (val != null) {
					Context context = (Context) val;
					List<SubSet> markerSubSets = SubSetOperations
							.getSubSetsForContext(context);
					markerSetSelect.removeAllItems();
					markerSetSelect.addItem("All Markers");
					for (int m = 0; m < (markerSubSets).size(); m++) {
						markerSetSelect.addItem(((SubSet) markerSubSets.get(m))
								.getId());
						markerSetSelect.setItemCaption(
								((SubSet) markerSubSets.get(m)).getId(),
								((SubSet) markerSubSets.get(m)).getName());

					}
				}
			}
		});

		arrayContextCB.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;

			public void valueChange(ValueChangeEvent event) {

				Object val = arrayContextCB.getValue();
				if (val != null) {
					Context context = (Context) val;
					List<SubSet> arraySubSets = SubSetOperations
							.getSubSetsForContext(context);
					arraySetSelect.removeAllItems();
					arraySetSelect.addItem("All Arrays");
					for (int m = 0; m < (arraySubSets).size(); m++) {
						arraySetSelect.addItem(((SubSet) arraySubSets.get(m))
								.getId());
						arraySetSelect.setItemCaption(
								((SubSet) arraySubSets.get(m)).getId(),
								((SubSet) arraySubSets.get(m)).getName());

					}
				}
			}
		});

		TabularViewPreferences tabViewPreferences = ((TabularViewUI) parent)
				.getTabViewPreferences();
		Context selectedtMarkerContext = null;

		if (tabViewPreferences.getMarkerFilter() != null)
			selectedtMarkerContext = tabViewPreferences.getMarkerFilter()
					.getContext();
		if (selectedtMarkerContext == null)
			selectedtMarkerContext = SubSetOperations
					.getCurrentMarkerContext(datasetId);
		List<Context> contexts = SubSetOperations.getMarkerContexts(datasetId);
		for (Context c : contexts) {
			markerContextCB.addItem(c);
			if (selectedtMarkerContext != null
					&& c.getId().longValue() == selectedtMarkerContext.getId()
							.longValue())
				markerContextCB.setValue(c);
		}

		 
		Context selectedtArrayContext = null;
		if (tabViewPreferences.getArrayFilter() != null)
			selectedtArrayContext = tabViewPreferences.getArrayFilter().getContext();
		if (selectedtArrayContext == null)
			selectedtArrayContext = SubSetOperations
					.getCurrentArrayContext(datasetId);
		contexts = SubSetOperations.getArrayContexts(datasetId);
		for (Context c : contexts) {
			arrayContextCB.addItem(c);
			if (selectedtArrayContext != null
					&& c.getId().longValue() == selectedtArrayContext.getId()
							.longValue())
				arrayContextCB.setValue(c);
		}

		 
		String[] selectedMarkerSet = null;
		if (tabViewPreferences.getMarkerFilter() != null)
			selectedMarkerSet = tabViewPreferences.getMarkerFilter()
					.getSelectedSet();
		if (selectedMarkerSet != null && selectedMarkerSet.length > 0) {
			int startIndex = 0;
			if (selectedMarkerSet[0].equalsIgnoreCase("All Markers")) {
				startIndex = startIndex + 1;
				markerSetSelect.select("All Markers");
			}
			for (int i = startIndex; i < selectedMarkerSet.length; i++) {
				markerSetSelect.select(new Long(selectedMarkerSet[i].trim()));
			}
		}

		String[] selectedArraySet = null;
		if (tabViewPreferences.getArrayFilter() != null)
			selectedArraySet = tabViewPreferences.getArrayFilter()
					.getSelectedSet();
		if (selectedArraySet != null && selectedArraySet.length > 0) {
			int startIndex = 0;
			if (selectedArraySet[0].equalsIgnoreCase("All Arrays")) {
				startIndex = startIndex + 1;
				arraySetSelect.select("All Arrays");
			}
			for (int i = startIndex; i < selectedArraySet.length; i++)
				arraySetSelect.select(new Long(selectedArraySet[i].trim()));
		}

		submit = new Button("Submit");

		submit.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4799561372701936132L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {

					FilterInfo markerFilter = getMarkerFilter();

					Preference p = PreferenceOperations.getData(datasetId,
							Constants.MARKER_FILTER_CONTROL, parent.getUserId());
					if (p != null)
						PreferenceOperations.setValue(markerFilter, p);
					else
						PreferenceOperations.storeData(markerFilter,
								FilterInfo.class.getName(),
								Constants.MARKER_FILTER_CONTROL, datasetId,
								parent.getUserId());

					FilterInfo arrayFilter = getArrayFilter();
					p = PreferenceOperations.getData(datasetId,
							Constants.ARRAY_FILTER_CONTROL, parent.getUserId());
					if (p != null)
						PreferenceOperations.setValue(arrayFilter, p);
					else
						PreferenceOperations.storeData(arrayFilter,
								FilterInfo.class.getName(),
								Constants.ARRAY_FILTER_CONTROL, datasetId,
								parent.getUserId());
					
					parent.setSearchStr(null);
					parent.getPagedTableView().setContainerDataSource(
							parent.getIndexedContainer());

					getApplication().getMainWindow().removeWindow(
							getFilterWindow());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		submit.setClickShortcut(KeyCode.ENTER);
		gridLayout1.addComponent(markerContextCB, 0, 0);
		gridLayout1.addComponent(arrayContextCB, 1, 0);
		gridLayout1.addComponent(markerSetSelect, 0, 1);
		gridLayout1.addComponent(arraySetSelect, 1, 1);
		gridLayout1.addComponent(spaceLabel, 0, 2);
		gridLayout1.addComponent(submit, 0, 3);
		addComponent(gridLayout1);

	}

	private String[] getSelectedSet(String selectedList) {
		String[] selectedSet = null;

		if (!selectedList.equals("[]"))
			selectedSet = selectedList.substring(1, selectedList.length() - 1)
					.split(",");

		return selectedSet;
	}

	private FilterInfo getMarkerFilter() {
		String value = markerSetSelect.getValue().toString();
		FilterInfo markerFilter = new FilterInfo(null, getSelectedSet(value));
		return markerFilter;
	}

	private FilterInfo getArrayFilter() {
		String value = arraySetSelect.getValue().toString();
		FilterInfo arrayFilter = new FilterInfo(
				(Context) arrayContextCB.getValue(), getSelectedSet(value));
		return arrayFilter;
	}

	private FilterWindow getFilterWindow() {
		return this;
	}

}
