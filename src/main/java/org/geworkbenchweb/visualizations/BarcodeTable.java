package org.geworkbenchweb.visualizations;
 
import java.util.ArrayList;
import java.util.HashMap;
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
	 
	private String[] columnNames = {"Regulator", "Borcode", "PValue","Act", "Exp", "Rank"};
	private List<Regulator>  regulators;
	private Map<String, List<Barcode>>   barcodeMap;
	private int barHeight;
	
	/*private  String[] genes = null;
	private  Double[] pValues = null;
	private  String[] daColors = null;
	private  String[] deColors = null;
	private  Integer[] deRanks = null;	 
	 
	private  Map<String, int[]> barcodePositionMap = null;
	private  Map<String, int[]> barcodeColorIndexMap = null;
	private  Map<String, int[]> barcodeArrayIndexMap = null;*/
	
	 
	public BarcodeTable(){};
	public BarcodeTable(List<Regulator> regulators, Map<String, 
			List<Barcode>> barcodeMap, int barHeight) {
		
	       this.regulators =  regulators;
	       this.barcodeMap = barcodeMap;
	       this.barHeight = barHeight;
	}
	 

	 
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
	 
		
		JSONSerializer jsonSerializer = new JSONSerializer().exclude("*.class");
		
        target.addAttribute("columnNames",jsonSerializer.deepSerialize(columnNames));
		target.addAttribute("regulators", jsonSerializer.deepSerialize(regulators));
		target.addAttribute("barcodeMap", jsonSerializer.deepSerialize(barcodeMap));
		target.addAttribute("barHeight", barHeight);
		
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
