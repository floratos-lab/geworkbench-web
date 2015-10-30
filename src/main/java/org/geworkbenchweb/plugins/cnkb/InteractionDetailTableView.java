package org.geworkbenchweb.plugins.cnkb;

import java.util.Iterator;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

public class InteractionDetailTableView extends Table {

	private static final long serialVersionUID = 8979430961160562312L;

	public void setTargetGeneData(final Map<String, InteractomeAndDetail> targetGenes, final Map<String, String> confidentTypeMap, Map<String, String> map) {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("Gene Symbol", String.class, null);
		container.addContainerProperty("Functionality", String.class, null);
		Iterator<InteractomeAndDetail> iter = targetGenes.values().iterator();
		String interactome = "UNKNOWN"; // this may be necessary for the existing results
		if(iter.hasNext()) {
			InteractomeAndDetail info = iter.next();
			interactome = info.interactome; // TODO interactomes will be multiple eventually, even for each target gene
		}
		container.addContainerProperty(interactome , String.class, null);
		for (String targetGene : targetGenes.keySet()) {
			Item item = container.addItem(targetGene);
			item.getItemProperty("Gene Symbol").setValue(targetGene);
			item.getItemProperty("Functionality").setValue(map.get(targetGene));
			InteractionDetail detail = targetGenes.get(targetGene).detail;
			StringBuilder sb = new StringBuilder();
			for (Short t : detail.getConfidenceTypes()) {
				double v = detail.getConfidenceValue(t);
				sb.append(confidentTypeMap.get(t.toString()) + ":" + v + ", ");
			}
			item.getItemProperty(interactome).setValue(sb.toString());
		}

		this.setContainerDataSource(container);
		this.setColumnHeaders(
				new String[] { "Gene Symbol", "Functionality", interactome });
	}
}
