package org.futto.app.ui.user;

import org.futto.app.R;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Call Clinician, and Sign out.
 * @author Dor Samet */
public class MainMenuActivity extends SessionActivity {
	//extends a SessionActivity
	TextView username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		displayUsername();
		//Button callClinicianButton = (Button) findViewById(R.id.main_menu_call_clinician);
		//callClinicianButton.setText(PersistentData.getCallClinicianButtonText());
	}

	private void displayUsername(){
		username = (TextView) findViewById(R.id.username_name);
		String user = PersistentData.getPatientID();
		username.setText(user);
	}

	/*#########################################################################
	############################## Buttons ####################################
	#########################################################################*/
	
//	public void graphResults (View v) { startActivity( new Intent(getApplicationContext(), GraphActivity.class) ); }
}
