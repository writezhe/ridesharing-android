package org.beiwe.app;

import org.beiwe.app.BackgroundService.BackgroundServiceBinder;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.ui.user.AboutActivityLoggedOut;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**All Activities in the app extend this Activity.  It ensures that the app's key services (i.e.
 * BackgroundService, LoginManager, PostRequest, DeviceInfo, and WifiListener) are running before
 * the interface tries to interact with any of those.
 * 
 * Activities that require the user to be logged in (SurveyActivity, GraphActivity, 
 * AudioRecorderActivity, etc.) extend SessionActivity, which extends this.
 * Activities that do not require the user to be logged in (the login, registration, and password-
 * reset Activities) extend this activity directly.
 * Therefore all Activities have this Activity's functionality (binding the BackgroundService), but
 * the login-protected Activities have additional functionality that forces the user to log in. 
 * 
 * @author Eli Jones, Josh Zagorsky
 */
public class RunningBackgroundServiceActivity extends Activity {
	/** The backgroundService variable is an Activity's connection to the ... BackgroundService.
	 * We ensure the BackgroundService is running in the onResume call, and functionality that
	 * relies on the BackgroundService is always tied to UI elements, reducing the chance of
	 * a null backgroundService variable to essentially zero. */
	protected BackgroundService backgroundService;

	//an unused variable for tracking whether the background Service is connected, uncomment if we ever need that.
//	protected boolean isBound = false;
	
	/**The ServiceConnection Class is our trigger for events that rely on the BackgroundService */
	protected ServiceConnection backgroundServiceConnection = new ServiceConnection() {
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder binder) {
	        // Log.d("ServiceConnection", "Background Service Connected");
	        BackgroundServiceBinder some_binder = (BackgroundServiceBinder) binder;
	        backgroundService = some_binder.getService();
	        doBackgroundDependantTasks();
//	        isBound = true;
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) {
	        Log.w("ServiceConnection", "Background Service Disconnected");
	        backgroundService = null;
//	        isBound = false;
	    }
	};
	
	@Override
	protected void onCreate(Bundle bundle){ 
		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
		PersistentData.initialize(getApplicationContext());
	}
	
	/** Override this function to do tasks on creation, but only after the background Service has been initialized. */
	protected void doBackgroundDependantTasks() { /*Log.d("RunningBackgroundServiceActivity", "doBackgroundDependantTasks ran as default (do nothing)");*/ }
	
	@Override
	/**On creation of RunningBackgroundServiceActivity we guarantee that the BackgroundService is
	 * actually running, we then bind to it so we can access program resources. */
	protected void onResume() {
		super.onResume();
		
		Intent startingIntent = new Intent(this.getApplicationContext(), BackgroundService.class);
		startingIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		startService(startingIntent);
        bindService( startingIntent, backgroundServiceConnection, Context.BIND_AUTO_CREATE);
	}


	@Override
	/** disconnect BackgroundServiceConnection when the Activity closes, otherwise we have a
	 * memory leak warning (and probably an actual memory leak, too). */
	protected void onPause() {
		super.onPause();
		unbindService(backgroundServiceConnection);
	}
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/
	
	@Override
	/** Common UI element, the menu button.*/
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logged_out_menu, menu);
		menu.findItem(R.id.menu_call_clinician).setTitle(PersistentData.getCallClinicianButtonText());
		return true;
	}

	
	@Override
	/** Common UI element, items in menu.*/
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_about:
			startActivity(new Intent(getApplicationContext(), AboutActivityLoggedOut.class));
			return true;
		case R.id.menu_call_clinician:
			callClinician(null);
			return true;
		case R.id.menu_call_research_assistant:
			callResearchAssistant(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	/** sends user to phone, calls the user's clinician. */
	public void callClinician(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = PersistentData.getPrimaryCareNumber();
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
		//FIXME: Eli. Need to test this permission.
	    startActivity(callIntent);
	}
	
	/** sends user to phone, calls the study's research assistant. */
	public void callResearchAssistant(View v) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = PersistentData.getPasswordResetNumber();
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
	    startActivity(callIntent);
	}
}