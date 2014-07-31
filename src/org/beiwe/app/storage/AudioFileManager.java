package org.beiwe.app.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;


//THIS FILE IS LIKELY TO BE DEPRECATED AND REMOVED, UNLESS IT ISN'T.  YAAAY.
// (please delete if you have the urge)


public class AudioFileManager {

//private String readDataFile(String fileName, OutputStream outputStream ) throws IOException{
//		
//		DataInputStream dataInputStream;// BufferedInputStream( new FileInputStream(null));
//		StringBuffer inputStringBuffer = new StringBuffer();
//		
//		try {			
//			String filePath = appContext.getFilesDir() + "/" + fileName;
//			File file = new File(filePath);
//			dataInputStream = new DataInputStream( new FileInputStream(file) );	
//			//we need a byte array of exactly the correct length
//			byte[] fileData = new byte[(int) file.length()];
//
//			try{ dataInputStream.readFully(fileData); }
//			catch (IOException e) {
//				Log.i("Upload", "read error in " + fileName);
//				e.printStackTrace(); }
//			
//			dataInputStream.close();
//		}
//		catch (FileNotFoundException e) {
//			Log.i("Upload", "file " + fileName + " does not exist");
//			e.printStackTrace(); }
//		return inputStringBuffer.toString();
//		
//	}

}
