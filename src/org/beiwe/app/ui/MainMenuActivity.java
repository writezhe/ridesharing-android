package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.session.SessionActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Hotline, and Sign out.
 * @author Dor Samet, Eli Jones */
public class MainMenuActivity extends SessionActivity {
	//extends a SessionActivity
	private Context appContext;	 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		appContext = getApplicationContext();
	}
	
	/**Calls... the hotline. */
	public void callHotline(View v) {
		super.callHotline();
	}

	/*#########################################################################
	############################## Buttons ####################################
	#########################################################################*/
	
	public void signOutButton(View v) {
		LoginManager.setLoggedIn(false);
		startActivity( new Intent(appContext, LoginActivity.class) );
	}
	
	public void graphResults (View v) { startActivity( new Intent(appContext, GraphActivity.class) ); }
	public void recordMessage(View v) { startActivity( new Intent(appContext, AudioRecorderActivity.class) );	}
	public void resetPassword(View v) { startActivity( new Intent(appContext, ResetPasswordActivity.class) ); }
}
