package org.geworkbenchweb.plugins.marina.results;

import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class MarinaResultsUI  extends VerticalLayout {

	private static final long serialVersionUID = 1781213075723503210L;

	public MarinaResultsUI(Long dataSetId) {
		
		CSMasterRegulatorTableResultSet resultset = (CSMasterRegulatorTableResultSet) 
				ObjectConversion.toObject(UserDirUtils.getResultSet(dataSetId));

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
}
