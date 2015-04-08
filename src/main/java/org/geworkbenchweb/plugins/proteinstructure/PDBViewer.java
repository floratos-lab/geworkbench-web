package org.geworkbenchweb.plugins.proteinstructure;

import java.io.File;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

// TODO
public class PDBViewer extends Panel implements Visualizer {

	private static final long serialVersionUID = -2450376890763433346L;
	final private Long dataId;

	public PDBViewer(Long dataSetId) {
		super("PDB Viewer"); // this label text is overwritten if the file name is available
		
		this.dataId = dataSetId;
		if (dataId == null)
			return;

		this.setSizeFull();
		VerticalLayout layout = (VerticalLayout) this.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        
		final String DATASETS = "data";
		final String SLASH = "/";

		DataSet data = FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		// Long ownerId = data.getOwner();
		FacadeFactory.getFacade().find(DataSet.class, dataSetId);
		String filename = data.getName();
		String fullPath = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ SessionHandler.get().getUsername() + SLASH + DATASETS + SLASH + filename;
		File file = new File(fullPath);

		super.setCaption("PDB file name: " + filename);
		Label content = new Label("This is the place holder for the new PDB viewer to be developed. File: "+file.getAbsolutePath());
		content.setContentMode(Label.CONTENT_PREFORMATTED);
		addComponent(content);
	}

	@Override
	public Long getDatasetId() {
		return dataId;
	}

}
