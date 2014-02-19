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
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
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

	final private Tree markerTree = new Tree();
	final private Tree arrayTree = new Tree();
	final private Tree markerSetTree = new Tree();
	final private Tree arraySetTree = new Tree();

	final private VerticalLayout contextpane = new VerticalLayout();;
	final private VerticalLayout mrkcontextpane = new VerticalLayout();

	private Long selectedSubSetId;

	SetViewLayout(final Long dataSetId) {

		markerTree.setImmediate(true);
		markerTree.setSelectable(true);
		markerTree.setMultiSelect(true);
		markerTree.setDescription("Markers");

		HierarchicalContainer markerData = new HierarchicalContainer();
		List<?> sets = SubSetOperations.getMarkerSets(dataSetId);
		Item mainItem = markerData.addItem("MarkerSets");

		markerData.addContainerProperty("setName", String.class, null);
		mainItem.getItemProperty("setName").setValue("Marker Sets");
		for (int i = 0; i < sets.size(); i++) {
			List<String> markers = ((SubSet) sets.get(i)).getPositions();
			Long subSetId = ((SubSet) sets.get(i)).getId();
			markerData.addItem(subSetId);
			markerData.getContainerProperty(subSetId, "setName").setValue(
					((SubSet) sets.get(i)).getName() + " [" + markers.size()
							+ "]");
			markerData.setParent(subSetId, "MarkerSets");
			markerData.setChildrenAllowed(subSetId, true);
			for (int j = 0; j < markers.size(); j++) {
				markerData.addItem(markers.get(j) + subSetId);
				markerData.getContainerProperty(markers.get(j) + subSetId,
						"setName").setValue(markers.get(j));
				markerData.setParent(markers.get(j) + subSetId, subSetId);
				markerData.setChildrenAllowed(markers.get(j) + subSetId, false);
			}
		}
		markerSetTree.setImmediate(true);
		markerSetTree.setSelectable(true);
		markerSetTree.setMultiSelect(false);
		markerSetTree.setContainerDataSource(markerData);
		markerSetTree.addListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {
				try {
					arraySetTree.select(null);
					if (!(event.getItemId() instanceof Long))
						selectedSubSetId = null;
					selectedSubSetId = (Long) event.getItemId();
				} catch (Exception e) {
				}
			}
		});

		HierarchicalContainer arrayData = new HierarchicalContainer();
		List<?> aSets = SubSetOperations.getArraySets(dataSetId);
		arrayData.addContainerProperty("setName", String.class, null);
		Item mainItem1 = arrayData.addItem("arraySets");
		mainItem1.getItemProperty("setName").setValue("Phenotype Sets");

		for (int i = 0; i < aSets.size(); i++) {
			List<String> arrays = ((SubSet) aSets.get(i)).getPositions();
			Long subSetId = ((SubSet) aSets.get(i)).getId();
			arrayData.addItem(subSetId);
			arrayData.getContainerProperty(subSetId, "setName").setValue(
					((SubSet) aSets.get(i)).getName() + " [" + arrays.size()
							+ "]");
			arrayData.setParent(subSetId, "arraySets");
			arrayData.setChildrenAllowed(subSetId, true);
			for (int j = 0; j < arrays.size(); j++) {
				arrayData.addItem(arrays.get(j) + subSetId);
				arrayData.getContainerProperty(arrays.get(j) + subSetId,
						"setName").setValue(arrays.get(j));
				arrayData.setParent(arrays.get(j) + subSetId, subSetId);
				arrayData.setChildrenAllowed(arrays.get(j) + subSetId, false);
			}
		}
		arraySetTree.setImmediate(true);
		arraySetTree.setMultiSelect(false);
		arraySetTree.setSelectable(true);
		arraySetTree.setContainerDataSource(arrayData);
		arraySetTree.addListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {
				try {
					markerSetTree.select(null);
					if (!(event.getItemId() instanceof Long))
						selectedSubSetId = null;
					selectedSubSetId = (Long) event.getItemId();
				} catch (Exception e) {
				}
			}
		});

		// arrayTree.addActionHandler(arrayTreeActionHandler);
		// arrayTree.addActionHandler(new ArrayTreeActionHandler(dataSetId,
		// arraySetTree, contextSelector));
		arrayTree.setImmediate(true);
		arrayTree.setMultiSelect(true);
		arrayTree.setSelectable(true);
		arrayTree.setDescription("Phenotypes");

		// TODO this probably can be done more efficiently
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
		markerTree.setContainerDataSource(markerTableView(markerLabels,
				dataSetId));
		arrayTree.setContainerDataSource(arrayTableView(arrayLabels));

		markerTree.setItemCaptionPropertyId("Labels");
		markerTree
				.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		arrayTree.setItemCaptionPropertyId("Labels");
		arrayTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		markerSetTree.setItemCaptionPropertyId("setName");
		markerSetTree
				.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		arraySetTree.setItemCaptionPropertyId("setName");
		arraySetTree
				.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		// marker context
		final ComboBox mrkcontextSelector = new ComboBox();
		mrkcontextSelector.setWidth("160px");
		mrkcontextSelector.setImmediate(true);
		mrkcontextSelector.setNullSelectionAllowed(false);
		mrkcontextSelector.addListener(new ChangeContextListener(
				mrkcontextSelector, dataSetId, markerSetTree,
				ChangeContextListener.ContextType.MARKER));

		markerTree.addActionHandler(new MarkerTreeActionHandler(dataSetId,
				markerSetTree, mrkcontextSelector));

		List<Context> mrkcontexts = SubSetOperations
				.getMarkerContexts(dataSetId);
		Context mrkcurrent = SubSetOperations
				.getCurrentMarkerContext(dataSetId);
		for (Context c : mrkcontexts) {
			mrkcontextSelector.addItem(c);
			if (mrkcurrent != null && c.getId() == mrkcurrent.getId())
				mrkcontextSelector.setValue(c);
		}

		Button mrknewContextButton = new Button("New");
		mrknewContextButton.addListener(new NewContextListener(this, dataSetId,
				mrkcontextSelector, "marker"));

		Label mrklabel = new Label("Context for Marker Sets");
		mrkcontextpane.addComponent(mrklabel);
		HorizontalLayout mrkhlayout = new HorizontalLayout();
		mrkhlayout.addComponent(mrkcontextSelector);
		mrkhlayout.addComponent(mrknewContextButton);
		mrkcontextpane.addComponent(mrkhlayout);

		// array context
		final ComboBox contextSelector = new ComboBox();
		contextSelector.setWidth("160px");
		contextSelector.setImmediate(true);
		contextSelector.setNullSelectionAllowed(false);
		contextSelector.addListener(new ChangeContextListener(contextSelector,
				dataSetId, arraySetTree,
				ChangeContextListener.ContextType.MICROARRAY));

		arrayTree.addActionHandler(new ArrayTreeActionHandler(dataSetId,
				arraySetTree, contextSelector));

		List<Context> contexts = SubSetOperations.getArrayContexts(dataSetId);
		Context current = SubSetOperations.getCurrentArrayContext(dataSetId);
		for (Context c : contexts) {
			contextSelector.addItem(c);
			if (current != null && c.getId() == current.getId())
				contextSelector.setValue(c);
		}

		Button newContextButton = new Button("New");
		newContextButton.addListener(new NewContextListener(this, dataSetId,
				contextSelector, "microarray"));

		// contextpane.setSpacing(true);
		Label label = new Label("Context for Phenotype Sets");
		contextpane.addComponent(label);
		HorizontalLayout hlayout = new HorizontalLayout();
		hlayout.addComponent(contextSelector);
		hlayout.addComponent(newContextButton);
		contextpane.addComponent(hlayout);

		this.addComponent(mrkcontextpane);
		this.addComponent(markerTree);
		this.addComponent(markerSetTree);
		this.addComponent(contextpane);
		this.addComponent(arrayTree);
		this.addComponent(arraySetTree);
	} /* end of constructor. this definitely needs to be refactored. */ 

	Long getSelectedSetId() {
		return selectedSubSetId;
	}
	
	/**
	 * Method is used to populate Phenotype Panel
	 * 
	 * @param maSet
	 * @return - Indexed container with array labels
	 */
	private HierarchicalContainer arrayTableView(String[] arrayLabels) {

		HierarchicalContainer tableData = new HierarchicalContainer();

		tableData.addContainerProperty("Labels", String.class, null);
		Item mainItem = tableData.addItem("Phenotypes");
		mainItem.getItemProperty("Labels").setValue(
				"Phenotypes" + " [" + arrayLabels.length + "]");

		for (int k = 0; k < arrayLabels.length; k++) {
			Item item = tableData.addItem(k);
			tableData.setChildrenAllowed(k, false);
			item.getItemProperty("Labels").setValue(arrayLabels[k]);
			tableData.setParent(k, "Phenotypes");
		}
		return tableData;
	}

	/**
	 * Method is used to populate Marker Panel
	 * 
	 * @param maSet
	 * @return - Indexed container with marker labels
	 */
	private HierarchicalContainer markerTableView(String[] markerLabels,
			Long dataSetId) {

		HierarchicalContainer tableData = new HierarchicalContainer();
		tableData.addContainerProperty("Labels", String.class, null);

		Item mainItem = tableData.addItem("Markers");
		mainItem.getItemProperty("Labels").setValue(
				"Markers" + " [" + markerLabels.length + "]");

		/* find annotation information */
		Map<String, String> map = DataSetOperations.getAnnotationMap(dataSetId);

		for (int j = 0; j < markerLabels.length; j++) {

			Item item = tableData.addItem(j);
			tableData.setChildrenAllowed(j, false);

			String markerLabel = markerLabels[j];
			String geneSymbol = map.get(markerLabel);
			if (geneSymbol != null) {
				markerLabel += " (" + geneSymbol + ")";
			}
			item.getItemProperty("Labels").setValue(markerLabel);
			tableData.setParent(j, "Markers");
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
