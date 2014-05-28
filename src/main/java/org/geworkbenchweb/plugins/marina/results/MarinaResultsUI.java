package org.geworkbenchweb.plugins.marina.results;

import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.MraResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

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
		
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		if(id==null) { // pending node
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		}
		MraResult result = FacadeFactory.getFacade().find(MraResult.class, id);

		String[][] rdata = result.getResult();
		Table mraTable= new Table();

		// Results don't have "MeanClass1" & "MeanClass2" if marina is run using 
		// probe shuffling instead of gene shuffling by setting min_samples > #case/control
		String[] columnNames = { "TFsym", "GeneName", "NumPosGSet", "NumNegGSet", "NumLedgePos", "NumLedgeNeg", "NumLedge",
				"ES", "NES", "absNES", "PV", "OddRatio", "TScore", "MeanClass1", "MeanClass2", "Original MRA/Recovered_MRA"};
		int colNum = columnNames.length;
		boolean complete = rdata.length > 0 && rdata[0].length == colNum;
		for (String col : columnNames){
			if(col.equals("TFsym") || col.equals("GeneName"))
				mraTable.addContainerProperty(col, String.class, null);
			else if(col.startsWith("Num") || col.startsWith("Original"))
				mraTable.addContainerProperty(col, Integer.class, null);
			else if (complete || (!col.equals("MeanClass1") && !col.equals("MeanClass2")))
				mraTable.addContainerProperty(col, Double.class, null);
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
	public Long getDatasetId() {
		return datasetId;
	}
}
