package org.geworkbenchweb.plugins.proteinstructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class SimplePDBViewer extends Panel implements Visualizer {

	private static final long serialVersionUID = -2450376890763433346L;
	final private Long dataId;

	public SimplePDBViewer(Long dataSetId) {
		super("Simple PDB Viewer"); // this label text is overwritten if the file name is available

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
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				sb.append(line).append('\n');
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		super.setCaption("PDB file name: " + filename);
		Label content = new Label(sb.toString());
		content.setContentMode(ContentMode.PREFORMATTED);
		setContent(content);
	}

	@Override
	public Long getDatasetId() {
		return dataId;
	}

}
