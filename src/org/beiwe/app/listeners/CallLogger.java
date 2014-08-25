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

// TODO: BootLogger does not require instantiation

public class CallLogger extends ContentObserver {

	public static String header = "hashed phone number,call type,date,duration in seconds";

	// Private variables
	private TextFileManager callLogFile = null;
	private Handler handler = null;
	private Uri allCalls = Uri.parse("content://call_log/calls");

	// Last recorded values
	private int lastRecordedID = 0;
	private int lastKnownSize = 0;
	private long lastRecordingDate = 0;

	// Fields
	private String id = android.provider.CallLog.Calls._ID;
	private String number = android.provider.CallLog.Calls.NUMBER;
	private String type = android.provider.CallLog.Calls.TYPE; 
	private String date = android.provider.CallLog.Calls.DATE;
	private String duration = android.provider.CallLog.Calls.DURATION;

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

		// Pull database info
		Cursor cursor = appContext.getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();

		// Set lastKnownSize
		lastKnownSize = cursor.getCount();

		// Record id of last made call
		lastRecordedID = cursor.getInt(cursor.getColumnIndex(id));
		
		// Record last date
		lastRecordingDate = cursor.getLong(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
	}


	/**
	 * On change, Looks for the last row, then goes back until reaching the row of the last recorded call,
	 * Then goes back down until reaching the newest line, and records everything to the log file.
	 */
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);

		// Get the most recent callLogFile
		callLogFile = TextFileManager.getCallLogFile();
		
		// Database information
		Cursor cursor = appContext.getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();

		// Comparison values
		int currentSize = cursor.getCount();
		Log.i("Call Log", "" + "Current Size is " + currentSize);
		Log.i("Call Log", "Last Known Size is " + lastKnownSize);
		int currentID = cursor.getInt(cursor.getColumnIndex(id));
		Log.i("Call Log", "" + "Current Size is " + currentID);
		Log.i("Call Log", "Last Known ID is " + lastRecordedID);
		long currentDate = cursor.getLong(cursor.getColumnIndex(date));

		// A call was deleted
		if (currentSize < lastKnownSize) {
			Log.i("Call Logger", "Last Call deleted, Last Call deleted, Last Call deleted, Last Call deleted");
		} else if ( currentSize == lastKnownSize && currentID == lastRecordedID ) {
			Log.i("CallLogger", "Something broke - this doesn't make sense...");
		} else {
			Log.i("CallLogger", "Last recorded ID " + lastRecordedID);
			// 	Descend until reaching the idOfLastCall row
			while (currentID != lastRecordedID) {
				cursor.moveToNext();
				Log.i("CallLogger", "Current ID is " + currentID);
				currentID = cursor.getInt(cursor.getColumnIndex(id));
			}

			// While there exists a next row
			while(!cursor.isBeforeFirst()) {
				Log.i("Call Logger", "" + (cursor.getInt(cursor.getColumnIndex(id))));
				if (currentID <= lastRecordedID) {
					cursor.moveToPrevious();
					currentID = cursor.getInt(cursor.getColumnIndex(id));
					continue;
				}
				StringBuilder callLoggerLine = new StringBuilder();
				// Add hashed phone number
				callLoggerLine.append(EncryptionEngine.hashPhoneNumber(cursor.getString(cursor.getColumnIndex(number))));
				callLoggerLine.append(TextFileManager.DELIMITER);

				// Add call type
				int callType = cursor.getInt(cursor.getColumnIndex(type));
				if (callType == CallLog.Calls.OUTGOING_TYPE) {
					callLoggerLine.append("Outgoing Call");
				} else if (callType == CallLog.Calls.INCOMING_TYPE) {
					callLoggerLine.append("Incoming Call");
				} else {
					callLoggerLine.append("Missed Call");
				}
				callLoggerLine.append(TextFileManager.DELIMITER);

				// Add date
				callLoggerLine.append(cursor.getLong(cursor.getColumnIndex(date)));
				callLoggerLine.append(TextFileManager.DELIMITER);
				
				// Add duration
				callLoggerLine.append(cursor.getInt(cursor.getColumnIndex(duration)));

				Log.i("Call Log", callLoggerLine.toString());
				callLogFile.write(callLoggerLine.toString());
				cursor.moveToPrevious();
			}
		}
		lastKnownSize = currentSize;
		lastRecordedID = currentID;
	}
}