package org.geworkbenchweb.plugins.msviper.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.layout.SetViewLayout;
import org.geworkbenchweb.layout.UMainLayout;
import org.geworkbenchweb.plugins.Visualizer; 
import org.geworkbenchweb.plugins.msviper.ExcelExport; 
 
import org.geworkbenchweb.pojos.MsViperResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.geworkbenchweb.visualizations.Barcode;
import org.geworkbenchweb.visualizations.BarcodeTable;
import org.geworkbenchweb.visualizations.Regulator;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener; 
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.AbstractOrderedLayout;  
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.OptionGroup; 
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.Reindeer;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class MsViperResultsUI extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 3900963160275267233L;
	private static Log log = LogFactory.getLog(MsViperResultsUI.class);
	
	
	

	private static final String SYMBOL = "Symbol";
	private static final String PROBEID = "Probe Id";
	private static final String[] geneOptions = { PROBEID, SYMBOL };
 
	final private Long datasetId;

	final String[] columnNames = { "MR Marker", "MR Gene Symbol",
			"Markers in Regulon", "NES", "absNES", "P-Value", "FDR",
			"NumLedge", "Shadow Pairs" };

	public MsViperResultsUI(Long dataSetId)  
	{
		this.datasetId = dataSetId;
		if (dataSetId == null)
			return;
		
	    setSpacing(true); 
		setImmediate(true);		
		
		final ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class,
				dataSetId);
		Long id = resultSet.getDataId();
		if (id == null) { // pending node
			addComponent(new Label("Pending computation - ID " + dataSetId));
			return;
		}
		
		final MsViperResult msViperResult = FacadeFactory.getFacade().find(
				MsViperResult.class, id);		
	 
		
		TabSheet tabs = new TabSheet();	
		tabs.setHeight("100%");
		 
		tabs.addTab(getMsViperResultTab(dataSetId, resultSet, msViperResult), "Activity", null);
	 
		if (msViperResult.getShadowResult() != null)		 
			tabs.addTab(getShadowResultTab(dataSetId, resultSet, msViperResult), "Shadow", null);
		 
		addComponent(tabs);
		
	}
	
	public VerticalLayout getMsViperResultTab(Long dataSetId, final ResultSet resultSet, final MsViperResult msViperResult) {
		
		VerticalLayout tab = new VerticalLayout();
		
		final TextField barHeigh = new TextField();
		final TextField graphForTop = new TextField();
		final VerticalSplitPanel splitPanel = new VerticalSplitPanel();
		final Table mraTable = new Table();		 
		 
	
		GridLayout gridLayout1 = new GridLayout(8, 1);

		final OptionGroup genePresent = new OptionGroup("", Arrays.asList(geneOptions));
		genePresent.select(PROBEID);

		genePresent.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -6950089065248041770L;

			public void valueChange(Property.ValueChangeEvent e) {
				updateBarcodeTable(msViperResult, graphForTop.getValue().toString().trim(), barHeigh.getValue().toString().trim(), genePresent.getValue().toString().trim(), mraTable, splitPanel);
			}
		});

		genePresent.addStyleName("horizontal");
		genePresent.setImmediate(true);

		FormLayout f1 = new FormLayout();
		FormLayout f2 = new FormLayout();

		graphForTop.setWidth("50px");
		graphForTop.setCaption("Graphs for top");
		graphForTop.setValue(10);
		final PositiveIntValidator v = new PositiveIntValidator(
				"Please enter a positive integer.");
		// graphForTop.addValidator(v);
		graphForTop.setImmediate(true);

		f1.addComponent(graphForTop);
		f1.setImmediate(true);

		graphForTop.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1048639156493298177L;

			public void valueChange(Property.ValueChangeEvent e) {
				if (v.isValidString(graphForTop.getValue().toString()))
					updateBarcodeTable(msViperResult, graphForTop.getValue().toString().trim(), barHeigh.getValue().toString().trim(), genePresent.getValue().toString().trim(), mraTable, splitPanel);
			}
		});
		
		
		barHeigh.setWidth("50px");
		barHeigh.setCaption("Bar Height");
		barHeigh.setValue(15);
		// barHeigh.addValidator(v);
		barHeigh.setImmediate(true);
		barHeigh.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (v.isValidString(barHeigh.getValue().toString()))
					updateBarcodeTable(msViperResult, graphForTop.getValue().toString().trim(), barHeigh.getValue().toString().trim(), genePresent.getValue().toString().trim(), mraTable, splitPanel);
			}
		});

		f2.addComponent(barHeigh);
		f2.setImmediate(true);

		MenuBar menuBar = new MenuBar();
		menuBar.setStyleName("transparent");
		menuBar.addItem("Export table", new Command() {

			private static final long serialVersionUID = -4510368918141762449L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				ExcelExport excelExport = new ExcelExport(mraTable);
				excelExport.excludeCollapsedColumns();
				excelExport.setDisplayTotals(false);
				excelExport.setDoubleDataFormat("General");
				excelExport.export();
			}

		}).setStyleName("plugin");

	
		gridLayout1.addComponent(menuBar);
		gridLayout1.addComponent(genePresent);
		gridLayout1.addComponent(f1);
		gridLayout1.addComponent(f2);
		gridLayout1.setSpacing(true);
		gridLayout1.setImmediate(true);

		//splitPanel.setHeight("100%");		 
		splitPanel.setHeight(500, Sizeable.UNITS_PIXELS);
		splitPanel.setSplitPosition(250, Sizeable.UNITS_PIXELS);
		splitPanel.setStyleName("small");
		splitPanel.setLocked(false);
		splitPanel.setImmediate(true);

	 
		mraTable.setContainerDataSource(getIndexedContainer(msViperResult,
				 genePresent.getValue().toString(), false));
		 

		mraTable.setSizeFull();
		mraTable.setImmediate(true);
		mraTable.setStyleName(Reindeer.TABLE_STRONG);
		mraTable.setColumnCollapsingAllowed(true);
		mraTable.setSelectable(true);

		mraTable.addListener(new Table.HeaderClickListener() {

			private static final long serialVersionUID = 1900035484003253359L;

			public void headerClick(HeaderClickEvent event) {
				updateBarcodeTable(msViperResult, graphForTop.getValue().toString().trim(), barHeigh.getValue().toString().trim(), genePresent.getValue().toString().trim(), mraTable, splitPanel);
			}

		});

		splitPanel.setFirstComponent(mraTable);
		if (msViperResult.getLeadingEdges() != null)
		{
			menuBar.addItem("Export all targets", new Command() {

				private static final long serialVersionUID = -4510368918141762449L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					downloadAllTargets(mraTable, msViperResult);
				}

			}).setStyleName("plugin");
			menuBar.addItem("Add targets to set", new Command() {

				private static final long serialVersionUID = -4510368918141762449L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					Object id = mraTable.getValue();
					if(id == null)
					{
						String msg = "Please select a row to add targets to set.";
						MessageBox mb = new MessageBox(getWindow(), 
								"Please select a row", MessageBox.Icon.INFO, msg, 
								new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
						mb.show();
						return;
					}
					Item item= mraTable.getItem(mraTable.getValue());
					String mr = item.getItemProperty(columnNames[0]).getValue()
							.toString();
					addMarkerSet("Add Markers to Set", resultSet.getParent(), msViperResult.getRegulons().get(mr));
				}

			}).setStyleName("plugin");

			updateBarcodeTable(msViperResult, graphForTop.getValue().toString().trim(), barHeigh.getValue().toString().trim(), genePresent.getValue().toString().trim(), mraTable, splitPanel);
		}
		else {
			menuBar.addItem("Export all shadow pairs", new Command() {

				private static final long serialVersionUID = -4510368918141762449L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					downloadAllShadowPairs(msViperResult);
				}

			}).setStyleName("plugin");
			barHeigh.setEnabled(false);
			graphForTop.setEnabled(false);
			genePresent.setEnabled(false);
		}

		tab.setSizeFull();
		tab.addComponent(gridLayout1);
		tab.addComponent(splitPanel);
		tab.setExpandRatio(splitPanel, 1);
		tab.setSpacing(true);
		tab.setImmediate(true);
		 
		 
		return tab;
		
	}
	
	
public VerticalLayout getShadowResultTab(Long dataSetId, final ResultSet resultSet, final MsViperResult msViperResult) {
		
		VerticalLayout tab = new VerticalLayout();		
	 
		final VerticalSplitPanel splitPanel = new VerticalSplitPanel();
		final Table mraTable = new Table();		 
	
		GridLayout gridLayout1 = new GridLayout(8, 1);
		 
		MenuBar menuBar = new MenuBar();
		menuBar.setStyleName("transparent");
		menuBar.addItem("Export table", new Command() {

			private static final long serialVersionUID = -4510368918141762449L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				ExcelExport excelExport = new ExcelExport(mraTable);
				excelExport.excludeCollapsedColumns();
				excelExport.setDisplayTotals(false);
				excelExport.setDoubleDataFormat("General");
				excelExport.export();
			}

		}).setStyleName("plugin");

	
		gridLayout1.addComponent(menuBar);
		 
		gridLayout1.setSpacing(true);
		gridLayout1.setImmediate(true);

		
		splitPanel.setHeight(500, Sizeable.UNITS_PIXELS);
		splitPanel.setSplitPosition(250, Sizeable.UNITS_PIXELS);
		splitPanel.setStyleName("small");
		splitPanel.setLocked(false);

	 
		mraTable.setContainerDataSource(getIndexedContainer(msViperResult, null, true));		 

		mraTable.setSizeFull();
		mraTable.setImmediate(true);
		mraTable.setStyleName(Reindeer.TABLE_STRONG);
		mraTable.setColumnCollapsingAllowed(true);
		mraTable.setSelectable(true);

		 

		splitPanel.setFirstComponent(mraTable);
	 
			menuBar.addItem("Export all shadow pairs", new Command() {

				private static final long serialVersionUID = -4510368918141762449L;

				@Override
				public void menuSelected(MenuItem selectedItem) {
					downloadAllShadowPairs(msViperResult);
				}

			}).setStyleName("plugin");
			 
		 

			tab.setSizeFull();
			tab.addComponent(gridLayout1);
			tab.addComponent(splitPanel);			 
			tab.setSpacing(true);
			tab.setImmediate(true);
		
		return tab;
		
	}
	
	

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

	IndexedContainer getIndexedContainer(final MsViperResult result,
			String present, final boolean isShadowTable) {
		
		String[][] rdata = null;
		final Map<String, List<String>> shadowPairs = result.getShadow_pairs();
		final Map<String, List<String>> leadingEdges = result.getLeadingEdges();
		
		if(isShadowTable == true)		 
			rdata = result.getShadowResult();	 
		else
			rdata= result.getMrsResult();
		
		IndexedContainer dataIn = new IndexedContainer() {
          
			private static final long serialVersionUID = 1L;

			@Override
            public Collection<?> getSortableContainerPropertyIds() {
                // Default implementation allows sorting only if the property
                // type can be cast to Comparable
                return getContainerPropertyIds();
            }
        };
        
        dataIn.setItemSorter(new DefaultItemSorter(new Comparator<Object>() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof Button && o2 instanceof Button) {
                    String caption1 = ((Button) o1).getCaption();
                    String caption2 = ((Button) o2).getCaption();
                    return caption1.compareTo(caption2);

                } 
                else if (o1 instanceof String && o2 instanceof String) {
                    return ((String) o1).compareTo(
                            ((String) o2));
                }else if (o1 instanceof Integer && o2 instanceof Integer) {
                    return ((Integer) o1).compareTo(
                            (Integer) o2);
                }
                else if (o1 instanceof Double && o2 instanceof Double) {
                    return ((Double) o1).compareTo(
                            (Double) o2);
                }
                else
                	return 0;
            }
        }));

		for (String col : columnNames) {
			if (col.equals("MR Marker") || col.equals("MR Gene Symbol"))
				dataIn.addContainerProperty(col, String.class, null);
			else if (col.equals("Markers in Regulon"))
				dataIn.addContainerProperty(col, Integer.class, null);
			else if (col.equals("Shadow Pairs")) {
				if (isShadowTable)
					dataIn.addContainerProperty(col, Button.class, null);
			} else if (col.equals("NumLedge")) {
				if (!isShadowTable)
					dataIn.addContainerProperty(col, Button.class, null);
			} else
				dataIn.addContainerProperty(col, Double.class, null);
		}		

		// Button[] buttonArray = new Button[rdata.length];
		for (int i = 0; i < rdata.length; i++) {
			Item item = dataIn.addItem(i);
			item.getItemProperty(columnNames[0]).setValue(rdata[i][0]);
			item.getItemProperty(columnNames[1]).setValue(rdata[i][1]);
			item.getItemProperty(columnNames[2]).setValue(rdata[i][2]);
			item.getItemProperty(columnNames[3]).setValue(rdata[i][3]);
			item.getItemProperty(columnNames[4]).setValue(
					Math.abs(new Double(rdata[i][3])));
			item.getItemProperty(columnNames[5]).setValue(rdata[i][4]);
			item.getItemProperty(columnNames[6]).setValue(rdata[i][5]);
			Button detailsField = null;
			if (!isShadowTable) {
				List<String> m = leadingEdges.get(rdata[i][0]);
				if (m == null) {
					detailsField = new Button("0");
					item.getItemProperty(columnNames[7]).setValue(detailsField);

				} else {
					Integer size = leadingEdges.get(rdata[i][0]).size();
					detailsField = new Button(size.toString());
					item.getItemProperty(columnNames[7]).setValue(detailsField);
				}

			} else  {
				List<String> m = shadowPairs.get(rdata[i][0]);
				if (m == null) {
					detailsField = new Button("0");
					item.getItemProperty(columnNames[8]).setValue(detailsField);
				} else {
					Integer size = shadowPairs.get(rdata[i][0]).size();
					detailsField = new Button(size.toString());
					item.getItemProperty(columnNames[8]).setValue(detailsField);

				}

			}
			detailsField.setStyleName(Reindeer.BUTTON_LINK);
			detailsField.setData(rdata[i][0]);
			detailsField.addListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {
					// Get the item identifier from the user-defined data.
					String mr = (String) event.getButton().getData();
					if (!isShadowTable)
						getLedgeExportWindow(mr, result);
					else
						getShadowPairWindow(mr, result);
				}
			});

		}

		return dataIn;

	}

	private List<Regulator> getRegulators(MsViperResult msViperResult, final String genePresentStr,  Table mraTable, int n) {

		List<Regulator> regulators = new ArrayList<Regulator>();
		Map<String, Double> mrs_signatures = msViperResult.getMrs_signatures();
		Map<String, Integer> ranks = msViperResult.getRanks();
		Double maxAbsNes = getAbsMaxNes(msViperResult.getMrsResult());
		double maxAbsSig = getAbsMaxSig(msViperResult.getMinVal(),
				msViperResult.getMaxVal());
		int count = 1;
		for (Object id : mraTable.getItemIds()) {
			if (count > n)
				break;
			Item item = mraTable.getItem(id);
			String mr = item.getItemProperty(columnNames[0]).getValue()
					.toString();
			double nes = new Double(item.getItemProperty(columnNames[3])
					.getValue().toString());
			double pvalue = new Double(item.getItemProperty(columnNames[5])
					.getValue().toString());
			String daColor = calculateColor(maxAbsNes, nes);
			String deColor = calculateColor(maxAbsSig, mrs_signatures.get(mr));
			int rank = ranks.get(mr);
			if (!genePresentStr.equalsIgnoreCase(PROBEID))
				mr = item.getItemProperty(columnNames[1]).getValue().toString();

			Regulator r = new Regulator(mr, pvalue, daColor, deColor, rank);
			regulators.add(r);
			count++;

		}

		return regulators;
	}

	private Map<String, List<Barcode>> getBarcodeMap(
			MsViperResult msViperResult, final Table mraTable, final String present, int n) {

		Map<String, List<Barcode>> allBarcodeMap = msViperResult.getBarcodes();
		if (allBarcodeMap == null || allBarcodeMap.size() == 0)
			return null;
		Map<String, List<Barcode>> showBarcodeMap = new HashMap<String, List<Barcode>>();
		//String present = genePresent.getValue().toString();

		int count = 1;
		for (Object id : mraTable.getItemIds()) {
			if (count > n)
				break;
			Item item = mraTable.getItem(id);

			if (present == PROBEID) {
				String mr = item.getItemProperty(columnNames[0]).getValue()
						.toString();
				showBarcodeMap.put(mr, allBarcodeMap.get(mr));
			} else {
				String mr1 = item.getItemProperty(columnNames[0]).getValue()
						.toString();
				String mr2 = item.getItemProperty(columnNames[1]).getValue()
						.toString();
				showBarcodeMap.put(mr2, allBarcodeMap.get(mr1));
			}

			count++;
		}
		return showBarcodeMap;
	}

	private double getAbsMaxNes(String[][] rdata) {
		double maxNes;
		List<Double> nesList = new ArrayList<Double>();
		for (int i = 0; i < rdata.length; i++)
			nesList.add(Math.abs(new Double(rdata[i][3])));
		Collections.sort(nesList);
		maxNes = nesList.get(nesList.size() - 1);

		return maxNes;
	}

	private double getAbsMaxSig(double min, double max) {
		double maxSig;

		maxSig = Math.max(Math.abs(min), Math.abs(max));

		return maxSig;
	}

	private String calculateColor(double absMaxValue, double value) {

		int colorindex = 0;
		if (absMaxValue != 0)
			colorindex = (int) (255 * value / absMaxValue);

		if (colorindex < 0) {
			colorindex = Math.abs(colorindex);
			return "rgb(" + (255 - colorindex) + ", " + (255 - colorindex)
					+ ", 255)";
		} else
			return "rgb(255, " + (255 - colorindex) + ", " + (255 - colorindex)
					+ ")";
	}

	private void updateBarcodeTable(final MsViperResult msViperResult, final String graphForTopStr, final String barHeighStr, final String genePresentStr, final Table mraTable, final VerticalSplitPanel splitPanel) {

		PositiveIntValidator v = new PositiveIntValidator("");		 
		if (!(v.isValidString(graphForTopStr) && v.isValidString(barHeighStr)))
			return;

		List<Regulator> regulators = getRegulators(msViperResult, genePresentStr, mraTable, new Integer(
				graphForTopStr));
		Map<String, List<Barcode>> barcodeMap = getBarcodeMap(msViperResult, mraTable, genePresentStr,
				new Integer(graphForTopStr));
		
		if (splitPanel.getSecondComponent() != null)
			splitPanel.removeComponent(splitPanel.getSecondComponent());
 
		if ( barcodeMap == null )
		{	
			return;
		}
		BarcodeTable barcodeTable = new BarcodeTable(regulators, barcodeMap,
				new Integer(barHeighStr));
		barcodeTable.setImmediate(true);
		barcodeTable.setSizeFull();

		if (splitPanel.getSecondComponent() != null)
			splitPanel.removeComponent(splitPanel.getSecondComponent());
		splitPanel.setSecondComponent(barcodeTable);
	}

	public class PositiveIntValidator extends IntegerValidator {
		private static final long serialVersionUID = -8205632597275359667L;
		private int max = 0;

		public PositiveIntValidator(String message) {
			super(message);
		}

		public PositiveIntValidator(String message, int max) {
			this(message);
			this.max = max;
		}

		protected boolean isValidString(String value) {
			try {
				int n = Integer.parseInt(value);
				if (n <= 0)
					return false;
				if (max > 0 && n > max)
					return false;
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	}

	private void downloadAllTargets(Table mraTable, MsViperResult msViperResult) {
		final Application app = getApplication();
		String dir = GeworkbenchRoot.getBackendDataDirectory() + File.separator
				+ SessionHandler.get().getUsername() + File.separator
				+ "export" + File.separator + "msviper";
		if (!new File(dir).exists())
			new File(dir).mkdirs();

		final File file = new File(dir, "allTargets_"
				+ System.currentTimeMillis() + ".csv");
		try {
			Map<String, List<String>> regulons = msViperResult
					.getRegulons();
			Map<String, Double> mrs_signatures = msViperResult
					.getMrs_signatures();
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			int k = 0;
			for (Object id : mraTable.getItemIds()) {
				Item item = mraTable.getItem(id);
				String mrMarker = item.getItemProperty(columnNames[0])
						.getValue().toString();
				if( k > 0)
				   pw.println("");
				pw.println(mrMarker);
				List<String> targetList = regulons.get(mrMarker);
				if (targetList == null || targetList.size() == 0)
					continue;
				for (int i = 0; i < targetList.size(); i++) {
					pw.println(targetList.get(i) + ", "
							+ mrs_signatures.get(targetList.get(i)));
				}
				
				k++;

			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file, app);
		app.getMainWindow().open(resource);
	}

	private void downloadAllShadowPairs(MsViperResult msViperResult) {
		final Application app = getApplication();
		String dir = GeworkbenchRoot.getBackendDataDirectory() + File.separator
				+ SessionHandler.get().getUsername() + File.separator
				+ "export" + File.separator + "msviper";
		if (!new File(dir).exists())
			new File(dir).mkdirs();

		final File file = new File(dir, "allShadowPairs_"
				+ System.currentTimeMillis() + ".csv");
		try {
			Map<String, List<String>> pairMap = msViperResult.getShadow_pairs();					 
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			 
			for (String key : pairMap.keySet()) {			 
				List<String> pairList = pairMap.get(key);
				for (int i = 0; i < pairList.size(); i++) {
					pw.println(pairList.get(i) + ", "
							+  key);
				}		 
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Resource resource = new FileResource(file, app);
		app.getMainWindow().open(resource);
	}
	
	private void getLedgeExportWindow(String mr, final MsViperResult result) {
		List<String> ledgeList = result.getLeadingEdges().get(mr);
		if (ledgeList != null && ledgeList.size() > 0) {
			Map<String, Double> ledgeMap = new HashMap<String, Double>();
			for (int i = 0; i < ledgeList.size(); i++)
				ledgeMap.put(ledgeList.get(i),
						result.getMrs_signatures().get(ledgeList.get(i)));
			DetailDialog dialog = new DetailDialog(this, ledgeMap, mr);
			dialog.openLedgeDialog();
		}
	}

	private void getShadowPairWindow(String mr, final MsViperResult result) {
		List<String> pairList = result.getShadow_pairs().get(mr);
		if (pairList != null && pairList.size() > 0) {
			DetailDialog dialog = new DetailDialog(this, pairList, mr);
			dialog.openPairDialog();
		}
	}
	
	 
	public void addMarkerSet(final String caption, final Long parentId, final List<String> targetList) {		 
		final Window nameWindow = new Window();
		nameWindow.setModal(true);
		nameWindow.setClosable(true);
		((AbstractOrderedLayout) nameWindow.getContent()).setSpacing(true);
		nameWindow.setWidth("300px");
		nameWindow.setHeight("150px");
		nameWindow.setResizable(false);
		nameWindow.setCaption(caption);
		nameWindow.setImmediate(true);

		final TextField setName = new TextField();
		setName.setInputPrompt("Please enter set name");
		setName.setImmediate(true);
		
		final Window mainWindow = this.getApplication().getMainWindow();
		
		Button submit = new Button("Submit", new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				UMainLayout mainLayout = null;
				ComponentContainer content = mainWindow.getContent();
				if(content instanceof UMainLayout) {
					mainLayout = (UMainLayout)content;
				} else {
					log.error("unable to get UMainLayout");
					return;
				}
				SetViewLayout setViewLayout = mainLayout.getSetViewLayout();
				
				String newSetName = (String) setName.getValue();

				try {
					Long subSetId = 0L;
					ArrayList<String> items = null;
					String parentSet = null;
					Tree tree = null;

					if(setName.getValue() != null) {
						 
							items = (ArrayList<String>) targetList;
							subSetId = SubSetOperations.storeMarkerSetInCurrentContext(items, newSetName, parentId);
							parentSet = "MarkerSets";
							if(setViewLayout!=null) {
								tree = setViewLayout.getMarkerSetTree();
							}
						 
						mainWindow.removeWindow(nameWindow);
					}
					
					if(tree!=null) { /* set view instead of workspace view */
						tree.addItem(subSetId);
						tree.getContainerProperty(subSetId, SetViewLayout.SET_DISPLAY_NAME).setValue(newSetName + " [" + items.size() + "]");
						tree.setParent(subSetId, parentSet);
						tree.setChildrenAllowed(subSetId, true);
						for(int j=0; j<items.size(); j++) {
							String itemLabel = items.get(j); 
							tree.addItem(itemLabel+subSetId);
							tree.getContainerProperty(itemLabel+subSetId, SetViewLayout.SET_DISPLAY_NAME).setValue(itemLabel);
							tree.setParent(itemLabel+subSetId, subSetId);
							tree.setChildrenAllowed(itemLabel+subSetId, false);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		submit.setClickShortcut(KeyCode.ENTER);
		nameWindow.addComponent(setName);
		nameWindow.addComponent(submit);
		mainWindow.addWindow(nameWindow);
	}


}
