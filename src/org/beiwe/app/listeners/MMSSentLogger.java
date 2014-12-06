package org.beiwe.app.listeners;

import java.util.logging.Logger;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class MMSSentLogger extends ContentObserver{
	
	private Handler handler = null;
	Context appContext = null;
	
	public MMSSentLogger (Handler handler, Context context ) {
		super(handler);
		this.appContext = context;
	}
	
	@Override
	public void onChange(boolean selfChange){
		super.onChange(selfChange);
		String message_type = android.provider.Telephony.BaseMmsColumns.MESSAGE_TYPE;

//		Cursor cursor = appContext.getContentResolver().query( Uri.parse("content://mms"), null, "msg_box = 4", null, "_id");
		Cursor cursor = appContext.getContentResolver().query( Uri.parse("content://mms"), null, null, null, "date");
		//we want to grab only message type == MESSAGE_TYPE_SENT, which is 2.
		if (cursor == null) {
			Log.i("MMS 0","null cursor");
			return;
		}
		if (cursor.getCount() < 1){
			Log.i("MMS 0","0 length cursor");
			return;
		}
		cursor.moveToFirst();
		Log.i("MMS 1", "Count: " + cursor.getCount());
		
		String columns = "";
		for (String column : cursor.getColumnNames() ) {
			int index = cursor.getColumnIndex(column);
			Log.i("mmscrap", "index: " + index);
			try {
				
				Log.i("mmscrap", "data\t" + column + ":" + index + ", " + cursor.getType( index )  + "\n");
			}
			catch (Exception e) { 
				Log.e("mms stuff", "bad index: " + index);
				e.printStackTrace();
			}
		}
		Log.i("mms 2", "column names: " + columns );

//		String id = cursor.getString(cursor.getColumnIndex("_id") );
//		String date = cursor.getString (cursor.getColumnIndex ("date") );
		
//		int id = cursor.getColumnIndex("_id") ;
//		int date = cursor.getColumnIndex ("date") ;
//		
//		int address = cursor.getColumnIndex("address");
//		byte[] data = cursor.getBlob(address);
//		Log.i("mms 4", "id: " + id + ", date: " + date + ", address: " + address); // + ", data: " + new String(data) );
		
		
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
