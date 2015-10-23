package org.geworkbenchweb.plugins.cnkb;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

public class InteractionDetailTableView extends Table {

	private static final long serialVersionUID = 8979430961160562312L;

	public void setData(final List<String> targetGenes) {
		IndexedContainer container = new IndexedContainer();
		// TODO some data are fake placeholder for now
		container.addContainerProperty("Gene Symbol", String.class, null);
		container.addContainerProperty("Functionality", String.class, null);
		container.addContainerProperty("Interactome 1", String.class, null);
		container.addContainerProperty("Interactome 2", String.class, null);
		container.addContainerProperty("Interactome 3", String.class, null);
		for(String targetGene: targetGenes) {
			Item item = container.addItem(targetGene);
			item.getItemProperty("Gene Symbol").setValue(targetGene);
			item.getItemProperty("Functionality").setValue("GO func");
			item.getItemProperty("Interactome 1").setValue("MI=x, PV=y, LI=Z, MO=w");
			item.getItemProperty("Interactome 2").setValue("MI=x, PV=y, LI=Z, MO=w2");
			item.getItemProperty("Interactome 3").setValue("MI=x, PV=y, LI=Z, MO=w3");
		}

		this.setContainerDataSource(container);
		this.setColumnHeaders(
				new String[] { "Gene Symbol", "Functionality", "Interactome 1", "Interactome 2", "Interactome 3" });
	}
}
