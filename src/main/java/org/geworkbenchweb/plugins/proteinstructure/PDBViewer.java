package org.geworkbenchweb.plugins.proteinstructure;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.geworkbenchweb.visualizations.MoleculeViewer;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

// TODO
public class PDBViewer extends Panel implements Visualizer {

	private static final long serialVersionUID = -2450376890763433346L;
	final private Long dataId;
	final private String fullPath;

	public PDBViewer(Long dataSetId) {
		super("PDB Viewer"); // this label text is overwritten if the file name is available
		
		this.dataId = dataSetId;
		if (dataId == null) {
			fullPath = null;
			return;
		}

		this.setSizeFull();
		VerticalLayout layout = (VerticalLayout) this.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        
		DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		// Long ownerId = data.getOwner();
		FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		String filename = data.getName();
		fullPath = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ SessionHandler.get().getUsername() + SLASH + DATASETS + SLASH + filename;

		super.setCaption("PDB file name: " + filename);
	}

	private final String DATASETS = "data";
	private final String SLASH = "/";
	
	@Override
	public void attach() {
        MoleculeViewer m = new MoleculeViewer(fullPath);
		addComponent(m);
	}
	
	@Override
	public Long getDatasetId() {
		return dataId;
	}

}
