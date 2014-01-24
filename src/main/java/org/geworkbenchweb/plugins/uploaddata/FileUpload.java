/**
 * 
 */
package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;

/**
 * 
 * Customized Upload that is used both for data file and affymatix annotation
 * file.
 * 
 * @author zji
 * 
 */
public class FileUpload extends com.vaadin.ui.Upload {

	private static final long serialVersionUID = -4034608759344653781L;

	private static Log log = LogFactory.getLog(FileUpload.class);

	private File dataFile = null;

	/** @param category is either 'data' or 'annotation' */
	FileUpload(final UploadDataUI parent, final Label fileUploadStatus,
			final HorizontalLayout pLayout, final ProgressIndicator pIndicator,
			final String category) {

		final DataFileReceiver fileReceiver = new DataFileReceiver(category);
		this.setReceiver(fileReceiver);

		this.setImmediate(true);
		this.setButtonCaption("Select "+category+" file");
		this.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadStarted(StartedEvent event) {
				// This method gets called immediately after upload is started.
				// A tricky design: disable part of the GUI and revive them when
				// the "Add to workspace" part is done.
				parent.enableUMainLayout(false);

				FileUpload.this.setVisible(false);
				fileUploadStatus.setValue("Upload in progress: \""
						+ event.getFilename() + "\"");
				pLayout.setVisible(true);
				pIndicator.setValue(0f);
				pIndicator.setPollingInterval(500);

				dataFile = null;
			}
		});

		this.addListener(new Upload.ProgressListener() {
			private static final long serialVersionUID = 1L;

			public void updateProgress(final long readBytes,
					final long contentLength) {
				pIndicator
						.setValue(new Float(readBytes / (float) contentLength));
			}
		});

		this.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = 1L;

			public void uploadSucceeded(SucceededEvent event) {
				// This method gets called when the upload finished successfully
				fileUploadStatus.setValue(" The "+category+" file \""
						+ event.getFilename() + "\" is selected");
			}
		});

		this.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFailed(FailedEvent event) {
				// This method gets called when the upload failed
				float v = 100 * (Float) (pIndicator.getValue());
				fileUploadStatus.setValue("Upload interrupted at "
						+ Math.round(v) + "%");

				if (dataFile != null) {
					if (!dataFile.delete())
						log.warn("problem in deleting " + dataFile);
					dataFile = null;
				}
			}
		});

		this.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadFinished(FinishedEvent event) {
				// This method gets called always when the upload finished,
				// either succeeding or failing
				pLayout.setVisible(false);
				FileUpload.this.setVisible(true);
				FileUpload.this.setCaption("Select different file");

				// FIXME this probably should be in 'succeeded', not in 'finished'!
				dataFile = fileReceiver.getFile();
			}
		});
	}

	public File getDataFile() {
		return dataFile;
	}

}
