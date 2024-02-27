package org.geworkbenchweb.plugins.msviper.results;

import java.util.List;
import java.util.Map;

import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class DetailDialog {

	private UI mainWindow;
	private Window loadDialog;
	private Map<String, Double> ledgeMap;
	private List<String> shadowPairList;
	private String mr;

	public DetailDialog(MsViperResultsUI ui, Map<String, Double> ledgeMap, final String mr) {
		this.mainWindow = UI.getCurrent();
		this.ledgeMap = ledgeMap;
		this.mr = mr;
	}

	public DetailDialog(MsViperResultsUI ui, List<String> shadowPairList, String mr) {
		this.mainWindow = UI.getCurrent();
		this.shadowPairList = shadowPairList;
		this.mr = mr;
	}

	public void openLedgeDialog() {
		loadDialog = new Window();
		loadDialog.setCaption("Leading Edge Details for " + mr);

		final Table ledgeTable = new Table();
		ledgeTable.setContainerDataSource(getLedgeIndexedContainer());

		ledgeTable.setSizeFull();
		ledgeTable.setImmediate(true);
		ledgeTable.setStyleName(Reindeer.TABLE_STRONG);
		ledgeTable.setColumnCollapsingAllowed(true);
		ledgeTable.setSelectable(true);

		final Button exportButton = new Button("Export");

		exportButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -5207079864397027215L;

			public void buttonClick(ClickEvent event) {

				ExcelExport excelExport = new ExcelExport(ledgeTable);
				excelExport.excludeCollapsedColumns();
				excelExport.setDisplayTotals(false);
				excelExport.setDoubleDataFormat("General");
				excelExport.export();

			}
		});

		VerticalLayout v = new VerticalLayout();
		v.setSpacing(true);
		v.addComponent(ledgeTable);
		v.addComponent(exportButton);

		loadDialog.setContent(v);
		loadDialog.setWidth("400px");
		loadDialog.setModal(true);
		loadDialog.setVisible(true);
		mainWindow.addWindow(loadDialog);
	}

	public void openPairDialog() {
		loadDialog = new Window();
		loadDialog.setCaption("Shadow Pairs for " + mr);

		final Table pairTable = new Table();
		pairTable.setContainerDataSource(getPairIndexedContainer());

		pairTable.setSizeFull();
		pairTable.setImmediate(true);
		pairTable.setStyleName(Reindeer.TABLE_STRONG);
		pairTable.setColumnCollapsingAllowed(true);
		pairTable.setSelectable(true);

		VerticalLayout v = new VerticalLayout();
		v.setSpacing(true);
		v.addComponent(pairTable);

		loadDialog.setContent(v);
		loadDialog.setWidth("340px");
		loadDialog.setModal(true);
		loadDialog.setVisible(true);
		mainWindow.addWindow(loadDialog);
	}

	IndexedContainer getLedgeIndexedContainer() {

		IndexedContainer dataIn = new IndexedContainer();
		dataIn.addContainerProperty("Markers in Leading Edge", String.class, null);
		dataIn.addContainerProperty("Signature Value", Double.class, null);

		// Button[] buttonArray = new Button[rdata.length];
		int i = 0;
		for (String key : ledgeMap.keySet()) {
			Item item = dataIn.addItem(i);
			item.getItemProperty("Markers in Leading Edge").setValue(key);
			item.getItemProperty("Signature Value").setValue(ledgeMap.get(key));
			i++;
		}
		return dataIn;

	}

	IndexedContainer getPairIndexedContainer() {

		IndexedContainer dataIn = new IndexedContainer();
		dataIn.addContainerProperty("V1", String.class, null);
		dataIn.addContainerProperty("V2", String.class, null);
		for (int i = 0; i < shadowPairList.size(); i++) {
			Item item = dataIn.addItem(i);
			item.getItemProperty("V1").setValue(shadowPairList.get(i));
			item.getItemProperty("V2").setValue(mr);

		}
		return dataIn;

	}

}
