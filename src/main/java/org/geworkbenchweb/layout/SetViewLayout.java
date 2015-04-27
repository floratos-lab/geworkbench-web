/**
 * 
 */
package org.geworkbenchweb.layout;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.DataSetOperations;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

/**
 * @author zji
 * 
 */
public class SetViewLayout extends CssLayout {

	private static final long serialVersionUID = -6141429703360641047L;

	private static Log log = LogFactory.getLog(SetViewLayout.class);

	final private Tree markerSetTree;
	final private Tree arraySetTree;

	private Long selectedSubSetId;

	SetViewLayout(final Long dataSetId) {

		// this is used by both the set tree and the label tree of markers, but nothing else
		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);

		HierarchicalContainer markerData = createMarkerSetContainer(dataSetId, map);
		markerSetTree = createSetTree(markerData);

		HierarchicalContainer arrayData = createMicroarraySetContainer(dataSetId);
		arraySetTree = createSetTree(arrayData);
		
		markerSetTree.addListener(new SetTreeClickListener(arraySetTree));
		arraySetTree.addListener(new SetTreeClickListener(markerSetTree));

		DataSet generic = FacadeFactory.getFacade().find(DataSet.class,
				dataSetId);
		Long id = generic.getDataId();
		if (id == null) {
			log.error("null ID for MicroarrayDataset");
			return;
		}
		String[] markerLabels = DataSetOperations.getStringLabels(
				"markerLabels", id);
		String[] arrayLabels = DataSetOperations.getStringLabels("arrayLabels",
				id);

		// marker context
		List<Context> mrkcontexts = SubSetOperations
				.getMarkerContexts(dataSetId);
		Context mrkcurrent = SubSetOperations
				.getCurrentMarkerContext(dataSetId);
		final ComboBox mrkcontextSelector = createSelector(mrkcontexts,
				mrkcurrent);
		mrkcontextSelector.addListener(new ChangeContextListener(
				mrkcontextSelector, dataSetId, markerSetTree,
				ChangeContextListener.ContextType.MARKER));

		Tree markerTree = createItemTree("Markers",
				createMarkerLabelContainer(markerLabels, map),
				new MarkerTreeActionHandler(dataSetId, markerSetTree,
						mrkcontextSelector));

		Button mrknewContextButton = new Button("New");
		mrknewContextButton.addListener(new NewContextListener(this, dataSetId,
				mrkcontextSelector, SubSet.SET_TYPE_MARKER));

		// array context
		List<Context> contexts = SubSetOperations.getArrayContexts(dataSetId);
		Context current = SubSetOperations.getCurrentArrayContext(dataSetId);
		final ComboBox contextSelector = createSelector(contexts, current);
		contextSelector.addListener(new ChangeContextListener(contextSelector,
				dataSetId, arraySetTree,
				ChangeContextListener.ContextType.MICROARRAY));

		Tree arrayTree = createItemTree("Arrays",
				createMicroarrayLabelContainer(arrayLabels),
				new ArrayTreeActionHandler(dataSetId, arraySetTree,
						contextSelector));

		Button newContextButton = new Button("New");
		newContextButton.addListener(new NewContextListener(this, dataSetId,
				contextSelector, SubSet.SET_TYPE_MICROARRAY));

		this.addComponent(createContextLayout("Context for Marker Sets", mrkcontextSelector, mrknewContextButton));
		this.addComponent(markerTree);
		this.addComponent(markerSetTree);
		this.addComponent(createContextLayout("Context for Array Sets/Phenotypes", contextSelector, newContextButton));
		this.addComponent(arrayTree);
		this.addComponent(arraySetTree);
		
		new SetRenameHandler(markerSetTree);
		new SetRenameHandler(arraySetTree);
	} /* end of constructor */ 

	Long getSelectedSetId() {
		return selectedSubSetId;
	}
	
	private class SetTreeClickListener implements
			ItemClickEvent.ItemClickListener {

		private static final long serialVersionUID = 8199388775619848909L;

		private final Tree otherSetTree;

		SetTreeClickListener(final Tree otherSetTree) {
			this.otherSetTree = otherSetTree;
		}

		@Override
		public void itemClick(ItemClickEvent event) {
			otherSetTree.select(null);
			Object id = event.getItemId();
			if(id instanceof Long) {
				selectedSubSetId = (Long) id;
			} else {
				selectedSubSetId = null;
			}
		}
	}
	
	private static ComboBox createSelector(final List<Context> contexts,
			Context current) {
		ComboBox selector = new ComboBox();
		selector.setWidth("160px");
		selector.setImmediate(true);
		selector.setNullSelectionAllowed(false);

		for (Context c : contexts) {
			selector.addItem(c);
			if (current != null && c.getId() == current.getId())
				selector.setValue(c);
		}
		return selector;
	}
	
	public final static String SET_DISPLAY_NAME = "set_display_name";

	private static Tree createSetTree(final HierarchicalContainer dataContainer) {
		Tree tree = new Tree();
		tree.setImmediate(true);
		tree.setSelectable(true);
		tree.setMultiSelect(false);

		tree.setContainerDataSource(dataContainer);
		tree.setItemCaptionPropertyId(SET_DISPLAY_NAME);
		tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		return tree;
	}
	
	private static Tree createItemTree(final String description,
			final HierarchicalContainer dataContainer,
			final TreeActionHandler handler) {
		Tree tree = new Tree();
		tree.setImmediate(true);
		tree.setSelectable(true);
		tree.setMultiSelect(true);
		tree.setDescription(description);

		tree.setContainerDataSource(dataContainer);
		tree.setItemCaptionPropertyId("Labels");
		tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		tree.addActionHandler(handler);

		return tree;
	}
	
	private static VerticalLayout createContextLayout(final String label,
			final ComboBox contextSelector, Button newContextButton) {
		VerticalLayout contextpane = new VerticalLayout();
		contextpane.addComponent(new Label(label));
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(contextSelector);
		layout.addComponent(newContextButton);
		contextpane.addComponent(layout);
		return contextpane;
	}
	
	private static HierarchicalContainer createMarkerSetContainer(
			final Long dataSetId, final Map<String, String> map) {

		HierarchicalContainer dataContainer = new HierarchicalContainer();
		dataContainer.addContainerProperty(SET_DISPLAY_NAME, String.class, null);

		String topItem = "MarkerSets";
		Item mainItem = dataContainer.addItem(topItem);
		mainItem.getItemProperty(SET_DISPLAY_NAME).setValue("Marker Sets");

		List<AbstractPojo> sets = SubSetOperations.getMarkerSets(dataSetId);
		for (AbstractPojo obj : sets) {
			SubSet subset = (SubSet)obj;
			List<String> markers = subset.getPositions();
			Long subSetId = subset.getId();
			dataContainer.addItem(subSetId);
			dataContainer.getContainerProperty(subSetId, SET_DISPLAY_NAME).setValue(
					subset.getName() + " [" + markers.size()
							+ "]");
			dataContainer.setParent(subSetId, topItem);
			dataContainer.setChildrenAllowed(subSetId, true);
			for (int j = 0; j < markers.size(); j++) {
				
				dataContainer.addItem(markers.get(j) + subSetId);	
				String markerWithGeneName = markers.get(j);
				if(map!=null) {
					String geneSymbol = map.get(markers.get(j));
					if (geneSymbol != null) {
						markerWithGeneName += " (" + geneSymbol + ")";
					}
				}
				dataContainer.getContainerProperty(markers.get(j) + subSetId,
						SET_DISPLAY_NAME).setValue(markerWithGeneName);
				dataContainer.setParent(markers.get(j) + subSetId, subSetId);
				dataContainer.setChildrenAllowed(markers.get(j) + subSetId,
						false);
			}
		}
		return dataContainer;
	}
	
	// this is similar to data container for markers, but no need to process the microarray annotation
	private static HierarchicalContainer createMicroarraySetContainer (
			final Long dataSetId) {
		HierarchicalContainer dataContainer = new HierarchicalContainer();
		dataContainer.addContainerProperty(SET_DISPLAY_NAME, String.class, null);

		String topItem = "arraySets";
		Item mainItem = dataContainer.addItem(topItem );
		mainItem.getItemProperty(SET_DISPLAY_NAME).setValue("Array Sets/Phenotypes");
 
		List<AbstractPojo> sets = SubSetOperations.getArraySets(dataSetId);
		for (AbstractPojo obj : sets) {
			SubSet subset = (SubSet)obj;
			List<String> microarrayLabels = subset.getPositions();
			Long subSetId = subset.getId();
			dataContainer.addItem(subSetId);
			dataContainer.getContainerProperty(subSetId, SET_DISPLAY_NAME).setValue(
					subset.getName() + " [" + microarrayLabels.size()
							+ "]");
			dataContainer.setParent(subSetId, topItem);
			dataContainer.setChildrenAllowed(subSetId, true);
			for (int j = 0; j < microarrayLabels.size(); j++) {
				String label = microarrayLabels.get(j) + subSetId;
				dataContainer.addItem(label);
				dataContainer.getContainerProperty(label, SET_DISPLAY_NAME)
						.setValue(label);
				dataContainer.setParent(label, subSetId);
				dataContainer.setChildrenAllowed(label, false);
			}
		}
		return dataContainer;
	}

	private static HierarchicalContainer createMarkerLabelContainer(
			final String[] labels, final Map<String, String> map) {

		HierarchicalContainer tableData = new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		String topItemLabel = "Markers";
		
		Item mainItem = tableData.addItem(topItemLabel);
		mainItem.getItemProperty("Labels").setValue(
				topItemLabel + " [" + labels.length + "]");

		for (int j = 0; j < labels.length; j++) {

			Item item = tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);

			String label = labels[j];
			
			if(map!=null) {
				String geneSymbol = map.get(label);
				if (geneSymbol != null) {
					label += " (" + geneSymbol + ")";
				}
			}
			item.getItemProperty("Labels").setValue(label);
			tableData.setParent(j, topItemLabel);
		}
		return tableData;
	}
	
	private static HierarchicalContainer createMicroarrayLabelContainer(
			final String[] labels) {

		HierarchicalContainer tableData = new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		String topItemLabel = "Arrays";
		
		Item mainItem = tableData.addItem(topItemLabel);
		mainItem.getItemProperty("Labels").setValue(
				topItemLabel + " [" + labels.length + "]");

		for (int j = 0; j < labels.length; j++) {

			Item item = tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);

			item.getItemProperty("Labels").setValue(labels[j]);
			tableData.setParent(j, topItemLabel);
		}
		return tableData;
	}

	public Tree getArraySetTree() {
		return arraySetTree;
	}

	public Tree getMarkerSetTree() {
		return markerSetTree;
	}

}
