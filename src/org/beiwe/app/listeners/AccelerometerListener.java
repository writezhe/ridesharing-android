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
	public static String header = "timestamp,accuracy,x,y,z";
	
	private SensorManager accelSensorManager;
	private Sensor accelSensor;
	private TextFileManager accelFile = null;
	
	private Context appContext;
	private PackageManager pkgManager;
	
	private Boolean exists = null;
	private Boolean enabled = null;
	
	private String accuracy;
	
	public Boolean check_status(){ 
		if (exists) return enabled;
		return false; }
	
	/**Listens for accelerometer updates.  NOT activated on instantiation.
	 * Use the turn_on() function to log any accelerometer updates to the 
	 * accelerometer log.
	 * @param applicationContext a Context from an activity or service. */
	public AccelerometerListener(Context applicationContext){
		this.appContext = applicationContext;
		this.pkgManager = appContext.getPackageManager();
		this.accelFile = TextFileManager.getAccelFile();
		this.accuracy = "unknown";
		this.exists = pkgManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
		
		if (this.exists) {
			enabled = false;
			this.accelSensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
			this.accelSensor = accelSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			if (accelSensorManager == null ){ 
//				Log.e("accelerometer does not exist?", " !!!!!!!!!!!!!!!!!! " );
				exists = false;	}
	} }
	
	/** Use the public toggle() function to enable/disable */ 
	public synchronized void turn_on() {
		accelSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
		enabled = true;	}
	/** Use the public toggle() function to enable/disable */
	public synchronized void turn_off(){
		accelSensorManager.unregisterListener(this);
		enabled = false; }
	
	/** If the accelerometer exists, toggle its state.
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
	
	/** Update the accuracy, synchronized so very closely timed trigger events do not overlap.
	 * (only triggered by the system.) */
	@Override
	public synchronized void onAccuracyChanged(Sensor arg0, int arg1) {	accuracy = "" + arg1; }
	
	/** On receipt of a sensor change, record it.  Include accuracy. 
	 * (only ever triggered by the system.) */
	@Override
	public synchronized void onSensorChanged(SensorEvent arg0) {
		Long javaTimeCode = System.currentTimeMillis();
		float[] values = arg0.values;
		String data = javaTimeCode.toString() + ',' + accuracy + ',' + values[0] + ',' + values[1] + ',' + values[2];
		
		accelFile.writeEncrypted(data);
	}
}
