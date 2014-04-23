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

		List<AbstractPojo> sets = SubSetOperations.getMarkerSets(dataSetId);
		HierarchicalContainer markerData = createSetContainer(sets,
				"MarkerSets", "Marker Sets");
		markerSetTree = createSetTree(markerData);

		List<AbstractPojo> aSets = SubSetOperations.getArraySets(dataSetId);
		HierarchicalContainer arrayData = createSetContainer(aSets,
				"arraySets", "Array Sets/Phenotypes");
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
				createLabelContainer(markerLabels, true, dataSetId),
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
				createLabelContainer(arrayLabels, false, null),
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
	
	private static HierarchicalContainer createSetContainer(
			final List<AbstractPojo> sets, final String topItem,
			final String setName) {
		HierarchicalContainer dataContainer = new HierarchicalContainer();
		dataContainer.addContainerProperty(SET_DISPLAY_NAME, String.class, null);

		Item mainItem = dataContainer.addItem(topItem);
		mainItem.getItemProperty(SET_DISPLAY_NAME).setValue(setName);

		for (int i = 0; i < sets.size(); i++) {
			List<String> markers = ((SubSet) sets.get(i)).getPositions();
			Long subSetId = ((SubSet) sets.get(i)).getId();
			dataContainer.addItem(subSetId);
			dataContainer.getContainerProperty(subSetId, SET_DISPLAY_NAME).setValue(
					((SubSet) sets.get(i)).getName() + " [" + markers.size()
							+ "]");
			dataContainer.setParent(subSetId, topItem);
			dataContainer.setChildrenAllowed(subSetId, true);
			for (int j = 0; j < markers.size(); j++) {
				dataContainer.addItem(markers.get(j) + subSetId);
				dataContainer.getContainerProperty(markers.get(j) + subSetId,
						SET_DISPLAY_NAME).setValue(markers.get(j));
				dataContainer.setParent(markers.get(j) + subSetId, subSetId);
				dataContainer.setChildrenAllowed(markers.get(j) + subSetId,
						false);
			}
		}
		return dataContainer;
	}

	/* When isMarker is false, dataSetId is ignored. */
	private static HierarchicalContainer createLabelContainer(final String[] labels,
			final boolean isMarker, final Long dataSetId) {

		HierarchicalContainer tableData = new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		String topItemLabel = "Arrays";
		if(isMarker)topItemLabel = "Markers";
		
		Item mainItem = tableData.addItem(topItemLabel);
		mainItem.getItemProperty("Labels").setValue(
				topItemLabel + " [" + labels.length + "]");

		/* find annotation information */
		Map<String, String> map = null;
		if(isMarker) {
			map = DataSetOperations.getAnnotationMap(dataSetId);
		}

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

	public Tree getArraySetTree() {
		return arraySetTree;
	}

	public Tree getMarkerSetTree() {
		return markerSetTree;
	}

}
