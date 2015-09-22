package org.geworkbenchweb.plugins.cnkb;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class InteractionDetailTableView extends VerticalLayout {

	private static final long serialVersionUID = 8979430961160562312L;

	public InteractionDetailTableView() {
		Table table = new Table();
		this.addComponent(table);

		IndexedContainer container = new IndexedContainer();
		// TODO fake data for now
		container.addContainerProperty("Gene Symbol", String.class, null);
		container.addContainerProperty("Functionality", String.class, null);
		container.addContainerProperty("Interactome 1", String.class, null);
		container.addContainerProperty("Interactome 2", String.class, null);
		container.addContainerProperty("Interactome 3", String.class, null);
		Item item = container.addItem("FOXM1");
		item.getItemProperty("Gene Symbol").setValue("FOXM1");
		item.getItemProperty("Functionality").setValue("GO func");
		item.getItemProperty("Interactome 1").setValue("MI=x, PV=y, LI=Z, MO=w");
		item.getItemProperty("Interactome 2").setValue("MI=x, PV=y, LI=Z, MO=w2");
		item.getItemProperty("Interactome 3").setValue("MI=x, PV=y, LI=Z, MO=w3");
		Item item2 = container.addItem("NFYB");
		item2.getItemProperty("Gene Symbol").setValue("NFYB");
		item2.getItemProperty("Functionality").setValue("GO func NFYB");
		item2.getItemProperty("Interactome 1").setValue("MI=x, PV=y, LI=Z, MO=w NFYB");
		item2.getItemProperty("Interactome 2").setValue("MI=x, PV=y, LI=Z, MO=w2 NFYB");
		item2.getItemProperty("Interactome 3").setValue("MI=x, PV=y, LI=Z, MO=w3 NFYB");

		table.setContainerDataSource(container);
		table.setColumnHeaders(
				new String[] { "Gene Symbol", "Functionality", "Interactome 1", "Interactome 2", "Interactome 3" });
	}

}
