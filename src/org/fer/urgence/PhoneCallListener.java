package org.fer.urgence;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallListener extends PhoneStateListener {
	 
	private String LOG_TAG = "CallListener";
	private EmergencyService service;
	private boolean isStateRinging = false;
	private boolean isStateOffhook = false;

	public PhoneCallListener(EmergencyService service) {
		super();
		this.service = service;
	}
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		
		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
			isStateRinging = true;
			break;

		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.i(LOG_TAG, "OFFHOOK");
			isStateOffhook = true;
			service.setSendSmsOnTimeout(true);
			break;
			
		case TelephonyManager.CALL_STATE_IDLE:
			Log.i(LOG_TAG, "IDLE " + isStateRinging + ", " + isStateOffhook);
			break;
			
		default:
			break;
		}
		
	}
	
	public void stopListening() {
		Log.i(LOG_TAG, "stopListening");
		service.removePhoneCallListener();
	}
}

