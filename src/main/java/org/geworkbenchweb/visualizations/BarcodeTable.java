package org.geworkbenchweb.visualizations;

import java.util.Map;
import java.util.List;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;

import flexjson.JSONSerializer;

/**
 * Server side component for the VCytoscape widget.
 */
public class BarcodeTable extends AbstractComponent {

	private static final long serialVersionUID = -2119963715440692055L;

	private String[] columnNames = { "Regulator", "Bar Graph", "PValue", "Activity", "Expression", "Exp. Rank" };
	private List<Regulator> regulators;
	private Map<String, List<Barcode>> barcodeMap;
	private int barHeight;
	// private boolean exportImage = false;

	public BarcodeTable() {
	};

	public BarcodeTable(List<Regulator> regulators, Map<String, List<Barcode>> barcodeMap, int barHeight) {

		this.regulators = regulators;
		this.barcodeMap = barcodeMap;
		this.barHeight = barHeight;
		// this.exportImage = exportImage;
	}

	// FIXME this should be replaced by vaadin 7 communication mechanism
	public void paintContent(PaintTarget target) throws PaintException {

		JSONSerializer jsonSerializer = new JSONSerializer().exclude("*.class");

		target.addAttribute("columnNames", jsonSerializer.deepSerialize(columnNames));
		target.addAttribute("regulators", jsonSerializer.deepSerialize(regulators));
		target.addAttribute("barcodeMap", jsonSerializer.deepSerialize(barcodeMap));
		target.addAttribute("barHeight", barHeight);
		// target.addAttribute("exportImage", exportImage);

	}
}
