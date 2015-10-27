package org.geworkbenchweb.plugins.cnkb;

import java.util.Iterator;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

public class InteractionDetailTableView extends Table {

	private static final long serialVersionUID = 8979430961160562312L;

	public void setTargetGeneData(final Map<String, String> targetGenes) {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("Gene Symbol", String.class, null);
		container.addContainerProperty("Functionality", String.class, null);
		Iterator<String> iter = targetGenes.values().iterator();
		String interactome = "UNKNOWN"; // this may be necessary for the existing results
		if(iter.hasNext())
			interactome = iter.next(); // TODO interactomes will be multiple eventually, even for each target gene
		container.addContainerProperty(interactome , String.class, null);
		for(String targetGene: targetGenes.keySet()) {
			Item item = container.addItem(targetGene);
			item.getItemProperty("Gene Symbol").setValue(targetGene);
			item.getItemProperty("Functionality").setValue("GO func");
			// TODO interaction attribute data are placeholder for now
			item.getItemProperty(interactome).setValue("MI=?, PV=?, LI=?, MO=?");
		}

		this.setContainerDataSource(container);
		this.setColumnHeaders(
				new String[] { "Gene Symbol", "Functionality", interactome });
	}
}
