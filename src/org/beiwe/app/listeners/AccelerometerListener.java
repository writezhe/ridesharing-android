package org.beiwe.app.listeners;

import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AccelerometerListener implements SensorEventListener{
	public static String header = "timestamp, x, y, z\n";
	
	private SensorManager accelSensorManager;
	private Sensor accelSensor;
	
	private TextFileManager accelFile = null;
	private TextFileManager logFile = null;
	
	private Context appContext;
	private PackageManager pkgManager;
	
	private Boolean exists = null;
	private Boolean enabled = null;
	
	public Boolean check_status(){ 
		if (exists) return enabled;
		return false; }
	
	public AccelerometerListener(Context applicationContext){
		/** Listens for accelerometer updates.  Not activated on instantiation.  Requires an 
		 * application Context object be passed in in order to interface device sensors.
		 * When activated using the turn_on() function it will log any accelerometer updates to the 
		 * accelerometer log. */
		this.appContext = applicationContext;
		this.pkgManager = appContext.getPackageManager();
		this.logFile = TextFileManager.getDebugLogFile();
		this.accelFile = TextFileManager.getAccelFile();
		
		this.exists = pkgManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
		
		if (this.exists) {
			enabled = false;
			this.accelSensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
			this.accelSensor = accelSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			if (accelSensorManager == null ){
				Log.i("accelerometer does not exist??", " !!!!!!!!!!!!!!!!!! " );
				exists = false;	}
		}
	}
	
	
	private synchronized void turn_on() {
		accelSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
		enabled = true;	}
	
	private synchronized void turn_off(){
		accelSensorManager.unregisterListener(this);
		enabled = false; }
	
	/** If the accelerometer exists, toggle its state
	 * @return Boolean.  True means it has been turned on, false means it has been turned off. */
	public synchronized Boolean toggle(){
		if ( !this.exists ){ return false; }
		else {
			if (enabled) {
				this.turn_off();
				return enabled; }	
			else {
				this.turn_on();
				return enabled; } }
	}
	
	//NOTE: for the onAccuracyChanged and onSensorChanged we are adding the synchronized keyword.
	// It may still be possible to have these trigger so rapidly that order is not preserved
	// (we have no data on this at all), so the functions need to check current state.
	
	@Override
	public synchronized void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.i("OH GOD THE ACCELEROMETER HAD AN ACCURACY CHANGE:", arg0.toString() + "\nint value:" + arg1);
		
		//Interesting: recieved an onacceracychange event thing with an int value of 3, and then the app crashed.
		// immediately reran the app (no code changes) and it ran/runs fine.  wtf.
	}

	@Override
	public synchronized void onSensorChanged(SensorEvent arg0) {
		float[] values = arg0.values;
		
		String data = "" + arg0.timestamp + ',' + values[0] + ',' + values[1] + ',' + values[2] + '\n';
//		accelFile.write(data);
		logFile.write("accel: " + data);
		//All values are in SI units (m/s^2) 
		//values[0]: Acceleration minus Gx on the x-axis 
		//values[1]: Acceleration minus Gy on the y-axis 
		//values[2]: Acceleration minus Gz on the z-axis
		//note on time: the accelerometer returns time with millisecond precision on a nexus 7 tablet.
	}
}
