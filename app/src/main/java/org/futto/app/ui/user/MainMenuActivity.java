package org.futto.app.ui.user;

import org.futto.app.R;
import org.futto.app.session.SessionActivity;

import android.os.Bundle;

/**The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Call Clinician, and Sign out.
 * @author Dor Samet */
public class MainMenuActivity extends SessionActivity {
	//extends a SessionActivity
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		
		//Button callClinicianButton = (Button) findViewById(R.id.main_menu_call_clinician);
		//callClinicianButton.setText(PersistentData.getCallClinicianButtonText());
	}
	
	/*#########################################################################
	############################## Buttons ####################################
	#########################################################################*/
	
//	public void graphResults (View v) { startActivity( new Intent(getApplicationContext(), GraphActivity.class) ); }
}
