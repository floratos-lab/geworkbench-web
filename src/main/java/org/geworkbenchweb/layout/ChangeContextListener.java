/**
 * 
 */
package org.geworkbenchweb.layout;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.pojos.Context;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Tree;

/**
 * @author zji
 * 
 */
public class ChangeContextListener implements Property.ValueChangeListener {

	private static final long serialVersionUID = 4544024893873623257L;

	private static Log log = LogFactory.getLog(ChangeContextListener.class);

	final private ComboBox contextSelector;
	final private Long dataSetId;
	final private Tree setTree;
	final private ContextType contextType;
	final Map<String, String> map;

	enum ContextType {
		MARKER, MICROARRAY
	};

	ChangeContextListener(final ComboBox contextSelector, final Long dataSetId,
			final Tree setTree, final ContextType contextType, final Map<String, String> map) {
		this.contextSelector = contextSelector;
		this.dataSetId = dataSetId;
		this.setTree = setTree;
		this.contextType = contextType;
		this.map = map;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Object val = contextSelector.getValue();

		if (!(val instanceof Context)) {
			log.error("invalid context type " + val);
			return;
		}

		Context context = (Context) val;
		String topItem = null;
		String setName = null;
		if (contextType == ContextType.MARKER) {
			SubSetOperations.setCurrentMarkerContext(dataSetId, context);
			topItem = "MarkerSets";
			setName = "Marker Sets";
		} else if (contextType == ContextType.MICROARRAY) {
			SubSetOperations.setCurrentArrayContext(dataSetId, context);
			topItem = "arraySets";
			setName = "Array Sets/Phenotypes";
		} else {
			log.error("invalid context type " + contextType);
			return;
		}

		HierarchicalContainer dataContainer = new HierarchicalContainer();
		dataContainer.addContainerProperty(SetViewLayout.SET_DISPLAY_NAME, String.class, null);
		Item mainItem1 = dataContainer.addItem(topItem);
		mainItem1.getItemProperty(SetViewLayout.SET_DISPLAY_NAME).setValue(setName);
		setTree.setContainerDataSource(dataContainer);

		List<SubSet> setList = SubSetOperations.getSubSetsForContext(context);
		for (SubSet subset : setList) {
			List<String> list = subset.getPositions();
			Long id = subset.getId();
			dataContainer.addItem(id);
			dataContainer.getContainerProperty(id, SetViewLayout.SET_DISPLAY_NAME).setValue(
					subset.getName() + " [" + list.size() + "]");
			dataContainer.setParent(id, topItem);
			dataContainer.setChildrenAllowed(id, true);
			for (int j = 0; j < list.size(); j++) {
				String item = list.get(j);
				dataContainer.addItem(item + id);
				String label = item; 
				if(map!=null) {
					String geneSymbol = map.get(item);
					if (geneSymbol != null) {
						label += " (" + geneSymbol + ")";
					}
				}
				dataContainer.getContainerProperty(item + id, SetViewLayout.SET_DISPLAY_NAME)
						.setValue(label);
				dataContainer.setParent(item + id, id);
				dataContainer.setChildrenAllowed(item + id, false);
			}
		}
	}
}
