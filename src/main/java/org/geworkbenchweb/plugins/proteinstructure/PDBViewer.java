package org.geworkbenchweb.plugins.proteinstructure;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.visualizations.MoleculeViewer;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

public class PDBViewer extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = -2450376890763433346L;
	final private Long dataId;
	final private String pdbContent;

	public PDBViewer(Long dataSetId) {

		this.dataId = dataSetId;
		if (dataId == null) {
			pdbContent = null;
			return;
		}

		this.setSizeFull();

		DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		// Long ownerId = data.getOwner();
		FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		String filename = data.getName();
		String fullPath = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ SessionHandler.get().getUsername() + SLASH + DATASETS + SLASH + filename;
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fullPath));
			String line = br.readLine();
			while (line != null) {
				sb.append(line + "\n");
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pdbContent = sb.toString();
	}

	private final String DATASETS = "data";
	private final String SLASH = "/";

	@Override
	public void attach() {
		final MenuBar toolBar = new MenuBar();
		toolBar.setStyleName("transparent");

		String[] representation = { "Ball and Stick", "van der Waals Spheres", "Stick", "Wireframe", "Line" };
		final MenuItem representationType = toolBar.addItem("3D Representation", null);
		representationType.setStyleName("plugin");

		final MoleculeViewer m = new MoleculeViewer(pdbContent);

		Command representationCommand = new Command() {

			private static final long serialVersionUID = -6824514348952478474L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				m.set3DRepresentation(selectedItem.getText());
			}

		};
		for (String r : representation) {
			representationType.addItem(r, representationCommand);
		}

		final MenuItem displaySettings = toolBar.addItem("Display Settings", null);
		final MenuItem proteinOptions = toolBar.addItem("Protein Options", null);

		Command displayCommand = new Command() {

			private static final long serialVersionUID = -6824514348952478474L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				String option = selectedItem.getText();
				boolean checked = selectedItem.isChecked();
				updateDisplay(m, option, checked);
			}

		};
		MenuItem a = displaySettings.addItem(OPTION_DISPLAY_ATOMS, displayCommand);
		a.setCheckable(true);
		a.setChecked(false);
		MenuItem b = displaySettings.addItem(OPTION_DISPLAY_BONDS, displayCommand);
		b.setCheckable(true);
		b.setChecked(false);
		MenuItem displayLabels = displaySettings.addItem(OPTION_DISPLAY_LABELS, displayCommand);
		displayLabels.setCheckable(true);
		displayLabels.setChecked(false);

		MenuItem r = proteinOptions.addItem(OPTION_DISPLAY_RIBBON, displayCommand);
		r.setCheckable(true);
		r.setChecked(true);
		MenuItem backbone = proteinOptions.addItem(OPTION_DISPLAY_BACKBONE, displayCommand);
		backbone.setCheckable(true);
		backbone.setChecked(false);
		MenuItem pipe = proteinOptions.addItem(OPTION_DISPLAY_PIPE_PLANK, displayCommand);
		pipe.setCheckable(true);
		pipe.setChecked(false);

		proteinOptions.addSeparator();
		MenuItem cartoonize = proteinOptions.addItem(OPTION_CARTOONIZE, displayCommand);
		cartoonize.setCheckable(true);
		cartoonize.setChecked(true);
		MenuItem colorByChain = proteinOptions.addItem(OPTION_COLOR_BY_CHAIN, displayCommand);
		colorByChain.setCheckable(true);
		colorByChain.setChecked(false);
		MenuItem colorByResidue = proteinOptions.addItem(OPTION_COLOR_BY_RESIDUE, displayCommand);
		colorByResidue.setCheckable(true);
		colorByResidue.setChecked(false);

		MenuItem residueColorType = proteinOptions.addItem("Residue Color", null);

		Command colorTypeCommand = new Command() {

			private static final long serialVersionUID = -6451943828238593502L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				m.setResidueColorType(selectedItem.getText().toLowerCase());
			}

		};
		residueColorType.addItem("Amino", colorTypeCommand);
		residueColorType.addItem("Shapely", colorTypeCommand);
		residueColorType.addItem("Polar", colorTypeCommand);
		residueColorType.addItem("Acidity", colorTypeCommand);
		residueColorType.addItem("Rainbow", colorTypeCommand);

		this.addComponent(toolBar);
		this.addComponent(m);
	}

	private final String OPTION_DISPLAY_ATOMS = "Display Atoms";
	private final String OPTION_DISPLAY_BONDS = "Display Bonds";
	private final String OPTION_DISPLAY_LABELS = "Display Labels";
	private final String OPTION_DISPLAY_RIBBON = "Display Ribbon";
	private final String OPTION_DISPLAY_BACKBONE = "Display Backbone";
	private final String OPTION_DISPLAY_PIPE_PLANK = "Display Pipe/Plank";
	private final String OPTION_CARTOONIZE = "Cartoonize";
	private final String OPTION_COLOR_BY_CHAIN = "Color by Chain";
	private final String OPTION_COLOR_BY_RESIDUE = "Color by Residue";

	private void updateDisplay(MoleculeViewer m, String option, boolean checked) {
		switch (option) {
			case OPTION_DISPLAY_ATOMS:
				m.setDisplayAtoms(checked);
				break;
			case OPTION_DISPLAY_BONDS:
				m.setDisplayBonds(checked);
				break;
			case OPTION_DISPLAY_LABELS:
				m.setDisplayLabels(checked);
				break;
			case OPTION_DISPLAY_RIBBON:
				m.setDisplayRibbon(checked);
				break;
			case OPTION_DISPLAY_BACKBONE:
				m.setDisplayBackbone(checked);
				break;
			case OPTION_DISPLAY_PIPE_PLANK:
				m.setDisplayPipe(checked);
				break;
			case OPTION_CARTOONIZE:
				m.setCartoonize(checked);
				break;
			case OPTION_COLOR_BY_CHAIN:
				m.setColorByChain(checked);
				break;
			case OPTION_COLOR_BY_RESIDUE:
				m.setColorByResidue(checked);
				break;
			default:
				Notification.show("not implemented option: " + option);
		}
	}

	@Override
	public Long getDatasetId() {
		return dataId;
	}

}
