package org.geworkbenchweb.plugins.marina.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMasterRegulatorTableResultSet;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class MarinaResultsUI  extends VerticalLayout {

	private static final long serialVersionUID = 1781213075723503210L;

	public MarinaResultsUI(Long dataSetId) {
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id", parameters);
		CSMasterRegulatorTableResultSet resultset = (CSMasterRegulatorTableResultSet)ObjectConversion.toObject(data.get(0).getData());

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
		mraTable.setCaption("MARINa results");
		for (int i = 0; i < rdata.length; i++){
			mraTable.addItem(rdata[i], null);
		}
		addComponent(mraTable);
	}
}
