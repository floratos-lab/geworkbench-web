package org.geworkbenchweb.visualizations;
 
import java.util.Map; 
import java.util.List;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget; 
import com.vaadin.ui.AbstractComponent;

import flexjson.JSONSerializer;

/**
 * Server side component for the VCytoscape widget.
 */
@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VBarcodeTable.class)
public class BarcodeTable extends AbstractComponent {
	 
	private static final long serialVersionUID = -2119963715440692055L;
	 
	private String[] columnNames = {"Regulator", "Bar Graph", "PValue","Activity", "Expression", "Exp. Rank"};
	private List<Regulator>  regulators;
	private Map<String, List<Barcode>>   barcodeMap;
	private int barHeight;
	//private boolean exportImage = false;	 
	 
	public BarcodeTable(){};
	public BarcodeTable(List<Regulator> regulators, Map<String, 
			List<Barcode>> barcodeMap, int barHeight) {
		
	       this.regulators =  regulators;
	       this.barcodeMap = barcodeMap;
	       this.barHeight = barHeight;
	       //this.exportImage = exportImage;
	}
	 

	 
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
	 
		
		JSONSerializer jsonSerializer = new JSONSerializer().exclude("*.class");
		
        target.addAttribute("columnNames",jsonSerializer.deepSerialize(columnNames));
		target.addAttribute("regulators", jsonSerializer.deepSerialize(regulators));
		target.addAttribute("barcodeMap", jsonSerializer.deepSerialize(barcodeMap));
		target.addAttribute("barHeight", barHeight);
		//target.addAttribute("exportImage", exportImage);
		
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		
	 
	} 
	
	 
	
}
