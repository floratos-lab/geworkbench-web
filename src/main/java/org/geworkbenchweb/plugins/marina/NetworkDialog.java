package org.geworkbenchweb.plugins.marina;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.parsers.InputFileFormatException;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

public class NetworkDialog {
	private Log log = LogFactory.getLog(NetworkDialog.class);
	private MarinaUI ui;

	private Window mainWindow;
	private Window loadDialog;
	private ComboBox formatBox;
	private ComboBox presentBox;
	private int correlationCol = 3;
	private String selectedRepresentedBy = AdjacencyMatrixDataSet.PROBESET_ID;
	private HashMap<String, String> interactionTypeMap = null;
	private boolean isRestrict = true;
	private String selectedFormat = AdjacencyMatrixDataSet.ADJ_FORMART;
	private String marina5colformat = "marina 5-column format";
	
	private final String networkName, networkString;

	public NetworkDialog(MarinaUI ui, String networkName, String networkString){
		this.ui = ui;
		this.mainWindow = ui.getApplication().getMainWindow();
		this.networkName = networkName;
		this.networkString = networkString;
	}

	public void openDialog(){
		loadDialog = new Window();
		loadDialog.setCaption("Load Interaction Network");

		formatBox = new ComboBox("File Format");
		formatBox.setSizeFull();
		formatBox.setNullSelectionAllowed(false);
		formatBox.addItem(AdjacencyMatrixDataSet.ADJ_FORMART);
		formatBox.addItem(AdjacencyMatrixDataSet.SIF_FORMART);
		formatBox.addItem(marina5colformat);

		presentBox = new ComboBox("Node Represented By");
		presentBox.setSizeFull();
		presentBox.setNullSelectionAllowed(false);
		presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
		presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
		presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
		presentBox.addItem(AdjacencyMatrixDataSet.OTHER);

		Button continueButton = new Button("Continue");
		Button cancelButton = new Button("Cancel");
		formatBox.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = -7717934520937460169L;
			public void valueChange(ValueChangeEvent event) {
				if (formatBox.getValue().toString().equals(
						AdjacencyMatrixDataSet.ADJ_FORMART)) {
					presentBox.removeAllItems();
					presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
					presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
					presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
					presentBox.addItem(AdjacencyMatrixDataSet.OTHER);
					presentBox.setValue(AdjacencyMatrixDataSet.PROBESET_ID);
				} else if (formatBox.getValue().toString().equals(
						marina5colformat)) {
					presentBox.removeAllItems();
					presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
					presentBox.setValue(AdjacencyMatrixDataSet.PROBESET_ID);
				} else {
					presentBox.removeAllItems();
					presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
					presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
					presentBox.addItem(AdjacencyMatrixDataSet.OTHER);
					presentBox.setValue(AdjacencyMatrixDataSet.GENE_NAME);
				}
			}
		});

		if (networkName.toLowerCase().endsWith(".sif"))
			formatBox.setValue(AdjacencyMatrixDataSet.SIF_FORMART);
		else if (networkName.toLowerCase().contains("5col"))
			formatBox.setValue(marina5colformat);
		else
			formatBox.setValue(AdjacencyMatrixDataSet.ADJ_FORMART);

		continueButton.addListener(new ClickListener(){
			private static final long serialVersionUID = -5207079864397027215L;
			public void buttonClick(ClickEvent event) {
				selectedFormat = formatBox.getValue().toString();
				selectedRepresentedBy = presentBox.getValue().toString();
				mainWindow.removeWindow(loadDialog);

				if ((selectedFormat.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART) && !networkName.toLowerCase().endsWith(".sif"))
						|| (networkName.toLowerCase().endsWith(".sif") && !selectedFormat
								.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART))
						||(selectedFormat.equals(marina5colformat) && !is5colnetwork(networkString))){
					ui.networkNotLoaded("The network format selected does not match that of the file.");
					return;
				}

				if (selectedFormat.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)) {
					interactionTypeMap = new org.geworkbench.parsers.AdjacencyMatrixFileFormat().getInteractionTypeMap();
				}
				
				if (!selectedFormat.equals(marina5colformat)){
					try {
						NetworkCreator networkCreator = new NetworkCreator(ui);
						AdjacencyMatrix matrix = networkCreator.parseAdjacencyMatrix(networkString,
								interactionTypeMap, selectedFormat,
								selectedRepresentedBy, isRestrict);
						ui.networkLoaded(networkCreator.getNetworkFromAdjMatrix(matrix));						 
					} catch (InputFileFormatException e1) {
						log.error(e1.getMessage());
						e1.printStackTrace();
					}
				}else{
					ui.networkLoaded(networkString);
				}
			}
		});
		cancelButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1940630593562212467L;
			public void buttonClick(ClickEvent event) {
				mainWindow.removeWindow(loadDialog);
				ui.networkNotLoaded(null);
			}
		});

		HorizontalLayout bar = new HorizontalLayout();
		bar.setSpacing(true);
		bar.addComponent(cancelButton);
		bar.addComponent(continueButton);
		
		Form loadform = new Form();
		loadform.getLayout().addComponent(formatBox);
		loadform.getLayout().addComponent(presentBox);
		loadform.getFooter().addComponent(bar);

		loadDialog.addComponent(loadform);
		loadDialog.setWidth("340px");
		loadDialog.setModal(true);
		loadDialog.setVisible(true);
		mainWindow.addWindow(loadDialog);
	}
	
	/**
	 * Test if the network is in 5-column format, and if all correlation cols are positive.
	 * @param bytes    network in bytes
	 * @return if the network is in 5-column format
	 */
	private boolean is5colnetwork(String network){
		if (network == null || network.length() == 0)
			return false;

		ui.allpos = true;
		String[] lines = network.split("\n");
		if (lines == null || lines.length == 0)
			return false;
		for (String line : lines) {
			String[] toks = line.split("\t");
			if (toks.length != 5 || !isDouble(toks[2]) || !isDouble(toks[3])
					|| !isDouble(toks[4]))
				return false;
			if (ui.allpos && Double.valueOf(toks[correlationCol]) < 0)
				ui.allpos = false;
		}
		return true;
	}

	private boolean isDouble(String s){
		try{
			Double.parseDouble(s);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}
