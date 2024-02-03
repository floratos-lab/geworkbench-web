package org.geworkbenchweb.visualizations;

import java.util.List;
import java.util.Map;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

@JavaScript({ "msviper.js" })
public class BarcodeTable extends AbstractJavaScriptComponent {

	@Override
	public BarcodeTableState getState() {
		return (BarcodeTableState) super.getState();
	}

	public BarcodeTable(List<Regulator> regulators, Map<String, List<Barcode>> barcodeMap, int barHeight) {
		getState().regulators = regulators;
		getState().barcodeMap = barcodeMap;
		getState().barHeight = barHeight;
	}
}
