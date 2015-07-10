package org.beiwe.app.ui.user;

import org.beiwe.app.R;
import org.beiwe.app.session.SessionActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Call Clinician, and Sign out.
 * @author Dor Samet */
public class MainMenuActivity extends SessionActivity {
	//extends a SessionActivity
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
	}
	
	/*#########################################################################
	############################## Buttons ####################################
	#########################################################################*/
	
	public void graphResults (View v) { startActivity( new Intent(getApplicationContext(), GraphActivity.class) ); }
}
