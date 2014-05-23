package org.geworkbenchweb.plugins.aracne;

import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.pojos.ConfigResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ConfigResultUI extends VerticalLayout implements Visualizer {
	private static final long serialVersionUID = -4230842720422872428L;
	private Long datasetId;
	
	public ConfigResultUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;
		
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, dataSetId);
		Long id = resultSet.getDataId();
		if(id==null) { // pending node
			addComponent(new Label("Pending computation - ID "+ dataSetId));
			return;
		}
		ConfigResult configResult = FacadeFactory.getFacade().find(ConfigResult.class, id);
		Float[] kernel = configResult.getKernel();
		Float[] threshold = configResult.getThreshold();
		
		setSpacing(true);
		
		if(threshold != null && threshold.length == 3){
			Table thresholdTable = new Table("config_threshold.txt");
			thresholdTable.addContainerProperty("alpha", Float.class, null);
			thresholdTable.addContainerProperty("beta",  Float.class, null);
			thresholdTable.addContainerProperty("gamma", Float.class, null);
			thresholdTable.addItem(threshold, 0);
			thresholdTable.setPageLength(thresholdTable.size());
			addComponent(thresholdTable);
		}
				
		if(kernel != null && kernel.length == 2){
			Table kernelTable = new Table("config_kernel.txt");
			kernelTable.addContainerProperty("alpha", Float.class, null);
			kernelTable.addContainerProperty("beta",  Float.class, null);
			kernelTable.addItem(kernel, 0);
			kernelTable.setPageLength(kernelTable.size());
			addComponent(kernelTable);
		}
	}
	
	@Override
	public Long getDatasetId() {
		return datasetId;
	}
}
