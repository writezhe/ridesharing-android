package org.beiwe.app.listeners;

import org.beiwe.app.BackgroundProcess;
import org.beiwe.app.Timer;
import org.beiwe.app.ui.LoginSessionManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SignoutListener extends BroadcastReceiver {

	private BackgroundProcess backgroundProcess = null;
	private LoginSessionManager sessionManager = null;
	private Context appContext = null;
	
	public SignoutListener(BackgroundProcess backgroundProcess) {
		this.backgroundProcess = backgroundProcess;
		appContext = this.backgroundProcess.getApplicationContext();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Timer.SIGN_OUT) ){
			Toast.makeText(appContext, "Received signout", Toast.LENGTH_SHORT).show();
			Log.i("Signout Listener", "Received Signout Message");
			
			sessionManager = new LoginSessionManager(appContext);
			if(sessionManager.isLoggedIn()) {
				sessionManager.logoutUser(); } } }
	
}
