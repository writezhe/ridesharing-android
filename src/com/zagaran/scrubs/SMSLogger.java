package com.zagaran.scrubs;

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


public class SMSLogger extends BroadcastReceiver {

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
					}
				}
				catch (Exception e) {
					Log.i("SMSLogger", "SMS_RECEIVED Caught exception: " + e.getMessage());
				}
			}
		}
	}

}
