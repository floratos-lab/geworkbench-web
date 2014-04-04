package org.geworkbenchweb.plugins.uploaddata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.DataSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.Upload.Receiver;

/** The receiver that provides a file output stream and remember the file. */
class DataFileReceiver implements Receiver {

	private static final long serialVersionUID = 3374720899370397672L;
	private static Log log = LogFactory.getLog(DataFileReceiver.class);

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
		filename = getUniqueFileName(filename);
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

	private String getUniqueFileName(String filename){
		int suffixId = filename.lastIndexOf('.');
		String mainpart = filename, suffix = "";
		if(suffixId >= 0){
			mainpart = filename.substring(0, suffixId);
			suffix = filename.substring(suffixId);
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("owner", SessionHandler.get().getId());
		params.put("name", mainpart+"%"+suffix);
		List<DataSet> datasets = FacadeFactory.getFacade().list(
				"select d from DataSet as d where d.owner=:owner and d.name like :name", params);
		if(datasets.size()==0) return filename;

		int maxseq = 0;
		for(DataSet ds : datasets){
			int start = suffixId + 1;//(
			int end = ds.getName().lastIndexOf(suffix)-1;//).
			if(start<end){
				String number = ds.getName().substring(start, end);
				try{
					maxseq = Math.max(maxseq, Integer.parseInt(number));
				}catch(NumberFormatException e){
					log.warn("dataset name number parsing error: "+number);
				}
			}
		}
		filename = mainpart+"("+(++maxseq)+")"+suffix;
		return filename;
	}
	
	public File getFile() {
		return file;
	}
}