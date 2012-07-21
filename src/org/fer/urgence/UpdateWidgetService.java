package org.fer.urgence;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	private static final String LOG = "org.fer.urgence";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG, "UpdateWidgetService.onStartCommand");
		// Create some random data

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				MyWidgetProvider.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
		Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

		RemoteViews remoteViews = new RemoteViews(this
				.getApplicationContext().getPackageName(),
				R.layout.widget_layout);
		initView(remoteViews);
		
		for (int widgetId : allWidgetIds) {

			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(),
					MyWidgetProvider.class);

			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.tvCall, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		stopSelf();

		return super.onStartCommand(intent, flags, startId);
	}
	
	private void initView(RemoteViews remoteViews) {
		List<ShortContact> contacts = getContacts();
		for(int i = 0 ; i < PreferenceMgr.MAX_PHONE_NUMBER; i++) {
			ShortContact contact = (i<contacts.size())?contacts.get(i):null;
			String title = getTitle(contact, i);
			int id = 0;
			switch (i) {
			case 0:
				id =  R.id.tvCall;
				break;
			case 1:
				id =  R.id.tvCall1;
				break;
			case 2:
				id =  R.id.tvCall2;
				break;
			case 3:
				id =  R.id.tvCall3;
				break;
			case 4:
				id =  R.id.tvCall4;
				break;

			default:
				throw new RuntimeException("Undefined identifier for this contact");
			}
			
			remoteViews.setTextViewText(id, title);
		}
	}
	
	private String getTitle(ShortContact contact, int pos) {
		if (contact==null || contact.getContactName()==null) {
			return "";
		}
		
		final String contactName = contact.getContactName().toString();
		
		if (pos==0) {
			return contactName;
		}
		
		String[] names = contactName.split(" ");
		
		if (names.length==1) {
			return contactName;
		} else {
			StringBuilder res = new StringBuilder();
			boolean isFirst = true;
			for(String name : names) {
				if (isFirst) {
					isFirst = false;
				} else {
					res.append(" ");
				}
				if (name.length()<=4) {
					res.append(name);
				} else {
					res.append(name.substring(0, 4));
				}
			}
			return res.toString();
		}
	}

	private List<ShortContact> getContacts() {
		SharedPreferences prefs = getApplication().getSharedPreferences(PreferenceMgr.URGENCE_PREFS, Activity.MODE_MULTI_PROCESS);
		return new PreferenceMgr(prefs).getAllContacts();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
