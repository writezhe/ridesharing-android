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

/** @author Dor Samet */

public class CallLogger extends ContentObserver {

	public static String header = "hashed phone number,call type,date,duration in seconds";

	// Private variables
	private Handler handler = null;
	private Uri allCalls = Uri.parse("content://call_log/calls");

	// Last recorded values
	private int lastRecordedID = 0;
	private int lastKnownSize = 0;
	// Fields
	private String id = android.provider.CallLog.Calls._ID;
	private String number = android.provider.CallLog.Calls.NUMBER;
	private String type = android.provider.CallLog.Calls.TYPE; 
	private String date = android.provider.CallLog.Calls.DATE;
	private String duration = android.provider.CallLog.Calls.DURATION;
	
	// Cursor field
	private Cursor textsDBQuery;

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

		// Pull database info, set lastKnownSize
		textsDBQuery = appContext.getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
		textsDBQuery.moveToFirst();
		lastKnownSize = textsDBQuery.getCount();
//		Log.i("CallLogger", "" + lastKnownSize);
		// Record id of last made call and the date
		if (lastKnownSize != 0) {
			lastRecordedID = textsDBQuery.getInt(textsDBQuery.getColumnIndex(id));
			textsDBQuery.getLong(textsDBQuery.getColumnIndex(android.provider.CallLog.Calls.DATE));
		}
	}

	
	/**On change, Looks for the last row, then goes back until reaching the row of the last recorded call,
	 * Then goes back down until reaching the newest line, and records everything to the log file. */
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		
		// Database information
		textsDBQuery = appContext.getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DEFAULT_SORT_ORDER);
		textsDBQuery.moveToFirst();
		
		int currentSize = textsDBQuery.getCount();
//		Log.i("Call Log", "" + "Current Size is " + currentSize);
//		Log.i("Call Log", "Last Known Size is " + lastKnownSize);
		
		// Record id of last made call, recod last date
		if (lastKnownSize == 0) {
			lastRecordedID = textsDBQuery.getInt(textsDBQuery.getColumnIndex(id));
			textsDBQuery.getLong(textsDBQuery.getColumnIndex(android.provider.CallLog.Calls.DATE));
		}
		
		// Comparison values
		int currentID = textsDBQuery.getInt(textsDBQuery.getColumnIndex(id));
//		Log.i("Call Log", "" + "Current Size is " + currentID);
//		Log.i("Call Log", "Last Known ID is " + lastRecordedID)
		;
		// A call was deleted
		if (currentSize < lastKnownSize) {
			Log.i("Call Logger", "Last Call deleted, Last Call deleted, Last Call deleted, Last Call deleted"); }
		else if ( currentSize == lastKnownSize && currentID == lastRecordedID ) {
			Log.i("CallLogger", "Something broke - this doesn't make sense..."); }
		else {
			Log.i("CallLogger", "Last recorded ID " + lastRecordedID);
		
			// 	Descend until reaching the idOfLastCall row
			while (currentID != lastRecordedID) {
				textsDBQuery.moveToNext();
				Log.i("CallLogger", "Current ID is " + currentID);
				currentID = textsDBQuery.getInt(textsDBQuery.getColumnIndex(id));
			}

			// While there exists a next row
			while(!textsDBQuery.isBeforeFirst()) {
				Log.i("Call Logger", "" + (textsDBQuery.getInt(textsDBQuery.getColumnIndex(id))));
				if (currentID <= lastRecordedID) {
					textsDBQuery.moveToPrevious();
					currentID = textsDBQuery.getInt(textsDBQuery.getColumnIndex(id));
					continue;
				}
				StringBuilder callLoggerLine = new StringBuilder();
				// Add hashed phone number
				callLoggerLine.append(EncryptionEngine.hashPhoneNumber(textsDBQuery.getString(textsDBQuery.getColumnIndex(number))));
				callLoggerLine.append(TextFileManager.DELIMITER);

				// Add call type
				int callType = textsDBQuery.getInt(textsDBQuery.getColumnIndex(type));
				if (callType == CallLog.Calls.OUTGOING_TYPE) { callLoggerLine.append("Outgoing Call"); }
				else if (callType == CallLog.Calls.INCOMING_TYPE) { callLoggerLine.append("Incoming Call"); }
				else { callLoggerLine.append("Missed Call"); }
				callLoggerLine.append(TextFileManager.DELIMITER);

				// Add date
				callLoggerLine.append(textsDBQuery.getLong(textsDBQuery.getColumnIndex(date)));
				callLoggerLine.append(TextFileManager.DELIMITER);
				
				// Add duration
				callLoggerLine.append(textsDBQuery.getInt(textsDBQuery.getColumnIndex(duration)));

				Log.i("Call Log", callLoggerLine.toString());
				TextFileManager.getCallLogFile().writeEncrypted(callLoggerLine.toString());
				textsDBQuery.moveToPrevious();
			}
		}
		lastKnownSize = currentSize;
		lastRecordedID = currentID;
	}
}