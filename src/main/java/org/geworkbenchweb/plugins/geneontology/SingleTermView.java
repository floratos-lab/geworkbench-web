package org.geworkbenchweb.plugins.geneontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.utils.GOTerm;
import org.geworkbenchweb.utils.GeneOntologyTree;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class SingleTermView extends VerticalLayout {

	private static final long serialVersionUID = 1365294256394466923L;
	
	private static Log log = LogFactory.getLog(SingleTermView.class);

	private final HierarchicalContainer dataSource = new HierarchicalContainer();
	private final Tree tree = new Tree("Single Term View");
	
	public SingleTermView() {
		dataSource.addContainerProperty(hw_PROPERTY_NAME, String.class, null);
		dataSource.addContainerProperty(hw_PROPERTY_ICON, ThemeResource.class,
				new ThemeResource("../runo/icons/16/document.png"));
		tree.setContainerDataSource(dataSource);
		tree.setItemCaptionPropertyId(hw_PROPERTY_NAME);
		tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

		this.setMargin(true);
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
					.getInstance();
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

		GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstance();
		for (GOTerm g : geneOntologyTree.getTerm(goTermId).getChildren()) {
			list.add(g.getId());
		}
		return list;
	}
	
	private boolean findAndAddChildren(Integer targetId, Integer childId,
			int parentItemId, int serial) {
		boolean found = false;
		if (childId.equals(targetId)) {
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
			boolean foundInSubtree = findAndAddChildren(targetId, grandchild,
					newItemId, serial);
			if (foundInSubtree) {
				found = true;
			}
		}
		if (found) {
			Item item = dataSource.getItem(newItemId);
			if (item != null) {
				log.debug("Item ID " + newItemId + " already added");
			} else {
				item = dataSource.addItem(newItemId);
				if (item == null) {
					log.error("Item ID " + newItemId + "failed to be added");
					return found;
				}
			}
			// Add name property for item
			GeneOntologyTree geneOntologyTree = GeneOntologyTree.getInstance();
			GOTerm term = geneOntologyTree.getTerm(childId);
			item.getItemProperty(hw_PROPERTY_NAME).setValue(term.getName());
			// set parent node
			if (!dataSource.containsId(parentItemId) && parentItemId!=0) { // add on because we need to set parenthood here
				dataSource.addItem(parentItemId); /* should not fail */
			}
			if (!dataSource.areChildrenAllowed(parentItemId)) {
				dataSource.setChildrenAllowed(parentItemId, true); /* should not fail */
			}
			dataSource.setParent(newItemId, parentItemId); /* should not fail */
		}

		return found;
	}

	public void updateDataSource(Integer goId) {
		dataSource.removeAllItems();
		for (int namespaceId : SingleTermView.getNamespaceIds()) {
			findAndAddChildren(goId, namespaceId, 0, 1);
		}

		// Expand whole tree
        for (int i = 0; i < dataSource.size(); i++) {
            tree.expandItemsRecursively(i);
        }
	}
}
