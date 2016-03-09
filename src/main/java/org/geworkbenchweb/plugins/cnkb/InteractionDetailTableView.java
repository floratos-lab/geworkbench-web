package org.geworkbenchweb.plugins.cnkb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.plugins.tabularview.PopupWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;

public class InteractionDetailTableView extends Table {

	private static final long serialVersionUID = 8979430961160562312L;

	public void setTargetGeneData(final Map<InteractionParticipant, Map<String, InteractionDetail>> targetGenes, final Map<String, String> confidentTypeMap, Map<String, String> map) {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("Gene Symbol", PopupView.class, null);
		container.addContainerProperty("Functionality", String.class, null);
		final List<String> interactome = new ArrayList<String>();
		for (InteractionParticipant targetGene : targetGenes.keySet()) {
			Map<String, InteractionDetail> info = targetGenes.get(targetGene);
			for(String itcm : info.keySet()) {
				if(!interactome.contains(itcm)) {
					interactome.add(itcm);
				}
			}
		}
		for(String i: interactome) {
			container.addContainerProperty(i, String.class, null);
			this.setColumnExpandRatio(i, 0.1f);
		}
		for (InteractionParticipant targetGene : targetGenes.keySet()) {
			Item item = container.addItem(targetGene);
			
			PopupView geneView = new PopupView(new PopupWindow(targetGene.getGeneName(), targetGene.getGeneId()));					 
		    geneView.setData(targetGene);
			item.getItemProperty("Gene Symbol").setValue(geneView);
			item.getItemProperty("Functionality").setValue(map.get(targetGene));
			Map<String, InteractionDetail> info = targetGenes.get(targetGene);
			for(String itcm : info.keySet()) {
				InteractionDetail detail = info.get(itcm);
				StringBuilder sb = new StringBuilder();
				for (Short t : detail.getConfidenceTypes()) {
					String v = String.format("%.3f", detail.getConfidenceValue(t));
					String d = confidentTypeMap.get(t.toString());
					String b = abrev.get(d.trim().toLowerCase());
					if(b==null) b = d;
					sb.append(b + ":" + v + ", ");
				}
				item.getItemProperty(itcm).setValue(sb.deleteCharAt(sb.length()-2).toString());
			}
		}

		this.setContainerDataSource(container);
		List<String> headers = new ArrayList<String>();
		headers.add("Gene Symbol");
		headers.add("Functionality");
		for(String i: interactome) {
			headers.add(i);
		}
		this.setColumnHeaders( headers.toArray(new String[0]) );
		this.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                          

			private static final long serialVersionUID = -7998816630835089530L;

			public String generateDescription(Component source, Object itemId, Object propertyId) {
				String c = (String)propertyId;
				if(c==null) return null;
				for(String i: interactome) {
					if(c.equalsIgnoreCase(i)) {
						Property p = ((Table)source).getItem(itemId).getItemProperty(propertyId);
						Object v = p.getValue();
						if(v!=null)return createTooltip(v.toString());
						else return null;
					}
				}
				return null;
			}
		});
	}
	
	static private String createTooltip(String confidenceText) {
		StringBuilder sb = new StringBuilder();
		for(String key : abrev.keySet()) {
			String value = abrev.get(key);
			if(confidenceText.toLowerCase().contains(value.toLowerCase())) {
				sb.append(value+"="+key+";");
			}
		}
		return sb.deleteCharAt(sb.length()-1).toString();
	}
	
	static private Map<String, String> abrev;
	static {
		abrev = new HashMap<String, String>();
		abrev.put("likelihood ratio", "LR");
		abrev.put("mutual information", "MI");
		abrev.put("mode of action", "MoA");
		abrev.put("p-value", "PV");
		abrev.put("-log10(p-value)", "LOG-PV");
		abrev.put("probability", "PB");
	}
}
