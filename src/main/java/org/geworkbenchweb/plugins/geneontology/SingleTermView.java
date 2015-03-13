package org.geworkbenchweb.plugins.geneontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.utils.GOTerm;
import org.geworkbenchweb.utils.GeneOntologyTree;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class SingleTermView extends VerticalLayout {

	private static final long serialVersionUID = 1365294256394466923L;
	
	private static Log log = LogFactory.getLog(SingleTermView.class);

	public SingleTermView() {
		Tree tree = new Tree("Simgle Term View");
		Container dataSource = updateDataSource(48523);
		tree.setContainerDataSource(dataSource);
		tree.setItemCaptionPropertyId(hw_PROPERTY_NAME);
		tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		this.addComponent(tree);
	}

	private static final Object hw_PROPERTY_NAME = "name";
	public static final Object hw_PROPERTY_ICON = "icon";

	private static Set<Integer> namespaceIds = new TreeSet<Integer>();

	private static Set<Integer> getNamespaceIds() {
		if (namespaceIds.size() > 0) {
			return namespaceIds;
		} else {
			GeneOntologyTree geneOntologyTree = GeneOntologyTree
					.getInstanceUntilAvailable();
			for (int i = 0; i < geneOntologyTree.getNumberOfRoots(); i++)
				namespaceIds.add(geneOntologyTree.getRoot(i).getId());
			return namespaceIds;
		}
	}

	/**
	 * return list of child IDs for a GO term ID
	 * 
	 * @param goTermId
	 * @return
	 */
	private static List<Integer> getOntologyChildren(int goTermId) {
		List<Integer> list = new ArrayList<Integer>();

		if (goTermId == 0) {
			for (Integer id : getNamespaceIds()) {
				list.add(id);
			}
			return list;
		}

		for (GOTerm g : geneOntologyTree.getTerm(goTermId).getChildren()) {
			list.add(g.getId());
		}
		return list;
	}
	
	private final static GeneOntologyTree geneOntologyTree = GeneOntologyTree
			.getInstanceUntilAvailable();

	private boolean findAndAddChildren(Integer targetGene, Integer childId,
			int parentItemId, HierarchicalContainer hwContainer, int serial) {
		boolean found = false;
		if (childId.equals(targetGene)) {
			found = true;
		}

		List<Integer> grandchildren = SingleTermView
				.getOntologyChildren(childId);
		if (grandchildren == null) {
			log.warn("this should not happen");
			return false;
		}

		int newItemId = serial;
		for (Integer grandchild : grandchildren) {
			serial++;
			boolean foundInSubtree = findAndAddChildren(targetGene, grandchild,
					newItemId, hwContainer, serial);
			if (foundInSubtree) {
				found = true;
			}
		}
		if (found) {
			Item item = hwContainer.getItem(newItemId);
			if (item != null) {
				log.debug("Item ID " + newItemId + " already added");
			} else {
				item = hwContainer.addItem(newItemId);
				if (item == null) {
					log.error("Item ID " + newItemId + "failed to be added");
					return found;
				}
			}
			// Add name property for item
			GOTerm term = geneOntologyTree.getTerm(childId);
			item.getItemProperty(hw_PROPERTY_NAME).setValue(term.getName());
			// set parent node
			if (!hwContainer.containsId(parentItemId) && parentItemId!=0) { // add on because we need to set parenthood here
				hwContainer.addItem(parentItemId); /* should not fail */
			}
			if (!hwContainer.areChildrenAllowed(parentItemId)) {
				hwContainer.setChildrenAllowed(parentItemId, true); /* should not fail */
			}
			hwContainer.setParent(newItemId, parentItemId); /* should not fail */
		}

		return found;
	}

	private HierarchicalContainer updateDataSource(int geneId) {
		// Create new container
		HierarchicalContainer hwContainer = new HierarchicalContainer();
		// Create containerproperty for name
		hwContainer.addContainerProperty(hw_PROPERTY_NAME, String.class, null);
		// Create containerproperty for icon
		hwContainer.addContainerProperty(hw_PROPERTY_ICON, ThemeResource.class,
				new ThemeResource("../runo/icons/16/document.png"));

		for (int namespaceId : SingleTermView.getNamespaceIds()) {
			findAndAddChildren(geneId, namespaceId, 0, hwContainer, 1);
		}
		return hwContainer;
	}
}
