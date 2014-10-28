package org.beiwe.app.storage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.beiwe.app.listeners.AccelerometerListener;
import org.beiwe.app.listeners.BluetoothListener;
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;
import org.beiwe.app.listeners.WifiListener;
import org.beiwe.app.session.LoginSessionManager;
import org.beiwe.app.survey.SurveyAnswersRecorder;
import org.beiwe.app.survey.SurveyTimingsRecorder;

import android.content.Context;
import android.util.Log;

/**
 * The (Text)FileManager.
 * The FileManager is implemented as a Singleton.  More accurately the static object contains several
 * singletons, static instances of FileManager Objects.  Before using the FileManager the app must
 * provide it with a Context and call the Start() function.  Failure to do so causes the app to crash.
 * This Is Intentional.  The Point of the app is to record data.
 * The Reason for this construction is to construct a file write system where there is only ever a
 * single pointer to each file type, and that these files are never overwritten, written to asynchronously,
 * or left accidentally empty.
 * The files handled here are the GPSFile, accelFile, powerStateLog, audioSurveyInfo, callLog, textsLog, surveyTimings,
 * currentDailyQuestions, currentWeeklyQuestions, deviceData, and debugLogFile.
 * On construction you provide a boolean flag ("persistent").  Persistent files do not get overwritten on application start.
 * To access a file use the following construction: TextFileManager.getXXXFile()
 * @author Eli */
public class TextFileManager {
	
	//TODO: Eli/Josh. We need to escape all separator values that get dumped into strings. 

	//Delimiter and newline strings
	public static String DELIMITER = ",";
	
	//Static instances of the individual FileManager objects.
	private static TextFileManager GPSFile = null;
	private static TextFileManager accelFile = null;
	private static TextFileManager powerStateLog = null;
	private static TextFileManager callLog = null;
	private static TextFileManager textsLog = null;
	private static TextFileManager bluetoothLog = null;
	private static TextFileManager wifiLog = null;

	private static TextFileManager surveyTimings = null;
	private static TextFileManager surveyAnswers = null;
	
	private static TextFileManager debugLogFile = null;
	private static TextFileManager currentDailyQuestions = null;
	private static TextFileManager currentWeeklyQuestions = null;
	private static TextFileManager deviceInfo = null;
	
	private static TextFileManager keyFile = null;
	
	//"global" static variables
	private static Context appContext;
	private static boolean started = false; 
	private static String getter_error = "You tried to access a file before calling TextFileManager.start().";
	
	//public static getters.
	// These are all simple and nearly identical, so I have squished them into one-liners. 
	public static TextFileManager getAccelFile(){ if ( accelFile == null ) throw new NullPointerException( getter_error ); return accelFile; }
	public static TextFileManager getGPSFile(){ if ( GPSFile == null ) throw new NullPointerException( getter_error ); return GPSFile; }
	public static TextFileManager getPowerStateFile(){ if ( powerStateLog == null ) throw new NullPointerException( getter_error ); return powerStateLog; }
	public static TextFileManager getCallLogFile(){ if ( callLog == null ) throw new NullPointerException( getter_error ); return callLog; }
	public static TextFileManager getTextsLogFile(){ if ( textsLog == null ) throw new NullPointerException( getter_error ); return textsLog; }
	public static TextFileManager getBluetoothLogFile(){ if ( bluetoothLog == null ) throw new NullPointerException( getter_error ); return bluetoothLog; }
	public static TextFileManager getWifiLogFile(){ if ( wifiLog == null ) throw new NullPointerException( getter_error ); return wifiLog; }
	public static TextFileManager getSurveyTimingsFile(){ if ( surveyTimings == null ) throw new NullPointerException( getter_error ); return surveyTimings; }
	public static TextFileManager getSurveyAnswersFile(){ if ( surveyAnswers == null ) throw new NullPointerException( getter_error ); return surveyAnswers; }
	//the persistent files
	public static TextFileManager getCurrentDailyQuestionsFile(){ if ( currentDailyQuestions == null ) throw new NullPointerException( getter_error ); return currentDailyQuestions; }
	public static TextFileManager getCurrentWeeklyQuestionsFile(){ if ( currentWeeklyQuestions == null ) throw new NullPointerException( getter_error ); return currentWeeklyQuestions; }
	public static TextFileManager getDebugLogFile(){ if ( debugLogFile == null ) throw new NullPointerException( getter_error ); return debugLogFile; }
	public static TextFileManager getDeviceInfoFile(){ if ( deviceInfo == null ) throw new NullPointerException( getter_error ); return deviceInfo; }
	public static TextFileManager getKeyFile() { if ( keyFile == null ) throw new NullPointerException( getter_error ); return keyFile; }
	
	//and (finally) the non-static object instance variables
	public String name = null;
	public String fileName = null;
	private String header = null;
	private Boolean persistent = null;
		
	/*###############################################################################
	######################## CONSTRUCTOR STUFF ######################################
	###############################################################################*/
	

	/**Starts the TextFileManager
	 * This must be called before code attempts to access files using getXXXFile().
	 * Initializes all TextFileManager object instances.
	 * Do not run more than once, it will error on you. 
	 * @param appContext a Context, provided by the app. */
	public static synchronized void start(Context appContext){
		if ( started ) { return; }
		
		// TODO: Eli/Josh.  make sure file names are to-spec
		// Persistent files
		debugLogFile = new TextFileManager(appContext, "logFile.txt", "THIS LINE IS A LOG FILE HEADER\n", true, true);
		currentDailyQuestions = new TextFileManager(appContext, "currentDailyQuestionsFile.json", "", true, true);
		currentWeeklyQuestions = new TextFileManager(appContext, "currentWeeklyQuestionsFile.json", "", true, true);
		
		// Regularly/periodically-created files
		GPSFile = new TextFileManager(appContext, "gps", GPSListener.header, false, true);
		accelFile = new TextFileManager(appContext, "accel", AccelerometerListener.header, false, true);
		textsLog = new TextFileManager(appContext, "textsLog", SmsSentLogger.header, false, true);
		callLog = new TextFileManager(appContext, "callLog", CallLogger.header, false, true);
		powerStateLog = new TextFileManager(appContext, "powerState", PowerStateListener.header, false, true);
		bluetoothLog = new TextFileManager(appContext, "bluetoothLog", BluetoothListener.header, false, true);
		wifiLog = new TextFileManager(appContext, "wifiLog", WifiListener.header, false, true);
		
		// Files created upon specific events
		deviceInfo = new TextFileManager(appContext, "phoneInfo", "", false, false);
		surveyTimings = new TextFileManager(appContext, "surveyTimings", SurveyTimingsRecorder.header, false, false);
		surveyAnswers = new TextFileManager(appContext, "surveyAnswers", SurveyAnswersRecorder.header, false, false);
		
		//the key file for encryption (it is persistent and never written to)
		keyFile = new TextFileManager(appContext, "keyFile", "", true, true);
		started = true;
	}
	
	/** This class has a PRIVATE constructor.  The constructor is only ever called 
	 * internally, via the static start() function, creating files for data storage. 
	 * @param appContext A Context.
	 * @param name The file's name.
	 * @param header The first line of the file.  Leave empty if you don't want a header, remember to include a new line at the end of the header.
	 * @param persistent Set this to true for a persistent file */
	private TextFileManager(Context appContext, String name, String header, Boolean persistent, Boolean createNow ){
		TextFileManager.appContext = appContext;
		this.name = name;
		this.header = header;
		this.persistent = persistent;
		if (createNow) { this.newFile(); }
	}
	
	/*###############################################################################
	######################## OBJECT INSTANCE FUNCTIONS ##############################
	###############################################################################*/
	
	/** Makes a new file.  Persistent files do not get a time stamp.
	 * All files get their header written as the first line. */
	public synchronized void newFile(){
		if ( this.persistent ) { this.fileName = this.name; } 
		else {
			String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
			this.fileName = LoginSessionManager.getPatientID() + "_" + this.name + "_" + timecode + ".csv"; }
		this.write(header);
	}
	
	/** If it's a SurveyAnswers or SurveyTimings file, we want to append the
	 * Survey ID so that the file name reads like this:
	 * [USERID]_SurveyAnswers[SURVEYID]_[TIMESTAMP].csv
	 * @param surveyId */
	public synchronized void newFile(String surveyId) {
		String nameHolder = this.name;
		this.name += surveyId;
		newFile();
		this.name = nameHolder;
	}
	
	
	/** Takes a string. writes that to the file, adds a new line to the string.
	 * Prints a stacktrace on a write error, but does not crash. If there is no
	 * file, a new file will be created.
	 * @param data a string*/
	public synchronized void write(String data){
		//write the output, we always want mode append
		FileOutputStream outStream;
		try {
			if (fileName == null) {
				this.newFile();
			}
			outStream = appContext.openFileOutput(fileName, Context.MODE_APPEND);
			outStream.write( ( data ).getBytes() );
			outStream.write( "\n".getBytes() );
			outStream.flush();
			outStream.close(); }
		catch (Exception e) {
			Log.i("FileManager", "Write error: " + this.name);
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
			try { while( (data = bufferedInputStream.read()) != -1)
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
	
	/** Creates a new instance of file, then delete the old file. */
	public synchronized void deleteSafely() {
		String oldFileName = this.fileName;
		// For files that are persistant we have to do a slightly unsafe deletion, for everything else
		// we allocate the new file and then delete the old file.
		
		if ( this.persistent ) { //delete then create (unsafe, potential threading issues) 
			TextFileManager.delete(oldFileName); 
			this.newFile(); }
		else { 					//create then delete
			this.newFile();
			TextFileManager.delete(oldFileName); } 
	}
	
	/** Deletes a file.  Exists to make file deletion thread-safe.
	 * @param fileName */
	public static synchronized void delete(String fileName){
		try { appContext.deleteFile(fileName); }
		catch (Exception e) {
			Log.i("TextFileManager", "cannot delete file " + fileName );
			e.printStackTrace(); }
	}
	
	/** Make new files for all the non-persistent files. */
	public static synchronized void makeNewFilesForEverything() {
		GPSFile.newFile();
		accelFile.newFile();
		powerStateLog.newFile();
		callLog.newFile();
		textsLog.newFile();
		bluetoothLog.newFile();
		wifiLog.newFile();
	}
	
	//TODO: Eli. this function is/was originally only public for debugging purposes, getAllFilesSafely() should be used instead?
	/** Very simple function, exists to make any function that needs to grab all extant files thread-safe.
	 * DO NOT USE THIS FUNCTION, USE getAllFilesSafely() INSTEAD.
	 * @return a string array of all files in the app's file directory. */
	public static synchronized String[] getAllFiles() { return appContext.getFilesDir().list(); }
	
	/** Returns a list of file names, all files in that list are retired and will not be written to again.
	 * @return a string array of files*/
	public static synchronized String[] getAllFilesSafely() {
		String[] file_list = getAllFiles();
		makeNewFilesForEverything();
		return file_list;
	}
	
	/** Returns all data files except for the persistent ones that shouldn't be uploaded
	 * @return String[] a list of file names */
	public static synchronized String[] getAllUploadableFiles() {
		Set<String> files = new HashSet<String>();
		Collections.addAll(files, getAllFiles());
		
		files.remove(TextFileManager.getCurrentDailyQuestionsFile().fileName);
		files.remove(TextFileManager.getCurrentWeeklyQuestionsFile().fileName);
		files.remove(TextFileManager.getDebugLogFile().fileName);
		
		makeNewFilesForEverything();
		
		return files.toArray(new String[files.size()]);
	}
	
	/*###############################################################################
	######################## DEBUG STUFF ############################################
	###############################################################################*/
	
	/** use the data read function, then converts it to a string. */
	public synchronized String getDataString(){ return new String( this.readDataFile() ); }
	
	/**For Debug Only.  Deletes all files, creates new ones. */
	public static synchronized void deleteEverything() {
		//Get complete list of all files, then make new files, then delete all files from the old files list.
		Set<String> files = new HashSet<String>(); 
		Collections.addAll(files, getAllFilesSafely());
		
		//Need to do this crap or else we end up deleting the persistent files repeatedly
		files.remove(TextFileManager.getDeviceInfoFile().fileName);
		TextFileManager.getDeviceInfoFile().deleteSafely();
		files.remove(TextFileManager.getCurrentDailyQuestionsFile().fileName);
		TextFileManager.getCurrentDailyQuestionsFile().deleteSafely();
		files.remove(TextFileManager.getCurrentWeeklyQuestionsFile().fileName);
		TextFileManager.getCurrentWeeklyQuestionsFile().deleteSafely();
		files.remove(TextFileManager.getDebugLogFile().fileName);
		TextFileManager.getDebugLogFile().deleteSafely();
		
		//and delete things
		for (String file_name : files) {
			Log.i("deleting file", file_name);
			try { appContext.deleteFile(file_name); }
			catch (Exception e) {
				Log.i("TextFileManager", "could not delete file " + file_name); 
				e.printStackTrace(); } }
	}
	
}
