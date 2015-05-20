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
			while(line!=null) {
				sb.append(line+"\n");
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
		final MenuBar toolBar =  new MenuBar();
		toolBar.setStyleName("transparent");
		
		String[] representation = {"Ball and Stick", "van der Waals Spheres", "Stick", "Wireframe", "Line"};
		final MenuItem representationType = toolBar.addItem("3D Representation", null);
		representationType.setStyleName("plugin");

       	Command reloadCommand = new Command() {

			private static final long serialVersionUID = -6824514348952478474L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				PDBViewer.this.removeAllComponents();
				PDBViewer.this.addComponent(toolBar);
		        MoleculeViewer m = new MoleculeViewer(pdbContent, selectedItem.getText());
		        PDBViewer.this.addComponent(m);
			}
       		
       	};

		for(String r : representation) {
			representationType.addItem(r, reloadCommand);
		}
		this.addComponent(toolBar);
		
       	/* other menu items to be added */
       	/*
       	Command otherCommand = new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO
			}
       	};
       	toolBar.addItem("Other", otherCommand); // ignore return value
       	*/

        MoleculeViewer m = new MoleculeViewer(pdbContent);
        this.addComponent(m);
	}
	
	@Override
	public Long getDatasetId() {
		return dataId;
	}

}
