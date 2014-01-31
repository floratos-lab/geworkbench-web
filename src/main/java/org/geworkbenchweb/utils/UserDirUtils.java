package org.geworkbenchweb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class UserDirUtils {

	private static Log log = LogFactory.getLog(UserDirUtils.class);
			
	private static final String RESULTSETS		=	"results";
	private static final String RES_EXTENSION	=	".res";
	private static final String	SLASH			=	"/";

	public static boolean DeleteUserDirecotry(long userId) {
		//TODO
		return true;
	}

	/* serializeResultSet */
	public static void serializeResultSet(Long resultSetId, Object object)
			throws IOException {

		ResultSet res = FacadeFactory.getFacade().find(ResultSet.class,
				resultSetId);
		String dirName = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ res.getOwner() + SLASH + RESULTSETS;
		File dir = new File(dirName);
		boolean dirCreated = true;
		if(!dir.exists()) {
			dirCreated = dir.mkdirs();
		}
		if(!dirCreated || !dir.isDirectory()) {
			throw new IOException("directory "+dirName+" was not created");
		}
		String fileName = dirName + SLASH + resultSetId + RES_EXTENSION;

		File file = new File(fileName);
		log.debug("creating result set file "+fileName);
		boolean succeed = file.createNewFile();
		if(!succeed) {
			throw new IOException("file "+fileName+" already exists. Quit serializing result set.");
		}
		FileOutputStream f_out = new FileOutputStream(file);
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

		obj_out.writeObject(object);
		obj_out.close();
	}
	
	/**
	 * Deletes the resultset from file system 
	 * @param resultSetId
	 * @return
	 */
	public static boolean deleteResultSet(long resultSetId) {
		
		String dataName 		=	String.valueOf(resultSetId);
		String fileName 		= 	GeworkbenchRoot.getBackendDataDirectory() +
				SLASH + SessionHandler.get().getId() + SLASH + RESULTSETS+ SLASH + dataName + RES_EXTENSION;
		File file = new File(fileName);
		if(file.exists()) {
			return file.delete();
		} else {
			// let's it through if the file or directory does not exist
			log.warn("the file or directory you tried to delete ("+fileName+") does not exsit");
			return true;
		}
	}

	/* deserialize the result set content */
	public static Object deserializeResultSet(Long resultSetId) throws FileNotFoundException, IOException, ClassNotFoundException {
		ResultSet res = FacadeFactory.getFacade().find(ResultSet.class,
				resultSetId);
		String fileName = GeworkbenchRoot.getBackendDataDirectory() + SLASH + res.getOwner()
				+ SLASH + RESULTSETS + SLASH + resultSetId + RES_EXTENSION;
		log.debug("deserializing file "+fileName);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				fileName));
		Object object = ois.readObject();
		ois.close();

		return object;
	}
}
