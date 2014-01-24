package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.geworkbenchweb.GeworkbenchRoot;
import org.vaadin.appfoundation.authentication.SessionHandler;

import com.vaadin.ui.Upload.Receiver;

/** The receiver that provides a file output stream and remember the file. */
class DataFileReceiver implements Receiver {

	private static final long serialVersionUID = 3374720899370397672L;

	private File file = null;

	final private String subdirectoryName;

	DataFileReceiver(String subdirectoryName) {
		this.subdirectoryName = subdirectoryName;
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimetype) {
		FileOutputStream fos = null; // Output stream to write to

		String dir = GeworkbenchRoot.getBackendDataDirectory()
				+ System.getProperty("file.separator")
				+ SessionHandler.get().getUsername()
				+ System.getProperty("file.separator") + subdirectoryName;
		if (!new File(dir).exists())
			new File(dir).mkdirs();
		file = new File(dir, filename);
		try {
			// Open the file for writing.
			fos = new FileOutputStream(file);
		} catch (final java.io.FileNotFoundException e) {
			// Error while opening the file. Not reported here.
			e.printStackTrace();
			file = null;
			return null;
		}
		return fos;
	}

	public File getFile() {
		return file;
	}
}