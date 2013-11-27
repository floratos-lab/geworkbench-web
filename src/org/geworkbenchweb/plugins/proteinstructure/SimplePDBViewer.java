package org.geworkbenchweb.plugins.proteinstructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.utils.UserDirUtils;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class SimplePDBViewer extends Panel implements Visualizer {

	private static final long serialVersionUID = -2450376890763433346L;
	final private Long dataId;

	public SimplePDBViewer(Long dataId) {
		super("Simple PDB Viewer"); // this label text is overwritten if the file name is available
		
		this.dataId = dataId;
		if (dataId == null)
			return;

		this.setSizeFull();
		VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);
        
		Object object = null;
		try {
			object = UserDirUtils.deserializeDataSet(dataId,
					DSProteinStructure.class);
		} catch (FileNotFoundException e) {
			layout.addComponent(new Label("Backing-up file not found - ID " + dataId));
			return;
		} catch (IOException e) {
			layout.addComponent(new Label("Result (ID " + dataId
					+ ") not available due to " + e));
			return;
		} catch (ClassNotFoundException e) {
			layout.addComponent(new Label("Result (ID " + dataId
					+ ") not available due to " + e));
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!(object instanceof DSProteinStructure)) {
			String type = null;
			if (object != null)
				type = object.getClass().getName();
			layout.addComponent(new Label("Result (ID " + dataId
					+ ") has wrong type: " + type));
			return;
		}
		DSProteinStructure data = (DSProteinStructure) object;
		File file = data.getFile();
		super.setCaption("PDB file name: "+file.getName());
		// FIXME - not to fix this class, but to look into
		// DSProteinStructure/CSProteinStructure, which are problematic.
		// Nevertheless, currently CSProteinStructure is the only implementation
		// of DSProteinStructure
		if (data instanceof CSProteinStructure) {
			CSProteinStructure p = (CSProteinStructure) data;
			Label content = new Label(p.getContent());
			content.setContentMode(ContentMode.PREFORMATTED);
			layout.addComponent(content);
		} else {
			layout.addComponent(new Label(
					"Error: the data type is not CSProteinStructure as expected."));
		}
	}

	@Override
	// this is kind of mandatory implementation until the design evolved to some
	// better structure.
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(
				this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return dataId;
	}

}
