package org.geworkbenchweb.layout;

import java.util.HashMap;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.events.UploadStartedEvent;
import org.geworkbenchweb.events.UploadStartedEvent.UploadStartedListener;
import org.geworkbenchweb.plugins.uploaddata.UploadDataUI;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.vaadin.artur.icepush.ICEPush;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class UploadDataListener implements UploadStartedListener{
	private final UMainLayout uMainLayout;
	private final ICEPush pusher;

	UploadDataListener(UMainLayout uMainLayout, ICEPush pusher) {
		this.uMainLayout = uMainLayout;
		this.pusher = pusher;
	}

	public void startUpload(final UploadStartedEvent e) {
		// start upload in background thread
		Thread uploadThread = new Thread() {
			@Override
			public void run() {
				final DataSet dataSet = e.getDataSet();
				HashMap<String, Object> params = e.getParameters();
				UploadDataUI ui = e.getUploadDataUI();
				
				boolean success = ui.startUpload(dataSet, params);

				if(!success)
				{
					//FIXME: delete cascade dataset from db
					FacadeFactory.getFacade().delete(dataSet);
					uMainLayout.removeItem(dataSet.getId());
					return;	
				}

				synchronized(uMainLayout.getApplication()) {
					MessageBox mb = new MessageBox(uMainLayout.getWindow(), 
							"Upload Completed", 
							MessageBox.Icon.INFO, 
							"Data upload is now completed. ",  
							new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
					mb.show(new MessageBox.EventListener() {
						private static final long serialVersionUID = 1L;
						@Override
						public void buttonClicked(ButtonType buttonType) {    	
							if(buttonType == ButtonType.OK) {
								NodeAddEvent resultEvent = new NodeAddEvent(dataSet);
								GeworkbenchRoot.getBlackboard().fire(resultEvent);
							}
						}
					});
				}
				pusher.push();
			}
		};
		uploadThread.start();
	}

}
