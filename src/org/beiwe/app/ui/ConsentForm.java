package org.beiwe.app.ui;

import org.beiwe.app.LoadingActivity;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.TextFileManager;
import org.beiwe.app.survey.QuestionsDownloader;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ConsentForm extends RunningBackgroundProcessActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_consent_form);
		
	}
	
	public void consentButton(View view) {
		LoginManager.setRegistered(true);
		LoginManager.loginOrRefreshLogin();

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
