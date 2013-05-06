package org.geworkbenchweb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.pojos.Annotation;
import org.geworkbenchweb.pojos.ResultSet;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class UserDirUtils {

	private static final String DATA_DIRECTORY 	= 	"data.directory";
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
	 * @return  
	 */
	public static boolean CreateUserDirectory(long userId) {

		String dataDir 		=   System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) + SLASH;
		String userDirName 	= 	String.valueOf(userId);

		boolean success = (new File(dataDir + SLASH + userDirName)).mkdir();

		if(success) {
			File dataSetDir 	=	new File(dataDir + SLASH + userDirName + SLASH + DATASETS);
			File resultSetDir	=	new File(dataDir + SLASH + userDirName + SLASH + RESULTSETS);
			File annotationDir 	=	new File(dataDir + SLASH + userDirName + SLASH + ANNOTATION);

			/* creating sub directories in user directory*/
			boolean a = dataSetDir.mkdir();
			boolean b = resultSetDir.mkdir();
			boolean c = annotationDir.mkdir();
			if(!(a && b && c)) {
				return false;
			}
		}else {
			/* couldn't create user diretory */
			return false;
		}
		return success;
	}

	public static boolean DeleteUserDirecotry(long userId) {
		//TODO
		return true;
	}

	/**
	 * @param Data set Id from the database table
	 * @param Byte data of the dataset
	 * @return
	 */
	public static boolean saveDataSet(long dataId, byte[] byteObject, Long userId) {

		String dataName 		=	String.valueOf(dataId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + userId + SLASH + DATASETS + SLASH + dataName + DATA_EXTENSION;
		boolean sucess 			=	createFile(fileName, byteObject);
		if(!sucess) return false; 
		return true;
	}
	
	/**
	 * Deletes the dataset from the file system
	 * @param dataId
	 * @return
	 */
	public static boolean deleteDataSet(long dataId) {
		
		String dataName 		=	String.valueOf(dataId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + SessionHandler.get().getId() + SLASH + DATASETS + SLASH + dataName + DATA_EXTENSION;
		boolean success 		=	deleteFile(fileName);
		if(!success) return false;
		return true;
	}

	// get either the input dataset or the result set
	// (1) separation of dataset and result seems unnecessary and complicates the design
	// (2) going through byte array is not necessary
	public static Object getData(long datasetID) {
		Long userId = SessionHandler.get().getId();
		String fileName = System.getProperty("user.home")
				+ SLASH
				+ GeworkbenchRoot.getAppProperties()
						.getProperty(DATA_DIRECTORY) + SLASH + userId + SLASH
				+ DATASETS + SLASH + datasetID + DATA_EXTENSION;
		String fileName2 = System.getProperty("user.home")
				+ SLASH
				+ GeworkbenchRoot.getAppProperties()
						.getProperty(DATA_DIRECTORY) + SLASH + userId + SLASH
				+ RESULTSETS + SLASH + datasetID + RES_EXTENSION;

		ObjectInputStream ois = null, ois2 = null;
		Object obj = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(fileName));
			obj = ois.readObject();
		} catch (FileNotFoundException e) {
			try {
				ois2 = new ObjectInputStream(new FileInputStream(fileName2));
				obj = ois2.readObject();
			} catch (FileNotFoundException e2) {
				// no-op: this may be the case when the result node is just created
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null)
					ois.close();
				if (ois2 != null)
					ois2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}
	
	/**
	 * Retrieves byte data from file
	 * @param Dataset Id
	 * @return byte[]
	 */
	public static byte[] getDataSet(long dataId) {

		if(dataId==0) return null; // 0 is used to a special 'initial' case. not the ideal design.
		
		String dataName 		=	String.valueOf(dataId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + SessionHandler.get().getId() + SLASH + DATASETS + SLASH + dataName + DATA_EXTENSION;
		return getDataFromFile(fileName);
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
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
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
		String fileName = System.getProperty("user.home")
				+ SLASH
				+ GeworkbenchRoot.getAppProperties()
						.getProperty(DATA_DIRECTORY) + SLASH + res.getOwner()
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
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
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
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + res.getOwner() + SLASH + RESULTSETS + SLASH + dataName + RES_EXTENSION;
		return getDataFromFile(fileName);
	}

	/* deserialize the result set content */
	public static Object deserializeResultSet(Long resultSetId) throws FileNotFoundException, IOException, ClassNotFoundException {
		ResultSet res = FacadeFactory.getFacade().find(ResultSet.class,
				resultSetId);
		String fileName = System.getProperty("user.home")
				+ SLASH
				+ GeworkbenchRoot.getAppProperties()
						.getProperty(DATA_DIRECTORY) + SLASH + res.getOwner()
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
		if(annot.getOwner() != null) {
			String fileName 		= 	System.getProperty("user.home") + SLASH +
					GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
					SLASH + SessionHandler.get().getId() + SLASH + ANNOTATION + SLASH + annotFileName + ANOT_EXTENSION;
			boolean sucess 			=	createFile(fileName, byteObject);
			if(!sucess) return false; 
			return true;
		}else {
			/* saving public annotation */
			String fileName 		= 	System.getProperty("user.home") + SLASH +
					GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
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
		if(annot.getOwner() != null) {
			fileName 			= 	System.getProperty("user.home") + SLASH +
										GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
										SLASH + SessionHandler.get().getId() + SLASH + ANNOTATION + SLASH + dataName + ANOT_EXTENSION;
		} else {
			fileName 			= 	System.getProperty("user.home") + SLASH +
										GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
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
			file.delete();
			return true;
		} else {
			return false;
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
