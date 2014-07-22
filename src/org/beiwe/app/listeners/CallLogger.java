package org.beiwe.app.listeners;

import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

/**
 * This is the call logger class. The objective of this class it to wait for a change in the phone's log file,
 * then record the number that was called, the type of call, the date the call was placed, and the duration of
 * the call. The phone number is hashed according the {@link EncriptionEngine}'s hash method.
 * 
 * @author Dor Samet
 */

public class CallLogger extends ContentObserver {

	// Private variables
	private TextFileManager debugLogFile = null;
	private TextFileManager callLogFile = null;
	private Handler handler = null;
	private int idOfLastCall = 0;
	
	// Enum - ID number for personal use
	private String id = android.provider.CallLog.Calls._ID;
	
	// Context
	Context appContext = null;
	
	// Columns that interest us - phone number, type of call, date, duration of call
	String[] fields = {android.provider.CallLog.Calls.NUMBER, 
			android.provider.CallLog.Calls.TYPE, 
			android.provider.CallLog.Calls.DATE, 
			android.provider.CallLog.Calls.DURATION,
	};
	

	// Constructor of the call logger object
	public CallLogger(Handler theHandler, Context context) {
		super(theHandler);
		theHandler = handler;
		appContext = context;
		debugLogFile = TextFileManager.getDebugLogFile(); 
		callLogFile = TextFileManager.getCallLogFile();
	}
	

	/**
	 * On change, Looks for the last row, then queries the results
	 */
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		
		// StringBuilder that will be used when creating the data to be written
		StringBuilder builder = new StringBuilder();
		
		// Database information
		Uri allCalls = Uri.parse("content://call_log/calls");
		Cursor cursor = appContext.getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
		
		// Cursor moves to the last row
		cursor.moveToLast();
		
		// Dealing with first time activation case - set idOfLastCall, and then create the log file with column names
		if (idOfLastCall == 0) { 
			firstTimeRoutine(cursor, builder);
		}
		
		// Query the results from the database
		queryResults(cursor, builder);
	}
	
	/**
	 * Writes the header lines to the log file, and resets the string builder.
	 * 
	 * @param dbCursor
	 * @param stringBuilder
	 */
	private void firstTimeRoutine(Cursor dbCursor, StringBuilder stringBuilder) {
		idOfLastCall = dbCursor.getInt(dbCursor.getColumnIndex(id));
		for (int i = 0; i < fields.length; i++) {
			stringBuilder.append(fields[i] + TextFileManager.delimiter);
		}
		formatThenAddToFile(stringBuilder);
		stringBuilder.setLength(0);
		dbCursor.moveToPrevious();		
	}
	
	/**
	 * Searches for the latest recorded row, then records each row's parameters that interest us.
	 * The query ends by adding the saved data to the log file
	 * 
	 * @param dbCursor
	 * @param stringBuilder
	 */
	private void queryResults(Cursor dbCursor, StringBuilder stringBuilder) {
		// Climb up the rows until reaching idOfLastCall's row
		while (idOfLastCall != dbCursor.getInt(dbCursor.getColumnIndex(id))) {
			dbCursor.moveToPrevious();
		}
		
		// While there exists a next row
		while(dbCursor.moveToNext()) {
			// Append all the values in "fields"
			for (int i = 0; i < fields.length; i++) {
				if(i == 0) { // If dealing with a phone, we need to hash it
					stringBuilder.append(EncryptionEngine.hashPhoneNumber(dbCursor.getString(dbCursor.getColumnIndex(fields[i]))));
				} else if(i == 1) { // If dealing with a type - we need to translate it to real words
					int type = dbCursor.getInt(dbCursor.getColumnIndex(fields[i]));
					switch(type) {
					case CallLog.Calls.OUTGOING_TYPE:
						stringBuilder.append("Outgoing call");
						break;

					case CallLog.Calls.INCOMING_TYPE:
						stringBuilder.append("Incoming call");
						break;

					case CallLog.Calls.MISSED_TYPE:
						stringBuilder.append("Missed call");
						break;
					}
				} else { // Otherwise just append the string to the builder
					stringBuilder.append(dbCursor.getString(dbCursor.getColumnIndex(fields[i])));	}
				// In each case, add a delimiter
				stringBuilder.append(TextFileManager.delimiter);
			}

			// Final Formatting before writing into file
			formatThenAddToFile(stringBuilder);
			
			// Present the data to log cat
			String result = stringBuilder.toString();
			Log.i("CallLogger", "Data so far is: " + result);	
		}
		
		// Currently pointing at a null row
		dbCursor.moveToLast();
		
		// Now we have the last ID!
		idOfLastCall = dbCursor.getInt(dbCursor.getColumnIndex(fields[4]));
		
		// Present the data to log cat
		String result = stringBuilder.toString();
		Log.i("CallLogger", "Final Data is: " + result);
	}

	/**
	 * Formats the stringBuilder that has too many delimiters, 
	 * then writes it to the csv file as a string.
	 * 
	 * @param stringBuilder
	 */
	private void formatThenAddToFile(StringBuilder stringBuilder) {
		stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(TextFileManager.delimiter));
		stringBuilder.append("\n");
		
		String result = stringBuilder.toString();
		debugLogFile.write(result);
	}
}