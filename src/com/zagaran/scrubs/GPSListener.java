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
	
	private Boolean trueGPS;
	private Boolean networkGPS;
	
	public Boolean enabled;
	
	public GPSListener (Context applicationContext){
		appContext = applicationContext;
				
		GPSFile = CSVFileManager.getGPSFile();
		logFile = CSVFileManager.getDebugLogFile();
		
		pkgManager = appContext.getPackageManager();
		
		trueGPS = pkgManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
		networkGPS = pkgManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
		
		Log.i("location services:", "GPS"+trueGPS.toString()+ " Network:"+networkGPS);
		
		
		enabled = false;
		
		try { locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE); }
		catch (SecurityException e) {
			Log.i("the LocationManager failed to initiate, SecurityException, see stack trace.", "");
			e.printStackTrace(); }
		
		Log.i("LocationListener instatiated", "");
	}
	
	public synchronized void turn_on(){
		if ( enabled ) { return; }
		//TODO: does this crash if you request location updates when it is already requested
		// parameters: provider, minTime, minDistance, listener);
		if ( trueGPS ) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); }
		if ( networkGPS ) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); }
		enabled = true;
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
		logFile.write(data);
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
