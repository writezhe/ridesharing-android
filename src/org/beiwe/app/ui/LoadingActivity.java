package org.beiwe.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.beiwe.app.BackgroundService;
import org.beiwe.app.R;
import org.beiwe.app.RunningBackgroundProcessActivity;
import org.beiwe.app.BackgroundService.BackgroundProcessBinder;
import org.beiwe.app.R.layout;
import org.beiwe.app.R.string;
import org.beiwe.app.storage.EncryptionEngine;
import org.beiwe.app.storage.PersistentData;
import org.beiwe.app.ui.registration.RegisterActivity;
import org.beiwe.app.ui.utils.AlertsManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**The LoadingActivity is a temporary RunningBackgroundProcessActivity (Not a SessionActivity,
 * check out those classes if you are confused) that pops up when the user opens the app.
 * This activity runs some simple checks to make sure that the device can actually run the app,
 * and then bumps the user to the correct screen (Register or MainMenu).
 * 
 * note: this cannot be a SessionActvity, doing so would cause it to instantiate itself infinitely when a user is logged out. 
 * @author Eli Jones, Dor Samet */

public class LoadingActivity extends RunningBackgroundProcessActivity {
	
	//swap the commented line below to enable/disable the debuginterface
	@SuppressWarnings("rawtypes")
	public static Class loadThisActivity = DebugInterfaceActivity.class;
//	public static Class loadThisActivity = MainMenuActivity.class;
	
	protected BackgroundService backgroundProcess;
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
				
		if ( testHashing() ) {
			Intent startingIntent = new Intent(this.getApplicationContext(), BackgroundService.class);
			startingIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
			startService(startingIntent);
			bindService( startingIntent, backgroundProcessConnection, Context.BIND_AUTO_CREATE);
		}
		else { failureExit(); }
		
		/* In order to have additional compatibility tests we need to guarantee that the background service is already running,
		this is complex, and all compatibility documentation for android indicates that the encryption we use are implemented for all 
		versions of android.  is currently a very low priority problem.  The following lines of code tends to crash the app.
		LoginManager.initialize( getApplicationContext() ); // probably fixed when we moved the LoginManager initialization to an earlier point in the activity cycle.
		if ( !testEncryption( ) ) { failureExit(); } //probably still broken. */
	}
	

	/**CHecks whether device is registered, sends user to the correct screen. */
	private void loadingSequence() {		
		//if the device is not registered, push the user to the register activity
		if ( !PersistentData.isRegistered() ){ startActivity(new Intent(this, RegisterActivity.class) ); }
		//if device is registered push user to the main menu.
		else { startActivity(new Intent(this, loadThisActivity) ); }
		unbindService(backgroundProcessConnection);
		finish(); //destroy the loading screen
	}
	
	
	/*##################################################################################
	############################### Testing Function ###################################
	##################################################################################*/
	
	/**Tests whether the device can run the hash algorithm the app requires
	 * @return boolean of whether hashing works */
	private Boolean testHashing() {
		// Runs the unsafe hashing function and catches errors, if it catches errors.
		try { EncryptionEngine.unsafeHash("input"); }
		catch (NoSuchAlgorithmException noSuchAlgorithm) { return false; }
		catch (UnsupportedEncodingException unSupportedEncoding) { return false; }
		return true;
	}
	
//	/** Returns true if the device cannot use the necessary encryption.
//	 * I apologize for the double negative, but the boolean logic is stupid if .*/
//	private boolean testEncryption() {
//		if ( LoginManager.isRegistered() ) {	
//			byte[] testKey = EncryptionEngine.newAESKey();
//			try { EncryptionEngine.encryptAES("test", testKey); }
//			catch (InvalidKeyException e) { return false; } //device does not support aes
//			catch (InvalidKeySpecException e) { return false; } //device does not support rsa
//		}
//		return true;
//	}
	

	/**Displays error, then exit.*/
	private void failureExit() {
		AlertsManager.showErrorAlert( getString( R.string.invalid_device), this, 1);
	}
}