package com.zagaran.scrubs;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Records outgoing SMS messages when they're sent
 * 
 * The listeners for incoming and outgoing SMS messages are in different
 * classes because they need to use different methods.  You CAN register
 * a BroadcastReceiver to listen for INCOMING texts, but if you register a
 * BroadcastReceiver to listen for OUTGOING texts, it WON'T pick them up if
 * they're sent with a typical Android default text-messaging app.
 * 
 * Because of that, this class listens for outgoing texts by registering a
 * ContentObserver that watches the file "content://sms" to see when it's
 * changed.
 * 
 * @author Josh Zagorsky June 2014
 */
public class SmsSentLogger extends ContentObserver {
	
	private Handler handler = null;
	Context appContext = null;

	public SmsSentLogger(Handler theHandler, Context context) {
		super(theHandler);
		theHandler = handler;
		appContext = context;
	}
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.i("SmsSentLogger", "onChange triggered");
		
		Uri uriSmsUri = Uri.parse("content://sms");
		
		Cursor cursor = appContext.getContentResolver().query(uriSmsUri, null, null, null, null);
		cursor.moveToNext();
		
		String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
		String address = cursor.getString(cursor.getColumnIndex("address"));
		String body = cursor.getString(cursor.getColumnIndex("body"));
		String date = cursor.getString(cursor.getColumnIndex("date"));
		
		String cursorContents = DatabaseUtils.dumpCurrentRowToString(cursor);
		Log.i("SmsSentLogger", "Cursor contents: " + cursorContents);
		
		if (protocol == null) {
			// Message was just sent
			Log.i("SmsSentLogger", "Message just sent.");
			Log.i("SmsSentLogger", "Phone number: " + address);
			Log.i("SmsSentLogger", "Message body: " + body);
			Log.i("SmsSentLogger", "Date/time: " + date);
			
			// TODO: write messages to the appropriate SMS log file
			// ************* TODO: figure out why this registers each outgoing text 5 times! ***************
			// TODO: figure out what MESSAGE_TYPE means and if it's important: http://stackoverflow.com/a/18873822
		}
		else {
			// Message was just received
			Log.i("SmsSentLogger", "Message just received.");
		}
	}

}
