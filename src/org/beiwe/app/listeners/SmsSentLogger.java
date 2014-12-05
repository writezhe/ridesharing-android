package org.beiwe.app.listeners;

import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony.TextBasedSmsColumns;
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

	public static String header = "timestamp,hashed phone number,sent vs received,message length,time sent";

	private TextFileManager smsLogFile = null;
	private Handler handler = null;
	Context appContext = null;

	
	public SmsSentLogger(Handler theHandler, Context context) {
		super(theHandler);
		theHandler = handler;
		appContext = context;
		smsLogFile = TextFileManager.getTextsLogFile();
	}
	
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		final String[] projection = new String[]{"_id", "ct_t"};
		
		Cursor cursor = appContext.getContentResolver().query( Uri.parse("content://mms-sms/conversations"), projection, null, null, null );
		cursor.moveToNext();
		
		Log.i("SMSes", " stuff?"  );
		
		String address = cursor.getString(cursor.getColumnIndex("address"));
		String body = cursor.getString(cursor.getColumnIndex("body"));
		String date = cursor.getString(cursor.getColumnIndex("date"));
		int msgType = cursor.getInt(cursor.getColumnIndex("type"));
		
		/* Improvement idea: we could log all message types; TextBasedSmsColumns has 6 types of messages: 
		 * https://developer.android.com/reference/android/provider/Telephony.TextBasedSmsColumns.html
		 * That would provide more data on draft messages, messages not sent immediately, etc.
		 * We could also use this class to log incoming messages as well as outgoing messages. */
		if (msgType == TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
			
			String data = "" + date + TextFileManager.DELIMITER;
			data += EncryptionEngine.hashPhoneNumber(address) + TextFileManager.DELIMITER;
			data += "sent" + TextFileManager.DELIMITER;
			data += body.length();
			
			Log.i("SMSLogger", "data = " + data);
			smsLogFile.writeEncrypted(data);
			
			// TODO: Josh. Figure out if when a text is sent, is written as a new line if no network.
		}
		
		// TODO: Josh. check how MMS messages are handled
	}
	
}

///* This is a more brittle way to check if a message is a sent message: check if the
//* message's "_id" is new, and if its "protocol" is null. The reason for this check is that
//* every time a message gets sent, the system registers multiple new messages (on one
//* testing phone, 4 new messages appear each time); one as its moved to the queue, one as
//* its moved to the outbox, etc. */
//
//// Make this a class variable:
//String idOfLastSmsSent = null;
//
//// Put these lines inside onChange():
//String id = cursor.getString(cursor.getColumnIndex("_id"));
//String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
//
//// Make this the if() statement:
////if ((protocol == null) && (idIsNew(id))) {
//
//// Uncomment this function:
//// Check if the message's ID hasn't been encountered before: http://stackoverflow.com/a/8242090
//private boolean idIsNew(String id) {
//	if ((idOfLastSmsSent == null) || (!idOfLastSmsSent.equals(id))) {
//		idOfLastSmsSent = id; // Reset idOfLastSmsSent
//		return true;
//	}
//	return false; // This is not a new/unique ID; return false
//}
