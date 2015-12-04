package org.geworkbenchweb.plugins.cnkb;

import java.util.ArrayList;
import java.util.List;
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
		List<String> interactome = new ArrayList<String>();
		for (String targetGene : targetGenes.keySet()) {
			InteractomeAndDetail info = targetGenes.get(targetGene);
			if(!interactome.contains(info.interactome)) {
				interactome.add(info.interactome);
			}
		}
		for(String i: interactome) {
			container.addContainerProperty(i, String.class, null);
		}
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
			String ip = targetGenes.get(targetGene).interactome;
			item.getItemProperty(ip).setValue(sb.toString());
		}

		this.setContainerDataSource(container);
		List<String> headers = new ArrayList<String>();
		headers.add("Gene Symbol");
		headers.add("Functionality");
		for(String i: interactome) {
			headers.add(i);
		}
		this.setColumnHeaders( headers.toArray(new String[0]) );
	}
}
