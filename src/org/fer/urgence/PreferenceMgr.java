package org.fer.urgence;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class PreferenceMgr {

	public static final String URGENCE_PREFS = "Urgence_Prefs";
	public static final int MAX_PHONE_NUMBER = 5;

	private static final String PREF_IS_SMS = "IsSMS_";
	private static final String PREF_PHONE_NUMBER = "PhoneNumber_";
	private static final String PREF_NAME = "Name_";
	
	public static final String PREF_SEND_SMS_ON_TRY = "sendSmsOnTry";

	private SharedPreferences prefs;

	public PreferenceMgr(SharedPreferences prefs) {
		super();
		this.prefs = prefs;
	}

	public List<ShortContact> getAllContacts() {
		ArrayList<ShortContact> res = new ArrayList<ShortContact>();
		for (int i = 0; i < MAX_PHONE_NUMBER; i++) {
			String name = prefs.getString(PREF_NAME + i, null);
			if (name != null) {
				String phoneNumber = prefs.getString(PREF_PHONE_NUMBER + i, null);
				Boolean isSms = prefs.getBoolean(PREF_IS_SMS + i, false);
				ShortContact shortContact = new ShortContact(name, phoneNumber, isSms);
				res.add(shortContact);
			}
		}
		return res;
	}
	
	public void saveAllContacts(List<ShortContact> contacts) {
		for(int i = 0; i<MAX_PHONE_NUMBER ; i++) {
			ShortContact shortContact = contacts.get(i);
			Editor editor = prefs.edit();
			editor.putString(PREF_NAME+i, shortContact.getContactName().toString());
			editor.putString(PREF_PHONE_NUMBER+i, shortContact.getPhoneNumber().toString());
			editor.putBoolean(PREF_IS_SMS+i, shortContact.isSms());
			editor.commit();
		}
	}  

	public void saveOptions(Options options) {
		Editor editor = prefs.edit();
		editor.putBoolean(PREF_SEND_SMS_ON_TRY, options.isSendSmsOnTry());
		editor.commit();
	}
	
	public Options restoreOptions() {
		Options res = new Options();
		res.setSendSmsOnTry(prefs.getBoolean(PREF_SEND_SMS_ON_TRY, false));
		return res;
	}
}
