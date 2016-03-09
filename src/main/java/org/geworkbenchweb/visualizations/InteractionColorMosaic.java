package org.geworkbenchweb.visualizations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.plugins.cnkb.InteractionDetail;
import org.geworkbenchweb.plugins.cnkb.InteractionParticipant;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VInteractionColorMosaic.class)
public class InteractionColorMosaic extends AbstractComponent {

	private static final long serialVersionUID = 4600493722698214718L;

	final private String[] interactome;
	final private String[] geneSymbol;
	final private String[] pValue;
	final private String[] color;
	
	public InteractionColorMosaic(final Map<InteractionParticipant, Map<String, InteractionDetail>> targetGenes) {
		List<String> interactome = new ArrayList<String>();
		for(InteractionParticipant gene  : targetGenes.keySet()) {
			Map<String, InteractionDetail> info = targetGenes.get(gene);
			for(String itcm : info.keySet()) {
				if(!interactome.contains(itcm)) {
					interactome.add(itcm);
				}
			}
		}
		if(interactome.size()==0) {
			interactome.add( "UNKNOWN" ); // this may be necessary for the existing results
		}
		List<String> geneList = new ArrayList<String>();
		List<String> pValueList = new ArrayList<String>();
		List<String> colorList = new ArrayList<String>();
		for (InteractionParticipant targetGene : targetGenes.keySet()) {
			geneList.add(targetGene.getGeneName());
			Map<String, InteractionDetail> info = targetGenes.get(targetGene);
			for(String itcm  : interactome) {
				InteractionDetail detail = info.get(itcm);
				List<Short> types = null;
				if(detail!=null) types = detail.getConfidenceTypes();
				String pValueString = "";
				String colorString = "#AAAAAA";
				if (types != null && types.size() > 0) {
					double p = detail.getConfidenceValue(types.get(0)); // FIXME this may not be p-value, just the first found 'confidence' value
					pValueString = ""+p;
					colorString = colorCode(p);
				}
				pValueList.add(pValueString);
				colorList.add(colorString);
			}
		}
		this.interactome = interactome.toArray(new String[0]);
		this.geneSymbol = geneList.toArray(new String[0]);
		this.pValue = pValueList.toArray(new String[0]);
		this.color = colorList.toArray(new String[0]);
	}
	
	private static String colorCode(double pValue) {

		final String colorFormat = "#%02X%02X%02X";

		int colorIndex = (int) (255 * pValue);
		if (colorIndex < 0)
			colorIndex = 0;
		else if (colorIndex > 255)
			colorIndex = 255;
		// 0~1 maps to read~green
		return String.format(colorFormat, 255 - colorIndex, colorIndex, 0);
	}
	
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

        target.addAttribute("interactome", interactome);
       	target.addAttribute("geneSymbol", geneSymbol);
       	target.addAttribute("pValue", pValue);
       	target.addAttribute("color", color);
	}
}
