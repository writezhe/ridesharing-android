package org.beiwe.app.session;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.BackgroundProcess.BackgroundProcessBinder;
import org.beiwe.app.R;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.ResetPasswordActivity;

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

public class SessionActivity extends Activity {
	
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
	        BackgroundProcess.resetAutomaticLogoutCountdownTimer();
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) {
	        Log.d("ServiceConnection", "Background Process Disconnected");
	        backgroundProcess = null;
	        isBound = false;
	    }
	};
	
	
	/*####################################################################
	########################## Log-in Logic ##############################
	####################################################################*/
	
//	@Override 
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//	}
	
	/**onResume() is always called when the activity opens.
	 * If the Activity gets created, onCreate gets called, and then onResume.
	 * If the Activity was already created but was paused, onResume gets called.
	 * 
	 * in onResume() we make bind the activity to the BackgroundProcess.
	 * Binding to the BackgroundProcess ensures that it is running, and that all
	 * functionality that requires .initialize() calls are made. 
	 * 
	 * The resetAutomaticLogoutCountdownTimer call is made as soon as we connect 
	 * to the BackgroundProcess. */
	@Override
	protected void onResume() {
		super.onResume();
		authenticateAndLoginIfNecessary();
		bindService( new Intent( this.getApplicationContext(), BackgroundProcess.class), backgroundProcessConnection, Context.BIND_AUTO_CREATE);
	}
	

	protected void tryToStartAutomaticLogoutCountdownTimer(){
		
	}
	

	protected void authenticateAndLoginIfNecessary() {
		if ( !LoginManager.isLoggedIn() ) {
			startActivity( new Intent(this, LoginActivity.class) );
		}
	}
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.common_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_about:
			// TODO Josh: create an about-the-app page/activity.
			return true;
		case R.id.menu_call_hotline:
			callHotline();
			return true;
		case R.id.menu_change_password:
			startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
			return true;
		case R.id.menu_signout:
			LoginManager.setLoggedIn(false);
			startActivity( new Intent(this, LoginActivity.class) );
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