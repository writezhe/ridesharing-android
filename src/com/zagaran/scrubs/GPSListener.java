package com.zagaran.scrubs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSListener implements LocationListener {
	
	public static String header = "time, latitude, longitude, altitude, accuracy\n";
	
	private CSVFileManager GPSFile;
	private CSVFileManager logFile;
	
	private Context appContext;
	private PackageManager pkgManager;
	private LocationManager locationManager;
	
	private Boolean trueGPS = null;
	private Boolean networkGPS = null;
	private Boolean enabled = null;
	
	public Boolean check_status(){
		//TODO: make button call this function, debug responses for various location functionality enabled/disabled.
		// (need to implement something for provider changes first.
		if (trueGPS || networkGPS) { return enabled; }
		else { return false; }
	}
	
	public GPSListener (Context applicationContext){
		/** Listens for GPS updates from the network GPS location provider and the true
		 * GPS provider, both if possible.  Not activated on instantiation.  Requires an 
		 * application Context object be passed in in order to interface with location services.
		 * When activated using the turn_on() function it will log any location updates to the 
		 * GPS log. */
		appContext = applicationContext;
		pkgManager = appContext.getPackageManager();
		GPSFile = CSVFileManager.getGPSFile();
		logFile = CSVFileManager.getDebugLogFile();
		
		trueGPS = pkgManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
		networkGPS = pkgManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
		enabled = false;
		Log.i("location services:", "GPS:"+trueGPS.toString()+ " Network:"+networkGPS);
		
		try { locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE); }
		catch (SecurityException e) {
			Log.i("the LocationManager failed to initiate, SecurityException, see stack trace.", "");
			e.printStackTrace(); }
		Log.i("LocationListener instatiated", "_");
	}
	
	//turn_on and turn_off functions:
	//should be idempotent, and when they succeed should return true.
	//should return false (and not crash) if called on a feature that does not work.
	public synchronized Boolean turn_on(){
		//if both DNE, return false.
		if ( !trueGPS & !networkGPS ) { return false; }
		// if already enabled return true
		if ( enabled ) { return true; }
		//for network and true GPS, enable them if their boolean flag is true.
		if ( trueGPS ) {			// parameters: provider, minTime, minDistance, listener);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); }
		if ( networkGPS ) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); }
		//set enabled flag, return
		enabled = true;
		return true;
	}
	
	public synchronized void turn_off(){
		locationManager.removeUpdates(this);
		enabled = false;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		//TODO: Crap, getTime only returns unixtime, milliseconds are rounded out.
		
		//order: time, latitude, longitude, altitude, horizontal_accuracy\n
		String data = location.getTime() + ","
				+ location.getLatitude() + ","
				+ location.getLongitude() + ","
				+ location.getAltitude() + ","
				+ location.getAccuracy() + "\n" ;
		//note, altitude is notoriously inaccurate, getAccuracy only applies to latitude/longitude
//		GPSFile.write(data);
		logFile.write("GPS: " + data);
	}

	// the arg for Provider Enabled and disabled should just be the straight name of the location service...  
	@Override
	public void onProviderDisabled(String arg0) { Log.i("A location provider was disabled.", arg0); }
	@Override
	public void onProviderEnabled(String arg0) { Log.i("A location provider was enabled.", arg0); }
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// arg0 is the name of the provider with a changed status
		// arg1 is the status of the provider. 0=out of service, 1=temporarily unavailable, 2=available
		Log.i("OH GOD WE GOT A STATUSCHANGE FROM THE GPSListener", arg0 + "," + arg1 + arg2 );
	}
}
