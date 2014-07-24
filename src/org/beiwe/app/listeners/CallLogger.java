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

// TODO: What happens to a call that is deleted by the user from the call history

public class CallLogger extends ContentObserver {

	public static String header = "hashed phone number,call type,date,duration in seconds\n";
	
	// Private variables
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
			android.provider.CallLog.Calls.DURATION
	};
	

	// Constructor of the call logger object
	public CallLogger(Handler theHandler, Context context) {
		super(theHandler);
		theHandler = handler;
		appContext = context;
		callLogFile = TextFileManager.getCallLogFile(); //TODO: change back to debug log file for debugging
	}
	

	/**
	 * On change, Looks for the last row, then goes back until reaching the row of the last recorded call,
	 * Then goes back down until reaching the newest line, and records everything to the log file.
	 */
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		
		// StringBuilder that will be used when creating the data to be written
		StringBuilder builder = new StringBuilder();
		
		// Database information
		Uri allCalls = Uri.parse("content://call_log/calls");
		Cursor cursor = appContext.getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
		
		cursor.moveToFirst();
		
		// Dealing with first time activation case - set idOfLastCall, and then create the log file with column names
		if (idOfLastCall == 0) { 
			idOfLastCall = cursor.getInt(cursor.getColumnIndex(id));
			Log.i("CallLogger", "Last recorded ID" + idOfLastCall);
			for (int i = 0; i < fields.length; i++) {
				builder.append(fields[i] + TextFileManager.delimiter);
			}
			// TODO: delete this line, because we only print the header line once
			//formatThenAddToFile(builder);
			builder.setLength(0);
		} else {
			int currentID = cursor.getInt(cursor.getColumnIndex(id));
			Log.i("CallLogger", "Last recorded ID " + idOfLastCall);
			// Climb until reaching the idOfLastCall row
			while (currentID != idOfLastCall) {
				cursor.moveToNext();
				Log.i("CallLogger", "Current ID is " + currentID);
				currentID = cursor.getInt(cursor.getColumnIndex(id));
			}

			cursor.moveToPrevious();
		}
		
		// While there exists a next row
		while(!cursor.isBeforeFirst()) {
			// Append all the values in "fields"
			for (int i = 0; i < fields.length; i++) {
				if(i == 0) { // If dealing with a phone, we need to hash it
					builder.append(EncryptionEngine.hashPhoneNumber(cursor.getString(cursor.getColumnIndex(fields[i]))));
					builder.append(TextFileManager.delimiter);
				} else if(i == 1) { // If dealing with a type - we need to translate it to real words
					int type = cursor.getInt(cursor.getColumnIndex(fields[i]));
					switch(type) {
					case CallLog.Calls.OUTGOING_TYPE:
						builder.append("Outgoing Call");
						break;

					case CallLog.Calls.INCOMING_TYPE:
						builder.append("Incoming Call");
						break;
 
					case CallLog.Calls.MISSED_TYPE:
						builder.append("Missed Call");
						break;
					}
					builder.append(TextFileManager.delimiter);
				}
				else { // Otherwise just append the string to the builder
					builder.append(cursor.getString(cursor.getColumnIndex(fields[i])));
					builder.append(TextFileManager.delimiter);
				}
			}
 
			// Final Formatting before writing into file
			formatThenAddToFile(builder);
			
			String result = builder.toString();
			Log.i("CallLogger", "Data so far is: " + result);
			cursor.moveToPrevious();
		}
		
		// Currently pointing at a null row
		cursor.moveToFirst();
 		
		// Now we have the last ID!
		idOfLastCall = cursor.getInt(cursor.getColumnIndex(id));
		Log.i("CallLogger", "Last Logged ID: " + idOfLastCall);

		
		String result = builder.toString();
		Log.i("CallLogger", "Final Data is: " + result);
 	}
 	

	/**
	 * Formats the stringBuilder that has too many delimiters, then writes it to the csv file as a string.
 	 * 
	 * @param stringBuilder
	 * @param logFile
 	 */
	private void formatThenAddToFile(StringBuilder stringBuilder) {
		stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(TextFileManager.delimiter));
		stringBuilder.append("\n");
		
		String result = stringBuilder.toString();
		callLogFile.write(result);
	}
}