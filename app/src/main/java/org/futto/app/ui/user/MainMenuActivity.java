package org.futto.app.ui.user;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.futto.app.R;
import org.futto.app.fcm.FuttoFirebaseMessageService;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;

/**The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Call Clinician, and Sign out.
 * @author Dor Samet */
public class MainMenuActivity extends SessionActivity {
	//extends a SessionActivity
	TextView username;
	TextView notification;
	ViewGroup route;
	private DrawerLayout drawerLayout ;
	private Toolbar toolbar;
	private NavigationView nvDrawer;
	private ActionBarDrawerToggle drawerToggle;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		displayUsername();

		// Setup drawer view
		displayToobar();
//
//		// Find our drawer view
//
		drawerLayout = (DrawerLayout) findViewById(R.id.main_DrawerLayout);
		nvDrawer = (NavigationView) findViewById(R.id.nvView);
		drawerToggle = setupDrawerToggle();


		drawerLayout.addDrawerListener(drawerToggle);

		setupDrawerContent(nvDrawer);
		setUpNotification();

		//Button callClinicianButton = (Button) findViewById(R.id.main_menu_call_clinician);
		//callClinicianButton.setText(PersistentData.getCallClinicianButtonText());
	}

	private void setUpNotification() {
		notification = (TextView) findViewById(R.id.noti_content);
		if(FuttoFirebaseMessageService.checkNotificationExit()){
//		    String title = getIntent().getStringExtra("title");
			String title = FuttoFirebaseMessageService.getNoteTitle();
		    notification.setText(FuttoFirebaseMessageService.getNoteTitle());
		}else{
			notification.setText("You don't have new message now.");
		}
	}

	private ActionBarDrawerToggle setupDrawerToggle() {
		return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		drawerToggle.onConfigurationChanged(newConfig);
	}
	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						selectDrawerItem(menuItem);
						return true;
					}
				});
	}

	public void selectDrawerItem(MenuItem menuItem) {
		// Create a new fragment and specify the fragment to show based on nav item clicked

		switch(menuItem.getItemId()) {
			case R.id.nav_first_fragment:
				StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
				break;
			case R.id.nav_second_fragment:
				StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
				break;
			case R.id.nav_third_fragment:
				StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
				break;
				default:
					StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
					break;

		}


		// Highlight the selected item has been done by NavigationView
		menuItem.setChecked(true);
		// Set action bar title
		setTitle(menuItem.getTitle());
		// Close the navigation drawer
		drawerLayout.closeDrawers();
	}



	private void displayToobar() {
		toolbar = findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar);
		toolbar.setTitleTextColor(Color.WHITE);
		getSupportActionBar().setTitle("Futto Main Menu");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);

	}



	private void displayUsername(){
		username = findViewById(R.id.username_name);
//		usernameNav = findViewById(R.id.nav_header_username);
		String user = PersistentData.getPatientID();
		username.setText(user);
//		usernameNav.setText(user);
	}


	/*#########################################################################
	############################## Buttons ####################################
	#########################################################################*/
	
//	public void graphResults (View v) { startActivity( new Intent(getApplicationContext(), GraphActivity.class) ); }
}
