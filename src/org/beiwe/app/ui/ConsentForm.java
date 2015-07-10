package org.beiwe.app.ui;

import org.beiwe.app.LoadingActivity;
import org.beiwe.app.PersistentData;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.QuestionsDownloader;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ConsentForm extends RunningBackgroundProcessActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consent_form);
	}
	
	/** On the press of the do not consent button, we pop up an alert, allowing the user
	 * to press "Cancel" if they did not mean to press the do not consent. */
	public void doNotConsentButton(View view) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConsentForm.this);
		alertBuilder.setTitle("Do Not Consent");
		alertBuilder.setMessage(getString(R.string.doNotConsentAlert));
		alertBuilder.setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				System.exit(0);
			}
		});
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) { return; }} );
		alertBuilder.create().show();
	}
	
	public void consentButton(View view) {
		PersistentData.setRegistered(true);
		PersistentData.loginOrRefreshLogin();

		// Download the survey questions and schedule the surveys
		QuestionsDownloader downloader = new QuestionsDownloader(getApplicationContext());
		downloader.downloadJsonQuestions();

		//This is important.  we need to start timers 
		backgroundProcess.startTimers();

		// Create new data files, these will now have a patientID prepended to those files
		TextFileManager.makeNewFilesForEverything();
		
		// Start the Main Screen Activity, destroy this activity
		startActivity(new Intent(getApplicationContext(), LoadingActivity.loadThisActivity) );
		finish();
	}
}
