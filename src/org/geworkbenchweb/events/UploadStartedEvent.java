package org.geworkbenchweb.events;

import java.util.HashMap;

import org.geworkbenchweb.plugins.uploaddata.UploadDataUI;
import org.geworkbenchweb.pojos.DataSet;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class UploadStartedEvent implements Event{
	private DataSet dataSet;
	private HashMap<String, Object> params;
	final private UploadDataUI uploadDataUi;
	
	public UploadStartedEvent(DataSet dataSet, HashMap<String, Object> params, UploadDataUI uploadDataUi){
		this.dataSet		=	dataSet;
		this.params			=	params;
		this.uploadDataUi	=	uploadDataUi;
	}
	
	public DataSet getDataSet(){
		return dataSet;
	}
	
	public HashMap<String, Object> getParameters() {
		return params;
	}
	
	public UploadDataUI getUploadDataUI(){
		return uploadDataUi;
	}
	
	public interface UploadStartedListener extends Listener{
		public void startUpload(UploadStartedEvent e);
	}
}
