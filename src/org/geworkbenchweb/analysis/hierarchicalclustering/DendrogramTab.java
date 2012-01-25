package org.geworkbenchweb.analysis.hierarchicalclustering;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.VerticalLayout;

public class DendrogramTab extends VerticalLayout {
	
	private static final long serialVersionUID = 1L;

	private Dendrogram dendrogram = new Dendrogram();

	private Container cont1 = new IndexedContainer();

	private static final String[] colors1 = new String[] { "#ee7c08",
		"#00b4f0", "#000000", "#40B527", "#ffffff", "#d8d2c6" };

	public DendrogramTab() {
		setCaption("Bar chart");
		addComponent(dendrogram);

		valuateCont1();		
		dendrogram.setImmediate(true);
		dendrogram.setContainerDataSource(cont1);
		dendrogram.setItemCaptionPropertyId("caption");
		dendrogram.setItemValuePropertyId("value");
		dendrogram.setColors(colors1);
		dendrogram.setSizeFull();
		
	}

	private void valuateCont1() {
		cont1.addContainerProperty("value", Double.class, null);
		cont1.addContainerProperty("caption", String.class, null);
		cont1.addContainerProperty("info", String.class, null);

		Object itemId = cont1.addItem();
		cont1.getContainerProperty(itemId, "value").setValue(50);
		cont1.getContainerProperty(itemId, "caption").setValue("A");
		cont1.getContainerProperty(itemId, "info").setValue("A info text");

		itemId = cont1.addItem();
		cont1.getContainerProperty(itemId, "value").setValue(100);
		cont1.getContainerProperty(itemId, "caption").setValue("B");
		cont1.getContainerProperty(itemId, "info").setValue("B info text");

		itemId = cont1.addItem();
		cont1.getContainerProperty(itemId, "value").setValue(75);
		cont1.getContainerProperty(itemId, "caption").setValue("C");
		cont1.getContainerProperty(itemId, "info").setValue("C info text.");

		itemId = cont1.addItem();
		cont1.getContainerProperty(itemId, "value").setValue(125);
		cont1.getContainerProperty(itemId, "caption").setValue("D");
		cont1.getContainerProperty(itemId, "info").setValue("D info text.");

	}
}
