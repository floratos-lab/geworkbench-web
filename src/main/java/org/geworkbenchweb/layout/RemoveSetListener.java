package org.geworkbenchweb.layout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.pojos.SubSetContext;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Tree;

import de.steinwedel.messagebox.MessageBox;

public class RemoveSetListener implements Button.ClickListener {

	private static final long serialVersionUID = 6829997098780053442L;

	final UMainLayout mainLayout;

	RemoveSetListener(final UMainLayout mainLayout) {
		this.mainLayout = mainLayout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		SetViewLayout setViewLayout = mainLayout.getSetViewLayout();
		Long selectedSetId = setViewLayout.getSelectedSetId();

		try {
			if (selectedSetId != null) {

				/* Deleteing all contexts and then subset */
				Map<String, Object> cParam = new HashMap<String, Object>();
				cParam.put("subsetid", selectedSetId);
				List<SubSetContext> subcontexts = FacadeFactory
						.getFacade()
						.list("Select a from SubSetContext a where a.subsetid=:subsetid",
								cParam);
				if (!subcontexts.isEmpty()) {
					FacadeFactory.getFacade().deleteAll(subcontexts);
				}

				Map<String, Object> Param = new HashMap<String, Object>();
				Param.put("id", selectedSetId);
				List<SubSet> subSets = FacadeFactory.getFacade().list(
						"Select a from SubSet a where a.id=:id", Param);

				if (subSets.get(0).getType().equalsIgnoreCase(SubSet.SET_TYPE_MICROARRAY)) {
					Tree arraySetTree = setViewLayout.getArraySetTree();
					if (arraySetTree.hasChildren(selectedSetId)) {
						Collection<?> children = arraySetTree
								.getChildren(selectedSetId);
						LinkedList<String> children2 = new LinkedList<String>();
						for (Iterator<?> i = children.iterator(); i.hasNext();)
							children2.add((String) i.next());

						// Remove the children of the collapsing node
						for (Iterator<String> i = children2.iterator(); i
								.hasNext();) {
							String child = i.next();
							arraySetTree.removeItem(child);
						}
					}
					arraySetTree.removeItem(selectedSetId);
				} else {
					Tree markerSetTree = setViewLayout.getMarkerSetTree();
					if (markerSetTree.hasChildren(selectedSetId)) {
						Collection<?> children = markerSetTree
								.getChildren(selectedSetId);
						LinkedList<String> children2 = new LinkedList<String>();
						for (Iterator<?> i = children.iterator(); i.hasNext();)
							children2.add((String) i.next());

						// Remove the children of the collapsing node
						for (Iterator<String> i = children2.iterator(); i
								.hasNext();) {
							String child = i.next();
							markerSetTree.removeItem(child);
						}
					}
					markerSetTree.removeItem(selectedSetId);
				}
				FacadeFactory.getFacade().deleteAll(subSets);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.createInfo().withCaption("Warning").withMessage("Please select SubSet to delete.").withOkButton()
					.open();
		}
	}

}
