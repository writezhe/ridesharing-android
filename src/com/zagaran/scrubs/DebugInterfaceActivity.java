package com.zagaran.scrubs;
 
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.zagaran.scrubs.FileManager;

public class DebugInterfaceActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_interface);
		
		FileManager.startFileManager(this.getApplicationContext());
		
		startScreenOnOffListener();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.debug_interface, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private void startScreenOnOffListener() {
		final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		final BroadcastReceiver mReceiver = new ScreenOnOffListener();
		registerReceiver(mReceiver, filter);
	}
	
	
	public void printInternalLog(View view) {
		Log.i("DebugInterfaceActivity", "'Print Internal Log' button was pressed!");
		// TODO: make this function write to something (Logcat or the app's screen)
	}


	public void clearInternalLog(View view) {
		Log.i("DebugInterfaceActivity", "'Clear Internal Log' button was pressed!");
		// TODO: make this function delete the log file
	}
}
