package org.beiwe.app;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.BackgroundProcess.BackgroundProcessBinder;
import org.beiwe.app.session.LoginManager;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.ui.AlertsManager;
import org.beiwe.app.ui.MainMenuActivity;
import org.beiwe.app.ui.RegisterActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

//TODO: Eli. Update doc.
/**This is a gateway activity - the point of this activity is to navigate in between the three
 * starting activities.
 * 
 * Right now all it does is to call on checkLogin, which is the actual transfer mechanism.
 * 
 * This activity is also designed for splash screens.
 * @author Eli Jones, Dor Samet */

public class LoadingActivity extends RunningBackgroundProcessActivity {
	
	//swap the commented line below to enable/disable the debuginterface
	public static Class loadThisActivity = DebugInterfaceActivity.class;
//	public static Class loadThisActivity = MainMenuActivity.class;
	
	protected BackgroundProcess backgroundProcess;
	protected boolean isBound = false;
	
	/**The ServiceConnection Class is our trigger for events that rely on the BackgroundService */
	protected ServiceConnection backgroundProcessConnection = new ServiceConnection() {
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder binder) {
	        Log.d("loading ServiceConnection", "Background Process Connected");
	        BackgroundProcessBinder some_binder = (BackgroundProcessBinder) binder;
	        backgroundProcess = some_binder.getService();
	        isBound = true;
	        loadingSequence();
	    }

	    @Override
	    public void onServiceDisconnected(ComponentName name) {
	        Log.d("loading ServiceConnection", "Background Process Disconnected");
	        backgroundProcess = null;
	        isBound = false;
	    }
	};
	
	
	/**onCreate - right now it just calls on checkLogin() in SessionManager, and moves the activity
	 * to the appropriate page. In the future it could hold a splash screen before redirecting activity. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		
		if ( isAbleToHash() ) {
			bindService( new Intent( this.getApplicationContext(), BackgroundProcess.class), backgroundProcessConnection, Context.BIND_AUTO_CREATE);
			startService(new Intent(this.getApplicationContext(), BackgroundProcess.class));
		}
		else failureExit();
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(backgroundProcessConnection);
	}


	private void loadingSequence() {		
		//if the device is not registered, push the user to the register activity
		if ( !LoginManager.isRegistered() ){ startActivity(new Intent(this, RegisterActivity.class) ); }
		//if device is registered push user to the main menu.
//		else { startActivity(new Intent(this, MainMenuActivity.class) ); }
		else { startActivity(new Intent(this, loadThisActivity) ); } 
		finish(); //destroy the loading screen
	}
	
	
	/**Tests whether the device can run the hash algorithm the app requires @return */
	private boolean isAbleToHash() {
		// Runs the unsafe hashing function and catches errors, if it catches errors.
		try {
			EncryptionEngine.unsafeHash("input");
			return true; }
		catch (NoSuchAlgorithmException noSuchAlgorithm) { failureExit(); }
		catch (UnsupportedEncodingException unSupportedEncoding) { failureExit(); }
		return false;
	}

	
	private void failureExit() {
		AlertsManager.showErrorAlert( getString( R.string.invalid_device), this);
		System.exit(1);
	}
}