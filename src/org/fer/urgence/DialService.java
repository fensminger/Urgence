package org.fer.urgence;

import java.text.MessageFormat;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.telephony.SmsManager;

public class DialService {

	private static final String SINGLE_LOCATION_UPDATE_ACTION = "SINGLE_LOCATION_UPDATE_ACTION";

	public void dial(Service service, List<ShortContact> shortContacts, int pos) {
		String url = "tel:" + shortContacts.get(pos).getPhoneNumber();
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    service.startActivity(intent);
		if (pos == 0 && shortContacts.size() > 1) {
			this.service = service;
			this.contacts = shortContacts;
			sendSmsLocation();
		}
	}

	private LocationManager locationManager;
	private PendingIntent singleUpatePI;
	private Service service;
	private List<ShortContact> contacts;

	private void sendSmsLocation() {
		String locationContext = Context.LOCATION_SERVICE;

		locationManager = (android.location.LocationManager) service.getSystemService(locationContext);
//		String locationProvider = android.location.LocationManager.PASSIVE_PROVIDER;
//		LocationProvider provider = locationManager.getProvider(locationProvider);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);

		service.registerReceiver(singleUpdateReceiver, new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION));

		singleUpatePI = PendingIntent.getBroadcast(service, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		locationManager.requestSingleUpdate(criteria, singleUpatePI);
	}

	protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// unregister the receiver so that the application does not keep
			// listening the broadcast even after the broadcast is received.
			context.unregisterReceiver(singleUpdateReceiver);
			// get the location from the intent send in broadcast using the key
			// - this step is very very important
			String key = LocationManager.KEY_LOCATION_CHANGED;
			Location location = (Location) intent.getExtras().get(key);

			// Call the function required
			if (location != null) {
				onLocationChanged(location);
			}

			// finally remove the updates for the pending intent
			locationManager.removeUpdates(singleUpatePI);
		}

		private void onLocationChanged(Location location) {
			Resources ress = service.getResources();
			String msg = ress.getText(R.string.dial_sms_location).toString();
			for (int i = 1; i < contacts.size(); i++) {
				ShortContact contact = contacts.get(i);
				if (contact != null && contact.isSms()) {
					String msgFormated = MessageFormat.format(msg, ""+location.getLatitude(), ""+location.getLongitude());
					SmsManager.getDefault().sendTextMessage(contact.getPhoneNumber().toString(), null, msgFormated, null, null);
				}
			}
		}
	};
}
