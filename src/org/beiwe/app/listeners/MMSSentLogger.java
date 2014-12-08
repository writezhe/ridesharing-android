package org.beiwe.app.listeners;

import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class MMSSentLogger extends ContentObserver{
	
	Context appContext = null;
	
	public MMSSentLogger (Handler handler, Context context ) {
		super(handler);
		this.appContext = context;
	}
	
	@Override
	public void onChange(boolean selfChange){
		super.onChange(selfChange);

//		Cursor cursor = appContext.getContentResolver().query( Uri.parse("content://mms"), null, "msg_box = 4", null, "_id");
		Cursor mmsCursor = appContext.getContentResolver().query( Uri.parse("content://mms/"), null, null, null, "date DESC");
		Cursor smsCursor = appContext.getContentResolver().query( Uri.parse("content://mms-sms/conversations?simple=true"), null, null, null, "date");
		
		if ( !check_and_position_cursor(mmsCursor) || !check_and_position_cursor(smsCursor) ){ return; }
		
		if ( !smsCursor.getString(smsCursor.getColumnIndex( "transport_type" )).equals("mms") ) {
//			Log.i("MMS stuff", "that was not an mms, ignoring.!");
			return;
		}
		
		long date = mmsCursor.getInt( mmsCursor.getColumnIndex("date") ) * 1000L;
		String recipient = smsCursor.getString( smsCursor.getColumnIndex("recipient_address") );
		
		String[] recipients = recipient.split(";");
		for (String number : recipients) {
			String ident = EncryptionEngine.hashPhoneNumber(number);
			String write_to_file = date + TextFileManager.DELIMITER + ident + TextFileManager.DELIMITER + "sent" + TextFileManager.DELIMITER + "MMS";
			Log.i("mms", write_to_file);
			TextFileManager.getTextsLogFile().writeEncrypted(write_to_file);
		}
//		String message = getMmsText( "" + mmsCursor.getInt( mmsCursor.getColumnIndex("_id")) );
//		Log.e("MMS message", "message length = " + message.length() );
//		Log.e("MMS message", "message = " + message);
//		
//		Log.e("MMS stuff", "MMS:");
//		print_things_from_valid_sources(mmsCursor);
//		Log.e("MMS stuff", "SMS:");
//		print_things_from_valid_sources(smsCursor);
		
	}
	
	public boolean check_and_position_cursor(Cursor cursor){
		if (cursor == null || cursor.getCount() < 1) {
//			Log.e("MMS validity check","invalid cursor");
			return false; }
		
		cursor.moveToFirst();
//		Log.i("MMS COUNT", "Count: " + cursor.getCount());
	
		if (cursor.getInt( cursor.getColumnIndex("msg_box") ) != 2) {
//			Log.e("MMS DUMP", "NOPE! :D");
			return false; }
		return true;
	}
	
//	public void print_things_from_valid_sources( Cursor cursor ){
//		
//		for (String column : cursor.getColumnNames() ) {
//			int index = cursor.getColumnIndex(column);
//			try {
//				switch ( cursor.getType( index ) ) {
//
//				case android.database.Cursor.FIELD_TYPE_BLOB:
//					Log.i("MMS DUMP", "name: " + column + ", index: " + index + ", type: blob - " + new String(cursor.getBlob(index) ) );
//					break;
//				case android.database.Cursor.FIELD_TYPE_FLOAT:
//					Log.i("MMS DUMP", "name: " + column + ", index: " + index + ", type: float, " + cursor.getFloat(index) );
//					break;
//				case android.database.Cursor.FIELD_TYPE_INTEGER:
//					Log.i("MMS DUMP", "name: " + column + ", index: " + index + ", type: int, " + cursor.getInt(index) );
//					break;
//				case android.database.Cursor.FIELD_TYPE_NULL:
//					Log.i("MMS DUMP", "name: " + column + ", index: " + index + ", type: null" );
//					break;
//				case android.database.Cursor.FIELD_TYPE_STRING:
//					Log.i("MMS DUMP", "name: " + column + ", index: " + index + ", type: string - " + cursor.getString(index) );
//					break;
//				default:
//					Log.i("MMS DUMP", "name: " + column + ", index: " + index + " did not match a type.");
//				}
//			}
//			catch (Exception e) { 
//				Log.e("MMS CRAP?", "SOMETHING BAD HAPPEND!");
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	private String getMmsText(String mmsid) {
//	    InputStream inputStream = null;
//	    StringBuilder builder = new StringBuilder();
//	    try {
//	        inputStream = appContext.getContentResolver().openInputStream( Uri.parse("content://mms/part/" + mmsid) );
//	        if (inputStream != null) { 
//	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8") );
//	            String temp = reader.readLine();
//	            while (temp != null) {
//	                builder.append(temp);
//	                temp = reader.readLine();
//	            }
//	        }
//	    } catch (IOException e) {}
//	    finally {
//	        if (inputStream != null) { try { inputStream.close(); }
//	            catch (IOException e) {}
//	        }
//	    }
//	    return builder.toString();
//	}
	
	
}
