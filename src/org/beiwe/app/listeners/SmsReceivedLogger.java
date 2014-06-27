package org.beiwe.app.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


// relevant stackoverflow:
// http://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-inbox-programmatically-in-android

// and another tutorial (the link is correct): 
// https://web.archive.org/web/20121022021217/http://mobdev.olin.edu/mobdevwiki/FrontPage/Tutorials/SMS%20Messaging

// TODO: look into also reading what's in the inbox.  Can we get more data from there, such as whether a message was opened?
// http://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-inbox-programmatically-in-android
// http://stackoverflow.com/questions/14545661/android-get-sms-from-inbox-optimized-way-to-read-all-messages-and-group-them
// http://stackoverflow.com/questions/5946262/read-inbox-messages-of-a-particular-number-and-display-them-in-an-activity

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
						Log.i("SMSLogger", "Message from: " + messageFrom);
						Log.i("SMSLogger", "Message text: " + messageBody);
						
						// TODO: Decide what data to pull out of the SMS message (time, phone number, anything else?)
						// TODO: One-way hash the phone number
						// TODO: Write the data to the text-message log file
					}
				}
				catch (Exception e) {
					Log.i("SMSLogger", "SMS_RECEIVED Caught exception: " + e.getMessage());
				}
			}
		}
	}

}
