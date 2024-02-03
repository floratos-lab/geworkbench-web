package org.geworkbenchweb.visualizations;

import java.util.List;
import java.util.Map;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class BarcodeTableState extends JavaScriptComponentState {
    public String[] columnNames = { "Regulator", "Bar Graph", "PValue", "Activity", "Expression", "Exp. Rank" };
	public List<Regulator> regulators;
	public Map<String, List<Barcode>> barcodeMap;
	public int barHeight;
}
