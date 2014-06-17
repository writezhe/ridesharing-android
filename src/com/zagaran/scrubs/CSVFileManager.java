package com.zagaran.scrubs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

//features
// 

// the file manager needs to be a singleton
// static function setup file manager... instantiates it, checks that it has never been run before
// non static function that grabs the file manager object
// the constructor needs to be PRivate

/* copy pasta:
debugLogFile
gpsFile
accelFile
screenState
callLog
textsLog
surveyData
 */


public class CSVFileManager {
	
//TODO: we need to escape all separator values that get dumped into strings 
//TODO: sanitize inputs for the survey info, coordinate with kevin on that, may be easier to implement serverside.
//TODO: implement public static header variables for all the classes that will need them, import here
//TODO: we probably want a static array pointing to all the static objects to make a static X_for_everything functions easier?
	//Static instances of the individual FileManager objects.
	private static CSVFileManager debugLogFile = null;
	private static CSVFileManager GPSFile = null;
	private static CSVFileManager accelFile = null;
	private static CSVFileManager powerStateLog = null;
	private static CSVFileManager callLog = null;
	private static CSVFileManager textsLog = null;
	private static CSVFileManager surveyResponse = null;
	
	//"global" static variables
	private static Context appContext;
	
	//public static getters
	public static synchronized CSVFileManager getDebugLogFile(){
		if (debugLogFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return debugLogFile; }
	public static synchronized CSVFileManager getAccelFile(){
		if (accelFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return accelFile; }
	public static synchronized CSVFileManager getGPSFile(){
		if (GPSFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return GPSFile; }
	public static synchronized CSVFileManager getPowerStateFile(){
		if (powerStateLog == null) throw new NullPointerException("you need to call startFileManager."); 
		return powerStateLog; }
	public static synchronized CSVFileManager getCallLogFile(){
		if (callLog == null) throw new NullPointerException("you need to call startFileManager."); 
		return callLog; }
	public static synchronized CSVFileManager getTextsLogFile(){
		if (textsLog == null) throw new NullPointerException("you need to call startFileManager."); 
		return textsLog; }
	public static synchronized CSVFileManager surveyResponseFile(){
		if (surveyResponse == null) throw new NullPointerException("you need to call startFileManager."); 
		return surveyResponse; }
	
	//and (finally) the non-static object instance variables
	private String name = null;
	private String fileName = null;
	private String header = null;
	
	
	/*###############################################################################
	######################## CONSTRUCTOR STUFF ######################################
	###############################################################################*/
	
	private CSVFileManager(Context appContext, String name, String header){
		CSVFileManager.appContext = appContext;
		this.name = name;
		this.header = header;
		//TODO: check if on file creation it wants mode private?
	}
	
	public static synchronized void startFileManager(Context appContext){
		//if any of the static FileManagers are not null, fail completely.
		if (debugLogFile != null || GPSFile != null || accelFile != null ){
			throw new NullPointerException("You may only start the FileManager once."); }
		
		debugLogFile = new CSVFileManager(appContext, "logFile", "THIS LINE IS A LOG FILE HEADER\n");
		debugLogFile.newLogFile();
		
		GPSFile = new CSVFileManager(appContext, "gpsFile", GPSListener.header );
		GPSFile.newFile();
		accelFile = new CSVFileManager(appContext, "accelFile", AccelerometerListener.header);
		accelFile.newFile();
		surveyResponse = new CSVFileManager(appContext, "surveyData", "generic header 1 2 3\n");
		surveyResponse.newFile();
		textsLog = new CSVFileManager(appContext, "textsLog", "generic header 1 2 3\n");
		textsLog.newFile();
		powerStateLog = new CSVFileManager(appContext, "screenState", "generic header 1 2 3\n");
		powerStateLog.newFile();
		callLog = new CSVFileManager(appContext, "callLog", "generic header 1 2 3\n");
		callLog.newFile();
	}
	
	/*###############################################################################
	######################## OBJECT INSTANCE FUNCTIONS ##############################
	###############################################################################*/
	
	public synchronized void newFile(){
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		this.fileName = this.name + "-" + timecode + ".txt";
		this.write(header);
	}
	
	public synchronized void write(String data){
		//write the output, we always want mode append
		FileOutputStream outStream;
		try {
			outStream = appContext.openFileOutput(fileName, Context.MODE_APPEND);
			outStream.write(data.getBytes());
			outStream.close(); }
		catch (Exception e) {
			//should print out error as
			// [label]       [output]
			// FileManager   Write error: logFile.txt
			Log.i("FileManager", "Write error: " + this.fileName);
			e.printStackTrace(); }
	}
	
	public synchronized String read() {
		FileInputStream inputStream;
		StringBuffer inputBuffer = new StringBuffer();
		int data;
		try {
			inputStream = appContext.openFileInput(this.fileName);
			try{ while( (data = inputStream.read()) != -1)
				inputBuffer.append((char)data); }
			catch (IOException e) {
				Log.i("FileManager", "read error in " + this.fileName);
				e.printStackTrace(); } }
		catch (FileNotFoundException e) {
			Log.i("FileManager", "file " + this.fileName + " does not exist");
			e.printStackTrace(); }
		return new String(inputBuffer);
	}
	
/*###############################################################################
######################## DEBUG STUFF ############################################
###############################################################################*/

// one of the delete functions will be upgraded to non-debug
	public synchronized void deleteMeSafely(){
		/**create new instance of file, then delete the old file.*/
		String old_file_name = this.fileName;
		this.newFile();
		try { appContext.deleteFile(old_file_name); }
		catch (Exception e) { throw new NullPointerException("UHOH, PROBLEM DELETING A FILE."); }
	}
	
	public static synchronized void deleteEverything(){
		/**Get complete list of all files, make new files, then delete all from old files list.*/
		String[] files = appContext.getFilesDir().list();
		
		newFilesForEverything();
		
		for (String file_name : files) {
			try { appContext.deleteFile(file_name); }
			catch (Exception e) { throw new NullPointerException("UHOH, PROBLEM BATCH FILE DELETION."); }
		}
	}
	
	
	public synchronized void newLogFile(){
		String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
		this.fileName = this.name;
		this.write(header + " -:- " + timecode);
	}
	
	
	public static synchronized void newFilesForEverything(){
		debugLogFile.newLogFile();
		
		GPSFile.newFile();
		accelFile.newFile();
		powerStateLog.newFile();
		callLog.newFile();
		textsLog.newFile();
		surveyResponse.newFile();
	}
}
