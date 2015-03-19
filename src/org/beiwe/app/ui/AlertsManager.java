package org.beiwe.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * This is a class that holds the function to show alerts. In case we want to use other alert functionalities,
 * these should be put here.
 * 
 * @author Dor Samet
 *
 */
public class AlertsManager {

	/** 
	 * Pops up an alert with the "message" on the user's "activity" screen.
	 * This alert is designed to have one OK button, can later implement it so that it will have more buttons
	 * 
	 * @param message
	 * @param activity
	 */
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
	
	public static void showErrorAlert(String message, Activity activity, final int alertNumber) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("A critical error occured");
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//this system.exit() operation does not work if the app loads a different activity over the activity that spawned this Alert
				System.exit(alertNumber);
			}
		});		
		builder.create().show();
	}
	
}
