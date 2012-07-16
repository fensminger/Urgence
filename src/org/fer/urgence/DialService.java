package org.fer.urgence;

import java.text.MessageFormat;

import android.app.Activity;
import android.app.PendingIntent;
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

	public void dial(Activity act, ShortContact[] shortContact) {
		String url = "tel:" + shortContact[0].getPhoneNumber();
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
		act.startActivity(intent);
		if (shortContact.length > 1) {
			this.act = act;
			this.contacts = shortContact;
			sendSmsLocation();
		}
	}

	private LocationManager locationManager;
	private PendingIntent singleUpatePI;
	private Activity act;
	private ShortContact[] contacts;

	private void sendSmsLocation() {
		String locationContext = Context.LOCATION_SERVICE;

		locationManager = (android.location.LocationManager) act.getSystemService(locationContext);
//		String locationProvider = android.location.LocationManager.PASSIVE_PROVIDER;
//		LocationProvider provider = locationManager.getProvider(locationProvider);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);

		act.registerReceiver(singleUpdateReceiver, new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION));

		singleUpatePI = PendingIntent.getBroadcast(act, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
			Resources ress = act.getResources();
			String msg = ress.getText(R.string.dial_sms_location).toString();
			for (int i = 1; i < contacts.length; i++) {
				ShortContact contact = contacts[i];
				if (contact != null && contact.isSms()) {
					String msgFormated = MessageFormat.format(msg, ""+location.getLatitude(), ""+location.getLongitude());
					SmsManager.getDefault().sendTextMessage(contact.getPhoneNumber().toString(), null, msgFormated, null, null);
				}
			}
		}
	};
}
