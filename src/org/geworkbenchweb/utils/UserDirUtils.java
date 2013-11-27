package org.geworkbenchweb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.APSerializable;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.Annotation;
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
	private static final String	ANOT_EXTENSION	=	".annot";
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
				AnnotationParser.setCurrentDataSet(correctType.cast(dataset));
			return correctType.cast(dataset);
		} else {
			throw new Exception("incorrect type " + correctType
					+ " to deserialize " + fileName);
		}
	}

	public static DSDataSet<? extends DSBioObject> deserializeDataSet(
			Long dataId, final Class<? extends DSDataSet<?>> correctType, Long userId)
			throws Exception {

		if (dataId == 0)
			return null; // 0 is used to a special 'initial' case. not the ideal
							// design.

		String fileName = GeworkbenchRoot.getBackendDataDirectory() + SLASH
				+ userId + SLASH + DATASETS + SLASH
				+ dataId + DATA_EXTENSION;
		FileInputStream fin = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fin);
		Object dataset = ois.readObject();
		ois.close();

		if (correctType.isInstance(dataset)) {
			if (correctType == DSMicroarraySet.class)
				AnnotationParser.setCurrentDataSet(correctType.cast(dataset));
			return correctType.cast(dataset);
		} else {
			throw new Exception("incorrect type " + correctType
					+ " to deserialize " + fileName);
		}
	}

	/* call this after deserializeDataSet to get annotation other than gene name and gene id */
	public static void setAnnotationParser(Long dataSetId, DSMicroarraySet maSet){
		Map<String, Object> parameters = new HashMap<String, Object>();	
		parameters.put("datasetid", dataSetId);	
		List<Annotation> annots = FacadeFactory.getFacade().list(
				"Select a from Annotation a, DataSetAnnotation da where a.id=da.annotationid and da.datasetid=:datasetid", parameters);
		if (!annots.isEmpty()){
			APSerializable aps = (APSerializable) ObjectConversion.toObject(UserDirUtils.getAnnotation(annots.get(0).getId()));
			AnnotationParser.setFromSerializable(aps);
		}else {
			AnnotationParser.setCurrentDataSet(maSet);
		}
	}
	
	/**
	 * @param ResultSet Id from the database table
	 * @param Byte data of the resultset
	 * @return
	 */
	// FIXME conversion through byte[] does not make sense
	public static boolean saveResultSet(long resultSetId, byte[] byteObject) {

		ResultSet res 			=	FacadeFactory.getFacade().find(ResultSet.class, resultSetId);
		String resultName 		=	String.valueOf(resultSetId);
		String fileName 		= 	GeworkbenchRoot.getBackendDataDirectory() +
				SLASH + res.getOwner() + SLASH + RESULTSETS + SLASH + resultName + RES_EXTENSION;
		boolean sucess 			=	createFile(fileName, byteObject);
		if(!sucess) return false; 
		return true;
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

	/**
	 * Retrieves byte resultset from file
	 * @param resultset Id
	 * @return byte[]
	 */
	// FIXME conversion through byte[] does not make sense
	public static byte[] getResultSet(long resultSetId) {

		ResultSet res 			=	FacadeFactory.getFacade().find(ResultSet.class, resultSetId);
		String dataName 		=	String.valueOf(resultSetId);
		String fileName 		= 	GeworkbenchRoot.getBackendDataDirectory() +
				SLASH + res.getOwner() + SLASH + RESULTSETS + SLASH + dataName + RES_EXTENSION;
		return getDataFromFile(fileName);
	}

	public static boolean isResultSetAvailable(long resultSetId) {

		ResultSet res 			=	FacadeFactory.getFacade().find(ResultSet.class, resultSetId);
		String dataName 		=	String.valueOf(resultSetId);
		String fileName 		= 	GeworkbenchRoot.getBackendDataDirectory() +
				SLASH + res.getOwner() + SLASH + RESULTSETS + SLASH + dataName + RES_EXTENSION;
		return new File(fileName).exists();
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
	 * @param Annotation Id from the annotation table
	 * @param Byte data of the annotation
	 * @return
	 */
	public static boolean saveAnnotation(long annotId, byte[] byteObject) {
		Annotation annot 		=	FacadeFactory.getFacade().find(Annotation.class, annotId);
		String annotFileName 	=	String.valueOf(annotId);
		String dataDirectory = GeworkbenchRoot.getBackendDataDirectory();
		if(annot.getOwner() != null) {
			String fileName 		= 	dataDirectory  +
					SLASH + annot.getOwner() + SLASH + ANNOTATION + SLASH + annotFileName + ANOT_EXTENSION;
			boolean sucess 			=	createFile(fileName, byteObject);
			if(!sucess) return false; 
			return true;
		}else {
			/* saving public annotation */
			String fileName 		= 	dataDirectory +
					SLASH + annotFileName + ANOT_EXTENSION;
			File f = new File(fileName);
			if(f.exists()) return true; 
			boolean sucess 			=	createFile(fileName, byteObject);
			if(!sucess) return false; 
			return true;
		}
	}

	/**
	 * Retrieves byte Annotation from file
	 * @param annotation Id
	 * @return byte[]
	 */
	public static byte[] getAnnotation(long annotId) {

		Annotation annot 		=	FacadeFactory.getFacade().find(Annotation.class, annotId);
		String dataName 		=	String.valueOf(annotId);
		String fileName 		=	null;
		String dataDirectory = GeworkbenchRoot.getBackendDataDirectory();
		if(annot.getOwner() != null) {
			fileName 			= 	dataDirectory +
										SLASH + annot.getOwner() + SLASH + ANNOTATION + SLASH + dataName + ANOT_EXTENSION;
		} else {
			fileName 			= 	dataDirectory +
										SLASH +  dataName + ANOT_EXTENSION;
		}
		return getDataFromFile(fileName);
	}

	/**
	 * Used to create and write byte date to the file
	 * @param File path to t be created
	 * @param Byte data to be stored in the file
	 * @return
	 */
	private static boolean createFile(String fileName, byte[] byteObject) {
		File file 				= 	new File(fileName );
		try {
			file.createNewFile();
			FileOutputStream f_out		= 	new FileOutputStream(file);
			ObjectOutputStream obj_out 	= 	new ObjectOutputStream (f_out);

			obj_out.writeObject ( byteObject );
			obj_out.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
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

	/**
	 * Gets byte array from the file
	 * @param String (File Name)
	 * @return Byte array
	 */
	// FIXME conversion through byte[] does not make sense
	private static byte[] getDataFromFile(String fileName) {

		byte[] data;
		try{
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fin);
			data = (byte[]) ois.readObject();
			ois.close();
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
		return data;
	}
}
