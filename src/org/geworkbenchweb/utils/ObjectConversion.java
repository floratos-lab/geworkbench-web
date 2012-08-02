package org.geworkbenchweb.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ObjectConversion {
	
	@SuppressWarnings("deprecation")
	public static Object toObject(byte[] bytes){ 

		Object object = null; 

		try{ 

			object = new java.io.ObjectInputStream(new 
					java.io.ByteArrayInputStream(bytes)).readObject(); 

		}catch(java.io.IOException ioe){ 

			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					ioe.getMessage()); 

		}catch(java.lang.ClassNotFoundException cnfe){ 

			java.util.logging.Logger.global.log(java.util.logging.Level.SEVERE, 
					cnfe.getMessage()); 

		} 

		return object; 

	}
	
	public static byte[] convertToByte(Object object) {

		byte[] byteData = null;
		ByteArrayOutputStream bos 	= 	new ByteArrayOutputStream();

		try {

			ObjectOutputStream oos 	= 	new ObjectOutputStream(bos); 

			oos.writeObject(object);
			oos.flush(); 
			oos.close(); 
			bos.close();
			byteData 				= 	bos.toByteArray();

		} catch (IOException ex) {

			System.out.println("Exception with in convertToByte");

		}

		return byteData;

	}


}
