package org.beiwe.app.storage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
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
currentQuestionsFile
 */

//* filename
//* type of data: "voice recording" or "accereometer
//* start timestamp, stop timestamp
//* user id #



public class TextFileManager {
	
//TODO: we need to escape all separator values that get dumped into strings 
//TODO: sanitize inputs for the survey info, coordinate with kevin on that, may be easier to implement serverside.
//TODO: implement public static header variables for all the classes that will need them, import here
//TODO: we probably want a static array pointing to all the static objects to make a static X_for_everything functions easier?
	//Static instances of the individual FileManager objects.
	private static TextFileManager GPSFile = null;
	private static TextFileManager accelFile = null;
	private static TextFileManager powerStateLog = null;
	private static TextFileManager callLog = null;
	private static TextFileManager textsLog = null;
	private static TextFileManager surveyResponse = null;
	
	private static TextFileManager debugLogFile = null;
	private static TextFileManager currentQuestionsFile = null;
	private static TextFileManager deviceDataFile = null;
	
	//"global" static variables
	private static Context appContext;
	private static boolean started = false; 
	private static String getter_error = "You tried to access a file before calling TextFileManager.start().";
	
	//public (static getters
	public static TextFileManager getAccelFile(){
		if (accelFile == null) throw new NullPointerException(getter_error); 
		return accelFile; }
	public static TextFileManager getGPSFile(){
		if (GPSFile == null) throw new NullPointerException(getter_error); 
		return GPSFile; }
	public static TextFileManager getPowerStateFile(){
		if (powerStateLog == null) throw new NullPointerException(getter_error); 
		return powerStateLog; }
	public static TextFileManager getCallLogFile(){
		if (callLog == null) throw new NullPointerException(getter_error); 
		return callLog; }
	public static TextFileManager getTextsLogFile(){
		if (textsLog == null) throw new NullPointerException(getter_error); 
		return textsLog; }
	public static TextFileManager getSurveyResponseFile(){
		if (surveyResponse == null) throw new NullPointerException(getter_error); 
		return surveyResponse; }
	//the non-standard files
	public static TextFileManager getCurrentQuestionsFile(){
		if (currentQuestionsFile == null) throw new NullPointerException(getter_error); 
		return currentQuestionsFile; }
	public static TextFileManager getDebugLogFile(){
		if (debugLogFile == null) throw new NullPointerException(getter_error); 
		return debugLogFile; }
	public static TextFileManager getPhoneInfoFile(){
		if (deviceDataFile == null) throw new NullPointerException(getter_error); 
		return deviceDataFile; }
	
	//and (finally) the non-static object instance variables
	private String name = null;
	private String fileName = null;
	private String header = null;
		
	/*###############################################################################
	######################## CONSTRUCTOR STUFF ######################################
	###############################################################################*/
	
	/** This class has a PRIVATE constructor.  The constructor is only ever called 
	 * internally, via the static start() function, to create files for data storage. 
	 * @param appContext A Context provided by the app.
	 * @param name The file's name.
	 * @param header The first line of the file.  Leave empty if you don't want a header, remember to include a new line at the end of the header.
	 * @param overwrite Set this to true if you want to create a new file. */
	private TextFileManager(Context appContext, String name, String header, Boolean overwrite ){
		TextFileManager.appContext = appContext;
		this.name = name;
		this.header = header;
		if (!overwrite) {this.newFile();}
	}
	
	/**Starts the TextFileManager
	 * This must be called before code attempts to access files using getXXXFile().
	 * Initializes all TextFileManager object instances.
	 * Do not run more than once, it will error on you. 
	 * @param appContext a Context, provided by the app. */
	public static synchronized void start(Context appContext){
		//if already started, flip out.
		//TODO: test this?  consider removing the pointer exception and just exiting
		if ( started ){ throw new NullPointerException("You may only start the FileManager once."); }
		else { started = true; }
		
		debugLogFile = new TextFileManager(appContext, "logFile", "THIS LINE IS A LOG FILE HEADER\n", false);
		currentQuestionsFile = new TextFileManager(appContext, "currentQuestionsFile.json", "", false);
		deviceDataFile = new TextFileManager(appContext, "phoneInfo", "", false);
		
		GPSFile = new TextFileManager(appContext, "gpsFile", GPSListener.header, true);
		accelFile = new TextFileManager(appContext, "accelFile", AccelerometerListener.header, true);
		surveyResponse = new TextFileManager(appContext, "surveyData", "generic header 1 2 3\n", true);
		textsLog = new TextFileManager(appContext, "textsLog", "generic header 1 2 3\n", true);
		powerStateLog = new TextFileManager(appContext, "screenState", "generic header 1 2 3\n", true);
		callLog = new TextFileManager(appContext, "callLog", "generic header 1 2 3\n", true);
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

	/** Returns a string of the file contents. 
	 * @return A string of the file contents. */
	public synchronized String read() {
		BufferedInputStream bufferedInputStream;
		StringBuffer inputStringBuffer = new StringBuffer();
		int data;
		try {  //Read through the (buffered) input stream, append to a stringbuffer.  Catch exceptions
			bufferedInputStream = new BufferedInputStream( appContext.openFileInput(fileName) );
			try{ while( (data = bufferedInputStream.read()) != -1)
				inputStringBuffer.append((char)data); }
			catch (IOException e) {
				Log.i("Upload", "read error in " + this.fileName);
				e.printStackTrace(); }
			bufferedInputStream.close(); }
		catch (FileNotFoundException e) {
			Log.i("TextFileManager", "file " + this.fileName + " does not exist");
			e.printStackTrace(); }
		catch (IOException e){
			Log.i("DataFileManager", "could not close " + this.fileName);
			e.printStackTrace(); }
		
		return inputStringBuffer.toString();
	}
	
	/**Returns a byte array of the file contents
	 * @return byte array of fie contents. */
	public synchronized byte[] readDataFile() {
		
		DataInputStream dataInputStream;
		String filePath = appContext.getFilesDir() + "/" + this.fileName;
		byte[] data = null;
		try {  //Read the (data) input stream, into a bytearray.  Catch exceptions.
			File file = new File(filePath);
			dataInputStream = new DataInputStream( new FileInputStream(file) );	
			data = new byte[(int) file.length()];
			try{ dataInputStream.readFully(data); }
			catch (IOException e) { Log.i("DataFileManager", "error reading " + this.fileName);
				e.printStackTrace(); }
			dataInputStream.close(); }
		catch (FileNotFoundException e) {
			Log.i("DataFileManager", "file " + this.fileName + " does not exist");
			e.printStackTrace(); }
		catch (IOException e) {
			Log.i("DataFileManager", "could not close " + this.fileName);
			e.printStackTrace(); }
		
		return data;
	}
	
	public synchronized void deleteSafely(){
		/**create new instance of file, then delete the old file.*/
		String old_file_name = this.fileName;
		this.newFile();
		try { appContext.deleteFile(old_file_name); }
		catch (Exception e) { throw new NullPointerException("UHOH, PROBLEM DELETING A FILE."); }
	}
	
/*###############################################################################
######################## DEBUG STUFF ############################################
###############################################################################*/
	
	/** use the data read function, then converts it to a string. */
	public synchronized String getDataString(){
		return new String( this.readDataFile() ); }
	
	/**Get complete list of all files, make new files, then delete all from old files list.
	 * 
	 */
	public static synchronized void deleteEverything(){
		String[] files = appContext.getFilesDir().list();
		makeNewFilesForEverything();
		
		TextFileManager.getDebugLogFile().newFile();
		TextFileManager.getDebugLogFile().newDebugLogFile();
		TextFileManager.getCurrentQuestionsFile().newFile();
		
		for (String file_name : files) {
			try { appContext.deleteFile(file_name); }
			catch (Exception e) { throw new NullPointerException("UHOH, PROBLEM BATCH FILE DELETION."); } }
	}
	
	public synchronized void newDebugLogFile(){
		String timecode = ((Long)System.currentTimeMillis()).toString();
		this.fileName = this.name;
		this.write( timecode + " -:- " + header );
	}
	
	public static synchronized void makeNewFilesForEverything(){
// do not include the following		
//		debugLogFile.newDebugLogFile();
//		surveyResponse.newFile();

		GPSFile.newFile();
		accelFile.newFile();
		powerStateLog.newFile();
		callLog.newFile();
		textsLog.newFile();
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
