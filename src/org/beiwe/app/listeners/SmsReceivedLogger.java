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
 * Listens for Broadcast saying that an SMS message has been received
 * 
 * @author Josh Zagorsky June 2014
 */
public class SmsReceivedLogger extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
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
						
						String data = "" + timestamp + TextFileManager.DELIMITER;
						data += EncryptionEngine.hashPhoneNumber(messageFrom) + TextFileManager.DELIMITER;
						data += "received" + TextFileManager.DELIMITER;
						data += messageBody.length();

						Log.i("SMSLogger", "data = " + data);
						TextFileManager.getTextsLogFile().write(data);
					}
				}
				catch (Exception e) {
					Log.i("SMSLogger", "SMS_RECEIVED Caught exception: " + e.getMessage());
				}
			}
		}
	}

}
