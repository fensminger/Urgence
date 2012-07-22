package org.fer.urgence;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationMgr implements LocationListener {
	private static final String LOG = "LocationMgr";

	private LocationManager locationManager = null;
	private EmergencyService service;
	
	public LocationMgr(EmergencyService service) {
		super();
		this.service = service;
	}

	public void searchForOneLocation() {
		Log.i(LOG, "LocationMgr.searchForOneLocation");
		String locationContext = Context.LOCATION_SERVICE;

		locationManager = (android.location.LocationManager) service.getSystemService(locationContext);
//		String locationProvider = android.location.LocationManager.PASSIVE_PROVIDER;
//		LocationProvider provider = locationManager.getProvider(locationProvider);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		
		locationManager.requestSingleUpdate(criteria, this, null);
	}
	
	public void releaseLocationManager() {
		if (locationManager!=null) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			Log.i(LOG, "LocationMgr.onReceive event : location is not null : " 
					+ location.getLatitude() + ", " + location.getLongitude());
			service.setLocation(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
