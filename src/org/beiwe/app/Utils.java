package org.beiwe.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Utils {

	public static void showAlert(String message, Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Alert");
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Nothing!
			}
		});		
		builder.create().show();
	}
	
}
