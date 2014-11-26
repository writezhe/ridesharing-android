package org.beiwe.app;

import org.beiwe.app.BackgroundProcess.BackgroundProcessBinder;
import org.beiwe.app.ui.AboutActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * All Activities in the app extend this Activity.  It ensures that the app's key services (i.e.
 * BackgroundProcess, LoginManager, PostRequest, DeviceInfo, and WifiListener) are running before
 * the interface tries to interact with any of those.
 * 
 * Activities that require the user to be logged in (SurveyActivity, GraphActivity, 
 * AudioRecorderActivity, etc.) extend SessionActivity, which extends this.
 * Activities that do not require the user to be logged in (the login, registration, and password-
 * reset Activities) extend this activity directly.
 * Therefore all Activities have this Activity's functionality (binding the BackgroundProcess), but
 * the login-protected Activities have additional functionality that forces the user to log in. 
 * 
 * @author Eli Jones, Josh Zagorsky
 */
public class RunningBackgroundProcessActivity extends Activity {
	
	/** The backgroundProcess variable is an ActivitySession's connection to the ... BackgroundProcess
	 * We manually ensure the BackgroundProcess is running in the onResume call, but it should trigger
	 * even when that is not the case.
	 * The isBound variable can be used in logic to check whether the BackgroundProcess is running.*/
	protected BackgroundProcess backgroundProcess;
	protected boolean isBound = false;
	
	/**The ServiceConnection Class is our trigger for events that rely on the BackgroundService */
	protected ServiceConnection backgroundProcessConnection = new ServiceConnection() {
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder binder) {
	        Log.d("ServiceConnection", "Background Process Connected");
	        BackgroundProcessBinder some_binder = (BackgroundProcessBinder) binder;
	        backgroundProcess = some_binder.getService();
	        isBound = true;
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) {
	        Log.d("ServiceConnection", "Background Process Disconnected");
	        backgroundProcess = null;
	        isBound = false;
	    }
	};


	@Override
	protected void onResume() {
		super.onResume();
		
		Intent startingIntent = new Intent(this.getApplicationContext(), BackgroundProcess.class);
		startingIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		startService(startingIntent);
        bindService( startingIntent, backgroundProcessConnection, Context.BIND_AUTO_CREATE);
	}


	@Override
	protected void onPause() {
		super.onPause();

		unbindService(backgroundProcessConnection);
	}
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logged_out_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_about:
			startActivity(new Intent(getApplicationContext(), AboutActivity.class));
			return true;
		case R.id.menu_call_hotline:
			callHotline();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	protected void callHotline() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phoneNum = (String) getApplicationContext().getResources().getText(R.string.hotline_phone_number);
	    callIntent.setData(Uri.parse("tel:" + phoneNum));
	    startActivity(callIntent);		
	}
	
}
