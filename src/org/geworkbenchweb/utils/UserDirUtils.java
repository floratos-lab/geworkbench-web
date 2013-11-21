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
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class UserDirUtils {

	private static Log log = LogFactory.getLog(UserDirUtils.class);
			
	private static final String DATASETS 		= 	"data";
	private static final String RESULTSETS		=	"results";
	private static final String ANNOTATION		=	"annotation";
	private static final String DATA_EXTENSION	=	".data";
	private static final String RES_EXTENSION	=	".res";
	private static final String	SLASH			=	"/";

	/**
	 * Creates the user directories in the file system to store the data.
	 * This happens when user register for geWorkbench
	 * @param User ID from the appuser database table 
	 * @return  error message when it fails
	 */
	public static String CreateUserDirectory(Long userId) {
		final boolean allowReusing = false;

		String dataDir 		=    GeworkbenchRoot.getBackendDataDirectory();
		String userDirName 	= 	String.valueOf(userId);

		File userDir = new File(dataDir + SLASH + userDirName);
		if(!allowReusing && userDir.exists())
			return userDir.getAbsolutePath()+ " exists and cannot be re-created for a new user.";
		if(!userDir.mkdir())
			return userDir.getAbsolutePath()+ "cannot be created.";

		/* creating sub directories in user directory*/
		File dataSetDir 	=	new File(dataDir + SLASH + userDirName + SLASH + DATASETS);
		if(!allowReusing && dataSetDir.exists())
			return dataSetDir.getAbsolutePath()+ " exists and cannot created for a new user.";
		if(!dataSetDir.mkdir())
			return dataSetDir.getAbsolutePath()+ "cannot be created.";

		File resultSetDir	=	new File(dataDir + SLASH + userDirName + SLASH + RESULTSETS);
		if(!allowReusing && resultSetDir.exists())
			return resultSetDir.getAbsolutePath()+ " exists and cannot created for a new user.";
		if(!resultSetDir.mkdir())
			return resultSetDir.getAbsolutePath()+ "cannot be created.";
		
		File annotationDir 	=	new File(dataDir + SLASH + userDirName + SLASH + ANNOTATION);
		if(!allowReusing && annotationDir.exists())
			return annotationDir.getAbsolutePath()+ " exists and cannot created for a new user.";
		if(!annotationDir.mkdir())
			return annotationDir.getAbsolutePath()+ "cannot be created.";

		return ""; // empty string for no success (no error)
	}

	public static boolean DeleteUserDirecotry(long userId) {
		//TODO
		return true;
	}

	/* this replaces the original saveDataSet */
	public static void serializeDataSet(Long dataId, DSDataSet<? extends DSBioObject> dataset, Long userId) throws IOException {

		String fileName = GeworkbenchRoot.getBackendDataDirectory() + SLASH + userId + SLASH
				+ DATASETS + SLASH + dataId + DATA_EXTENSION;
		File file = new File(fileName);
		file.createNewFile();
		FileOutputStream f_out = new FileOutputStream(file);
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

		obj_out.writeObject(dataset);
		obj_out.close();
	}
	
	/**
	 * Deletes the dataset from the file system
	 * @param dataId
	 * @return
	 */
	public static boolean deleteDataSet(long dataId) {
		
		String dataName 		=	String.valueOf(dataId);
		String fileName 		= 	GeworkbenchRoot.getBackendDataDirectory() +
				SLASH + SessionHandler.get().getId() + SLASH + DATASETS + SLASH + dataName + DATA_EXTENSION;
		boolean success 		=	deleteFile(fileName);
		if(!success) return false;
		return true;
	}

	/* this replaces the original getDataSet */
	public static DSDataSet<? extends DSBioObject> deserializeDataSet(
			Long dataId, final Class<? extends DSDataSet<?>> correctType)
			throws Exception {

		if (dataId == 0)
			return null; // 0 is used to a special 'initial' case. not the ideal
							// design.

		String fileName = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ SessionHandler.get().getId() + SLASH + DATASETS + SLASH
				+ dataId + DATA_EXTENSION;
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		Object dataset = ois.readObject();
		ois.close();

		if (correctType.isInstance(dataset)) {
			if (correctType == DSMicroarraySet.class)
				AnnotationParser.setCurrentDataSet(correctType.cast(dataset)); // FIXME replace with new mechanism
				// TODO annotation = getAnnotation(dataId);
				// then replace getGeneSymbol etc to annotation.getGeneSymbol(probeSetId);
			return correctType.cast(dataset);
		} else {
			throw new Exception("incorrect type " + correctType
					+ " to deserialize " + fileName);
		}
	}

	/* serializeResultSet */
	public static void serializeResultSet(Long resultSetId, Object object)
			throws IOException {

		ResultSet res = FacadeFactory.getFacade().find(ResultSet.class,
				resultSetId);
		String fileName = GeworkbenchRoot.getBackendDataDirectory() + SLASH + res.getOwner()
				+ SLASH + RESULTSETS + SLASH + resultSetId + RES_EXTENSION;

		File file = new File(fileName);
		file.createNewFile();
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
		boolean success 		=	deleteFile(fileName);
		if(!success) return false;
		return true;
	}

	/* deserialize the result set content */
	public static Object deserializeResultSet(Long resultSetId) throws FileNotFoundException, IOException, ClassNotFoundException {
		ResultSet res = FacadeFactory.getFacade().find(ResultSet.class,
				resultSetId);
		String fileName = GeworkbenchRoot.getBackendDataDirectory() + SLASH + res.getOwner()
				+ SLASH + RESULTSETS + SLASH + resultSetId + RES_EXTENSION;
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				fileName));
		Object object = ois.readObject();
		ois.close();

		return object;
	}

	/**
	 * Deletes the supplied file from the filesystem
	 * @param fileName
	 * @return 
	 */
	private static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if(file.exists()) {
			return file.delete();
		} else {
			// let's it through if the file or directory does not exist
			log.warn("the file or directory you tried to delete ("+fileName+") does not exsit");
			return true;
		}
	}
}
