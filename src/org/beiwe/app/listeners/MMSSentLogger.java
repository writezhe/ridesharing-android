package org.beiwe.app.listeners;

import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**MMSSentLogger listens for outgoing MMSes.
 * In order to catch outgoing MMSes we need to monitor the texts database using a ContentObserver
 * and then make a database query based on the 
 * 
 * @author Eli */

public class MMSSentLogger extends ContentObserver{
	
	Context appContext = null;
	
	/** ContentObservers require a Handler object for instantiation,
	 * and our future logic will require a Context.
	 * @param handler
	 * @param context */
	public MMSSentLogger (Handler handler, Context context ) {
		super(handler);
		this.appContext = context;
	}
	
	@Override
	public void onChange(boolean selfChange){
		super.onChange(selfChange);

//		Cursor cursor = appContext.getContentResolver().query( Uri.parse("content://mms"), null, "msg_box = 4", null, "_id"); //this does not appear to work.
		Cursor mmsCursor = appContext.getContentResolver().query( Uri.parse("content://mms/"), null, null, null, "date DESC");
		Cursor smsCursor = appContext.getContentResolver().query( Uri.parse("content://mms-sms/conversations?simple=true"), null, null, null, "date");
		
		//test the MMS and SMS cursors for basic validity, move them into position.  If anything is invalid, exit.
		if ( !checkAndPositionCursor(mmsCursor) || !checkAndPositionCursor(smsCursor) ){ return; }
		//test that the data in these cursors are useful, if not, exit.
		if ( !checkValidData(mmsCursor) || !checkValidData(smsCursor) ) { return; }
		
		//we only care about MMSes in this code, so if we received an SMS change, we exit here.
		if ( !smsCursor.getString(smsCursor.getColumnIndex( "transport_type" )).equals("mms") ) { return; }
		
		long date = mmsCursor.getInt( mmsCursor.getColumnIndex("date") ) * 1000L;
		String recipient = smsCursor.getString( smsCursor.getColumnIndex("recipient_address") );
		
		String[] recipients = recipient.split(";");
		for (String number : recipients) {
			String ident = EncryptionEngine.hashPhoneNumber(number);
			String write_to_file = date + TextFileManager.DELIMITER + ident + TextFileManager.DELIMITER + "sent" + TextFileManager.DELIMITER + "MMS";
			Log.i("mms", write_to_file);
			TextFileManager.getTextsLogFile().writeEncrypted(write_to_file);
		}
		
		//verbose logging code
//		String message = getMmsText( "" + mmsCursor.getInt( mmsCursor.getColumnIndex("_id")) );
//		Log.e("MMS message", "message length = " + message.length() );
//		Log.e("MMS message", "message = " + message);
//		
//		Log.e("MMS stuff", "MMS:");
//		print_things_from_valid_sources(mmsCursor);
//		Log.e("MMS stuff", "SMS:");
//		print_things_from_valid_sources(smsCursor);
		
	}
	
	/**Checks for basic validity of our database cursors, moves database cursor to correct location. 
	 * @param cursor
	 * @return returns false if cursor is invalid */
	public boolean checkAndPositionCursor(Cursor cursor){
		if (cursor == null || cursor.getCount() < 1) {
//			Log.e("MMS validity check failed","invalid cursor");
			return false; }
		
		//move cursor to most recent update (this should hold true, but databases can have concurrent access etc.)
		cursor.moveToFirst();
//		Log.i("MMS COUNT", "Count: " + cursor.getCount())
		return true;
	}

	/** Checks for data validity */
	public boolean checkValidData(Cursor cursor){
		//if the message box is not box number 2 (sent) then this was one of those many,
		// many, extra, dumb, onChange triggers, and we return false.
		if (cursor.getInt( cursor.getColumnIndex("msg_box") ) != 2 ) { return false; }
		return true;
	}
	
	//The following is the code that was written to work out what the data coming in meant.
	// Since this is probably going to be useful to someone in the future, keep it.
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
