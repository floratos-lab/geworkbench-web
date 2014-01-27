package org.geworkbenchweb.plugins.tabularview;

import java.util.List;
import java.util.Set;

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
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class FilterWindow extends Window {

	private static final long serialVersionUID = 5480097015206241444L;

	final private ComboBox markerContextCB;
	final private ListSelect markerSetSelect;
	final private ComboBox arrayContextCB;
	final private ListSelect arraySetSelect;

	public FilterWindow(final Tabular parent) {

		markerSetSelect = createSetSelect("Select Marker Sets:");
		arraySetSelect = createSetSelect("Select Array Sets:");

		TabularViewPreferences tabViewPreferences = ((TabularViewUI) parent)
				.getTabViewPreferences();

		Long datasetId = parent.getDatasetId();
		Context selectedtMarkerContext = null;
		if (tabViewPreferences.getMarkerFilter() != null)
			selectedtMarkerContext = tabViewPreferences.getMarkerFilter()
					.getContext();
		if (selectedtMarkerContext == null)
			selectedtMarkerContext = SubSetOperations
					.getCurrentMarkerContext(datasetId);
		List<Context> markerContexts = SubSetOperations.getMarkerContexts(datasetId);
		markerContextCB = createContextCombo("Marker Context", markerContexts,
				selectedtMarkerContext, markerSetSelect, "All Markers");

		Context selectedtArrayContext = null;
		if (tabViewPreferences.getArrayFilter() != null)
			selectedtArrayContext = tabViewPreferences.getArrayFilter()
					.getContext();
		if (selectedtArrayContext == null)
			selectedtArrayContext = SubSetOperations
					.getCurrentArrayContext(datasetId);
		List<Context> arrayContexts = SubSetOperations.getArrayContexts(datasetId);
		arrayContextCB = createContextCombo("Array Context", arrayContexts,
				selectedtArrayContext, arraySetSelect, "All Arrays");

		Long[] selectedMarkerSet = null;
		if (tabViewPreferences.getMarkerFilter() != null)
			selectedMarkerSet = tabViewPreferences.getMarkerFilter()
					.getSelectedSet();
		setSelection(markerSetSelect, selectedMarkerSet, "All Markers");

		Long[] selectedArraySet = null;
		if (tabViewPreferences.getArrayFilter() != null)
			selectedArraySet = tabViewPreferences.getArrayFilter()
					.getSelectedSet();
		setSelection(arraySetSelect, selectedArraySet, "All Arrays");
		
		Button submit = new Button("Submit");
		submit.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4799561372701936132L;

			@Override
			public void buttonClick(ClickEvent event) {
				filter(parent);
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		
		GridLayout gridLayout1 = new GridLayout(2, 4);
		gridLayout1.setSpacing(true);
		gridLayout1.setImmediate(true);
		gridLayout1.addComponent(markerContextCB, 0, 0);
		gridLayout1.addComponent(arrayContextCB, 1, 0);
		gridLayout1.addComponent(markerSetSelect, 0, 1);
		gridLayout1.addComponent(arraySetSelect, 1, 1);
		gridLayout1.addComponent(new Label("                       "), 0, 2);
		gridLayout1.addComponent(submit, 0, 3);

		setModal(true);
		setClosable(true);
		setWidth("500px");
		setHeight("320px");
		setResizable(false);
		setCaption("Filter Setting");
		setImmediate(true);
		setContent(gridLayout1);
	}

	static private ListSelect createSetSelect(String caption) {
		ListSelect listSelect = new ListSelect(caption);
		listSelect.setMultiSelect(true);
		listSelect.setRows(5);
		listSelect.setColumns(15);
		listSelect.setImmediate(true);

		return listSelect;
	}
	
	/* This needs to be called AFTER the items are added. */
	static private void setSelection(ListSelect listSelect, Long[] selectedSet,
			String allSelected) {
		if (selectedSet != null && selectedSet.length > 0) {
			int startIndex = 0;
			if (selectedSet[0] == 0) {
				startIndex = startIndex + 1;
				listSelect.select(allSelected);
			}
			for (int i = startIndex; i < selectedSet.length; i++) {
				listSelect.select(selectedSet[i]);
			}
		}
	}
	
	static private ComboBox createContextCombo(String caption, List<Context> contexts,
			Context selectedtArrayContext, final ListSelect listSelect,
			final String allSelected) {
		final ComboBox contextCB = new ComboBox(caption);
		contextCB.setWidth("160px");
		contextCB.setImmediate(true);
		contextCB.setNullSelectionAllowed(false);

		contextCB.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 5667499645414167736L;

			public void valueChange(ValueChangeEvent event) {

				Object val = contextCB.getValue();
				if (val != null) {
					Context context = (Context) val;
					List<SubSet> subsets = SubSetOperations
							.getSubSetsForContext(context);
					listSelect.removeAllItems();
					listSelect.addItem(allSelected);
					for (int m = 0; m < (subsets).size(); m++) {
						listSelect.addItem(((SubSet) subsets.get(m)).getId());
						listSelect.setItemCaption(
								((SubSet) subsets.get(m)).getId(),
								((SubSet) subsets.get(m)).getName());

					}
				}
			}
		});
		
		for (Context c : contexts) {
			contextCB.addItem(c);
			if (selectedtArrayContext != null
					&& c.getId().longValue() == selectedtArrayContext.getId()
							.longValue())
				contextCB.setValue(c);
		}

		return contextCB;
	}
	
	private void filter(final Tabular parent) {
		try {
			Long datasetId = parent.getDatasetId();
			Long userId = parent.getUserId();

			FilterInfo markerFilter = new FilterInfo(
					(Context) markerContextCB.getValue(),
					getSelectedSet(markerSetSelect));
			Preference p1 = PreferenceOperations.getData(datasetId,
					Constants.MARKER_FILTER_CONTROL, userId);
			if (p1 != null)
				PreferenceOperations.setValue(markerFilter, p1);
			else
				PreferenceOperations.storeData(markerFilter,
						FilterInfo.class.getName(),
						Constants.MARKER_FILTER_CONTROL, datasetId, userId);

			FilterInfo arrayFilter = new FilterInfo(
					(Context) arrayContextCB.getValue(),
					getSelectedSet(arraySetSelect));
			Preference p2 = PreferenceOperations.getData(datasetId,
					Constants.ARRAY_FILTER_CONTROL, userId);
			if (p2 != null)
				PreferenceOperations.setValue(arrayFilter, p2);
			else
				PreferenceOperations.storeData(arrayFilter,
						FilterInfo.class.getName(),
						Constants.ARRAY_FILTER_CONTROL, datasetId, userId);

			parent.setSearchStr(null);
			parent.getPagedTableView().setContainerDataSource(
					parent.getIndexedContainer());

			UI.getCurrent().removeWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static private Long[] getSelectedSet(ListSelect listSelect) {
		Object object = listSelect.getValue();
		if (object instanceof Set<?>) {
			Set<?> set = (Set<?>) object;
			if (set.size() == 0)
				return null;
			
			/* special case of 'all selected' */
			Object first = set.iterator().next();
			if(first instanceof String) {
				return new Long[]{0L};
			}
			
			return set.toArray(new Long[0]);
		} else {
			return null;
		}
	}
}
