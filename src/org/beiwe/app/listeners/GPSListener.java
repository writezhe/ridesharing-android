package org.beiwe.app.listeners;

import org.beiwe.app.storage.TextFileManager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/* Notes/observation on Location Services:
 * We are passing in "0" as the minimum time for location updates to be pushed to us, this results in about
 * 1 update every second.  This is based on logs made using a nexus 7 tablet.
 * This makes sense, GPSs on phones do not Have that kind of granularity/resolution.
 * However, we need the resolution in milliseconds for the line-by-line encryption scheme.
 * So, we grab the system time instead.  This may add a fraction of a second to the timestamp.
 * 
 * There are some features which were considered but not included:
 * We are NOT recording which location provider provided the update, or which location providers
 * are available on a given device.  This was not within scope for the original study, and implementation
 * is not trivial.  
 */

public class GPSListener implements LocationListener {
	
	public static String header = "time, latitude, longitude, altitude, accuracy\n";
	
	private TextFileManager GPSFile;
	private TextFileManager logFile;
	
	private Context appContext;
	private PackageManager pkgManager;
	private LocationManager locationManager;
	
	private Boolean trueGPS = null;
	private Boolean networkGPS = null;
	private Boolean enabled = null;
	//does not have an explicit "exists" boolean.  Use check_status() function, it will return false if there is no GPS. 
	
	public synchronized Boolean check_status(){
		// (need to implement something for provider changes first.
		if (trueGPS || networkGPS) { return enabled; }
		else { return false; }
	}
	
	/** Listens for GPS updates from the network GPS location provider and/or the true
	 * GPS provider, both if possible.  It is NOT activated upon instantiation.  Requires an 
	 * application Context object be passed in in order to interface with location services.
	 * When activated using the turn_on() function it will log any location updates to the GPS log.
	 * @param applicationContext A Context provided an Activity or Service. */
	
	public GPSListener (Context appContext){
		this.appContext = appContext;
		pkgManager = this.appContext.getPackageManager();
		GPSFile = TextFileManager.getGPSFile();
		logFile = TextFileManager.getDebugLogFile();
		
		trueGPS = pkgManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
		networkGPS = pkgManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
		enabled = false;
		Log.i("location services:", "GPS:"+trueGPS.toString()+ " Network:"+networkGPS);
		
		try { locationManager = (LocationManager) this.appContext.getSystemService(Context.LOCATION_SERVICE); }
		catch (SecurityException e) {
			Log.i("the LocationManager failed to initiate, SecurityException, see stack trace.", "");
			e.printStackTrace(); }
		Log.i("LocationListener instatiated", "event...");
	}
	
	/** Turns on GPS providers, provided they are accessible. */
	private synchronized void turn_on(){
		//if both DNE, return false.
		if ( !trueGPS & !networkGPS ) {
			Log.i("GPS", "GPS was told to turn on, but it is not available.");
			return; }
		// if already enabled return true.
		if ( enabled ) {
			Log.i("GPS","GPS was turned on when it was already on.");
			return; }
		//If the feature exists, request locations from it. (enable if their boolean flag is true.)
		if ( trueGPS ) {			// parameters: provider, minTime, minDistance, listener);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); 
			enabled = true; }
		if ( networkGPS ) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); 
			enabled = true; }
	}
	
	private synchronized void turn_off(){
		// pretty confident this cannot fail.
		locationManager.removeUpdates(this);
		enabled = false;
	}
	
	/**Checks for state of the GPSListener, turns it on/off accordingly
	 * @return Boolean.  True if on, false if off.	 */
	public synchronized Boolean toggle() {
		if ( enabled ) { this.turn_off(); }
		else { this.turn_on(); }
		return enabled;
	}
	
	@Override
	public void onLocationChanged(Location location) {		
		Long javaTimeCode = System.currentTimeMillis();
		//order: time, latitude, longitude, altitude, horizontal_accuracy\n
		String data = javaTimeCode.toString() + TextFileManager.delimiter
				+ location.getLatitude() + TextFileManager.delimiter
				+ location.getLongitude() + TextFileManager.delimiter
				+ location.getAltitude() + TextFileManager.delimiter
				+ location.getAccuracy() + '\n' ;
		//note, altitude is notoriously inaccurate, getAccuracy only applies to latitude/longitude
		GPSFile.write(data);
//		logFile.write("GPS: " + data);
	}
	
/*#################################################################################################
/*###### we don't require these built-in overrides for any features, they are irrelevant  #########
/*###############################################################################################*/
	
	// arg0 for Provider Enabled/Disabled is a string saying "network" or "gps".
	@Override
	public void onProviderDisabled(String arg0) { Log.i("A location provider was disabled.", arg0); }
	@Override
	public void onProviderEnabled(String arg0) { Log.i("A location provider was enabled.", arg0); }
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		//FIXME:  implement the receipt of a status change, make it enable/disable a provider?  change state accordingly?  ack.
		
		//Called when the provider status changes, when a provider is unable to fetch a location,
		// or if the provider has recently become available after a period of unavailability.
		// arg0 is the name of the provider that changed status.
		// arg1 is the status of the provider. 0=out of service, 1=temporarily unavailable, 2=available
		Log.i("OH GOD WE GOT A STATUSCHANGE FROM THE GPSListener", arg0 + "," + arg1 + "," + arg2.toString() );
		logFile.write("STATUSCHANGE FROM THE GPSListener" + arg0 + "," + arg1 + "," + arg2.toString());
	}
}
