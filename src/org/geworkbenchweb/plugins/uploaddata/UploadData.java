package org.geworkbenchweb.plugins.uploaddata;

import org.geworkbenchweb.layout.VisualPlugin;

public class UploadData extends VisualPlugin {

	private Long dataSetId;
	
	public UploadData(Long dataSetId) {
		this.dataSetId = dataSetId;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Upload Data";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Please use this interface to upload data";
	}

	@Override
	public boolean checkForVisualizer() {
		return false;
	}

	@Override
	public Long getDataSetId() {
		// TODO Auto-generated method stub
		return dataSetId;
	}

}
