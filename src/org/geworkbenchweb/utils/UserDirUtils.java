package org.geworkbenchweb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.geworkbenchweb.GeworkbenchRoot;
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
	public static boolean saveDataSet(long dataId, byte[] byteObject) {

		String dataName 		=	String.valueOf(dataId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + SessionHandler.get().getId() + SLASH + DATASETS + SLASH + dataName + DATA_EXTENSION;
		boolean sucess 			=	createFile(fileName, byteObject);
		if(!sucess) return false; 
		return true;
	}

	/**
	 * Retrieves byte data from file
	 * @param Dataset Id
	 * @return byte[]
	 */
	public static byte[] getDataSet(long dataId) {

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

	/**
	 * Retrieves byte resultset from file
	 * @param resultset Id
	 * @return byte[]
	 */
	public static byte[] getResultSet(long resultSetId) {

		ResultSet res 			=	FacadeFactory.getFacade().find(ResultSet.class, resultSetId);
		String dataName 		=	String.valueOf(resultSetId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + res.getOwner() + SLASH + RESULTSETS + SLASH + dataName + RES_EXTENSION;
		return getDataFromFile(fileName);
	}
	
	
	/**
	 * @param Annotation Id from the annotation table
	 * @param Byte data of the annotation
	 * @return
	 */
	public static boolean saveAnnotation(long annotId, byte[] byteObject) {
		
		String annotFileName 	=	String.valueOf(annotId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + SessionHandler.get().getId() + SLASH + ANNOTATION + SLASH + annotFileName + ANOT_EXTENSION;
		boolean sucess 			=	createFile(fileName, byteObject);
		if(!sucess) return false; 
		return true;
	}

	/**
	 * Retrieves byte Annotation from file
	 * @param annotation Id
	 * @return byte[]
	 */
	public static byte[] getAnnotation(long annotId) {

		String dataName 		=	String.valueOf(annotId);
		String fileName 		= 	System.getProperty("user.home") + SLASH +
				GeworkbenchRoot.getAppProperties().getProperty(DATA_DIRECTORY) +
				SLASH + SessionHandler.get().getId() + SLASH + ANNOTATION + SLASH + dataName + ANOT_EXTENSION;
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
	 * Gets byte array from the file
	 * @param String (File Name)
	 * @return Byte array
	 */
	private static byte[] getDataFromFile(String fileName) {

		byte[] data;
		try{
			FileInputStream fin = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fin);
			data = (byte[]) ois.readObject();
			ois.close();
		}catch(Exception ex){
			//ex.printStackTrace();
			return null;
		} 
		return data;
	}
}
