package org.futto.app.listeners;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.futto.app.storage.EncryptionEngine;
import org.futto.app.storage.TextFileManager;

/* Improvement idea: we could also read the messages in the inbox, and possibly
 * get more data from that, such as whether a message was opened. */

/**
 * Listens for Broadcast saying that an SMS message has been received, and records
 * their timestamp to the log.
 * 
 * Note: the timestamp is the time at which the phone received the message.
 * For SMS messages (but not MMS messages), it is also possible to record the
 * time the message was sent; this is recorded in the last column ("time sent")
 * 
 * @author Josh Zagorsky June 2014 */
public class SmsReceivedLogger extends BroadcastReceiver {

	/** onReceive, splits incoming texts into SMS and MMS. */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			handleIncomingSMS(intent);
		}
		else if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
			handleIncomingMMS(intent);
		}
	}

	/** pulls out source phone number from an MMS, writes info to texts log */
	private void handleIncomingMMS(Intent intent) {
		 Bundle bundle = intent.getExtras();

         if (bundle != null) {
        	 // MMS detection code based on http://stackoverflow.com/q/14452808
             byte[] buffer = bundle.getByteArray("data");
             String incomingNumber = new String(buffer);
             int indx = incomingNumber.indexOf("/TYPE");
             if(indx>0 && (indx-15)>0){
                 int newIndx = indx - 15;
                 incomingNumber = incomingNumber.substring(newIndx, indx);
                 indx = incomingNumber.indexOf("+");
                 if(indx>0){
                     incomingNumber = incomingNumber.substring(indx);
//                     "timestamp,hashed phone number,sent vs received,message length,time sent";

//                     TODO: Josh. Low priority. feature. determine if we can get the length of the text, if it has an attachment.

                 }
             }
         }
	}
	
	/** pulls out source phone number and length from an SMS, writes info to texts log. */
	@SuppressLint("InlinedApi") @SuppressWarnings("deprecation") //these yell at you in the old/new code paths.
	private void handleIncomingSMS(Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] messages = null;
		String messageFrom;
		if (bundle != null) {
			try {
				Object[] pdus = (Object[]) bundle.get("pdus");
				String format = bundle.getString("format");
				messages = new SmsMessage[pdus.length];
			}
			catch (Exception e) { Log.e("SMSReceivedLogger", "SMS_RECEIVED Caught exception: " + e.getCause() + ", " + e.getMessage()); }
			//TODO:Eli. Low priority. if we have implemented a message parameter add "did not crash" message here
		}
	}
}
