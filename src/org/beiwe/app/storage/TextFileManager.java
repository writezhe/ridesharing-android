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
import org.beiwe.app.listeners.CallLogger;
import org.beiwe.app.listeners.GPSListener;
import org.beiwe.app.listeners.PowerStateListener;
import org.beiwe.app.listeners.SmsSentLogger;

import android.content.Context;
import android.util.Log;

//TODO:  filename should contain...
// type of data: "voice recording" or "accelerometer", then a dash, then a start timestamp, then a userID number

/**
 * The (Text)FileManager.
 * The FileManager is implemented as a Singleton.  More accurately the static object contains several
 * singletons, static instances of FileManager Objects.  Before using the FileManager the app must
 * provide it with a Context and call the Start() function.  Failure to do so causes the app to crash.
 * This Is Intentional.  The Point of the app is to record data.
 * The Reason for this construction is to construct a file write system where there is only ever a
 * single pointer to each file type, and that these files are never overwritten, written to asynchronously,
 * or left accidentally empty.
 * The files handled here are the GPSFile, accelFile, powerStateLog, audioSurveyInfo, callLog, textsLog, surveyResponse,
 * currentQuestuons, deviceData, and debugLogFile.
 * On construction you provide a boolean flag ("persistent").  Persistent files do not get overwritten on application start.
 * To access a file use the following construction: TextFileManager.getXXXFile()
 * @author Eli */
public class TextFileManager {
	
//TODO: we need to escape all separator values that get dumped into strings 
//TODO: sanitize inputs for the survey info, coordinate with kevin on that, may be easier to implement serverside.
//TODO: implement public static header variables for all the classes that will need them, import here
//TODO: we probably want a static array pointing to all the static objects to make a static X_for_everything functions easier?
	//Delimiter and newline strings
	public static String delimiter = ",";
	public static String newline = "\n";
	
	//Static instances of the individual FileManager objects.
	private static TextFileManager GPSFile = null;
	private static TextFileManager accelFile = null;
	private static TextFileManager powerStateLog = null;
	private static TextFileManager callLog = null;
	private static TextFileManager textsLog = null;
	private static TextFileManager surveyResponse = null;
	private static TextFileManager audioSurveyLog = null;
	
	private static TextFileManager debugLogFile = null;
	private static TextFileManager currentQuestions = null;
	private static TextFileManager deviceInfo = null;
	
	//"global" static variables
	private static Context appContext;
	private static boolean started = false; 
	private static String getter_error = "You tried to access a file before calling TextFileManager.start().";
	
	//public static getters.
	// These are all simple and nearly identical, so I have squished them into one-liners. 
	public static TextFileManager getAccelFile(){ if ( accelFile == null ) throw new NullPointerException( getter_error ); return accelFile; }
	public static TextFileManager getAudioLogFile(){ if ( audioSurveyLog == null ) throw new NullPointerException( getter_error ); return accelFile; }
	public static TextFileManager getGPSFile(){ if ( GPSFile == null ) throw new NullPointerException( getter_error ); return GPSFile; }
	public static TextFileManager getPowerStateFile(){ if ( powerStateLog == null ) throw new NullPointerException( getter_error ); return powerStateLog; }
	public static TextFileManager getCallLogFile(){ if ( callLog == null ) throw new NullPointerException( getter_error ); return callLog; }
	public static TextFileManager getTextsLogFile(){ if ( textsLog == null ) throw new NullPointerException( getter_error ); return textsLog; }
	public static TextFileManager getSurveyResponseFile(){ if ( surveyResponse == null ) throw new NullPointerException( getter_error ); return surveyResponse; }
	//the persistant files
	public static TextFileManager getCurrentQuestionsFile(){ if ( currentQuestions == null ) throw new NullPointerException( getter_error ); return currentQuestions; }
	public static TextFileManager getDebugLogFile(){ if ( debugLogFile == null ) throw new NullPointerException( getter_error ); return debugLogFile; }
	public static TextFileManager getDeviceInfoFile(){ if ( deviceInfo == null ) throw new NullPointerException( getter_error ); return deviceInfo; }
	
	//and (finally) the non-static object instance variables
	private String name = null;
	private String fileName = null;
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
		//if already started, flip out.
		//TODO: test this?  consider removing the pointer exception and just exiting
		if ( started ){ throw new NullPointerException("You may only start the FileManager once."); }
		else { started = true; }
		
		debugLogFile = new TextFileManager(appContext, "logFile.txt", "THIS LINE IS A LOG FILE HEADER\n", true);
		currentQuestions = new TextFileManager(appContext, "currentQuestionsFile.json", "", true);
		deviceInfo = new TextFileManager(appContext, "phoneInfo.txt", "", true);
		
		GPSFile = new TextFileManager(appContext, "gpsFile", GPSListener.header, false);
		accelFile = new TextFileManager(appContext, "accelFile", AccelerometerListener.header, false);
		textsLog = new TextFileManager(appContext, "textsLog", SmsSentLogger.header, false);
		powerStateLog = new TextFileManager(appContext, "screenState", PowerStateListener.header, false);
		callLog = new TextFileManager(appContext, "callLog", CallLogger.header, false);
		audioSurveyLog = new TextFileManager(appContext, "audioSurveyLog", "generic header 1 2 3\n", false);

		/* Don't create a new surveyResponse file; these should get created when and only when the
		 * user opens a new survey. TODO: run this by Eli and see if this is the way to do it. */
		// surveyResponse = new TextFileManager(appContext, "surveyData", AnswerRecorder.header, false);
	}
	
	/** This class has a PRIVATE constructor.  The constructor is only ever called 
	 * internally, via the static start() function, creating files for data storage. 
	 * @param appContext A Context.
	 * @param name The file's name.
	 * @param header The first line of the file.  Leave empty if you don't want a header, remember to include a new line at the end of the header.
	 * @param persistent Set this to true for a persistent file */
	private TextFileManager(Context appContext, String name, String header, Boolean persistent ){
		TextFileManager.appContext = appContext;
		this.name = name;
		this.header = header;
		this.persistent = persistent;
		this.newFile();
	}
	
	/*###############################################################################
	######################## OBJECT INSTANCE FUNCTIONS ##############################
	###############################################################################*/
	
	/** Makes a new file.  Persistant files do not get a time stamp.
	 * All files get their header written as the first line. */
	public synchronized void newFile(){
		if ( this.persistent ) { this.fileName = this.name; } 
		else {
			String timecode = ((Long)(System.currentTimeMillis() / 1000L)).toString();
			this.fileName = this.name + "-" + timecode + ".csv"; }
		this.write(header);
	}
	
	/**Takes a string. writes that to the file.
	 * Prints a stacktrace on a write error.
	 * @param data a string*/
	public synchronized void write(String data){
		//write the output, we always want mode append
		FileOutputStream outStream;
		try {
			outStream = appContext.openFileOutput(fileName, Context.MODE_APPEND);
			outStream.write(data.getBytes());
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
	private static synchronized void delete(String fileName){
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
		audioSurveyLog.newFile(); }
	
	//FIXME: this function is only public for debugging purposes, getAllFilesSafely() should be used instead.
	/** Very simple function, exists to make any function that needs to grab all extant files thread-safe.
	 * DO NOT USE THIS FUNCTION, USE getAllFilesSafely() INSTEAD.
	 * @return a string array of all files in the app's file directory. */
	public static synchronized String[] getAllFiles() { return appContext.getFilesDir().list(); }
	
	//TODO: remove persistant files (move all persistant files to an internal directory, remove directory from return.
	/** Returns a list of file names, all files in that list are retired and will not be written to again.
	 * @return a string array of files*/
	public static synchronized String[] getAllFilesSafely() {
		String[] file_list = getAllFiles();
		makeNewFilesForEverything();
		return file_list;
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
		files.remove(TextFileManager.getCurrentQuestionsFile().fileName);
		TextFileManager.getCurrentQuestionsFile().deleteSafely();
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
