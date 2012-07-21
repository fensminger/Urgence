package org.fer.urgence;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EmergencyWidgetProvider extends AppWidgetProvider {

	private static final String LOG = "org.fer.urgence";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.w(LOG, "onUpdate method called");
		
		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				EmergencyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent intent = new Intent(context.getApplicationContext(),
				EmergencyService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		// Update the widgets via the service
		context.startService(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		int pos = 0;
		try {
			pos = intent.getExtras().getInt(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
		} catch (NullPointerException e) {
			// We do nothing when the widget is not initialized
			return;
		}
		
		Log.d(LOG, "onReceive method call : " + pos);
		Intent intentService = new Intent(context.getApplicationContext(),
				EmergencyService.class);
		intentService.setAction(EmergencyService.START_CLICK_ACTION+pos);
		intentService.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, pos);
		context.startService(intentService);
	}
	
}