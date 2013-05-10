package org.geworkbenchweb.plugins.marina.results;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.utils.UserDirUtils;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class MarinaResultsUI  extends VerticalLayout implements Visualizer {

	private static final long serialVersionUID = 1781213075723503210L;

	final private Long datasetId;
	
	public MarinaResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
		Object object = null;
		try {
			object = UserDirUtils.deserializeResultSet(dataSetId);
		} catch (FileNotFoundException e) { 
			// TODO pending node should be designed and implemented explicitly as so, eventually
			// let's make a naive assumption for now that "file not found" means pending computation
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		} catch (IOException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		} catch (ClassNotFoundException e) {
			addComponent(new Label("Result (ID "+ dataSetId+ ") not available due to "+e));
			return;
		}
		if(! (object instanceof CSMasterRegulatorTableResultSet)) {
			String type = null;
			if(object!=null) type = object.getClass().getName();
			addComponent(new Label("Result (ID "+ dataSetId+ ") has wrong type: "+type));
			return;
		}
		CSMasterRegulatorTableResultSet resultset = (CSMasterRegulatorTableResultSet) object;

		Object[][] rdata = resultset.getData();
		Table mraTable= new Table();

		// Results don't have "MeanClass1" & "MeanClass2" if marina is run using 
		// probe shuffling instead of gene shuffling by setting min_samples > #case/control
		String[] columnNames = { "TFsym", "GeneName", "NumPosGSet", "NumNegGSet", "NumLedgePos", "NumLedgeNeg", "NumLedge",
				"ES", "NES", "absNES", "PV", "OddRatio", "TScore", "MeanClass1", "MeanClass2", "Original MRA/Recovered_MRA"};
		int colNum = columnNames.length;
		boolean complete = rdata.length > 0 && rdata[0].length == colNum;
		for (String col : columnNames){
			if (complete || (!col.equals("MeanClass1") && !col.equals("MeanClass2")))
				mraTable.addContainerProperty(col, String.class, null);
		}
		mraTable.setSizeFull();
		mraTable.setImmediate(true);
		for (int i = 0; i < rdata.length; i++){
			mraTable.addItem(rdata[i], null);
		}
		mraTable.setStyleName(Reindeer.TABLE_STRONG);
		mraTable.setColumnCollapsingAllowed(true);
		setSizeFull();
		addComponent(mraTable);
	}

	@Override
	public PluginEntry getPluginEntry() {
		return new PluginEntry("MARINa result viewer", "Show result of MARINa analysis");
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
