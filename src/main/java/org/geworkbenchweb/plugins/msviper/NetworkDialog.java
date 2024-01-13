package org.geworkbenchweb.plugins.msviper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbenchweb.GeworkbenchRoot;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NetworkDialog {
	private Log log = LogFactory.getLog(NetworkDialog.class);
	private MsViperUI ui;

	private Window loadDialog;
	private ComboBox formatBox;
	private ComboBox presentBox;
	private int correlationCol = 3;
	private String selectedRepresentedBy = AdjacencyMatrixDataSet.PROBESET_ID;
	private HashMap<String, String> interactionTypeMap = null;
	private String selectedFormat = AdjacencyMatrixDataSet.ADJ_FORMART;
	private String marina5colformat = "marina 5-column format";

	private final String networkName;

	/* This is not only a visual clue, but crucial to trigger the UI update. */
	final ProgressIndicator indicator = new ProgressIndicator(new Float(0.0));

	public NetworkDialog(MsViperUI ui, String networkName) {
		this.ui = ui;
		this.networkName = networkName;
	}

	public void openDialog() {
		loadDialog = new Window();
		loadDialog.setCaption("Load Interaction Network");

		formatBox = new ComboBox("File Format");
		formatBox.setSizeFull();
		formatBox.setNullSelectionAllowed(false);
		formatBox.addItem(AdjacencyMatrixDataSet.ADJ_FORMART);
		// formatBox.addItem(AdjacencyMatrixDataSet.SIF_FORMART);
		// formatBox.addItem(marina5colformat);

		presentBox = new ComboBox("Node Represented By");
		presentBox.setSizeFull();
		presentBox.setNullSelectionAllowed(false);
		presentBox.addItem(AdjacencyMatrixDataSet.PROBESET_ID);
		presentBox.addItem(AdjacencyMatrixDataSet.GENE_NAME);
		presentBox.addItem(AdjacencyMatrixDataSet.ENTREZ_ID);
		presentBox.addItem(AdjacencyMatrixDataSet.OTHER);

		final Button continueButton = new Button("Continue");
		final Button cancelButton = new Button("Cancel");
		formatBox.addValueChangeListener(new Property.ValueChangeListener() {
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

		continueButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -5207079864397027215L;

			public void buttonClick(ClickEvent event) {
				final WorkThread thread = new WorkThread();
				thread.start();

				indicator.setVisible(true);
				continueButton.setVisible(false);
			}
		});
		cancelButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1940630593562212467L;

			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(loadDialog);
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

		indicator.setSizeFull();
		indicator.setVisible(false);

		VerticalLayout layout = new VerticalLayout();
		loadDialog.setContent(layout);
		layout.addComponent(loadform);
		layout.addComponent(indicator);
		layout.addComponent(bar);
		loadDialog.setWidth("340px");
		loadDialog.setModal(true);
		loadDialog.setVisible(true);
		UI.getCurrent().addWindow(loadDialog);
	}

	private class WorkThread extends Thread {

		public void run() {

			selectedFormat = formatBox.getValue().toString();
			selectedRepresentedBy = presentBox.getValue().toString();

			if ((selectedFormat.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)
					&& !networkName.toLowerCase().endsWith(".sif"))
					|| (networkName.toLowerCase().endsWith(".sif") && !selectedFormat
							.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART))) {
				ui.networkNotLoaded("The network format selected does not match that of the file.");
				return;
			}

			if (selectedFormat.equalsIgnoreCase(AdjacencyMatrixDataSet.SIF_FORMART)) {
				interactionTypeMap = new org.geworkbench.parsers.AdjacencyMatrixFileFormat().getInteractionTypeMap();
			}

			String uploadedFile = GeworkbenchRoot.getBackendDataDirectory()
					+ File.separator + "networks" + File.separator + "msViper" + File.separator + ui.userId
					+ File.separator + ui.dataSetId + File.separator + networkName;
			if (!selectedFormat.equals(marina5colformat)) {
				try {
					NetworkCreator networkCreator = new NetworkCreator(ui, indicator);
					AdjacencyMatrix matrix = networkCreator.parseAdjacencyMatrix(uploadedFile,
							interactionTypeMap, selectedFormat,
							selectedRepresentedBy);

					if (matrix.getNodeNumber() == 0) {
						ui.networkNotLoaded("zero node in the network");
					} else {
						ui.networkLoaded();
					}
				} catch (InputFileFormatException e1) {
					log.error(e1.getMessage());
					ui.networkNotLoaded(e1.getMessage());
				}
			} else if (!is5colnetwork(uploadedFile)) {
				ui.networkNotLoaded("The network file is not 5-column format as claimed.");
			} else { /* the case of 5-columned file */
				ui.networkLoaded();
			}

			UI.getCurrent().removeWindow(loadDialog);
		}
	}

	/**
	 * Test if the network is in 5-column format, and if all correlation cols are
	 * positive.
	 * 
	 * @return if the network is in 5-column format
	 */
	private boolean is5colnetwork(String networkFile) {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(networkFile));

			ui.allpos = true;

			String line = br.readLine();
			while (line != null) {
				String[] toks = line.split("\t");
				if (toks.length != 5 || !isDouble(toks[2])
						|| !isDouble(toks[3]) || !isDouble(toks[4])) {
					br.close();
					return false;
				}
				if (ui.allpos && Double.valueOf(toks[correlationCol]) < 0)
					ui.allpos = false;

				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return true;
	}

	private boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
