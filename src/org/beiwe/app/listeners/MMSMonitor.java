package org.beiwe.app.listeners;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class MMSMonitor extends ContentObserver{
	
	private Handler handler = null;
	Context appContext = null;
	
	public MMSMonitor (Handler handler, Context context ) {
		super(handler);
		this.appContext = context;
	}
	
	@Override
	public void onChange(boolean selfChange){
		super.onChange(selfChange);
		
		Cursor cursor = appContext.getContentResolver().query( Uri.parse("content://mms"), null, "msg_box = 4", null, "_id");
		if (cursor != null && cursor.getCount() > 0) {
            int mmsCount = cursor.getCount();
            Log.i("MMSes log 1", "MMSMonitor :: Init MMSCount ==" + mmsCount);
         }
		
	}
//	      try {
//	         monitorStatus = false;
//	         if (!monitorStatus) {
//	            contentResolver.registerContentObserver(Uri.parse("content://mms-sms"), true, mmsObserver);
//
//	            Uri uriMMSURI = Uri.parse("content://mms");
//	            Cursor mmsCur = mainActivity.getContentResolver().query(uriMMSURI, null, "msg_box = 4", null, "_id");
//	            if (mmsCur != null && mmsCur.getCount() > 0) {
//	               mmsCount = mmsCur.getCount();
//	               Log("", "MMSMonitor :: Init MMSCount ==" + mmsCount);
//	            }
//	         }
//	      } catch (Exception e) {
//	         Log("", "MMSMonitor :: startMMSMonitoring Exception== "+ e.getMessage());
//	      }
//	   }
}
