/**
 * 
 */
package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.layout.UMainLayout;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * Customized Upload Layout that is used both for data file and affymatix annotation.
 * file.
 * 
 */
public class FileUploadLayout extends VerticalLayout {

	private static final long serialVersionUID = -9168908956203374092L;

	private static Log log = LogFactory.getLog(FileUploadLayout.class);

	private File dataFile = null;
	
	final Upload upload;

	/** @param category is either 'data' or 'annotation' */
	FileUploadLayout(final UploadDataUI uploadDataUI, final String category) {
		
		/* two component inside progressLayout */
		final ProgressIndicator pIndicator = new ProgressIndicator();
		final Button cancelButton = new Button("Cancel");
		cancelButton.setStyleName("small");
		
		/* three top level components */
		final Label statusLabel = new Label("Please select a file to upload");
		final HorizontalLayout progressLayout = new HorizontalLayout();
		upload = new Upload();
		
		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		progressLayout.addComponent(pIndicator);
		progressLayout.addComponent(cancelButton);
		
		this.addComponent(statusLabel);
		this.addComponent(progressLayout);
		this.addComponent(upload);

		cancelButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 1L;
        	
        	@Override
            public void buttonClick(ClickEvent event) {
                upload.interruptUpload();
            }
        });
		
		final DataFileReceiver fileReceiver = new DataFileReceiver(category);
		upload.setReceiver(fileReceiver);
		upload.setImmediate(true); /* This has very visible effect for Upload. */
		upload.setButtonCaption("Select "+category+" file");
		
		upload.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadStarted(StartedEvent event) {
				// This method gets called immediately after upload is started.
				// A tricky design: disable part of the GUI and revive them when
				// the "Add to workspace" part is done.
				UMainLayout mainLayout = uploadDataUI.getMainLayout();
				mainLayout.lockGuiForUpload();

				upload.setVisible(false);
				statusLabel.setValue("Upload in progress: \""
						+ event.getFilename() + "\"");
				progressLayout.setVisible(true);
				pIndicator.setValue(0f);
				pIndicator.setPollingInterval(500);

				dataFile = null;
			}
		});

		upload.addListener(new Upload.ProgressListener() {
			private static final long serialVersionUID = 1L;

			public void updateProgress(final long readBytes,
					final long contentLength) {
				pIndicator
						.setValue(new Float(readBytes / (float) contentLength));
			}
		});

		upload.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadSucceeded(SucceededEvent event) {
				// This method gets called when the upload finished successfully
				statusLabel.setValue(" The " + category + " file \""
						+ event.getFilename() + "\" is selected");
			}
		});

		upload.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFailed(FailedEvent event) {
				// This method gets called when the upload failed
				float v = 100 * (Float) (pIndicator.getValue());
				statusLabel.setValue("Upload interrupted at " + Math.round(v)
						+ "%");

				if (dataFile != null) {
					if (!dataFile.delete())
						log.warn("problem in deleting " + dataFile);
					dataFile = null;
				}
			}
		});

		upload.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(FinishedEvent event) {
				// This method gets called always when the upload finished,
				// either succeeding or failing
				progressLayout.setVisible(false);
				upload.setVisible(true);
				upload.setCaption("Select different file");

				// FIXME this probably should be in 'succeeded', not in
				// 'finished'!
				dataFile = fileReceiver.getFile();
			}
		});
	}

	public File getDataFile() {
		return dataFile;
	}

	/* this complicates the code */
	public void interruptUpload() {
		upload.interruptUpload();
	}
}
