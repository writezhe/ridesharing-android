package org.beiwe.app.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import org.beiwe.app.CrashHandler;

import android.content.Context;
import android.util.Log;

public class AudioFileManager {

   /**Generates new file name variables. The name consists of the time the recording takes place. */
    private static String generateNewEncryptedAudioFileName() {
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		return PersistentData.getPatientID() + "_voiceRecording" + "_" + timecode + ".mp4";
    }
    
    /** Reads in the existing temporary audio file and encrypts it. Generates AES keys as needed.
     * Behavior is to spend as little time writing the file as possible, at the expense of memory.*/
	public static void encryptAudioFile(String unencryptedTempAudioFilePath, Context appContext) {
		if (unencryptedTempAudioFilePath != null) {
			// If the audio file has been written to, encrypt the audio file
			String fileName = generateNewEncryptedAudioFileName();
			byte[] aesKey = EncryptionEngine.newAESKey();
			String encryptedRSA = null;
			String encryptedAudio = null;
			try{
				encryptedRSA = EncryptionEngine.encryptRSA( aesKey ); 
				encryptedAudio = EncryptionEngine.encryptAES( readInAudioFile(unencryptedTempAudioFilePath, appContext), aesKey ); }
			catch (InvalidKeySpecException e) {
				Log.e("AudioFileManager", "encrypted write operation to the audio file without a keyFile.");
				CrashHandler.writeCrashlog(e, appContext); }
	        catch (InvalidKeyException e) {
	        	Log.e("AudioFileManager", "encrypted write operation to the audio file without an aes key? how is that even...");
	        	CrashHandler.writeCrashlog(e, appContext); }
			writePlaintext( encryptedRSA , fileName, appContext );
			writePlaintext( encryptedAudio, fileName, appContext );
		}
	}

	
    /** Writes string data to a the audio file. */
	public static synchronized void writePlaintext(String data, String outputFileName, Context appContext){
		FileOutputStream outStream;
		try {  //We use MODE_APPEND because... we know it works.
			outStream = appContext.openFileOutput(outputFileName, Context.MODE_APPEND);
			outStream.write( ( data ).getBytes() );
			outStream.write( "\n".getBytes() );
			outStream.flush();
			outStream.close(); }
		catch (FileNotFoundException e) {
			Log.e("AudioRecording", "could not find file to write to, " + outputFileName);
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext); }
		catch (IOException e) {
			Log.e("AudioRecording", "error in the write operation: " + e.getMessage() );
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext); }
	}
    
	
	/** Reads a byte array of the current temp audio file's contents.
	 * @return byte array of file contents. */
	public static synchronized byte[] readInAudioFile(String unencryptedTempAudioFilePath, Context appContext) {
		DataInputStream dataInputStream;
		byte[] data = null;
		File file = new File(unencryptedTempAudioFilePath);
		try {  //Read the (data) input stream, into a bytearray.  Catch exceptions.
			dataInputStream = new DataInputStream( new FileInputStream( file ) );
			data = new byte[ (int) file.length() ];
			try{ dataInputStream.readFully(data); }
			catch (IOException e) { Log.i("DataFileManager", "error reading " + unencryptedTempAudioFilePath);
				e.printStackTrace(); }
			dataInputStream.close(); }
		catch (FileNotFoundException e) {
			Log.i("AudioRecording", "file " + unencryptedTempAudioFilePath + " does not exist");
			CrashHandler.writeCrashlog(e, appContext); }
		catch (IOException e) {
			Log.i("AudioRecording", "could not close " + unencryptedTempAudioFilePath);
			CrashHandler.writeCrashlog(e, appContext); }
		return data;
	}
	
}