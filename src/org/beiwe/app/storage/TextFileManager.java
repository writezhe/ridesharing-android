package org.beiwe.app.storage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.GPSListener;



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


public class TextFileManager {
	
//TODO: we need to escape all separator values that get dumped into strings 
//TODO: sanitize inputs for the survey info, coordinate with kevin on that, may be easier to implement serverside.
//TODO: implement public static header variables for all the classes that will need them, import here
//TODO: we probably want a static array pointing to all the static objects to make a static X_for_everything functions easier?
	//Static instances of the individual FileManager objects.
	private static TextFileManager debugLogFile = null;
	private static TextFileManager GPSFile = null;
	private static TextFileManager accelFile = null;
	private static TextFileManager powerStateLog = null;
	private static TextFileManager callLog = null;
	private static TextFileManager textsLog = null;
	private static TextFileManager surveyResponse = null;
	
	private static TextFileManager currentQuestionsFile = null;
	
	//"global" static variables
	private static Context appContext;
	
	//public static getters
	public static synchronized TextFileManager getDebugLogFile(){
		if (debugLogFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return debugLogFile; }
	public static synchronized TextFileManager getAccelFile(){
		if (accelFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return accelFile; }
	public static synchronized TextFileManager getGPSFile(){
		if (GPSFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return GPSFile; }
	public static synchronized TextFileManager getPowerStateFile(){
		if (powerStateLog == null) throw new NullPointerException("you need to call startFileManager."); 
		return powerStateLog; }
	public static synchronized TextFileManager getCallLogFile(){
		if (callLog == null) throw new NullPointerException("you need to call startFileManager."); 
		return callLog; }
	public static synchronized TextFileManager getTextsLogFile(){
		if (textsLog == null) throw new NullPointerException("you need to call startFileManager."); 
		return textsLog; }
	public static synchronized TextFileManager getSurveyResponseFile(){
		if (surveyResponse == null) throw new NullPointerException("you need to call startFileManager."); 
		return surveyResponse; }
	
	public static synchronized TextFileManager currentQuestionsFile(){
		if (currentQuestionsFile == null) throw new NullPointerException("you need to call startFileManager."); 
		return currentQuestionsFile; }
	
	
	//and (finally) the non-static object instance variables
	private String name = null;
	private String fileName = null;
	private String header = null;
	
	
	/*###############################################################################
	######################## CONSTRUCTOR STUFF ######################################
	###############################################################################*/
	
	private TextFileManager(Context appContext, String name, String header){
		TextFileManager.appContext = appContext;
		this.name = name;
		this.header = header;
		//TODO: check if on file creation it wants mode private?
	}
	
	public static synchronized void startFileManager(Context appContext){
		//if any of the static FileManagers are not null, fail completely.
		if (debugLogFile != null || GPSFile != null || accelFile != null ){
			throw new NullPointerException("You may only start the FileManager once."); }
		
		debugLogFile = new TextFileManager(appContext, "logFile", "THIS LINE IS A LOG FILE HEADER\n");
		debugLogFile.newDebugLogFile();
		
		
//		 * filename
//		 * type of data: "voice recording" or "accereometer
//		 * start timestamp, stop timestamp
//		 * user id #
		
		GPSFile = new TextFileManager(appContext, "gpsFile", GPSListener.header );
		GPSFile.newFile();
		accelFile = new TextFileManager(appContext, "accelFile", AccelerometerListener.header);
		accelFile.newFile();
		surveyResponse = new TextFileManager(appContext, "surveyData", "generic header 1 2 3\n");
		surveyResponse.newFile();
		textsLog = new TextFileManager(appContext, "textsLog", "generic header 1 2 3\n");
		textsLog.newFile();
		powerStateLog = new TextFileManager(appContext, "screenState", "generic header 1 2 3\n");
		powerStateLog.newFile();
		callLog = new TextFileManager(appContext, "callLog", "generic header 1 2 3\n");
		callLog.newFile();
		
		//note the empty header
		currentQuestionsFile = new TextFileManager(appContext, "currentQuestionsFile.json", "");
		currentQuestionsFile.newFile();
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

	/**
	 * Returns a string of the file passed in
	 * @return
	 * @throws IOException
	 */
	public synchronized String read() {

		BufferedInputStream bufferedInputStream;// BufferedInputStream( new FileInputStream inputStream;)
		StringBuffer inputStringBuffer = new StringBuffer();
		int data;
		try {
			bufferedInputStream = new BufferedInputStream( appContext.openFileInput(fileName) );
			try{ while( (data = bufferedInputStream.read()) != -1)
				inputStringBuffer.append((char)data); }
			catch (IOException e) {
				Log.i("Upload", "read error in " + this.fileName);
				e.printStackTrace(); }
		}
		catch (FileNotFoundException e) {
			Log.i("Upload", "file " + this.fileName + " does not exist");
			e.printStackTrace(); }
		return inputStringBuffer.toString();
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
		
		makeNewFilesForEverything();
		
		for (String file_name : files) {
			try { appContext.deleteFile(file_name); }
			catch (Exception e) { throw new NullPointerException("UHOH, PROBLEM BATCH FILE DELETION."); }
		}
	}
	
	public synchronized void newDebugLogFile(){
		String timecode = ((Long)System.currentTimeMillis()).toString();
		this.fileName = this.name;
		this.write( timecode + " -:- " + header );
	}
	
	public static synchronized void makeNewFilesForEverything(){
//		debugLogFile.newDebugLogFile();
		GPSFile.newFile();
		accelFile.newFile();
		powerStateLog.newFile();
		callLog.newFile();
		textsLog.newFile();
		surveyResponse.newFile();
	}
	
	
	// TODO: I (Josh) believe this function getAllFiles() is NOT thread-safe
	// with deleteEverything()- Eli, we need to work this out
	private static synchronized String[] getAllFiles() {
		return appContext.getFilesDir().list();
	}
	
	/** Returns a list of file names, all files in that list are retired and will not be written to again.
	 * @return */
	public static synchronized String[] getAllFilesSafely() {
		String[] file_list = getAllFiles();
		makeNewFilesForEverything();
		return file_list;
	}
}
