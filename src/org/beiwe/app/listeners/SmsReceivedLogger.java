package org.beiwe.app.listeners;

import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/* Improvement idea: we could also read the messages in the inbox, and possibly
 * get more data from that, such as whether a message was opened. */

/**
 * Listens for Broadcast saying that an SMS message has been received.
 * 
 * Note: the timestamp is the time at which the phone received the message.
 * For SMS messages (but not MMS messages), it is also possible to record the
 * time the message was sent; this is recorded in the last column ("time sent")
 * 
 * @author Josh Zagorsky June 2014
 */
public class SmsReceivedLogger extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			handleIncomingSMS(intent);
		}
		else if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
			handleIncomingMMS(intent);
		}
	}

	
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
                     
                     String data = "" + System.currentTimeMillis() + TextFileManager.DELIMITER;
                     data += EncryptionEngine.hashPhoneNumber(incomingNumber) + TextFileManager.DELIMITER;
                     data += "received" + TextFileManager.DELIMITER;
                     data += "MMS";

                     Log.i("SMSReceivedLogger", "data = " + data);
                     TextFileManager.getTextsLogFile().writeEncrypted(data);
                 }
             }
         }
	}
	
	
	private void handleIncomingSMS(Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] messages = null;
		String messageFrom;
		if (bundle != null) {
			try {
				Object[] pdus = (Object[]) bundle.get("pdus");
				messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu( (byte[]) pdus[i]);
					messageFrom = messages[i].getOriginatingAddress();
					String messageBody = messages[i].getMessageBody();
					long timestamp = messages[i].getTimestampMillis();
					
					String data = "" + System.currentTimeMillis() + TextFileManager.DELIMITER;
					data += EncryptionEngine.hashPhoneNumber(messageFrom) + TextFileManager.DELIMITER;
					data += "received" + TextFileManager.DELIMITER;
					data += messageBody.length() + TextFileManager.DELIMITER;
					data += timestamp;

					Log.i("SMSReceivedLogger", "data = " + data);
					TextFileManager.getTextsLogFile().writeEncrypted(data);
				}
			}
			catch (Exception e) {
				Log.i("SMSReceivedLogger", "SMS_RECEIVED Caught exception: " + e.getMessage());
			}
		}		
	}

}
