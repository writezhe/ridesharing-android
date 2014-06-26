package com.zagaran.scrubs.listeners;

import com.zagaran.scrubs.storage.CSVFileManager;

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
	
	private CSVFileManager accelFile = null;
	private CSVFileManager logFile = null;
	
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
		appContext = applicationContext;
		pkgManager = appContext.getPackageManager();
		logFile = CSVFileManager.getDebugLogFile();
		accelFile = CSVFileManager.getAccelFile();
		
		exists = pkgManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
		
		if (exists) {
			enabled = false;
			accelSensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
			accelSensor = accelSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			if (accelSensorManager == null ){
				//goto fail
				Log.i("accelerometer does not exist??", " !!!!!!!!!!!!!!!!!! " );
				exists = false;
			}
		}
	}
	

	
	//turn_on and turn_off functions:
	//should be idempotent, and when they succeed should return true.
	//should return false (and NOT CRASH) if called on a feature that does not exist on the device.
	public synchronized Boolean turn_on() {
		/** If an accelerometer exists and is not on, turn it on and return true.
		 * If it does not exist or is already on, return false. */
		if (exists & !enabled) {
			accelSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
			enabled = true;
			return true;}
		return false;
	}
	
	public synchronized Boolean turn_off(){
		/** If an accelerometer exists and is not already on, turn it on and return true.
		 * If it is on or does not exist return false. */
		if (exists & enabled){
			accelSensorManager.unregisterListener(this);
			enabled = false;
			return true; }
		return false;
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
