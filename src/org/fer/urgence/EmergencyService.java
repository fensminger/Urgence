package org.fer.urgence;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

public class EmergencyService extends Service {
	private static final int TIMOUT_PHONE_LISTENER_DIVIDE_BY_TEN_SECONDS = 12;
	private static final int ONE_TIMEOUT_SLEEP_MS = 10000;

	private static final String LOG = "EmergencyService";

	public static final String START_CLICK_ACTION = "WidgetClickAction_";
	public static final String UPDATE_WIDGET_VIEW_ACTION = "UpdateWidgetViewAction";
	
	private PhoneCallListener phoneListener = null;
	private Thread timeOutPhoneListener = null;
	private Location location = null;
	private LocationMgr locMgr = null;
	private boolean sendSmsOnTimeout = false;
	private volatile Object synchroPhoneListener = new Object();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action!=null && action.startsWith(START_CLICK_ACTION)) {
			startDial(intent, flags, startId);
		} else if (UPDATE_WIDGET_VIEW_ACTION.equals(action)) {
			initWidget(intent, flags, startId);
		} else {
			initWidget(intent, flags, startId);
		}
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}

	private void startDial(Intent intent, int flags, int startId) {
		int pos = intent.getExtras().getInt(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
		Log.d(LOG, "UpdateWidgetService.onClick : " + pos);
		
		dial(getContacts(), pos);
	}
	
	public void dial(List<ShortContact> shortContacts, int pos) {
		// Preparing to send SMS if necessary
		if (pos == 0 && shortContacts.size() > 1) {
			synchronized(synchroPhoneListener) {
				if (phoneListener==null) {
					sendSmsOnTimeout = false;
					locMgr = new LocationMgr(this);
					locMgr.searchForOneLocation();
					phoneListener = new PhoneCallListener(this);
					timeOutPhoneListener = new Thread(new Runnable() {
						@Override
						public void run() {
							checkTimeoutPhoneListener();
						}
					});
					timeOutPhoneListener.start();
					TelephonyManager telephonyManager = (TelephonyManager) this
							.getSystemService(Context.TELEPHONY_SERVICE);
					telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
				}
			}
		}
		
		// Call the emergency
		
		String url = "tel:" + shortContacts.get(pos).getPhoneNumber();
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    startActivity(intent);
	}
	
	private void checkTimeoutPhoneListener() {
		int nbTenSeconds = 0;
		while (nbTenSeconds  < TIMOUT_PHONE_LISTENER_DIVIDE_BY_TEN_SECONDS) {
			nbTenSeconds++;
			Log.i(LOG, "TimeOutPhoneListener : " + (nbTenSeconds*ONE_TIMEOUT_SLEEP_MS/1000));
			try {
				Thread.sleep(ONE_TIMEOUT_SLEEP_MS);
			} catch (InterruptedException e) {
				Log.w(LOG, e.getMessage());
			}
			if (isCallStarted()) {
				// No timeout
				return;
			}
			nbTenSeconds++;
		}
		// Timeout started
		Log.i(LOG, "TimeOutPhoneListener : Timeout");
		if (isSendSmsOnTimeout()) {
			Log.i(LOG, "TimeOutPhoneListener : Timeout, but send SMS.");
			sendSmsWithGps();
		} else {
			if (getOptions().isSendSmsOnTry()) {
				Log.i(LOG, "TimeOutPhoneListener : Timeout, but send SMS try emergency.");
				sendTryEmergencySmsWithGps();
			} else {
				Log.i(LOG, "TimeOutPhoneListener : Timeout, does not send SMS.");
			}
		}
		removePhoneCallListener();
	}

	private void sendSms(String msg) {
		List<ShortContact> contacts = getContacts();
		for (int i = 1; i < contacts .size(); i++) {
			ShortContact contact = contacts.get(i);
			if (contact != null && contact.isSms()) {
				Log.i(LOG, "UpdateWidgetService.sendSms to " + contact.getPhoneNumber().toString() + " : " + msg);
				SmsManager.getDefault().sendTextMessage(contact.getPhoneNumber().toString(), null, msg, null, null);
			}
		}
	}

	private void sendSmsWithGps() {
		if (!isLocation()) {
			Resources ress = getResources();
			String msg = ress.getText(R.string.dial_sms_body).toString();
			sendSms(msg);
		} else {
			Resources ress = getResources();
			String msg = ress.getText(R.string.dial_sms_location).toString();
			String msgFormated = msg.replaceAll("\\{0\\}", location.getLatitude()+","+location.getLongitude());
			Log.i(LOG, "UpdateWidgetService.sendSmsWithGps msg : " + msgFormated);
			sendSms(msgFormated);
		}
	}

	private void sendTryEmergencySmsWithGps() {
		if (!isLocation()) {
			Resources ress = getResources();
			String msg = ress.getText(R.string.dial_try_sms_body).toString();
			sendSms(msg);
		} else {
			Resources ress = getResources();
			String msg = ress.getText(R.string.dial_try_sms_location).toString();
			String msgFormated = msg.replaceAll("\\{0\\}", location.getLatitude()+","+location.getLongitude());
			Log.i(LOG, "UpdateWidgetService.sendTryEmergencySmsWithGps msg : " + msgFormated);
			sendSms(msgFormated);
		}
	}
	
	public void setSendSmsOnTimeout(boolean b) {
		synchronized(synchroPhoneListener) {
			sendSmsOnTimeout = b; 
		}
	}

	
	public boolean isSendSmsOnTimeout() {
		synchronized(synchroPhoneListener) {
			return sendSmsOnTimeout;
		}
	}

	private boolean isLocation() {
		boolean isLocation;
		synchronized(synchroPhoneListener) {
			isLocation = (location != null); 
		}
		return isLocation;
	}
	
	public void setLocation(Location location) {
		synchronized(synchroPhoneListener) {
			this.location = location; 
		}
	}
	
	private boolean isCallStarted() {
		boolean res;
		synchronized(synchroPhoneListener) {
			res = (phoneListener == null); 
		}
		return res;
	}

	public void removePhoneCallListener() {
		Log.i(LOG, "UpdateWidgetService.removePhoneCallListener");
		synchronized(synchroPhoneListener) {
			if (phoneListener != null) {
				TelephonyManager telephonyManager = (TelephonyManager) this
						.getSystemService(Context.TELEPHONY_SERVICE);
				telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
			}
			phoneListener = null;
			timeOutPhoneListener = null;
			location = null;
			if (locMgr!=null) {
				locMgr.releaseLocationManager();
			}
			locMgr = null;
		}
	}

	private void initWidget(Intent intent, int flags, int startId) {
		Log.i(LOG, "UpdateWidgetService.onStartCommand");

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				EmergencyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);		
		
		RemoteViews remoteViews = new RemoteViews(this
				.getApplicationContext().getPackageName(),
				R.layout.widget_layout);
		initView(remoteViews);
		
		int pos = 1;
		for (int widgetId : allWidgetIds) {

			initOneClickListener(remoteViews, allWidgetIds, R.id.tvCall, pos++);
			initOneClickListener(remoteViews, allWidgetIds, R.id.tvCall1, pos++);
			initOneClickListener(remoteViews, allWidgetIds, R.id.tvCall2, pos++);
			initOneClickListener(remoteViews, allWidgetIds, R.id.tvCall3, pos++);
			initOneClickListener(remoteViews, allWidgetIds, R.id.tvCall4, pos++);
			
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
	
	private void initOneClickListener(RemoteViews remoteViews, int[] allWidgetIds, int viewId, int pos) {
		Intent clickIntent = new Intent(this.getApplicationContext(),
				EmergencyWidgetProvider.class);

		clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE+pos);
		clickIntent.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, pos);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
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
	
	private Options getOptions() {
		SharedPreferences prefs = getApplication().getSharedPreferences(PreferenceMgr.URGENCE_PREFS, Activity.MODE_MULTI_PROCESS);
		return new PreferenceMgr(prefs).restoreOptions();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
